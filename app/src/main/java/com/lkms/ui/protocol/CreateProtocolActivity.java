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

// ✨ BƯỚC 2.1: THÊM IMPORT CHO ENUM
import com.lkms.data.repository.enumPackage.java.LKMSConstantEnums.ProtocolApproveStatus;

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
                TextView textView = (TextView) super.getView(position, convertView, parent);
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

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                TextView textView = (TextView) super.getDropDownView(position, convertView, parent);
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

    private void setupEventListeners() {
        addStepButton.setOnClickListener(v -> {
            ProtocolStep newStep = new ProtocolStep();
            stepsList.add(newStep);
            stepsAdapter.notifyItemInserted(stepsList.size() - 1);
            stepsRecyclerView.smoothScrollToPosition(stepsList.size() - 1);
        });

        addItemToListButton.setOnClickListener(v -> {
            Item selectedItem = (Item) selectAvailableItemSpinner.getSelectedItem();
            if (selectedItem == null) {
                Toast.makeText(this, "Chưa có vật tư để chọn.", Toast.LENGTH_SHORT).show();
                return;
            }

            String quantityStr = selectQuantityInput.getText().toString();
            if (quantityStr.isEmpty()) {
                selectQuantityInput.setError("Số lượng không được để trống");
                return;
            }
            int quantity;
            try {
                quantity = Integer.parseInt(quantityStr);
                if (quantity <= 0) {
                    selectQuantityInput.setError("Số lượng phải lớn hơn 0");
                    return;
                }
                if (quantity > selectedItem.getQuantity()) {
                    selectQuantityInput.setError("Không thể vượt quá tồn kho (" + selectedItem.getQuantity() + ")");
                    return;
                }
            } catch (NumberFormatException e) {
                selectQuantityInput.setError("Số lượng không hợp lệ");
                return;
            }

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

            selectQuantityInput.setText("");
            selectQuantityInput.setError(null);
            selectAvailableItemSpinner.setSelection(0);
        });

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

            List<ProtocolStep> validSteps = new ArrayList<>();
            for (ProtocolStep step : stepsList) {
                if (step.getInstruction() != null && !step.getInstruction().trim().isEmpty()) {
                    validSteps.add(step);
                }
            }

            if (validSteps.isEmpty()) {
                Toast.makeText(this, "Protocol phải có ít nhất một bước hợp lệ.", Toast.LENGTH_SHORT).show();
                return;
            }

            for (int i = 0; i < validSteps.size(); i++) {
                validSteps.get(i).setStepOrder(i + 1);
            }

            Protocol protocolData = new Protocol();
            protocolData.setProtocolTitle(protocolName);
            protocolData.setIntroduction(introduction);
            protocolData.setSafetyWarning(safetyWarning);
            protocolData.setVersionNumber(version);

            // ✨ BƯỚC 2.2: GÁN TRẠNG THÁI MẶC ĐỊNH BẰNG ENUM
            protocolData.setApproveStatus(ProtocolApproveStatus.APPROVED);

            int creatorId = AuthHelper.getLoggedInUserId(getApplicationContext());
            protocolData.setCreatorUserId(creatorId);

            viewModel.createProtocol(protocolData, validSteps, itemsList, creatorId);
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
