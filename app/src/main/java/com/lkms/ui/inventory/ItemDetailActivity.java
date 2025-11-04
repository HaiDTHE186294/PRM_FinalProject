package com.lkms.ui.inventory;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.lkms.R;
import com.lkms.data.model.java.Item;
import com.lkms.data.repository.IInventoryRepository;
import com.lkms.data.repository.implement.java.InventoryRepositoryImplJava;
import com.lkms.domain.inventoryusecase.InventoryManagementUseCase;

public class ItemDetailActivity extends AppCompatActivity {
    private TextView tvItemName, tvItemCas, tvItemLotNumber, tvItemQuantity, tvItemUnit, tvItemLocation, tvItemExpiration;
    private InventoryManagementUseCase inventoryManagementUseCase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_item_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Ánh xạ các TextView
        tvItemName = findViewById(R.id.tv_item_name);
        tvItemCas = findViewById(R.id.tv_item_cas);
        tvItemLotNumber = findViewById(R.id.tv_item_lot_number);
        tvItemQuantity = findViewById(R.id.tv_item_quantity);
        tvItemUnit = findViewById(R.id.tv_item_unit);
        tvItemLocation = findViewById(R.id.tv_item_location);
        tvItemExpiration = findViewById(R.id.tv_item_expiration);

        Intent intent = getIntent();
        int itemId = intent.getIntExtra("ITEM_ID", -1);

        if (itemId != -1) {
            inventoryManagementUseCase = new InventoryManagementUseCase(new InventoryRepositoryImplJava());
            inventoryManagementUseCase.getItemById(itemId, new IInventoryRepository.InventoryItemCallback() {
                @Override
                public void onSuccess(Item item) {
                    runOnUiThread(() -> {
                        tvItemName.setText(item.getItemName());
                        tvItemCas.setText("CAS: " + item.getCasNumber());
                        tvItemLotNumber.setText("Lot Number: " + item.getLotNumber());
                        tvItemQuantity.setText("Quantity: " + item.getQuantity());
                        tvItemUnit.setText("Unit: " + item.getUnit());
                        tvItemLocation.setText("Location: " + item.getLocation());
                        tvItemExpiration.setText("Expiration Date: " + item.getExpirationDate());
                    });
                }
                @Override
                public void onError(String errorMessage) {
                    runOnUiThread(() -> {
                        Toast.makeText(ItemDetailActivity.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                    });
                }
            });
        } else {
            Toast.makeText(this, "Invalid Item ID", Toast.LENGTH_SHORT).show();
        }

    }
}