package com.lkms.ui.inventory;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.lkms.R;
import com.lkms.data.model.java.Item;
import com.lkms.data.repository.enumPackage.java.LKMSConstantEnums;
import com.lkms.domain.inventory.InventoryItemUseCase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

public class InventoryItemActivity extends AppCompatActivity {

    InventoryItemUseCase inventoryItemUseCase;

    TextInputEditText nameEditText;
    TextInputEditText lotNumberEditText;
    TextInputEditText quantityEditText;
    TextInputEditText locationEditText;

    DatePicker datePicker;

    Button checkOutButton;
    Button checkInButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_inventory_item);

        inventoryItemUseCase = new InventoryItemUseCase();

        nameEditText = findViewById(R.id.item_name_edit_text);
        lotNumberEditText = findViewById(R.id.lot_number_edit_text);
        quantityEditText = findViewById(R.id.item_quantity_edit_text);
        locationEditText = findViewById(R.id.location_edit_text);

        datePicker = findViewById(R.id.datePicker);

        checkOutButton = findViewById(R.id.check_out_button);
        checkInButton = findViewById(R.id.check_in_button);

        //TODO: Cho ai sau này làm phần nối sang InventoryItem này:
        //      Nếu muốn người dùng cập nhật 1 item thì truyền id của item thông qua code:
        //      intent.putExtra("UPDATE ITEM ID", <id của item ở đây>);
        //      Còn nếu muốn add item thì không cần truyền id thông qua intent.
        //Activity must know either user want to add a new item or update an existing one,
        //base on the existence of "UPDATE ITEM ID" in the intent
//        if (getIntent().hasExtra("UPDATE ITEM ID"))
//        {
//            setupForUpdatingItem(
//                    getIntent().getIntExtra("UPDATE ITEM ID", -1)
//            );
//        }
//        else
//        {
//            setupForAddingItem();
//        }

        //TODO: For testing purpose, we'll call this method with hardcoded item id
//        setupForUpdatingItem(1);
        setupForAddingItem();
    }


    void setupForAddingItem()
    {
        //TODO: for testing purpose, this button will act as a "save" button
        checkInButton.setOnClickListener(v -> {
            try {
                Item item = new Item();

                item.setItemName(nameEditText.getText().toString());
                item.setLotNumber(lotNumberEditText.getText().toString());
                item.setQuantity(Integer.parseInt(quantityEditText.getText().toString()));
                item.setLocation(locationEditText.getText().toString());

                String formattedDate = String.format(
                    "%d-%02d-%02d",
                    datePicker.getYear(),
                    datePicker.getMonth() + 1,
                    datePicker.getDayOfMonth()
                );
                item.setExpirationDate(formattedDate);

                inventoryItemUseCase.addItem(item);

                Toast.makeText(this, "Added Successfully!", Toast.LENGTH_SHORT).show();
            }
            catch (Exception e)
            {
                Toast.makeText(this, "Something went wrong behind the scene", Toast.LENGTH_SHORT).show();
                Log.e("InventoryItemERROR", "An error occurred while updating item:" + e.getMessage());
            }
        });

        //TODO: for testing purpose, this button will act as a quit button
        checkOutButton.setOnClickListener(v -> finish());
    }

    void setupForUpdatingItem(int id)
    {
        if (id == -1)
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();

        //TODO: for testing purpose, this button will act as a "save" button
        checkInButton.setOnClickListener(v -> {
            try {

                String formattedDate = String.format(
                        "%d-%02d-%02d",
                        datePicker.getYear(),
                        datePicker.getMonth() + 1,
                        datePicker.getDayOfMonth()
                );

                inventoryItemUseCase.updateItem(
                    nameEditText.getText().toString(),
                    lotNumberEditText.getText().toString(),
                    Integer.parseInt(quantityEditText.getText().toString()),
                    locationEditText.getText().toString(),
                    formattedDate
                );
                Toast.makeText(this, "Saved Successfully!", Toast.LENGTH_SHORT).show();
            }
            catch (Exception e)
            {
                Toast.makeText(this, "Something went wrong behind the scene", Toast.LENGTH_SHORT).show();
                Log.e("InventoryItemERROR", "An error occurred while updating item:" + e.getMessage());
            }
        });

        //TODO: for testing purpose, this button will act as a quit button
        checkOutButton.setOnClickListener(v -> finish());

        //Setup observer
        inventoryItemUseCase.getItem().observe(this, item -> {

            if (item != null)
            {
                String expirationDate = item.getExpirationDate();
                DateTimeFormatter formatter = null;

                nameEditText.setText(item.getItemName());
                lotNumberEditText.setText(item.getLotNumber());
                quantityEditText.setText(String.valueOf(item.getQuantity()));
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

    void setupObserver()
    {
        inventoryItemUseCase.getItem().observe(this, item -> {

            if (item != null)
            {
            }
        });
    }
}