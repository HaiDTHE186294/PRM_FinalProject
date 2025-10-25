package com.lkms.domain.loginmaindashboardusecase;

import com.lkms.data.model.java.Booking;
import com.lkms.data.model.java.Equipment;
import com.lkms.data.model.java.Experiment;
import com.lkms.data.model.java.Item;
import com.lkms.data.repository.implement.java.EquipmentRepositoryImplJava;
import com.lkms.data.repository.implement.java.ExperimentRepositoryImplJava;
import com.lkms.data.repository.implement.java.InventoryRepositoryImplJava;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
    public void getUpcomingEquipmentBookings(int userId, EquipmentRepositoryImplJava.BookingListCallback callback) {
        // Bước 1: Lấy các booking đã được duyệt của user
        equipmentRepository.getBookingApproved(userId, new EquipmentRepositoryImplJava.BookingListCallback() {
            @Override
            public void onSuccess(List<Booking> bookings) {
                if (bookings == null || bookings.isEmpty()) {
                    callback.onSuccess(new ArrayList<>());
                    return;
                }

                List<Booking> resultList = new ArrayList<>();
                AtomicInteger remaining = new AtomicInteger(bookings.size());

                // Bước 2: Lấy thông tin thiết bị cho từng booking
                for (Booking booking : bookings) {
                    equipmentRepository.getEquipmentDetails(booking.getEquipmentId(), new EquipmentRepositoryImplJava.EquipmentCallback() {
                        @Override
                        public void onSuccess(Equipment equipment) {
                            booking.setEquipmentName(equipment.getEquipmentName());
                            synchronized (resultList) {
                                resultList.add(booking);
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
    public void getAllInventoryItems(InventoryRepositoryImplJava.InventoryListCallback callback) {
        inventoryRepository.getAllInventoryItems(new InventoryRepositoryImplJava.InventoryListCallback() {
            @Override
            public void onSuccess(List<Item> items) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date today = new Date();

                for (Item item : items) {
                    try {
                        String expirationStr = item.getExpirationDate(); // Lấy ngày hết hạn
                        if (expirationStr != null && !expirationStr.isEmpty()) {
                            Date expirationDate = sdf.parse(expirationStr);
                            long diffMillis = expirationDate.getTime() - today.getTime();
                            long daysLeft = TimeUnit.MILLISECONDS.toDays(diffMillis);

                            // ⚠️ Cần có trong class Item: setDaysLeft(int)
                            item.setDaysLeft((int) daysLeft);
                        } else {
                            item.setDaysLeft(-999); // Không có ngày hết hạn
                        }
                    } catch (Exception e) {
                        item.setDaysLeft(-999); // Lỗi parse
                    }
                }
                callback.onSuccess(items);
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }
}
