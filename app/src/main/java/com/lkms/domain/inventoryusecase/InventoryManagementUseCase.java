package com.lkms.domain.inventoryusecase;

import com.lkms.data.model.java.Item;
import com.lkms.data.repository.IInventoryRepository;
import com.lkms.data.repository.implement.java.InventoryRepositoryImplJava;

import java.util.List;

public class InventoryManagementUseCase {
    private final InventoryRepositoryImplJava repository;

    public InventoryManagementUseCase(InventoryRepositoryImplJava repository) {
        this.repository = repository;
    }

    public void getAllInventoryItems(IInventoryRepository.InventoryListCallback callback) {
        repository.getAllInventoryItems(new IInventoryRepository.InventoryListCallback() {
            @Override
            public void onSuccess(List<Item> items) {
                callback.onSuccess(items);
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }

    public void getItemById(int itemId, IInventoryRepository.InventoryItemCallback callback) {
        repository.getItemById(itemId, new IInventoryRepository.InventoryItemCallback() {
            @Override
            public void onSuccess(Item item) {
                callback.onSuccess(item);  // Trả kết quả về Activity
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);  // Trả lỗi về Activity
            }
        });
    }

    public void searchInventory(String query, IInventoryRepository.InventoryListCallback callback) {
        repository.searchInventory(query, new IInventoryRepository.InventoryListCallback() {
            @Override
            public void onSuccess(List<Item> items) {
                callback.onSuccess(items);
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }
}
