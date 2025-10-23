package com.lkms.ui.equipmentBooking;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.lkms.R;
import com.lkms.data.model.java.Equipment;
import com.lkms.data.repository.implement.java.EquipmentRepositoryImplJava;
import com.lkms.domain.EquipmentBookingUseCase;

public class EquipmentDetailActivity extends AppCompatActivity {

    public static final String EXTRA_EQUIPMENT_ID = "EXTRA_EQUIPMENT_ID";
    public static final String EXTRA_EQUIPMENT_NAME = "EXTRA_EQUIPMENT_NAME"; // thêm dòng này


    private int equipmentId;

    private TextView tvName, tvModel, tvSerial, tvStatus;
    private Button btnBookNow;

    private EquipmentBookingUseCase useCase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equipment_detail);

        tvName = findViewById(R.id.tvEquipmentName);
        tvModel = findViewById(R.id.tvEquipmentModel);
        tvSerial = findViewById(R.id.tvEquipmentSerial);
        tvStatus = findViewById(R.id.tvEquipmentStatus);
        btnBookNow = findViewById(R.id.btnBookNow);

        equipmentId = getIntent().getIntExtra(EXTRA_EQUIPMENT_ID, -1);
        useCase = new EquipmentBookingUseCase(new EquipmentRepositoryImplJava());

        if (equipmentId != -1) {
            loadEquipmentDetails(equipmentId);
        } else {
            Toast.makeText(this, "Thiết bị không hợp lệ", Toast.LENGTH_SHORT).show();
        }

        btnBookNow.setOnClickListener(v -> {
            Intent intent = new Intent(this, BookingActivity.class);
            intent.putExtra(BookingActivity.EXTRA_EQUIPMENT_ID, equipmentId);
            intent.putExtra(BookingActivity.EXTRA_EQUIPMENT_NAME, tvName.getText().toString());
            startActivity(intent);
        });
    }

    private void loadEquipmentDetails(int equipmentId) {
        useCase.getEquipmentById(equipmentId, new com.lkms.data.repository.IEquipmentRepository.EquipmentCallback() {
            @Override
            public void onSuccess(Equipment equipment) {
                runOnUiThread(() -> {
                    tvName.setText(equipment.getEquipmentName());
                    tvModel.setText("Model: " + equipment.getModel());
                    tvSerial.setText("Serial: " + equipment.getSerialNumber());
                    tvStatus.setText("Available: " + (equipment.getAvailability() ? "Yes" : "No"));
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> Toast.makeText(EquipmentDetailActivity.this, errorMessage, Toast.LENGTH_LONG).show());
            }
        });
    }
}
