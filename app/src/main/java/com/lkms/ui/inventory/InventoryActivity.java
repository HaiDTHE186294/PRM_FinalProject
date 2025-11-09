package com.lkms.ui.inventory;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.SearchView;

import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import com.lkms.R;
import com.lkms.data.model.java.Item;
import com.lkms.data.repository.IInventoryRepository;
import com.lkms.data.repository.implement.java.InventoryRepositoryImplJava;
import com.lkms.domain.inventory.InventoryManagementUseCase;

import java.util.List;

public class InventoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private InventoryManagementAdapter adapter;
    private InventoryManagementUseCase inventoryManagementUseCase;
    private List<Item> allItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_inventory);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.recycler_inventory);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        inventoryManagementUseCase = new InventoryManagementUseCase(new InventoryRepositoryImplJava());

        loadInventoryItems();

        // Xử lý SearchView
        SearchView searchView = findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchInventory(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    loadInventoryItems();
                } else {
                    searchInventory(newText);
                }
                return true;
            }
        });

        // --- Nút quét QR ---
        ImageButton btnScan = findViewById(R.id.btn_scan);
        btnScan.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                startQrScanner();
            } else {
                requestCameraPermission.launch(Manifest.permission.CAMERA);
            }
        });

        FloatingActionButton fabAddItem = findViewById(R.id.fab_add_item);
        fabAddItem.setOnClickListener( v -> {
            Intent intent = new Intent(InventoryActivity.this, ItemAddUpdateActivity.class);
            startActivity(intent);
        });
    }


    private void loadInventoryItems() {
        inventoryManagementUseCase.getAllInventoryItems(new IInventoryRepository.InventoryListCallback() {
            @Override
            public void onSuccess(List<Item> items) {
                runOnUiThread(() -> {
                    if (items != null && !items.isEmpty()) {
                        adapter = new InventoryManagementAdapter(items);
                        recyclerView.setAdapter(adapter);
                    } else {
                        Toast.makeText(InventoryActivity.this, "No inventory items found.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    Toast.makeText(InventoryActivity.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void searchInventory(String query) {
        inventoryManagementUseCase.searchInventory(query, new IInventoryRepository.InventoryListCallback() {
            @Override
            public void onSuccess(List<Item> items) {
                runOnUiThread(() -> {
                    if (items != null && !items.isEmpty()) {
                        adapter = new InventoryManagementAdapter(items);
                        recyclerView.setAdapter(adapter);
                    } else {
                        Toast.makeText(InventoryActivity.this, "No results found for '" + query + "'", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    Toast.makeText(InventoryActivity.this, "Search Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    // --- Camera permission launcher ---
    private final ActivityResultLauncher<String> requestCameraPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startQrScanner();
                } else {
                    Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
                }
            });


    private void startQrScanner() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Scan the QR code of an item");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        //  options.setCaptureActivity(CaptureAct.class); // bạn có thể tạo class rỗng CaptureAct để tùy chỉnh
        barcodeLauncher.launch(options);
    }


    // --- ZXing scanner launcher ---
    private final ActivityResultLauncher<ScanOptions> barcodeLauncher =
            registerForActivityResult(new ScanContract(), result -> {
                if (result.getContents() != null) {
                    String scannedCode = result.getContents();
                    Toast.makeText(this, "Scanned: " + scannedCode, Toast.LENGTH_SHORT).show();

                    try {
                        // Chuyển từ chuỗi sang số nguyên
                        int itemId = Integer.parseInt(scannedCode);

                        Intent intent = new Intent(this, ItemDetailActivity.class);
                        intent.putExtra("ITEM_ID", itemId);
                        startActivity(intent);

                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "QR is invalid!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Scan cancelled", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onResume() {
        super.onResume();
        loadInventoryItems();
    }

}