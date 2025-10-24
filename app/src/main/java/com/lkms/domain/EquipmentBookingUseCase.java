package com.lkms.domain;

import android.os.Build;

import com.lkms.data.model.java.Booking;
import com.lkms.data.repository.IEquipmentRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 */
public class EquipmentBookingUseCase {

    private final IEquipmentRepository repository;

    public EquipmentBookingUseCase(IEquipmentRepository repository) {
        this.repository = repository;
    }

    /**
     * Lấy danh sách thiết bị
     */
    public void loadEquipmentList(IEquipmentRepository.EquipmentListCallback callback) {
        repository.getAllEquipment(callback);
    }

    /**
     * Lấy booking cho một thiết bị, trả về danh sách ngày đã book dưới dạng LocalDate
     */
    public void loadBookingsForCalendar(
            int equipmentId,
            String startDate,
            String endDate,
            int experimentId,
            CalendarBookingCallback callback
    ) {
        repository.getEquipmentBookings(equipmentId, startDate, endDate, new IEquipmentRepository.BookingListCallback() {
            @Override
            public void onSuccess(List<Booking> bookings) {
                List<LocalDate> bookedDays = new ArrayList<>();

                for (Booking b : bookings) {
                    try {
                        if (!"Approved".equalsIgnoreCase(b.getBookingStatus()) &&
                                !"Pending".equalsIgnoreCase(b.getBookingStatus())) {
                            continue; // bỏ các booking Cancel/Rejected
                        }

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            LocalDate start = LocalDate.parse(b.getStartTime().substring(0, 10));
                            LocalDate end = LocalDate.parse(b.getEndTime().substring(0, 10));

                            // Thêm tất cả các ngày trong đoạn [start, end]
                            LocalDate d = start;
                            while (!d.isAfter(end)) {
                                bookedDays.add(d);
                                d = d.plusDays(1);
                            }
                        }

                    } catch (Exception e) {
                        // bỏ qua nếu parse lỗi
                    }
                }

                callback.onSuccess(bookedDays);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }


    /**
     * Tạo booking mới
     */
    public void bookEquipment(
            int userId,
            int equipmentId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            int experimentID,
            IEquipmentRepository.BookingIdCallback callback
    ) {

        // 1. Lấy toàn bộ booking của thiết bị trong khoảng rộng
        String startRange = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startRange = LocalDate.now().minusYears(1).toString();
        }
        String endRange = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            endRange = LocalDate.now().plusYears(1).toString();
        }

        repository.getEquipmentBookings(
                equipmentId,
                startRange,
                endRange,
                new IEquipmentRepository.BookingListCallback() {
                    @Override
                    public void onSuccess(List<Booking> bookings) {

                        // 2. Tạo danh sách tất cả ngày đã booked
                        List<LocalDate> bookedDays = new ArrayList<>();
                        for (Booking b : bookings) {
                            if (!"Approved".equalsIgnoreCase(b.getBookingStatus()) &&
                                    !"Pending".equalsIgnoreCase(b.getBookingStatus())) continue;

                            LocalDate bookedStart = null;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                bookedStart = LocalDate.parse(b.getStartTime().substring(0,10));
                            }
                            LocalDate bookedEnd = null;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                bookedEnd = LocalDate.parse(b.getEndTime().substring(0,10));
                            }

                            LocalDate tmp = bookedStart;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                while (!tmp.isAfter(bookedEnd)) {
                                    bookedDays.add(tmp);
                                    tmp = tmp.plusDays(1);
                                }
                            }
                        }

                        // 3. Tạo danh sách các ngày user muốn đặt
                        LocalDate start = null;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            start = startTime.toLocalDate();
                        }
                        LocalDate end = null;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            end = endTime.toLocalDate();
                        }
                        List<LocalDate> newBookingDays = new ArrayList<>();
                        LocalDate tmp = start;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            while (!tmp.isAfter(end)) {
                                newBookingDays.add(tmp);
                                tmp = tmp.plusDays(1);
                            }
                        }

                        // 4. Check trùng
                        for (LocalDate day : newBookingDays) {
                            if (bookedDays.contains(day)) {
                                callback.onError("Thời gian đã có người đặt, vui lòng chọn khoảng khác");
                                return;
                            }
                        }

                        // 5. Không trùng → tạo booking mới
                        repository.createBooking(
                                userId,
                                equipmentId,
                                experimentID,
                                startTime.toString(),
                                endTime.toString(),
                                callback
                        );
                    }

                    @Override
                    public void onError(String message) {
                        callback.onError("Không kiểm tra được lịch hiện có: " + message);
                    }
                }
        );
    }



    /**
     * Callback trả về danh sách ngày đã book
     */
    public interface CalendarBookingCallback {
        void onSuccess(List<LocalDate> bookedDays);
        void onError(String message);
    }

    public void getEquipmentById(int serialNum, IEquipmentRepository.EquipmentCallback callback) {
        repository.getEquipmentById(serialNum, callback);
    }

    public void getManualUrlBySerial(String serial, IEquipmentRepository.StringCallback callback) {
        repository.getManualBySerialNumber(serial, callback);
    }
}
