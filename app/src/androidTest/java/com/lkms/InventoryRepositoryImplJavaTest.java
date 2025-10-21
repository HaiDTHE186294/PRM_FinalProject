package com.lkms;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.lkms.data.model.java.Item;
import com.lkms.data.repository.IInventoryRepository;
import com.lkms.data.repository.implement.java.InventoryRepositoryImplJava;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileWriter;
import java.util.Date;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class InventoryRepositoryImplJavaTest {

    private final InventoryRepositoryImplJava repo = new InventoryRepositoryImplJava();
    private static final int SLEEP_TIME_MS = 3000; // Thời gian chờ cho các yêu cầu cơ bản

    // ----------------------------------------------------------------------------------
    // ✅ 1. Test lấy tất cả các Item trong kho
    // ----------------------------------------------------------------------------------
    @Test
    public void testGetAllInventoryItems() throws InterruptedException {
        repo.getAllInventoryItems(new IInventoryRepository.InventoryListCallback() {
            @Override
            public void onSuccess(List<Item> result) {
                System.out.println("✅ Loaded " + (result != null ? result.size() : 0) + " inventory items.");
                if (result != null) {
                    for (Item item : result) {
                        System.out.println(" - " + item.getItemName() + " | ID: " + item.getItemId());
                    }
                }
            }

            @Override
            public void onError(String error) {
                System.out.println("❌ Error: " + error);
            }
        });
        Thread.sleep(SLEEP_TIME_MS);
    }

    // ----------------------------------------------------------------------------------
    // ✅ 2. Test tìm kiếm Item trong kho (theo tên hoặc CAS)
    // ----------------------------------------------------------------------------------
    @Test
    public void testSearchInventory() throws InterruptedException {
        final String query = "Test Chemical"; // 🔍 Thử với từ khóa giả định

        repo.searchInventory(query, new IInventoryRepository.InventoryListCallback() {
            @Override
            public void onSuccess(List<Item> result) {
                System.out.println("✅ Found " + (result != null ? result.size() : 0) + " items for query '" + query + "'");
                if (result != null) {
                    for (Item item : result) {
                        System.out.println(" - " + item.getItemName() + " | CAS: " + item.getCasNumber());
                    }
                }
            }

            @Override
            public void onError(String error) {
                System.out.println("❌ No items found: " + error);
            }
        });
        Thread.sleep(SLEEP_TIME_MS);
    }

    // ----------------------------------------------------------------------------------
    // ✅ 3. Test lấy SDS URL theo CAS number
    // ----------------------------------------------------------------------------------
    @Test
    public void testGetSdsUrl() throws InterruptedException {
        final String casNumber = "S1"; // 🔹 Thay bằng CAS thật có trong DB

        repo.getSdsUrl(casNumber, new IInventoryRepository.StringCallback() {
            @Override
            public void onSuccess(String result) {
                System.out.println("✅ SDS URL for " + casNumber + ": " + result);
            }

            @Override
            public void onError(String error) {
                System.out.println("❌ Failed to get SDS: " + error);
            }
        });
        Thread.sleep(SLEEP_TIME_MS);
    }

    // ----------------------------------------------------------------------------------
    // ✅ 4. Test thêm mới Item vào kho
    // ----------------------------------------------------------------------------------
    @Test
    public void testAddNewInventoryItem() throws InterruptedException {
        final String uniqueName = "Test Item " + System.currentTimeMillis();

        final Item newItem = new Item(
                null, // auto-increment
                uniqueName,
                "9876-54-3",
                "LOT-" + System.currentTimeMillis(),
                100,
                "mL",
                "Shelf C1",
                "2027-01-01"
        );

        repo.addNewInventoryItem(newItem, new IInventoryRepository.InventoryItemCallback() {
            @Override
            public void onSuccess(Item result) {
                System.out.println("✅ Added new item successfully: " + result.getItemName() + " | ID: " + result.getItemId());
            }

            @Override
            public void onError(String error) {
                System.out.println("❌ Failed to add new item: " + error);
            }
        });
        Thread.sleep(SLEEP_TIME_MS);
    }

    // ----------------------------------------------------------------------------------
    // ✅ 5. Test update Item đã có trong kho
    // ----------------------------------------------------------------------------------
    @Test
    public void testUpdateInventoryItem() throws InterruptedException {
        final int itemIdToUpdate = 3; // LƯU Ý: Thay bằng ID có sẵn trong DB

        final Item updatedData = new Item(
                itemIdToUpdate,
                "Chemical (Updated: " + new Date() + ")", // Tên mới
                "1234-56-7",
                "LOT-001",
                150, // Số lượng mới
                "g",
                "Shelf B2",
                "2027-01-15"
        );

        repo.updateInventoryItem(itemIdToUpdate, updatedData, new IInventoryRepository.InventoryItemCallback() {
            @Override
            public void onSuccess(Item result) {
                System.out.println("✅ Item updated successfully: ID " + result.getItemId());
                System.out.println("   Quantity: " + result.getQuantity());
            }

            @Override
            public void onError(String error) {
                System.out.println("❌ Failed to update item: " + error);
            }
        });
        Thread.sleep(SLEEP_TIME_MS);
    }

    // ----------------------------------------------------------------------------------
    // ✅ 6. Test upload file SDS lên Supabase Storage và thêm bản ghi SDS
    // ----------------------------------------------------------------------------------
    @Test
    public void testUploadAndAddSds() throws Exception {
        var context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        // --- 1️⃣ Tạo file mẫu ---
        final File testFile = new File(context.getFilesDir(), "sds_java_sample_" + System.currentTimeMillis() + ".txt");
        if (testFile.exists()) testFile.delete();
        try (FileWriter writer = new FileWriter(testFile)) {
            writer.write("This is a dummy SDS file for upload test in Java.");
        }

        System.out.println("📂 File created: " + testFile.getAbsolutePath() + ", size = " + testFile.length() + " bytes");

        // --- 2️⃣ Upload file ---
        repo.uploadFileSdsToStorage(
                testFile,
                new IInventoryRepository.StringCallback() {
                    @Override
                    public void onSuccess(String fileUrl) {
                        System.out.println("✅ Upload successful! Public URL: " + fileUrl);

                        // --- 3️⃣ Thêm bản ghi SDS vào DB ---
                        final String uniqueCasNumber = "CAS-" + System.currentTimeMillis();
                        repo.addSds(
                                uniqueCasNumber,
                                fileUrl,
                                new IInventoryRepository.IdCallback() {
                                    @Override
                                    public void onSuccess(String result) {
                                        System.out.println("✅ SDS added successfully with sdsId (CAS): " + result);
                                        // Dọn dẹp file mẫu sau khi test hoàn tất
                                        if (testFile.exists()) testFile.delete();
                                    }

                                    @Override
                                    public void onError(String error) {
                                        System.out.println("❌ Error adding SDS record: " + error);
                                    }
                                }
                        );
                    }

                    @Override
                    public void onError(String error) {
                        System.out.println("❌ Error uploading file: " + error);
                    }
                }
        );

        // Chờ lâu hơn vì bao gồm cả upload và 1 cuộc gọi DB sau đó
        Thread.sleep(7000);
    }

    // ----------------------------------------------------------------------------------
    // ✅ 7. Test add casNumber và url vào bảng SDS (dạng đơn giản, không upload)
    // ----------------------------------------------------------------------------------
    @Test
    public void testAddSdsSimple() throws InterruptedException {
        final String uniqueCasNumber = "CAS-" + System.currentTimeMillis();
        final String url = "https://mock-url/sds/" + uniqueCasNumber + ".pdf";

        repo.addSds(uniqueCasNumber, url, new IInventoryRepository.IdCallback() {
            @Override
            public void onSuccess(String result) {
                System.out.println("✅ SDS added successfully with sdsId (CAS): " + result);
            }

            @Override
            public void onError(String error) {
                System.out.println("❌ Failed to add SDS: " + error);
            }
        });

        Thread.sleep(SLEEP_TIME_MS);
    }
}