package com.lkms.domain;

import com.lkms.data.model.java.Item;
import com.lkms.data.repository.IInventoryRepository;

import java.util.List;

public class SdsLookUpUseCase {

    private final IInventoryRepository repository;

    public SdsLookUpUseCase(IInventoryRepository repository) {
        this.repository = repository;
    }

    // -------------------- Search Inventory --------------------
    public void searchItems(String query, InventoryCallback callback) {
//        if (query == null || query.trim().length() < 2) {
//            callback.onError("Query must be at least 2 characters");
//            return;
//        }

        repository.searchInventory(query, new IInventoryRepository.InventoryListCallback() {
            @Override
            public void onSuccess(List<Item> items) {
                // Filter thêm logic nghiệp vụ nếu muốn, ví dụ chỉ lấy items có SDS
                callback.onSuccess(items);
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }

    // -------------------- Get SDS URL by CAS --------------------
    public void getSdsUrl(String casNumber, SdsCallback callback) {
        if (casNumber == null || casNumber.trim().isEmpty()) {
            callback.onError("CAS number cannot be empty");
            return;
        }

        repository.getSdsUrl(casNumber, new IInventoryRepository.StringCallback() {
            @Override
            public void onSuccess(String url) {
                // Có thể thêm logic nghiệp vụ, ví dụ check url hợp lệ
                if (url.startsWith("http")) {
                    callback.onSuccess(url);
                } else {
                    callback.onError("Invalid SDS URL received");
                }
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }

    // -------------------- Callback Interfaces --------------------
    public interface InventoryCallback {
        void onSuccess(List<Item> items);
        void onError(String errorMessage);
    }

    public interface SdsCallback {
        void onSuccess(String sdsUrl);
        void onError(String errorMessage);
    }
}
