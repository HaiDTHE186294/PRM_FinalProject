package com.lkms.domain.loginmaindashboardusecase;

import android.util.Log;

import com.lkms.data.model.java.Booking;
import com.lkms.data.model.java.BookingDisplay;
import com.lkms.data.model.java.Equipment;
import com.lkms.data.model.java.Experiment;
import com.lkms.data.model.java.InventoryDisplayItem;
import com.lkms.data.model.java.Item;
import com.lkms.data.repository.implement.java.EquipmentRepositoryImplJava;
import com.lkms.data.repository.implement.java.ExperimentRepositoryImplJava;
import com.lkms.data.repository.implement.java.InventoryRepositoryImplJava;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class MainDashboardUseCase {

    private final EquipmentRepositoryImplJava equipmentRepository;
    private final ExperimentRepositoryImplJava experimentRepository;
    private final InventoryRepositoryImplJava inventoryRepository;

    public MainDashboardUseCase() {
        equipmentRepository = new EquipmentRepositoryImplJava();
        experimentRepository = new ExperimentRepositoryImplJava();
        inventoryRepository = new InventoryRepositoryImplJava();
    }

    // === Dữ liệu thiết bị ===
    public void getUpcomingEquipmentBookings(int userId, EquipmentRepositoryImplJava.BookingDisplayListCallback callback) {
        // Bước 1: Lấy các booking đã được duyệt của user
        equipmentRepository.getBookingApproved(userId, new EquipmentRepositoryImplJava.BookingListCallback() {
            @Override
            public void onSuccess(List<Booking> bookings) {
                if (bookings == null || bookings.isEmpty()) {
                    callback.onSuccess(new ArrayList<>());
                    return;
                }

                List<BookingDisplay> resultList = new ArrayList<>();
                AtomicInteger remaining = new AtomicInteger(bookings.size());

                // Bước 2: Lấy thông tin thiết bị cho từng booking
                for (Booking booking : bookings) {
                    equipmentRepository.getEquipmentDetails(booking.getEquipmentId(), new EquipmentRepositoryImplJava.EquipmentCallback() {
                        @Override
                        public void onSuccess(Equipment equipment) {
                            synchronized (resultList) {
                                resultList.add(new BookingDisplay(equipment.getEquipmentId(), equipment.getEquipmentName(), booking.getStartTime(), booking.getEndTime()));
                            }

                            if (remaining.decrementAndGet() == 0) {
                                callback.onSuccess(resultList);
                            }
                        }

                        @Override
                        public void onError(String errorMessage) {
                            if (remaining.decrementAndGet() == 0) {
                                callback.onSuccess(resultList);
                            }
                        }
                    });
                }
            }

            @Override
            public void onError(String message) {
                callback.onError("Lỗi khi lấy danh sách booking: " + message);
            }
        });
    }


    // === Dữ liệu experiment đang diễn ra ===
    public void getOngoingExperiments(int userId, ExperimentRepositoryImplJava.ExperimentListCallback callback) {
        experimentRepository.getExperimentIdsByUserId(userId, new ExperimentRepositoryImplJava.IdListCallback() {
            @Override
            public void onSuccess(List<Integer> experimentIds) {
                if (experimentIds == null || experimentIds.isEmpty()) {
                    callback.onSuccess(new ArrayList<>());
                    return;
                }

                // Tiếp tục gọi để lấy chi tiết các experiment đang In Process
                experimentRepository.getOngoingExperimentsByIds(experimentIds, new ExperimentRepositoryImplJava.ExperimentListCallback() {
                    @Override
                    public void onSuccess(List<Experiment> experiments) {
                        callback.onSuccess(experiments);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        callback.onError("Error fetching experiments: " + errorMessage);
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError("Error fetching experiment IDs: " + errorMessage);
            }
        });
    }

    // === Dữ liệu inventory item (mới thêm) ===
    public void getAllInventoryItems(InventoryRepositoryImplJava.InventoryDisplayListCallback callback) {
        inventoryRepository.getAllInventoryItems(new InventoryRepositoryImplJava.InventoryListCallback() {
            @Override
            public void onSuccess(List<Item> items) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date today = new Date();
                List<InventoryDisplayItem> displayList = new ArrayList<>();

                for (Item item : items) {
                    int daysLeft;
                    try {
                        String expirationStr = item.getExpirationDate();
                        if (expirationStr != null && !expirationStr.isEmpty()) {
                            Date expirationDate = sdf.parse(expirationStr);
                            long diffMillis = expirationDate.getTime() - today.getTime();
                            daysLeft = (int) TimeUnit.MILLISECONDS.toDays(diffMillis);
                        } else {
                            daysLeft = -999;
                        }
                    } catch (Exception e) {
                        daysLeft = -999;
                    }
                    displayList.add(new InventoryDisplayItem(item.getItemId(), item.getItemName(),item.getCasNumber(), item.getLotNumber(), item.getQuantity(), item.getUnit(), item.getLocation(), item.getExpirationDate(), daysLeft));
                    Log.d("INVENTORY_DEBUG", "ItemId: " + item.getItemId() + " | Name: " + item.getItemName() + " | DaysLeft: " + daysLeft);
                }
                callback.onSuccess(displayList);
            }
            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }
}
