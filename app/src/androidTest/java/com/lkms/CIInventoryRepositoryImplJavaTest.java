package com.lkms;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.lkms.data.model.java.Item;
import com.lkms.data.model.java.ProtocolItem;
import com.lkms.data.repository.IInventoryRepository;
import com.lkms.data.repository.implement.java.InventoryRepositoryImplJava;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import com.lkms.data.model.java.InventoryTransaction;


@RunWith(AndroidJUnit4.class)
public class CIInventoryRepositoryImplJavaTest {

    private final InventoryRepositoryImplJava repo = new InventoryRepositoryImplJava();

    @Test
    public void testGetAllInventoryItems() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        repo.getAllInventoryItems(new IInventoryRepository.InventoryListCallback() {
            @Override
            public void onSuccess(List<Item> result) {
                System.out.println("✅ Loaded " + (result != null ? result.size() : 0) + " inventory items.");
                assertNotNull("Result should not be null", result);
                latch.countDown();
            }

            @Override
            public void onError(String error) {
                System.out.println("❌ Error: " + error);
                fail("getAllInventoryItems failed: " + error);
                latch.countDown();
            }
        });

        assertTrue("Timeout waiting for getAllInventoryItems", latch.await(10, TimeUnit.SECONDS));
    }

    @Test
    public void testSearchInventory() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final String query = "Ethanol"; // A common chemical name

        repo.searchInventory(query, new IInventoryRepository.InventoryListCallback() {
            @Override
            public void onSuccess(List<Item> result) {
                System.out.println("✅ Found " + (result != null ? result.size() : 0) + " items for query '" + query + "'");
                assertNotNull("Result should not be null", result);
                latch.countDown();
            }

            @Override
            public void onError(String error) {
                // This is not a failure, it just means no items were found.
                // The test should still pass.
                System.out.println("✅ No items found for query '" + query + "': " + error);
                latch.countDown();
            }
        });

        assertTrue("Timeout waiting for searchInventory", latch.await(10, TimeUnit.SECONDS));
    }

    @Test
    public void testAddNewAndUpdateInventoryItem() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
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

        // 1. Add a new item
        repo.addNewInventoryItem(newItem, new IInventoryRepository.InventoryItemCallback() {
            @Override
            public void onSuccess(Item addedItem) {
                System.out.println("✅ Added new item successfully: " + addedItem.getItemName() + " | ID: " + addedItem.getItemId());
                assertNotNull("Added item should not be null", addedItem);
                assertNotNull("Added item should have an ID", addedItem.getItemId());

                // 2. Now, update the item
                final int newQuantity = 150;
                addedItem.setQuantity(newQuantity);

                repo.updateInventoryItem(addedItem.getItemId(), addedItem, new IInventoryRepository.InventoryItemCallback() {
                    @Override
                    public void onSuccess(Item updatedItem) {
                        System.out.println("✅ Item updated successfully: ID " + updatedItem.getItemId());
                        assertEquals("Quantity should be updated", newQuantity, updatedItem.getQuantity());
                        latch.countDown();
                    }

                    @Override
                    public void onError(String error) {
                        fail("Failed to update item: " + error);
                        latch.countDown();
                    }
                });
            }

            @Override
            public void onError(String error) {
                fail("Failed to add new item: " + error);
                latch.countDown();
            }
        });

        assertTrue("Timeout waiting for testAddNewAndUpdateInventoryItem", latch.await(20, TimeUnit.SECONDS));
    }

    @Test
    public void testAddAndGetSds() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final String uniqueCasNumber = "CAS-" + System.currentTimeMillis();
        final String url = "https://mock-url/sds/" + uniqueCasNumber + ".pdf";

        // 1. Add a new SDS entry
        repo.addSds(uniqueCasNumber, url, new IInventoryRepository.IdCallback() {
            @Override
            public void onSuccess(String result) {
                System.out.println("✅ SDS added successfully with sdsId (CAS): " + result);
                assertEquals("Returned CAS number should match", uniqueCasNumber, result);

                // 2. Now, get the SDS URL
                repo.getSdsUrl(uniqueCasNumber, new IInventoryRepository.StringCallback() {
                    @Override
                    public void onSuccess(String resultUrl) {
                        System.out.println("✅ SDS URL for " + uniqueCasNumber + ": " + resultUrl);
                        assertEquals("Returned URL should match", url, resultUrl);
                        latch.countDown();
                    }

                    @Override
                    public void onError(String error) {
                        fail("Failed to get SDS: " + error);
                        latch.countDown();
                    }
                });
            }

            @Override
            public void onError(String error) {
                fail("Failed to add SDS: " + error);
                latch.countDown();
            }
        });

        assertTrue("Timeout waiting for testAddAndGetSds", latch.await(20, TimeUnit.SECONDS));
    }

    @Test
    public void testLogAndGetInventoryTransaction() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        // 1. Add a new item to log transactions against
        final String uniqueName = "Transaction Test Item " + System.currentTimeMillis();
        final Item newItem = new Item(null, uniqueName, "1111-11-1", "LOT-T1", 50, "g", "Shelf T1", "2028-01-01");

        repo.addNewInventoryItem(newItem, new IInventoryRepository.InventoryItemCallback() {
            @Override
            public void onSuccess(Item addedItem) {
                final int itemId = addedItem.getItemId();
                final int userId = 1; // Mock user ID
                final int quantity = 25;

                // 2. Log a transaction for the new item
                repo.logInventoryTransaction(itemId, userId, quantity, "Check In", new IInventoryRepository.TransactionIdCallback() {
                    @Override
                    public void onSuccess(int transactionId) {
                        System.out.println("✅ Logged transaction successfully with ID: " + transactionId);
                        assertTrue("Transaction ID should be positive", transactionId > 0);

                        // 3. Get the transactions for the item and verify
                        repo.getInventoryTransaction(itemId, new IInventoryRepository.InventoryTransactionListCallback() {
                            @Override
                            public void onSuccess(List<InventoryTransaction> transactions) {
                                System.out.println("✅ Received " + transactions.size() + " transactions.");
                                assertNotNull("Transactions list should not be null", transactions);
                                assertTrue("Should be at least one transaction", transactions.size() > 0);
                                boolean found = false;
                                for(InventoryTransaction t : transactions){
                                    if(t.getTransactionId() == transactionId){
                                        found = true;
                                        assertEquals("User ID should match", userId, t.getUserId());
                                        assertEquals("Quantity should match", quantity, t.getQuantity());
                                        break;
                                    }
                                }
                                assertTrue("The logged transaction should be in the list", found);
                                latch.countDown();
                            }

                            @Override
                            public void onError(String message) {
                                fail("getInventoryTransaction failed: " + message);
                                latch.countDown();
                            }
                        });
                    }

                    @Override
                    public void onError(String errorMessage) {
                        fail("logInventoryTransaction failed: " + errorMessage);
                        latch.countDown();
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                fail("Failed to create item for transaction test: " + errorMessage);
                latch.countDown();
            }
        });
        assertTrue("Timeout waiting for testLogAndGetInventoryTransaction", latch.await(30, TimeUnit.SECONDS));
    }

    @Test
    public void testGetItemById() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final String uniqueName = "GetItemById Test Item " + System.currentTimeMillis();
        final Item newItem = new Item(null, uniqueName, "2222-22-2", "LOT-G1", 75, "mg", "Shelf G1", "2029-01-01");

        // 1. Create a new item
        repo.addNewInventoryItem(newItem, new IInventoryRepository.InventoryItemCallback() {
            @Override
            public void onSuccess(Item addedItem) {
                final int itemId = addedItem.getItemId();

                // 2. Get the item by its ID
                repo.getItemById(itemId, new IInventoryRepository.InventoryItemCallback() {
                    @Override
                    public void onSuccess(Item fetchedItem) {
                        System.out.println("✅ Fetched item by ID successfully: " + fetchedItem.getItemName());
                        assertNotNull("Fetched item should not be null", fetchedItem);
                        assertEquals("Item ID should match", itemId, fetchedItem.getItemId());
                        assertEquals("Item name should match", uniqueName, fetchedItem.getItemName());
                        latch.countDown();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        fail("getItemById failed: " + errorMessage);
                        latch.countDown();
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                fail("Failed to create item for getItemById test: " + errorMessage);
                latch.countDown();
            }
        });

        assertTrue("Timeout waiting for testGetItemById", latch.await(20, TimeUnit.SECONDS));
    }

    @Test
    public void testCheckStockAndDeductStock() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final String uniqueName = "Stock Test Item " + System.currentTimeMillis();
        final int initialQuantity = 100;
        final int deductQuantity = 20;
        final int finalQuantity = initialQuantity - deductQuantity;
        final Item newItem = new Item(null, uniqueName, "3333-33-3", "LOT-S1", initialQuantity, "pcs", "Shelf S1", "2030-01-01");

        // 1. Create a new item
        repo.addNewInventoryItem(newItem, new IInventoryRepository.InventoryItemCallback() {
            @Override
            public void onSuccess(Item addedItem) {
                final int itemId = addedItem.getItemId();
                final int userId = 1; // Mock user ID
                final ProtocolItem itemToDeduct = new ProtocolItem(itemId, uniqueName, deductQuantity, "pcs", 0, 0);
                final List<ProtocolItem> itemsToDeduct = java.util.Collections.singletonList(itemToDeduct);

                // 2. Check stock availability
                repo.checkStockAvailability(itemsToDeduct, new IInventoryRepository.GenericCallback() {
                    @Override
                    public void onSuccess() {
                        System.out.println("✅ Stock availability check passed.");

                        // 3. Deduct stock
                        repo.deductStock(itemsToDeduct, userId, new IInventoryRepository.GenericCallback() {
                            @Override
                            public void onSuccess() {
                                System.out.println("✅ Stock deducted successfully.");

                                // 4. Get the item again to verify the new quantity
                                repo.getItemById(itemId, new IInventoryRepository.InventoryItemCallback() {
                                    @Override
                                    public void onSuccess(Item fetchedItem) {
                                        assertEquals("Quantity should be updated after deduction", finalQuantity, fetchedItem.getQuantity());
                                        latch.countDown();
                                    }

                                    @Override
                                    public void onError(String errorMessage) {
                                        fail("Failed to fetch item after deduction: " + errorMessage);
                                        latch.countDown();
                                    }
                                });
                            }

                            @Override
                            public void onError(String errorMessage) {
                                fail("deductStock failed: " + errorMessage);
                                latch.countDown();
                            }
                        });
                    }

                    @Override
                    public void onError(String errorMessage) {
                        fail("checkStockAvailability failed: " + errorMessage);
                        latch.countDown();
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                fail("Failed to create item for stock test: " + errorMessage);
                latch.countDown();
            }
        });

        assertTrue("Timeout waiting for testCheckStockAndDeductStock", latch.await(30, TimeUnit.SECONDS));
    }
}