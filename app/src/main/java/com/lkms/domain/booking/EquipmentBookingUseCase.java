package com.lkms.domain.booking;

import android.os.Build;

import com.lkms.data.model.java.Booking;
import com.lkms.data.repository.IEquipmentRepository;
import com.lkms.data.repository.enumPackage.java.LKMSConstantEnums;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EquipmentBookingUseCase {

    private final IEquipmentRepository repository;

    private final String CONFIRMED =
            String.valueOf(LKMSConstantEnums.BookingStatus.CONFIRMED);
    private final String PENDING =
            String.valueOf(LKMSConstantEnums.BookingStatus.PENDING);

    public EquipmentBookingUseCase(IEquipmentRepository repository) {
        this.repository = repository;
    }

    public void loadEquipmentList(IEquipmentRepository.EquipmentListCallback callback) {
        repository.getAllEquipment(callback);
    }

    public void loadBookingsForCalendar(
            int equipmentId,
            String startDate,
            String endDate,
            CalendarBookingCallback callback
    ) {
        repository.getEquipmentBookings(equipmentId, startDate, endDate,
                new IEquipmentRepository.BookingListCallback() {
                    @Override
                    public void onSuccess(List<Booking> bookings) {
                        List<LocalDate> bookedDays = new ArrayList<>();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            for (Booking booking : bookings) {
                                if (isValidStatus(booking)) {
                                    bookedDays.addAll(expandBookedDays(booking));
                                }
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

    public void bookEquipment(
            int userId,
            int equipmentId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            int experimentID,
            IEquipmentRepository.BookingIdCallback callback
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            callback.onError("Phiên bản Android không hỗ trợ chức năng này.");
            return;
        }

        String startRange = LocalDate.now().minusYears(1).toString();
        String endRange = LocalDate.now().plusYears(1).toString();

        repository.getEquipmentBookings(
                equipmentId,
                startRange,
                endRange,
                new IEquipmentRepository.BookingListCallback() {
                    @Override
                    public void onSuccess(List<Booking> bookings) {
                        List<LocalDate> bookedDays = new ArrayList<>();
                        for (Booking booking : bookings) {
                            if (isValidStatus(booking)) {
                                bookedDays.addAll(expandBookedDays(booking));
                            }
                        }

                        List<LocalDate> newBookingDays =
                                expandDateRange(startTime.toLocalDate(), endTime.toLocalDate());

                        for (LocalDate day : newBookingDays) {
                            if (bookedDays.contains(day)) {
                                callback.onError("Thời gian đã có người đặt, vui lòng chọn khoảng khác");
                                return;
                            }
                        }

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

    private boolean isValidStatus(Booking booking) {
        String status = booking.getBookingStatus();
        return CONFIRMED.equalsIgnoreCase(status) ||
                PENDING.equalsIgnoreCase(status);
    }

    private List<LocalDate> expandBookedDays(Booking booking) {
        List<LocalDate> days = new ArrayList<>();
        LocalDate start = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            start = LocalDate.parse(booking.getStartTime().substring(0, 10));
        }
        LocalDate end = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            end = LocalDate.parse(booking.getEndTime().substring(0, 10));
        }
        days.addAll(expandDateRange(start, end));
        return days;
    }

    private List<LocalDate> expandDateRange(LocalDate start, LocalDate end) {
        List<LocalDate> days = new ArrayList<>();
        LocalDate tmp = start;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            while (!tmp.isAfter(end)) {
                days.add(tmp);
                tmp = tmp.plusDays(1);
            }
        }
        return days;
    }

    public interface CalendarBookingCallback {
        void onSuccess(List<LocalDate> bookedDays);
        void onError(String message);
    }

    public void getEquipmentById(int serialNum,
                                 IEquipmentRepository.EquipmentCallback callback) {
        repository.getEquipmentById(serialNum, callback);
    }

    public void getManualUrlBySerial(String serial,
                                     IEquipmentRepository.StringCallback callback) {
        repository.getManualBySerialNumber(serial, callback);
    }

}
