package com.lkms.ui.protocol;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import com.lkms.ui.protocol.adapter.ItemsDisplayAdapter;
import com.lkms.ui.protocol.adapter.StepsAdapter;
import com.lkms.ui.protocol.viewmodel.CreateProtocolViewModel;
import com.lkms.util.AuthHelper;
import com.lkms.data.repository.enumPackage.java.LKMSConstantEnums.ProtocolApproveStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    private final List<ProtocolItem> itemsList = new ArrayList<>();
    private final List<Item> availableItems = new ArrayList<>();
    private ArrayAdapter<Item> availableItemsSpinnerAdapter;
    private CreateProtocolViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_protocol);

        viewModel = new ViewModelProvider(this).get(CreateProtocolViewModel.class);

        initViews();
        setupToolbar();
        setupAdapters();
        // Sửa ở đây: Hàm setupEventListeners đã được chia nhỏ
        setupEventListeners();
        observeViewModel();

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
        stepsAdapter = new StepsAdapter(stepsList);
        stepsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        stepsRecyclerView.setAdapter(stepsAdapter);

        itemsAdapter = new ItemsDisplayAdapter(itemsList, availableItems);
        itemsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        itemsRecyclerView.setAdapter(itemsAdapter);

        availableItemsSpinnerAdapter = new ArrayAdapter<Item>(this, android.R.layout.simple_spinner_item, this.availableItems) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                return createSpinnerView(position, convertView, parent, false);
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                return createSpinnerView(position, convertView, parent, true);
            }

            private View createSpinnerView(int position, @Nullable View convertView, @NonNull ViewGroup parent, boolean isDropDown) {
                TextView textView = (TextView) (isDropDown ?
                        super.getDropDownView(position, convertView, parent) :
                        super.getView(position, convertView, parent));

                Item currentItem = getItem(position);
                if (currentItem != null) {
                    String displayText = String.format(
                            "(Tồn kho: %d %s) %s",
                            currentItem.getQuantity(),
                            currentItem.getUnit(),
                            currentItem.getItemName()
                    );
                    textView.setText(displayText);
                }
                return textView;
            }
        };

        selectAvailableItemSpinner.setAdapter(availableItemsSpinnerAdapter);
    }

    // =================================================================================
    // 🔥 BẮT ĐẦU PHẦN TÁI CẤU TRÚC (REFACTOR)
    // =================================================================================

    /**
     * Hàm điều phối, gọi các hàm con để đăng ký sự kiện cho từng nút.
     * Phương thức này giờ đây có Độ phức tạp nhận thức (Cognitive Complexity) rất thấp.
     */
    private void setupEventListeners() {
        setupAddStepButtonListener();
        setupAddItemButtonListener();
        setupSaveProtocolButtonListener();
    }

    /**
     * Xử lý sự kiện cho nút "Thêm bước".
     */
    private void setupAddStepButtonListener() {
        addStepButton.setOnClickListener(v -> {
            stepsList.add(new ProtocolStep());
            stepsAdapter.notifyItemInserted(stepsList.size() - 1);
            stepsRecyclerView.smoothScrollToPosition(stepsList.size() - 1);
        });
    }

    /**
     * Xử lý sự kiện cho nút "Thêm vật tư vào danh sách".
     * Logic validate và thêm vật tư được gói gọn trong hàm này.
     */
    private void setupAddItemButtonListener() {
        addItemToListButton.setOnClickListener(v -> {
            Item selectedItem = (Item) selectAvailableItemSpinner.getSelectedItem();
            if (selectedItem == null) {
                Toast.makeText(this, "Chưa có vật tư để chọn.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Sử dụng Optional để xử lý việc parse số lượng một cách an toàn và gọn gàng hơn
            Optional<Integer> quantityOptional = getValidQuantity(selectedItem);
            if (!quantityOptional.isPresent()) {
                return; // Dừng lại nếu số lượng không hợp lệ
            }
            int quantity = quantityOptional.get();

            // Kiểm tra xem vật tư đã tồn tại trong danh sách chưa
            boolean itemExists = itemsList.stream().anyMatch(item -> item.getItemId().equals(selectedItem.getItemId()));
            if (itemExists) {
                Toast.makeText(this, "Vật tư này đã có trong danh sách.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Nếu tất cả đều hợp lệ, thêm vào danh sách và cập nhật UI
            addNewProtocolItem(selectedItem, quantity);
        });
    }

    /**
     * Xử lý sự kiện cho nút "Lưu Protocol".
     * Logic validate toàn bộ form và gọi ViewModel được gói gọn trong hàm này.
     */
    private void setupSaveProtocolButtonListener() {
        saveProtocolButton.setOnClickListener(v -> {
            String protocolName = protocolNameInput.getText().toString().trim();
            String version = versionInput.getText().toString().trim();

            // Validate các trường bắt buộc
            if (!isProtocolInfoValid(protocolName, version)) {
                return;
            }

            // Lọc và đánh số thứ tự các bước hợp lệ
            List<ProtocolStep> validSteps = getValidSteps();
            if (validSteps.isEmpty()) {
                Toast.makeText(this, "Protocol phải có ít nhất một bước hợp lệ.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Tạo đối tượng Protocol và gọi ViewModel để lưu
            Protocol protocolData = createProtocolData(protocolName, version);
            viewModel.createProtocol(protocolData, validSteps, itemsList, protocolData.getCreatorUserId());
        });
    }

    // --- Các hàm trợ giúp (Helper Methods) được tách ra từ logic phức tạp ---

    /**
     * Kiểm tra và trả về số lượng hợp lệ từ người dùng.
     * @param selectedItem Item được chọn để kiểm tra tồn kho.
     * @return Optional chứa số lượng nếu hợp lệ, ngược lại trả về Optional rỗng.
     */
    private Optional<Integer> getValidQuantity(Item selectedItem) {
        String quantityStr = selectQuantityInput.getText().toString();
        if (quantityStr.isEmpty()) {
            selectQuantityInput.setError("Số lượng không được để trống");
            return Optional.empty();
        }
        try {
            int quantity = Integer.parseInt(quantityStr);
            if (quantity <= 0) {
                selectQuantityInput.setError("Số lượng phải lớn hơn 0");
                return Optional.empty();
            }
            if (quantity > selectedItem.getQuantity()) {
                selectQuantityInput.setError("Không thể vượt quá tồn kho (" + selectedItem.getQuantity() + ")");
                return Optional.empty();
            }
            selectQuantityInput.setError(null); // Xóa lỗi nếu hợp lệ
            return Optional.of(quantity);
        } catch (NumberFormatException e) {
            selectQuantityInput.setError("Số lượng không hợp lệ");
            return Optional.empty();
        }
    }

    /**
     * Thêm một vật tư mới vào danh sách và cập nhật UI.
     */
    private void addNewProtocolItem(Item selectedItem, int quantity) {
        ProtocolItem newItem = new ProtocolItem();
        newItem.setItemId(selectedItem.getItemId());
        newItem.setQuantity(quantity);

        itemsList.add(newItem);
        itemsAdapter.notifyItemInserted(itemsList.size() - 1);
        itemsRecyclerView.smoothScrollToPosition(itemsList.size() - 1);

        // Reset các trường nhập liệu
        selectQuantityInput.setText("");
        selectQuantityInput.clearFocus();
        selectAvailableItemSpinner.setSelection(0);
    }

    /**
     * Kiểm tra thông tin cơ bản của Protocol (tên và phiên bản).
     */
    private boolean isProtocolInfoValid(String protocolName, String version) {
        if (protocolName.isEmpty()) {
            protocolNameInput.setError("Tên protocol không được để trống");
            return false;
        }
        if (version.isEmpty()) {
            versionInput.setError("Phiên bản không được để trống");
            return false;
        }
        return true;
    }

    /**
     * Lấy danh sách các bước hợp lệ (có nội dung) và đánh số thứ tự cho chúng.
     */
    private List<ProtocolStep> getValidSteps() {
        List<ProtocolStep> validSteps = new ArrayList<>();
        for (ProtocolStep step : stepsList) {
            if (step.getInstruction() != null && !step.getInstruction().trim().isEmpty()) {
                validSteps.add(step);
            }
        }
        // Đánh số thứ tự cho các bước hợp lệ
        for (int i = 0; i < validSteps.size(); i++) {
            validSteps.get(i).setStepOrder(i + 1);
        }
        return validSteps;
    }

    /**
     * Tạo đối tượng Protocol từ dữ liệu người dùng nhập.
     */
    private Protocol createProtocolData(String protocolName, String version) {
        Protocol protocolData = new Protocol();
        protocolData.setProtocolTitle(protocolName);
        protocolData.setIntroduction(protocolIntroductionInput.getText().toString().trim());
        protocolData.setSafetyWarning(safetyWarningInput.getText().toString().trim());
        protocolData.setVersionNumber(version);
        protocolData.setApproveStatus(ProtocolApproveStatus.APPROVED);
        protocolData.setCreatorUserId(AuthHelper.getLoggedInUserId(getApplicationContext()));
        return protocolData;
    }

    // =================================================================================
    // 🔥 KẾT THÚC PHẦN TÁI CẤU TRÚC
    // =================================================================================

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
                this.availableItemsSpinnerAdapter.notifyDataSetChanged();

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
}
