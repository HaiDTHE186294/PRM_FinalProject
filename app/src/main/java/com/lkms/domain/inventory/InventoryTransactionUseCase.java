package com.lkms.domain.inventory;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.lkms.data.model.java.InventoryTransaction;
import com.lkms.data.model.java.Item;
import com.lkms.data.repository.IInventoryRepository;
import com.lkms.data.repository.implement.java.InventoryRepositoryImplJava;

import java.util.List;

public class InventoryTransactionUseCase {

    public InventoryRepositoryImplJava repository = new InventoryRepositoryImplJava();

    private final MutableLiveData<List<InventoryTransaction>> transactionList = new MutableLiveData<>();

    public InventoryTransactionUseCase() {
    }

    public LiveData<List<InventoryTransaction>> getList() {
        return transactionList;
    }

    public void loadTransacitonList(int itemId)
    {
        repository.getInventoryTransaction(itemId, new IInventoryRepository.InventoryTransactionListCallback() {

            @Override
            public void onSuccess(List<InventoryTransaction> transactions) {
                transactionList.postValue(transactions);
            }

            @Override
            public void onError(String message) {
                transactionList.postValue(null);
            }
        });
    }
}
