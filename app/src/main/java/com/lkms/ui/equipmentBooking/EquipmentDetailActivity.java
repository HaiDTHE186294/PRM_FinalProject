package com.lkms.ui.equipmentBooking;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import com.lkms.R;
import com.lkms.data.model.java.Equipment;
import com.lkms.data.repository.IEquipmentRepository;
import com.lkms.data.repository.implement.java.EquipmentRepositoryImplJava;
import com.lkms.domain.EquipmentBookingUseCase;
import com.lkms.ui.common.MaintenanceLogFragment;
import com.lkms.ui.common.QRScannerFragment;

public class EquipmentDetailActivity extends AppCompatActivity {

    public static final String EXTRA_EQUIPMENT_ID = "EXTRA_EQUIPMENT_ID";
    public static final String EXTRA_EQUIPMENT_NAME = "EXTRA_EQUIPMENT_NAME";

    private int equipmentId;

    private TextView tvName, tvModel, tvSerial, tvStatus;
    private Button btnBookNow, btnDownloadManual, btnViewManual;
    private Button btnScanQR;


    private String manualUrl;
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
        btnDownloadManual = findViewById(R.id.btnDownloadManual);
        btnViewManual = findViewById(R.id.btnViewManual);

        QRScannerFragment qrFragment = new QRScannerFragment();
        qrFragment.setQRScanListener(serial -> {
            loadManualUrl(serial, true);
        });
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentQrContainer, qrFragment)
                .commit();

        btnDownloadManual.setEnabled(false);
        btnViewManual.setEnabled(false);

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

        MaintenanceLogFragment maintenanceFragment = new MaintenanceLogFragment();
        Bundle args = new Bundle();
        args.putInt("equipment_id", equipmentId);
        maintenanceFragment.setArguments(args);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentMaintenanceContainer, maintenanceFragment)
                .commit();

    }

    private void loadEquipmentDetails(int equipmentId) {
        useCase.getEquipmentById(equipmentId, new IEquipmentRepository.EquipmentCallback() {
            @Override
            public void onSuccess(Equipment equipment) {
                runOnUiThread(() -> {
                    tvName.setText(equipment.getEquipmentName());
                    tvModel.setText("Model: " + equipment.getModel());
                    tvSerial.setText("Serial: " + equipment.getSerialNumber());
                    tvStatus.setText("Available: " + (equipment.getAvailability() ? "Yes" : "No"));
                });
                loadManualUrl(equipment.getSerialNumber(), false);
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> Toast.makeText(EquipmentDetailActivity.this, errorMessage, Toast.LENGTH_LONG).show());
            }
        });
    }

    private void loadManualUrl(String serial, boolean openAfterLoad) {
        useCase.getManualUrlBySerial(serial, new IEquipmentRepository.StringCallback() {
            @Override
            public void onSuccess(String url) {
                runOnUiThread(() -> {
                    manualUrl = url;
                    if (manualUrl != null && !manualUrl.isEmpty()) {
                        btnDownloadManual.setEnabled(true);
                        btnViewManual.setEnabled(true);
                        btnDownloadManual.setOnClickListener(v -> downloadManualPDF());
                        btnViewManual.setOnClickListener(v -> openPdfViewer());

                        if (openAfterLoad) {
                            openPdfViewer(); // mở PDF ngay
                        }
                    } else {
                        disableManualButtons();
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> disableManualButtons());
            }
        });
    }


    private void disableManualButtons() {
        btnDownloadManual.setText("No Manual");
        btnDownloadManual.setEnabled(false);

        btnViewManual.setText("No Manual");
        btnViewManual.setEnabled(false);
    }

    private void downloadManualPDF() {
        if (manualUrl == null || manualUrl.isEmpty()) {
            Toast.makeText(this, "No manual file available", Toast.LENGTH_SHORT).show();
            return;
        }

        DownloadManager downloadManager =
                (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

        Uri uri = Uri.parse(manualUrl);
        DownloadManager.Request request = new DownloadManager.Request(uri);

        request.setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
        );
        request.setTitle("Downloading Manual");
        request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                "equipment_manual.pdf"
        );

        downloadManager.enqueue(request);

        Toast.makeText(this, "Downloading started...", Toast.LENGTH_SHORT).show();
    }

    private void openPdfViewer() {
        Intent intent = new Intent(this, PdfViewerActivity.class);
        intent.putExtra(PdfViewerActivity.EXTRA_PDF_URL, manualUrl);
        startActivity(intent);
    }


    private final ActivityResultLauncher<ScanOptions> qrLauncher = registerForActivityResult(
            new ScanContract(),
            result -> {
                if (result.getContents() != null) {
                    String serialFromQR = result.getContents();
                    Toast.makeText(this, "Scanned: " + serialFromQR, Toast.LENGTH_SHORT).show();
                    loadManualUrl(serialFromQR, true);
                }
            }
    );
}
