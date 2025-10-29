package com.lkms.domain.inventory;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.lkms.data.model.java.InventoryTransaction;
import com.lkms.data.model.java.Item;
import com.lkms.data.model.java.User;
import com.lkms.data.repository.IInventoryRepository;
import com.lkms.data.repository.implement.java.InventoryRepositoryImplJava;
import com.lkms.data.repository.implement.java.UserRepositoryImplJava;

import java.util.List;

public class InventoryItemUseCase {

    InventoryRepositoryImplJava _repos;

    private final MutableLiveData<Item> _item = new MutableLiveData<>();

    public InventoryItemUseCase() {
        _repos = new InventoryRepositoryImplJava();
    }

    public LiveData<Item> getItem() {
        return _item;
    }

    /**Load an item from the list of items (a.k.a Inventory)
     *
     * @param itemId
     */
    public void loadItem(int itemId)
    {
        // Check if the user data is already loaded
        Item existItem = _item.getValue();
        if (existItem != null && existItem.getItemId() == itemId)
            return;

        _item.postValue(null);
        _repos.getAllInventoryItems( new IInventoryRepository.InventoryListCallback() {

            @Override
            public void onSuccess(List<Item> items) {
                for (Item item : items) {
                    if (item.getItemId() == itemId) {
                        _item.postValue(item);
                        break;
                    }
                }
            }

            @Override
            public void onError(String errorMessage) {
                //TODO: Handle error here
            }

        });
    }

    /**Add a new item into the inventory
     *
     * @param item
     */
    public void addItem(Item item)
    {
        _repos.addNewInventoryItem(
            item, new InventoryRepositoryImplJava.InventoryItemCallback () {
                @Override
                public void onSuccess(Item item) {
                    Log.d("InventoryItemUseCaseDEBUG", "Added successfully" + item.toString());
                    _item.postValue(item);
                }

                @Override
                public void onError(String errorMessage) {
                    Log.e("InventoryItemUseCaseERROR", errorMessage);
                    _item.postValue(null);
                }
            }
        );
    }

    /** Update an item inside the inventory
     *
     * @param itemName
     * @param lotNum
     * @param quantity
     * @param location
     * @param expirationDate
     */
    public void updateItem(String itemName, String lotNum, int quantity, String location, String expirationDate)
    {
        Item item = _item.getValue();
        if (item == null)
            return;

        item.setItemName(itemName);
        item.setLotNumber(lotNum);
        item.setQuantity(quantity);
        item.setLocation(location);
        item.setExpirationDate(expirationDate);

        _repos.updateInventoryItem(
                item.getItemId(), item,
                new InventoryRepositoryImplJava.InventoryItemCallback () {
                    @Override
                    public void onSuccess(Item item) {
                        Log.d("InventoryItemUseCaseDEBUG", "Update success" + item.toString());
                        _item.postValue(item);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Log.e("InventoryItemUseCaseERROR", errorMessage);
                        _item.postValue(null);
                    }
                }
        );
    }
}
