package com.lkms.ui.protocol;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.textfield.TextInputEditText;
import com.lkms.R;
import com.lkms.data.model.java.Item;
import com.lkms.data.model.java.Protocol;
import com.lkms.data.model.java.ProtocolItem;
import com.lkms.data.model.java.ProtocolStep;
import com.lkms.data.repository.enumPackage.java.LKMSConstantEnums.ProtocolApproveStatus;
import com.lkms.ui.protocol.viewmodel.CreateProtocolViewModel;
import com.lkms.util.AuthHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CreateProtocolActivity extends AppCompatActivity {

    // --- Data Models ---
    private static class HeaderDataModel {
        String name = "";
        String introduction = "";
        String safety = "";
        String version = "";
    }

    private static class SectionHeaderModel {
        String title;
        boolean hasAddButton;

        SectionHeaderModel(String title, boolean hasAddButton) {
            this.title = title;
            this.hasAddButton = hasAddButton;
        }
    }

    // --- UI Components ---
    private Toolbar toolbar;
    private RecyclerView mainRecyclerView;
    private Button saveProtocolButton;
    private ProgressBar progressBar;

    // --- Data, Adapters, ViewModel ---
    private CreateProtocolAdapter mainAdapter;
    private final List<Object> displayList = new ArrayList<>();
    private final HeaderDataModel headerData = new HeaderDataModel();
    private final List<ProtocolStep> stepsList = new ArrayList<>();
    private final List<ProtocolItem> itemsList = new ArrayList<>();
    private final List<Item> availableItems = new ArrayList<>();
    private CreateProtocolViewModel viewModel;

    // --- Constants for Adapter ---
    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_SECTION_HEADER = 1;
    private static final int VIEW_TYPE_STEP = 2;
    private static final int VIEW_TYPE_ITEM_CONTROLS = 3;
    private static final int VIEW_TYPE_ITEM = 4;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // SỬA: Sử dụng layout mới
        setContentView(R.layout.activity_create_protocol);

        viewModel = new ViewModelProvider(this).get(CreateProtocolViewModel.class);

        initViews();
        setupToolbar();
        setupMainRecyclerView();
        setupSaveButtonListener();
        observeViewModel();

        viewModel.loadInitialData();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar_create_protocol);
        mainRecyclerView = findViewById(R.id.main_recycler_view);
        saveProtocolButton = findViewById(R.id.button_save_protocol);
        progressBar = findViewById(R.id.progress_bar_create);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void setupMainRecyclerView() {
        mainAdapter = new CreateProtocolAdapter();
        mainRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mainRecyclerView.setAdapter(mainAdapter);
        buildDisplayList(); // Xây dựng danh sách hiển thị ban đầu
    }

    private void buildDisplayList() {
        displayList.clear();
        // Thêm các thành phần theo đúng thứ tự hiển thị
        displayList.add(headerData);
        displayList.add(new SectionHeaderModel("Steps", true));
        displayList.addAll(stepsList);
        displayList.add(new SectionHeaderModel("Required Items", false));
        displayList.add("ITEM_CONTROLS"); // Một object giả để định danh view controls
        displayList.addAll(itemsList);

        mainAdapter.notifyDataSetChanged();
    }

    private void observeViewModel() {
        viewModel.isLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            saveProtocolButton.setEnabled(!isLoading);
        });

        viewModel.getCreationSuccess().observe(this, protocolId -> {
            if (protocolId != null) {
                Toast.makeText(this, "Tạo protocol thành công!", Toast.LENGTH_LONG).show();
                finish();
            }
        });

        viewModel.getError().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(this, "Lỗi: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getItems().observe(this, items -> {
            if (items != null) {
                this.availableItems.clear();
                this.availableItems.addAll(items);
                // Thông báo cho adapter biết dữ liệu spinner đã thay đổi
                mainAdapter.notifyItemChanged(displayList.indexOf("ITEM_CONTROLS"));
            }
        });
    }

    private void setupSaveButtonListener() {
        saveProtocolButton.setOnClickListener(v -> {
            // Validate các trường bắt buộc
            if (headerData.name.trim().isEmpty()) {
                Toast.makeText(this, "Protocol Name không được để trống.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (headerData.version.trim().isEmpty()) {
                Toast.makeText(this, "Version không được để trống.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Lọc và đánh số thứ tự các bước hợp lệ
            List<ProtocolStep> validSteps = getValidSteps();
            if (validSteps.isEmpty()) {
                Toast.makeText(this, "Protocol phải có ít nhất một bước hợp lệ.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Tạo đối tượng Protocol và gọi ViewModel để lưu
            Protocol protocolData = createProtocolData();
            viewModel.createProtocol(protocolData, validSteps, itemsList, protocolData.getCreatorUserId());
        });
    }

    private List<ProtocolStep> getValidSteps() {
        List<ProtocolStep> validSteps = new ArrayList<>();
        for (ProtocolStep step : stepsList) {
            if (step.getInstruction() != null && !step.getInstruction().trim().isEmpty()) {
                validSteps.add(step);
            }
        }
        for (int i = 0; i < validSteps.size(); i++) {
            validSteps.get(i).setStepOrder(i + 1);
        }
        return validSteps;
    }

    private Protocol createProtocolData() {
        Protocol protocolData = new Protocol();
        protocolData.setProtocolTitle(headerData.name);
        protocolData.setIntroduction(headerData.introduction);
        protocolData.setSafetyWarning(headerData.safety);
        protocolData.setVersionNumber(headerData.version);
        protocolData.setApproveStatus(ProtocolApproveStatus.APPROVED);
        protocolData.setCreatorUserId(AuthHelper.getLoggedInUserId(getApplicationContext()));
        return protocolData;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    // ===================================================================
    // ================== ADAPTER VÀ VIEW HOLDER MỚI =====================
    // ===================================================================
    private class CreateProtocolAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        @Override
        public int getItemViewType(int position) {
            Object item = displayList.get(position);
            if (item instanceof HeaderDataModel) return VIEW_TYPE_HEADER;
            if (item instanceof SectionHeaderModel) return VIEW_TYPE_SECTION_HEADER;
            if (item instanceof ProtocolStep) return VIEW_TYPE_STEP;
            if (item instanceof String && item.equals("ITEM_CONTROLS")) return VIEW_TYPE_ITEM_CONTROLS;
            if (item instanceof ProtocolItem) return VIEW_TYPE_ITEM;
            return -1;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            switch (viewType) {
                case VIEW_TYPE_HEADER:
                    return new HeaderViewHolder(inflater.inflate(R.layout.part_create_protocol_header, parent, false));
                case VIEW_TYPE_SECTION_HEADER:
                    return new SectionHeaderViewHolder(inflater.inflate(R.layout.part_create_protocol_section_header, parent, false));
                case VIEW_TYPE_STEP:
                    return new StepViewHolder(inflater.inflate(R.layout.item_create_step, parent, false));
                case VIEW_TYPE_ITEM_CONTROLS:
                    return new ItemControlsViewHolder(inflater.inflate(R.layout.part_create_protocol_item_controls, parent, false));
                case VIEW_TYPE_ITEM:
                    return new ItemViewHolder(inflater.inflate(R.layout.item_create_item_display, parent, false));
                default:
                    throw new IllegalArgumentException("Invalid view type");
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            Object item = displayList.get(position);
            switch (holder.getItemViewType()) {
                case VIEW_TYPE_HEADER:
                    ((HeaderViewHolder) holder).bind((HeaderDataModel) item);
                    break;
                case VIEW_TYPE_SECTION_HEADER:
                    ((SectionHeaderViewHolder) holder).bind((SectionHeaderModel) item);
                    break;
                case VIEW_TYPE_STEP:
                    ((StepViewHolder) holder).bind((ProtocolStep) item);
                    break;
                case VIEW_TYPE_ITEM_CONTROLS:
                    ((ItemControlsViewHolder) holder).bind();
                    break;
                case VIEW_TYPE_ITEM:
                    ((ItemViewHolder) holder).bind((ProtocolItem) item);
                    break;
            }
        }

        @Override
        public int getItemCount() {
            return displayList.size();
        }
    }

    // --- Header ViewHolder ---
    private class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextInputEditText name, intro, safety, version;
        HeaderViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.edit_text_protocol_name);
            intro = itemView.findViewById(R.id.edit_text_protocol_introduction);
            safety = itemView.findViewById(R.id.edit_text_protocol_safety);
            version = itemView.findViewById(R.id.edit_text_protocol_version);

            // Listener để cập nhật dữ liệu khi người dùng nhập
            name.addTextChangedListener(new SimpleTextWatcher(s -> headerData.name = s));
            intro.addTextChangedListener(new SimpleTextWatcher(s -> headerData.introduction = s));
            safety.addTextChangedListener(new SimpleTextWatcher(s -> headerData.safety = s));
            version.addTextChangedListener(new SimpleTextWatcher(s -> headerData.version = s));
        }

        void bind(HeaderDataModel data) {
            name.setText(data.name);
            intro.setText(data.introduction);
            safety.setText(data.safety);
            version.setText(data.version);
        }
    }

    // --- Section Header ViewHolder ---
    private class SectionHeaderViewHolder extends RecyclerView.ViewHolder {
        TextView titleText;
        Button addButton;
        SectionHeaderViewHolder(View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.text_view_section_title);
            addButton = itemView.findViewById(R.id.button_add);
        }

        void bind(SectionHeaderModel model) {
            titleText.setText(model.title);
            addButton.setVisibility(model.hasAddButton ? View.VISIBLE : View.GONE);
            if(model.title.equals("Steps")){
                addButton.setText("Add Step");
                addButton.setOnClickListener(v -> {
                    stepsList.add(new ProtocolStep());
                    buildDisplayList(); // Rebuild để cập nhật
                    mainRecyclerView.smoothScrollToPosition(displayList.size() - 1);
                });
            }
        }
    }

    // --- Step ViewHolder ---
    private class StepViewHolder extends RecyclerView.ViewHolder {
        TextView stepOrderText;
        TextInputEditText stepInstructionEdit;
        ImageButton removeStepButton;

        StepViewHolder(View itemView) {
            super(itemView);
            stepOrderText = itemView.findViewById(R.id.text_view_step_order);
            stepInstructionEdit = itemView.findViewById(R.id.edit_text_step_instruction);
            removeStepButton = itemView.findViewById(R.id.button_remove_step);

            stepInstructionEdit.addTextChangedListener(new SimpleTextWatcher(s -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Object item = displayList.get(position);
                    if(item instanceof ProtocolStep){
                        ((ProtocolStep)item).setInstruction(s);
                    }
                }
            }));
        }

        void bind(ProtocolStep step) {
            int stepIndex = stepsList.indexOf(step);
            stepOrderText.setText(String.format("%d.", stepIndex + 1));
            stepInstructionEdit.setText(step.getInstruction());
            removeStepButton.setOnClickListener(v -> {
                stepsList.remove(step);
                buildDisplayList();
            });
        }
    }

    // --- Item Controls ViewHolder ---
    private class ItemControlsViewHolder extends RecyclerView.ViewHolder {
        Spinner spinner;
        TextInputEditText quantityInput;
        Button addButton;
        ArrayAdapter<Item> spinnerAdapter;

        ItemControlsViewHolder(View itemView) {
            super(itemView);
            spinner = itemView.findViewById(R.id.spinner_select_available_item);
            quantityInput = itemView.findViewById(R.id.edit_text_select_quantity);
            addButton = itemView.findViewById(R.id.button_add_item_to_list);

            spinnerAdapter = new ArrayAdapter<Item>(CreateProtocolActivity.this, android.R.layout.simple_spinner_item, availableItems) {
                @NonNull @Override
                public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                    return createSpinnerView(position, false);
                }
                @Override
                public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                    return createSpinnerView(position, true);
                }
                private View createSpinnerView(int position, boolean isDropDown) {
                    TextView textView = (TextView) (isDropDown ?
                            super.getDropDownView(position, null, (ViewGroup) spinner.getParent()) :
                            super.getView(position, null, (ViewGroup) spinner.getParent()));

                    Item currentItem = getItem(position);
                    if (currentItem != null) {
                        String displayText = String.format("(Tồn kho: %d %s) %s",
                                currentItem.getQuantity(), currentItem.getUnit(), currentItem.getItemName());
                        textView.setText(displayText);
                    }
                    return textView;
                }
            };
            spinner.setAdapter(spinnerAdapter);

            addButton.setOnClickListener(v -> {
                Item selectedItem = (Item) spinner.getSelectedItem();
                if (selectedItem == null) {
                    Toast.makeText(CreateProtocolActivity.this, "Chưa có vật tư để chọn.", Toast.LENGTH_SHORT).show();
                    return;
                }

                String quantityStr = quantityInput.getText().toString();
                if (quantityStr.isEmpty() || Integer.parseInt(quantityStr) <= 0) {
                    Toast.makeText(CreateProtocolActivity.this, "Số lượng phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                    return;
                }
                int quantity = Integer.parseInt(quantityStr);

                if (itemsList.stream().anyMatch(item -> item.getItemId().equals(selectedItem.getItemId()))) {
                    Toast.makeText(CreateProtocolActivity.this, "Vật tư này đã có trong danh sách.", Toast.LENGTH_SHORT).show();
                    return;
                }

                ProtocolItem newItem = new ProtocolItem();
                newItem.setItemId(selectedItem.getItemId());
                newItem.setQuantity(quantity);
                itemsList.add(newItem);
                buildDisplayList();

                quantityInput.setText("");
            });
        }

        void bind() {
            spinnerAdapter.notifyDataSetChanged(); // Cập nhật spinner khi có dữ liệu mới
        }
    }

    // --- Item Display ViewHolder ---
    private class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView itemNameText, itemQuantityText;
        ImageButton removeItemButton;
        ItemViewHolder(View itemView) {
            super(itemView);
            itemNameText = itemView.findViewById(R.id.text_view_item_name);
            itemQuantityText = itemView.findViewById(R.id.text_view_item_quantity);
            removeItemButton = itemView.findViewById(R.id.button_remove_display_item);
        }

        void bind(ProtocolItem item) {
            Optional<Item> availableItemOpt = availableItems.stream()
                    .filter(i -> i.getItemId().equals(item.getItemId())).findFirst();
            String itemName = availableItemOpt.map(Item::getItemName).orElse("Unknown Item");

            itemNameText.setText(itemName);
            itemQuantityText.setText("x " + item.getQuantity());

            removeItemButton.setOnClickListener(v -> {
                itemsList.remove(item);
                buildDisplayList();
            });
        }
    }

    // --- Simple Text Watcher ---
    private static class SimpleTextWatcher implements TextWatcher {
        private final java.util.function.Consumer<String> onTextChanged;
        SimpleTextWatcher(java.util.function.Consumer<String> onTextChanged) { this.onTextChanged = onTextChanged; }
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) { onTextChanged.accept(s.toString()); }
        @Override public void afterTextChanged(Editable s) {}
    }
}

