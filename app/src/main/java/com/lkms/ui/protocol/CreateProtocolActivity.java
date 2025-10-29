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
import com.lkms.ui.protocol.viewmodel.CreateProtocolViewModel;
import com.lkms.util.AuthHelper;
import java.util.ArrayList;
import java.util.List;

public class CreateProtocolActivity extends AppCompatActivity {

    // --- Khai báo UI Components ---
    private Toolbar toolbar;
    private TextInputEditText protocolNameInput;
    private TextInputEditText protocolIntroductionInput;
    private TextInputEditText safetyWarningInput;
    private TextInputEditText versionInput;
    private RecyclerView stepsRecyclerView;
    private RecyclerView itemsRecyclerView;
    private Button addStepButton;
    private Button saveProtocolButton;
    private ProgressBar progressBar;

    // --- Các View mới cho việc chọn Item ---
    private Spinner selectAvailableItemSpinner;
    private TextInputEditText selectQuantityInput;
    private Button addItemToListButton;

    // --- Khai báo Data, Adapters, và ViewModel ---
    private StepsAdapter stepsAdapter;
    private ItemsDisplayAdapter itemsAdapter;
    private final List<ProtocolStep> stepsList = new ArrayList<>();
    private final List<ProtocolItem> itemsList = new ArrayList<>(); // List các item ĐÃ được thêm vào protocol
    private final List<Item> availableItems = new ArrayList<>(); // List TẤT CẢ các item có trong hệ thống
    private ArrayAdapter<Item> availableItemsSpinnerAdapter; // Adapter cho Spinner MỚI
    private CreateProtocolViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_protocol);

        viewModel = new ViewModelProvider(this).get(CreateProtocolViewModel.class);

        initViews();
        setupToolbar();
        setupAdapters(); // Đổi tên hàm
        setupEventListeners();
        observeViewModel();

        // Bắt đầu tải danh sách vật tư
        viewModel.loadInitialData();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar_create_protocol);
        protocolNameInput = findViewById(R.id.edit_text_protocol_name);
        protocolIntroductionInput = findViewById(R.id.edit_text_protocol_introduction);
        safetyWarningInput = findViewById(R.id.edit_text_protocol_safety);
        versionInput = findViewById(R.id.edit_text_protocol_version);
        stepsRecyclerView = findViewById(R.id.recycler_view_steps);
        itemsRecyclerView = findViewById(R.id.recycler_view_items);
        addStepButton = findViewById(R.id.button_add_step);
        saveProtocolButton = findViewById(R.id.button_save_protocol);
        progressBar = findViewById(R.id.progress_bar_create);

        // Ánh xạ các view mới
        selectAvailableItemSpinner = findViewById(R.id.spinner_select_available_item);
        selectQuantityInput = findViewById(R.id.edit_text_select_quantity);
        addItemToListButton = findViewById(R.id.button_add_item_to_list);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void setupAdapters() {
        // Adapter cho Steps RecyclerView
        stepsAdapter = new StepsAdapter(stepsList);
        stepsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        stepsRecyclerView.setAdapter(stepsAdapter);

        // Adapter cho Items RecyclerView (hiển thị item đã chọn)
        itemsAdapter = new ItemsDisplayAdapter(itemsList, availableItems);
        itemsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        itemsRecyclerView.setAdapter(itemsAdapter);

        // Adapter cho Spinner chọn vật tư
        availableItemsSpinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, this.availableItems);
        availableItemsSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selectAvailableItemSpinner.setAdapter(availableItemsSpinnerAdapter);
    }

    private void setupEventListeners() {
        addStepButton.setOnClickListener(v -> {
            ProtocolStep newStep = new ProtocolStep();
            // Step order sẽ được xử lý trong adapter để đảm bảo luôn đúng
            stepsList.add(newStep);
            stepsAdapter.notifyItemInserted(stepsList.size() - 1);
            stepsRecyclerView.smoothScrollToPosition(stepsList.size() - 1);
        });

        // Logic mới cho nút "Add to List"
        addItemToListButton.setOnClickListener(v -> {
            Item selectedItem = (Item) selectAvailableItemSpinner.getSelectedItem();
            if (selectedItem == null) {
                Toast.makeText(this, "Chưa có vật tư để chọn.", Toast.LENGTH_SHORT).show();
                return;
            }

            String quantityStr = selectQuantityInput.getText().toString();
            if (quantityStr.isEmpty() || Integer.parseInt(quantityStr) <= 0) {
                selectQuantityInput.setError("Số lượng phải lớn hơn 0");
                return;
            }
            int quantity = Integer.parseInt(quantityStr);

            // Kiểm tra xem item này đã có trong danh sách chưa
            for (ProtocolItem existingItem : itemsList) {
                if (existingItem.getItemId().equals(selectedItem.getItemId())) {
                    Toast.makeText(this, "Vật tư này đã có trong danh sách.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            ProtocolItem newItem = new ProtocolItem();
            newItem.setItemId(selectedItem.getItemId());
            newItem.setQuantity(quantity);

            itemsList.add(newItem);
            itemsAdapter.notifyItemInserted(itemsList.size() - 1);
            itemsRecyclerView.smoothScrollToPosition(itemsList.size() - 1);

            // Reset ô nhập liệu
            selectQuantityInput.setText("");
            selectQuantityInput.setError(null); // Xóa lỗi nếu có
            selectAvailableItemSpinner.setSelection(0);
        });

        // Sự kiện nhấn nút "Save Protocol"
        saveProtocolButton.setOnClickListener(v -> {
            String protocolName = protocolNameInput.getText().toString().trim();
            String introduction = protocolIntroductionInput.getText().toString().trim();
            String safetyWarning = safetyWarningInput.getText().toString().trim();
            String version = versionInput.getText().toString().trim();

            if (protocolName.isEmpty()) {
                protocolNameInput.setError("Tên protocol không được để trống");
                return;
            }
            if (version.isEmpty()) {
                versionInput.setError("Phiên bản không được để trống");
                return;
            }

            Protocol protocolData = new Protocol();
            // Sử dụng các setter của model Protocol của bạn
            protocolData.setProtocolTitle(protocolName);
            protocolData.setIntroduction(introduction);
            protocolData.setSafetyWarning(safetyWarning);
            protocolData.setVersionNumber(version);

            int creatorId = AuthHelper.getLoggedInUserId(getApplicationContext());
            viewModel.createProtocol(protocolData, stepsList, itemsList, creatorId);
        });
    }

    private void observeViewModel() {
        viewModel.isLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            saveProtocolButton.setEnabled(!isLoading);
        });

        viewModel.getCreationSuccess().observe(this, protocolId -> {
            if (protocolId != null) {
                Toast.makeText(this, "Tạo protocol thành công với ID: " + protocolId, Toast.LENGTH_LONG).show();
                finish();
            }
        });

        viewModel.getError().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(this, "Lỗi: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });

        // Observer cho danh sách vật tư có sẵn
        viewModel.getItems().observe(this, items -> {
            if (items != null) {
                this.availableItems.clear();
                this.availableItems.addAll(items);
                // Cập nhật cho Spinner chọn item
                this.availableItemsSpinnerAdapter.notifyDataSetChanged();

                // Nếu có vật tư, bật các control lên, nếu không thì vô hiệu hóa
                boolean hasItems = !items.isEmpty();
                selectAvailableItemSpinner.setEnabled(hasItems);
                selectQuantityInput.setEnabled(hasItems);
                addItemToListButton.setEnabled(hasItems);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ================================================================
    // ============ ADAPTER CHO STEPS===============
    // ================================================================
    private class StepsAdapter extends RecyclerView.Adapter<StepsAdapter.StepViewHolder> {
        private final List<ProtocolStep> localStepsList;

        StepsAdapter(List<ProtocolStep> stepsList) {
            this.localStepsList = stepsList;
        }

        @NonNull
        @Override
        public StepViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_create_step, parent, false);
            return new StepViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull StepViewHolder holder, int position) {
            holder.bind(localStepsList.get(position));
        }

        @Override
        public int getItemCount() {
            return localStepsList.size();
        }

        class StepViewHolder extends RecyclerView.ViewHolder {
            TextView stepOrderText;
            TextInputEditText stepInstructionEdit;
            ImageButton removeStepButton;

            StepViewHolder(@NonNull View itemView) {
                super(itemView);
                stepOrderText = itemView.findViewById(R.id.text_view_step_order);
                stepInstructionEdit = itemView.findViewById(R.id.edit_text_step_instruction);
                removeStepButton = itemView.findViewById(R.id.button_remove_step);

                removeStepButton.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        localStepsList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, localStepsList.size());
                    }
                });

                stepInstructionEdit.addTextChangedListener(new TextWatcher() {
                    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            localStepsList.get(position).setInstruction(s.toString());
                        }
                    }
                    @Override public void afterTextChanged(Editable s) {}
                });
            }

            void bind(ProtocolStep step) {
                // Cập nhật số thứ tự dựa trên vị trí trong adapter
                stepOrderText.setText(String.format("%d.", getAdapterPosition() + 1));
                step.setStepOrder(getAdapterPosition() + 1);
                stepInstructionEdit.setText(step.getInstruction());
            }
        }
    }

    // =======================================================================
    // ====== ADAPTER MỚI ĐỂ HIỂN THỊ ITEM ĐÃ CHỌN (ItemsDisplayAdapter) ======
    // =======================================================================
    private class ItemsDisplayAdapter extends RecyclerView.Adapter<ItemsDisplayAdapter.ItemViewHolder> {
        private final List<ProtocolItem> localItemsList;
        private final List<Item> localAvailableItems; // Cần list này để tìm tên

        ItemsDisplayAdapter(List<ProtocolItem> itemsList, List<Item> availableItems) {
            this.localItemsList = itemsList;
            this.localAvailableItems = availableItems;
        }

        @NonNull
        @Override
        public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_create_item_display, parent, false);
            return new ItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
            ProtocolItem protocolItem = localItemsList.get(position);
            // Tìm tên của item dựa trên itemId
            String itemName = "Unknown Item (ID: " + protocolItem.getItemId() + ")";
            for (Item availableItem : localAvailableItems) {
                if (availableItem.getItemId().equals(protocolItem.getItemId())) {
                    itemName = availableItem.getItemName();
                    break;
                }
            }
            holder.bind(itemName, protocolItem);
        }

        @Override
        public int getItemCount() {
            return localItemsList.size();
        }

        class ItemViewHolder extends RecyclerView.ViewHolder {
            TextView itemNameText, itemQuantityText;
            ImageButton removeItemButton;

            ItemViewHolder(@NonNull View itemView) {
                super(itemView);
                itemNameText = itemView.findViewById(R.id.text_view_item_name);
                itemQuantityText = itemView.findViewById(R.id.text_view_item_quantity);
                removeItemButton = itemView.findViewById(R.id.button_remove_display_item);

                removeItemButton.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        localItemsList.remove(position);
                        notifyItemRemoved(position);
                    }
                });
            }

            void bind(String name, ProtocolItem item) {
                itemNameText.setText(name);
                itemQuantityText.setText("x " + item.getQuantity());
            }
        }
    }
}
