package com.lkms.ui.protocol.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.lkms.data.model.java.Item;
import com.lkms.data.model.java.Protocol;
import com.lkms.data.model.java.ProtocolItem;
import com.lkms.data.model.java.ProtocolStep;
import com.lkms.data.repository.IProtocolRepository;
import com.lkms.data.repository.implement.java.ProtocolRepositoryImplJava;
import com.lkms.domain.protocolusecase.GetProtocolDetailsUseCase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ProtocolDetailViewModel extends ViewModel {


    private final IProtocolRepository repository;
    private final GetProtocolDetailsUseCase getProtocolDetailsUseCase;

    private final MutableLiveData<Protocol> protocol = new MutableLiveData<>();
    private final MutableLiveData<List<ProtocolStep>> steps = new MutableLiveData<>();
    // private final MutableLiveData<List<ProtocolItem>> items = new MutableLiveData<>(); // LiveData cũ, không dùng nữa
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    private final MutableLiveData<List<ProtocolItemView>> itemsWithDetails = new MutableLiveData<>();

    /**
     * Lớp dữ liệu này chỉ để hiển thị, không làm ảnh hưởng đến model gốc.
     * Nó chứa tất cả thông tin mà Adapter cần.
     */
    public static class ProtocolItemView {
        public final int itemId;
        public final String itemName;
        public final Integer quantity;
        public final String unit;

        public ProtocolItemView(int itemId, String itemName, Integer quantity, String unit) {
            this.itemId = itemId;
            this.itemName = itemName;
            this.quantity = quantity;
            this.unit = unit;
        }
    }

    // --- CÁC HÀM GETTER ---
    public LiveData<Protocol> getProtocol() {
        return protocol;
    }
    public LiveData<List<ProtocolStep>> getSteps() {
        return steps;
    }
    public LiveData<Boolean> isLoading() {
        return isLoading;
    }
    public LiveData<String> getError() {
        return error;
    }

    public LiveData<List<ProtocolItemView>> getItems() {
        return itemsWithDetails;
    }

    // --- HÀM KHỞI TẠO ---
    public ProtocolDetailViewModel() {
        // Khởi tạo cả repository và usecase
        this.repository = new ProtocolRepositoryImplJava();
        this.getProtocolDetailsUseCase = new GetProtocolDetailsUseCase(repository);
    }

    public void loadProtocolDetails(int protocolId) {
        isLoading.postValue(true);

        // Hàm cũ `getProtocolDetailsUseCase` vẫn được gọi như trước
        getProtocolDetailsUseCase.execute(protocolId, new IProtocolRepository.ProtocolContentCallback() {
            @Override
            public void onProtocolReceived(Protocol receivedProtocol) {
                // Dữ liệu Protocol vẫn được cập nhật bình thường
                protocol.postValue(receivedProtocol);
            }

            @Override
            public void onStepsReceived(List<ProtocolStep> receivedSteps) {
                // Dữ liệu Steps vẫn được cập nhật bình thường
                steps.postValue(receivedSteps);
            }

            @Override
            public void onItemsReceived(List<ProtocolItem> receivedItems) {
                // ĐÂY LÀ ĐIỂM THAY ĐỔI QUAN TRỌNG NHẤT
                // Khi nhận được danh sách item chỉ có ID, ta bắt đầu Giai đoạn 2

                // Nếu không có vật tư nào, cập nhật danh sách rỗng và tắt loading
                if (receivedItems == null || receivedItems.isEmpty()) {
                    itemsWithDetails.postValue(new ArrayList<>());
                    isLoading.postValue(false);
                    return;
                }

                // 1. Rút trích danh sách các ID từ kết quả vừa nhận được
                List<Integer> itemIds = receivedItems.stream()
                        .map(ProtocolItem::getItemId)
                        .collect(Collectors.toList());

                // 2. Gọi hàm mới trong repository để lấy tên và chi tiết cho các ID đó
                repository.getItemsDetailsByIds(itemIds, new IProtocolRepository.ItemsDetailCallback() {
                    @Override
                    public void onSuccess(List<Item> itemDetails) {
                        // 3. Bây giờ ta có 2 danh sách: `receivedItems` (chứa quantity) và `itemDetails` (chứa itemName)
                        //    Ta cần kết hợp chúng lại thành một danh sách hoàn chỉnh.
                        List<ProtocolItemView> finalViewList = combineItemData(receivedItems, itemDetails);

                        // Cập nhật LiveData cuối cùng cho Adapter
                        itemsWithDetails.postValue(finalViewList);
                        isLoading.postValue(false); // Tắt loading khi đã hoàn thành tất cả
                    }

                    @Override
                    public void onError(String errorMessage) {
                        // Nếu bước 2 thất bại, vẫn hiển thị ID để người dùng không thấy màn hình trắng
                        error.postValue("Lỗi lấy tên vật tư: " + errorMessage + ". Hiển thị tạm ID.");
                        List<ProtocolItemView> partialList = createPartialViewList(receivedItems);
                        itemsWithDetails.postValue(partialList);
                        isLoading.postValue(false);
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                // Lỗi này xảy ra nếu ngay cả bước 1 cũng thất bại
                error.postValue(errorMessage);
                isLoading.postValue(false);
            }
        });
    }

    /**
     * Hàm tiện ích để kết hợp thông tin từ hai danh sách.
     */
    private List<ProtocolItemView> combineItemData(List<ProtocolItem> protocolItems, List<Item> itemDetails) {
        List<ProtocolItemView> finalViewList = new ArrayList<>();
        // Chuyển List<Item> thành Map để tra cứu nhanh hơn O(1)
        Map<Integer, Item> detailsMap = itemDetails.stream()
                .collect(Collectors.toMap(Item::getItemId, item -> item));

        for (ProtocolItem pItem : protocolItems) {
            Item detail = detailsMap.get(pItem.getItemId());
            if (detail != null) {
                // Nếu tìm thấy thông tin chi tiết, tạo đối tượng View hoàn chỉnh
                finalViewList.add(new ProtocolItemView(
                        pItem.getItemId(),
                        detail.getItemName(),  // Lấy tên từ đây
                        pItem.getQuantity(),   // Lấy số lượng từ đây
                        detail.getUnit()       // Lấy đơn vị từ đây
                ));
            } else {
                // Nếu không tìm thấy, hiển thị ID để không mất dữ liệu
                finalViewList.add(createPartialView(pItem));
            }
        }
        return finalViewList;
    }

    /**
     * Hàm tiện ích để tạo danh sách hiển thị tạm thời khi không lấy được tên.
     */
    private List<ProtocolItemView> createPartialViewList(List<ProtocolItem> protocolItems) {
        List<ProtocolItemView> partialList = new ArrayList<>();
        for (ProtocolItem pItem : protocolItems) {
            partialList.add(createPartialView(pItem));
        }
        return partialList;
    }

    private ProtocolItemView createPartialView(ProtocolItem pItem) {
        return new ProtocolItemView(
                pItem.getItemId(),
                "Vật tư ID: " + pItem.getItemId(), // Hiển thị tạm ID
                pItem.getQuantity(),
                "" // Không có đơn vị
        );
    }
}
