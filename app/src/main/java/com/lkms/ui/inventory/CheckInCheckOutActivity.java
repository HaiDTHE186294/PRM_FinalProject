package com.lkms.ui.inventory;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.lkms.R;
import com.lkms.data.model.java.InventoryTransaction;
import com.lkms.data.model.java.Item;
import com.lkms.data.repository.IInventoryRepository;
import com.lkms.data.repository.implement.java.InventoryRepositoryImplJava;
import com.lkms.domain.inventory.InventoryItemUseCase;
import com.lkms.domain.inventory.InventoryManagementUseCase;
import com.lkms.domain.inventory.InventoryTransactionUseCase;
import com.lkms.util.AuthHelper;

import java.util.List;

public class CheckInCheckOutActivity extends AppCompatActivity {

    private TextView tvItemName, tvCurrentQuantity;
    private MaterialToolbar toolbar;
    private EditText etQuantity;
    private MaterialButton btnCancel, btnConfirm;

    private RecyclerView rvTransactions;
    private InventoryTransactionAdapter transactionAdapter;

    private InventoryItemUseCase inventoryItemUseCase = new InventoryItemUseCase();
    private InventoryManagementUseCase inventoryManagementUseCase = new InventoryManagementUseCase(new InventoryRepositoryImplJava());
    private InventoryTransactionUseCase inventoryTransactionUseCase = new InventoryTransactionUseCase();


    private Item currentItem;
    private String mode;
    private int itemId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_check_in_check_out);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        toolbar = findViewById(R.id.toolbar);

        tvItemName = findViewById(R.id.tv_item_name);
        tvCurrentQuantity = findViewById(R.id.tv_item_quantity);
        etQuantity = findViewById(R.id.et_quantity);
        btnCancel = findViewById(R.id.btn_cancel);
        btnConfirm = findViewById(R.id.btn_confirm);

        rvTransactions = findViewById(R.id.rv_transactions);

        Intent intent = getIntent();
        itemId = intent.getIntExtra("ITEM_ID", -1);
        mode = intent.getStringExtra("MODE");

        if (itemId != -1 && mode != null) {
            loadItemDetails();
            loadTransactionHistory();
            setupUI();
        } else {
            Toast.makeText(this, "Invalid data received", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnCancel.setOnClickListener(v -> finish()); // Go back to the previous activity

        btnConfirm.setOnClickListener(v -> handleConfirmClick());
    }

    private void loadItemDetails() {
        inventoryManagementUseCase.getItemById(itemId, new IInventoryRepository.InventoryItemCallback() {
            @Override
            public void onSuccess(Item item) {

                inventoryItemUseCase.overrideItem(item);

                runOnUiThread(() -> {
                    currentItem = item;
                    tvItemName.setText(item.getItemName());
                    tvCurrentQuantity.setText("Quantity: " + item.getQuantity());
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    Toast.makeText(CheckInCheckOutActivity.this, "Error loading item: " + errorMessage, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void loadTransactionHistory() {

        inventoryTransactionUseCase.getList().observe(
                this, transactions -> {
                    rvTransactions.setLayoutManager(new LinearLayoutManager(CheckInCheckOutActivity.this));
                    transactionAdapter = new InventoryTransactionAdapter(transactions);
                    rvTransactions.setAdapter(transactionAdapter);
                }
        );

        inventoryTransactionUseCase.loadTransacitonList(itemId);
    }

    private void setupUI() {
        if ("checkin".equalsIgnoreCase(mode)) {
            btnConfirm.setText("CHECK IN");
            toolbar.setTitle("CHECK IN");
            etQuantity.setHint("Quantity to Check In");
        } else if ("checkout".equalsIgnoreCase(mode)) {
            btnConfirm.setText("CHECK OUT");
            toolbar.setTitle("CHECK OUT");
            etQuantity.setHint("Quantity to Check Out");
        }
    }

    private void handleConfirmClick() {
        String quantityStr = etQuantity.getText().toString();
        if (TextUtils.isEmpty(quantityStr)) {
            etQuantity.setError("Quantity cannot be empty");
            return;
        }

        int quantityChange = Integer.parseInt(quantityStr);
        if (quantityChange <= 0) {
            etQuantity.setError("Quantity must be positive");
            return;
        }

        if (currentItem == null) {
            Toast.makeText(this, "Item data not loaded yet.", Toast.LENGTH_SHORT).show();
            return;
        }

        int newQuantity = -1;
        String transactionType = "";
        if ("checkout".equalsIgnoreCase(mode))
        {
            if (quantityChange > currentItem.getQuantity()) {
                etQuantity.setError("Checkout quantity cannot exceed current quantity");
                return;
            }
            newQuantity = currentItem.getQuantity() - quantityChange;
            transactionType = "checkout";

        }
        else if ("checkin".equalsIgnoreCase(mode))
        {
            newQuantity = currentItem.getQuantity() + quantityChange;
            transactionType = "checkin";
        }

        //UPDATE QUANTITY
        if (newQuantity != -1 && transactionType != "")
            inventoryItemUseCase.updateQuantity(newQuantity, AuthHelper.getLoggedInUserId(this), transactionType);

        finish();
    }


}
