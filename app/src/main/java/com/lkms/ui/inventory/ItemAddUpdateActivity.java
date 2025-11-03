package com.lkms.ui.inventory;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.lkms.R;
import com.lkms.domain.inventory.InventoryItemUseCase;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ItemAddUpdateActivity extends AppCompatActivity {

    InventoryItemUseCase inventoryItemUseCase;

    TextInputEditText nameEditText;
    TextInputEditText casNumberEditText;
    TextInputEditText lotNumberEditText;
    TextInputEditText unitEditText;
    TextInputEditText locationEditText;

    DatePicker datePicker;

    Button cancelButton;
    Button addButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_item_add_update);

        inventoryItemUseCase = new InventoryItemUseCase();

        nameEditText = findViewById(R.id.item_name_edit_text);
        casNumberEditText = findViewById(R.id.cas_number_edit_text);
        lotNumberEditText = findViewById(R.id.lot_number_edit_text);
        unitEditText = findViewById(R.id.unit_edit_text);
        locationEditText = findViewById(R.id.location_edit_text);

        datePicker = findViewById(R.id.datePicker);

        cancelButton = findViewById(R.id.cancel_button);
        addButton = findViewById(R.id.add_button);

        //TODO: Cho ai sau này làm phần nối sang ItemAddUpdateActivity này:
        //      Nếu muốn người dùng cập nhật 1 item thì truyền id của item thông qua code:
        //      intent.putExtra("UPDATE ITEM ID", <id của item ở đây>);
        //      Còn nếu muốn add item thì không cần truyền id thông qua intent.
        if (getIntent().hasExtra("UPDATE ITEM ID"))
        {
            setupForUpdatingItem(
                getIntent().getIntExtra("UPDATE ITEM ID", -1)
            );
        }
        else
        {
            setupForAddingItem();
        }

        //TODO: For testing purpose, we'll call these method with hardcoded item id
//        setupForUpdatingItem(1);
//        setupForAddingItem();
    }


    void setupForAddingItem()
    {
        addButton.setOnClickListener(v -> {

            String name = nameEditText.getText().toString();
            String casNum = casNumberEditText.getText().toString();
            String lotNum = lotNumberEditText.getText().toString();
            String unit = unitEditText.getText().toString();
            String location = locationEditText.getText().toString();

            if (name.isEmpty() || casNum.isEmpty() || lotNum.isEmpty() || location.isEmpty() || unit.isEmpty()) {
                Toast.makeText(this, "All fields are required to be filled!", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                inventoryItemUseCase.addItem(name, casNum, lotNum, location, unit, getDate());
                Toast.makeText(this, "Added Successfully!", Toast.LENGTH_SHORT).show();
            }
            catch (Exception e)
            {
                Toast.makeText(this, "Something went wrong behind the scene", Toast.LENGTH_SHORT).show();
                Log.e("InventoryItemERROR", "An error occurred while updating item:" + e.getMessage());
            }
        });

        cancelButton.setOnClickListener(v -> finish());
    }

    void setupForUpdatingItem(int id)
    {
        if (id == -1)
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();

        addButton.setText("UPDATE");
        addButton.setOnClickListener(v -> {
            try {

                String name = nameEditText.getText().toString();
                String casNum = casNumberEditText.getText().toString();
                String lotNum = lotNumberEditText.getText().toString();
                String unit = unitEditText.getText().toString();
                String location = locationEditText.getText().toString();

                if (name.isEmpty() || casNum.isEmpty() || lotNum.isEmpty() || location.isEmpty() || unit.isEmpty()) {
                    Toast.makeText(this, "All fields are required to be filled!", Toast.LENGTH_SHORT).show();
                    return;
                }

                inventoryItemUseCase.updateItem(name, casNum, lotNum, location, unit, getDate());
                Toast.makeText(this, "Updated Successfully!", Toast.LENGTH_SHORT).show();
            }
            catch (Exception e)
            {
                Toast.makeText(this, "Something went wrong behind the scene", Toast.LENGTH_SHORT).show();
                Log.e("InventoryItemERROR", "An error occurred while updating item:" + e.getMessage());
            }
        });

        cancelButton.setOnClickListener(v -> finish());

        //Setup observer
        inventoryItemUseCase.getItem().observe(this, item -> {

            if (item != null)
            {
                String expirationDate = item.getExpirationDate();
                DateTimeFormatter formatter = null;

                nameEditText.setText(item.getItemName());
                lotNumberEditText.setText(item.getLotNumber());
                casNumberEditText.setText(String.valueOf(item.getQuantity()));
                locationEditText.setText(item.getLocation());

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { //Handle build version stuff
                    formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    LocalDate date = LocalDate.parse(expirationDate, formatter);
                    datePicker.init(
                            date.getYear(),
                            date.getMonthValue() - 1,
                            date.getDayOfMonth(),
                            (view, year, month, day) -> {
                            }
                    );
                }
            }

        });

        inventoryItemUseCase.loadItem(id);
    }

    @SuppressLint("DefaultLocale")
    String getDate()
    {
        return String.format(
            "%d-%02d-%02d",
            datePicker.getYear(),
            datePicker.getMonth() + 1,
            datePicker.getDayOfMonth()
        );
    }
}