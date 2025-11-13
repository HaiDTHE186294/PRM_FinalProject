import com.lkms.data.model.java.Booking;
import com.lkms.data.model.java.Equipment;
import com.lkms.data.repository.IEquipmentRepository;
import com.lkms.data.repository.implement.java.EquipmentRepositoryImplJava;

import org.junit.Test;

import java.util.List;

public class EquipmentRepositoryImplJavaTest {

    private final EquipmentRepositoryImplJava repo = new EquipmentRepositoryImplJava();

    // ✅ 1. Test lấy toàn bộ thiết bị
    @Test
    public void testGetAllEquipment() throws InterruptedException {
        repo.getAllEquipment(new IEquipmentRepository.EquipmentListCallback() {
            @Override
            public void onSuccess(List<Equipment> equipmentList) {
                System.out.println("✅ Equipment list size: " + equipmentList.size());
                for (Equipment e : equipmentList) {
                    System.out.println(" - " + e.getEquipmentName() + " (ID=" + e.getEquipmentId() + ")");
                }
            }

            @Override
            public void onError(String error) {
                System.err.println("❌ Error: " + error);
            }
        });
        Thread.sleep(3000);
    }

    // ✅ 2. Test lấy thông tin thiết bị theo ID
    @Test
    public void testGetEquipmentById() throws InterruptedException {
        int id = 2;
        repo.getEquipmentById(id, new IEquipmentRepository.EquipmentCallback() {
            @Override
            public void onSuccess(Equipment equipment) {
                System.out.println("✅ Equipment: " + equipment.getEquipmentName() + " (ID=" + equipment.getEquipmentId() + ")");
            }

            @Override
            public void onError(String error) {
                System.err.println("❌ Error: " + error);
            }
        });
        Thread.sleep(3000);
    }

    // ✅ 3. Test lấy danh sách booking theo thiết bị + khoảng thời gian
    @Test
    public void testGetEquipmentBookings() throws InterruptedException {
        String startDate = "2024-01-01";
        String endDate = "2124-01-01";

        repo.getEquipmentBookings(1, startDate, endDate, new IEquipmentRepository.BookingListCallback() {
            @Override
            public void onSuccess(List<Booking> bookings) {
                System.out.println("✅ Bookings: " + bookings.size());
                for (Booking b : bookings) {
                    System.out.println(" - Booking ID=" + b.getBookingId() +
                            " | " + b.getStartTime() + " → " + b.getEndTime());
                }
            }

            @Override
            public void onError(String error) {
                System.err.println("❌ Error: " + error);
            }
        });
        Thread.sleep(3000);
    }

    // ✅ 4. Test tạo booking mới (Supabase auto ID)
    @Test
    public void testCreateBooking() throws InterruptedException {
        String start = "2004-09-24";
        String end = "2204-09-23";

        repo.createBooking(1, 1, 1, start, end, new IEquipmentRepository.BookingIdCallback() {
            @Override
            public void onSuccess(int bookingId) {
                System.out.println("✅ Booking created with ID: " + bookingId);
            }

            @Override
            public void onError(String error) {
                System.err.println("❌ Error: " + error);
            }
        });
        Thread.sleep(3000);
    }

    // ✅ 5. Test lấy URL manual (hướng dẫn thiết bị)
    @Test
    public void testGetManualDownloadUrl() throws InterruptedException {
        repo.getManualDownloadUrl(1, new IEquipmentRepository.StringCallback() {
            @Override
            public void onSuccess(String result) {
                System.out.println("✅ Manual URL: " + result);
            }

            @Override
            public void onError(String error) {
                System.err.println("❌ Error: " + error);
            }
        });
        Thread.sleep(3000);
    }

    // ✅ 6. Test lấy manual theo serialNumber = manualId
    @Test
    public void testManualIdEqualsSerialNumber() throws InterruptedException {
        repo.getManualBySerialNumber("S1", new IEquipmentRepository.StringCallback() {
            @Override
            public void onSuccess(String result) {
                System.out.println("✅ Manual URL for S1: " + result);
            }

            @Override
            public void onError(String error) {
                System.err.println("❌ Error: " + error);
            }
        });
        Thread.sleep(3000);
    }
}
