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
    public void loadBookingsForCalendar(int equipmentId, String startDate, String endDate, int experimentId,CalendarBookingCallback callback) {
        repository.getEquipmentBookings(equipmentId, startDate, endDate, new IEquipmentRepository.BookingListCallback() {
            @Override
            public void onSuccess(List<Booking> bookings) {
                List<LocalDate> bookedDays = new ArrayList<>();
                for (Booking b : bookings) {
                    try {
                        // Chỉ lấy phần date (yyyy-MM-dd) từ startTime
                        LocalDate date = null;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            date = LocalDate.parse(b.getStartTime().substring(0, 10));
                        }
                        bookedDays.add(date);
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
    public void bookEquipment(int userId, int equipmentId, LocalDateTime startTime, LocalDateTime endTime, int experimentID,IEquipmentRepository.BookingIdCallback callback) {
        repository.createBooking(
                userId,
                equipmentId,
                experimentID,
                startTime.toString(),
                endTime.toString(),
                callback
        );
    }

    /**
     * Callback trả về danh sách ngày đã book
     */
    public interface CalendarBookingCallback {
        void onSuccess(List<LocalDate> bookedDays);
        void onError(String message);
    }

    public void getEquipmentById(int equipmentId, IEquipmentRepository.EquipmentCallback callback) {
        repository.getEquipmentById(equipmentId, callback);
    }
}
