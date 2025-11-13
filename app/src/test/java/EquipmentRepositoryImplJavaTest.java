import com.lkms.data.model.java.Booking;
import com.lkms.data.model.java.Equipment;
import com.lkms.data.repository.IEquipmentRepository;
import com.lkms.data.repository.implement.java.EquipmentRepositoryImplJava;

// Import thư viện Test và Latch
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class EquipmentRepositoryImplJavaTest {

    private static final Logger logger = Logger.getLogger(EquipmentRepositoryImplJavaTest.class.getName());
    private final EquipmentRepositoryImplJava repo = new EquipmentRepositoryImplJava();

    // ✅ 1. Test lấy toàn bộ thiết bị
    @Test
    public void testGetAllEquipment() throws InterruptedException {
        logger.info("📋 Testing getAllEquipment...");
        final CountDownLatch latch = new CountDownLatch(1);

        repo.getAllEquipment(new IEquipmentRepository.EquipmentListCallback() {
            @Override
            public void onSuccess(List<Equipment> equipmentList) {
                logger.info("✅ Equipment list size: " + equipmentList.size());
                Assert.assertNotNull("Danh sách không được null", equipmentList);
                Assert.assertFalse("Danh sách không được rỗng", equipmentList.isEmpty());
                latch.countDown();
            }

            @Override
            public void onError(String error) {
                logger.severe("❌ Error: " + error);
                Assert.fail("Test thất bại: " + error);
                latch.countDown();
            }
        });

        if (!latch.await(5, TimeUnit.SECONDS)) {
            Assert.fail("Test timed out");
        }
    }

    // ✅ 2. Test lấy thông tin thiết bị theo ID
    @Test
    public void testGetEquipmentById() throws InterruptedException {
        logger.info("🔍 Testing getEquipmentById...");
        int id = 2;
        final CountDownLatch latch = new CountDownLatch(1);

        repo.getEquipmentById(id, new IEquipmentRepository.EquipmentCallback() {
            @Override
            public void onSuccess(Equipment equipment) {
                logger.info("✅ Equipment: " + equipment.getEquipmentName() + " (ID=" + equipment.getEquipmentId() + ")");
                Assert.assertNotNull("Equipment không được null", equipment);
                Assert.assertEquals(id, (long) equipment.getEquipmentId());
                latch.countDown();
            }

            @Override
            public void onError(String error) {
                logger.severe("❌ Error: " + error);
                Assert.fail("Test thất bại: " + error);
                latch.countDown();
            }
        });

        if (!latch.await(5, TimeUnit.SECONDS)) {
            Assert.fail("Test timed out");
        }
    }

    // ✅ 3. Test lấy danh sách booking theo thiết bị + khoảng thời gian
    @Test
    public void testGetEquipmentBookings() throws InterruptedException {
        logger.info("📅 Testing getEquipmentBookings...");
        String startDate = "2024-01-01";
        String endDate = "2124-01-01";
        final CountDownLatch latch = new CountDownLatch(1);

        repo.getEquipmentBookings(1, startDate, endDate, new IEquipmentRepository.BookingListCallback() {
            @Override
            public void onSuccess(List<Booking> bookings) {
                logger.info("✅ Bookings found: " + bookings.size());
                Assert.assertNotNull("Danh sách booking không được null", bookings);
                // Có thể có 0 booking nên không cần check empty
                latch.countDown();
            }

            @Override
            public void onError(String error) {
                logger.severe("❌ Error: " + error);
                Assert.fail("Test thất bại: " + error);
                latch.countDown();
            }
        });

        if (!latch.await(5, TimeUnit.SECONDS)) {
            Assert.fail("Test timed out");
        }
    }

    // ✅ 4. Test tạo booking mới (Supabase auto ID)
    @Test
    public void testCreateBooking() throws InterruptedException {
        logger.info("➕ Testing createBooking...");
        String start = "2004-09-24";
        String end = "2204-09-23";
        final CountDownLatch latch = new CountDownLatch(1);

        repo.createBooking(1, 1, 1, start, end, new IEquipmentRepository.BookingIdCallback() {
            @Override
            public void onSuccess(int bookingId) {
                logger.info("✅ Booking created with ID: " + bookingId);
                Assert.assertTrue("Booking ID phải > 0", bookingId > 0);
                latch.countDown();
            }

            @Override
            public void onError(String error) {
                logger.severe("❌ Error: " + error);
                Assert.fail("Test thất bại: " + error);
                latch.countDown();
            }
        });

        if (!latch.await(5, TimeUnit.SECONDS)) {
            Assert.fail("Test timed out");
        }
    }

    // ✅ 5. Test lấy URL manual (hướng dẫn thiết bị)
    @Test
    public void testGetManualDownloadUrl() throws InterruptedException {
        logger.info("📄 Testing getManualDownloadUrl...");
        final CountDownLatch latch = new CountDownLatch(1);

        repo.getManualDownloadUrl(1, new IEquipmentRepository.StringCallback() {
            @Override
            public void onSuccess(String result) {
                logger.info("✅ Manual URL: " + result);
                Assert.assertNotNull("URL không được null", result);
                Assert.assertFalse("URL không được rỗng", result.isEmpty());
                latch.countDown();
            }

            @Override
            public void onError(String error) {
                logger.severe("❌ Error: " + error);
                Assert.fail("Test thất bại: " + error);
                latch.countDown();
            }
        });

        if (!latch.await(5, TimeUnit.SECONDS)) {
            Assert.fail("Test timed out");
        }
    }

    // ✅ 6. Test lấy manual theo serialNumber = manualId
    @Test
    public void testManualIdEqualsSerialNumber() throws InterruptedException {
        logger.info("🔢 Testing getManualBySerialNumber...");
        final CountDownLatch latch = new CountDownLatch(1);

        repo.getManualBySerialNumber("S1", new IEquipmentRepository.StringCallback() {
            @Override
            public void onSuccess(String result) {
                logger.info("✅ Manual URL for S1: " + result);
                Assert.assertNotNull("URL không được null", result);
                Assert.assertFalse("URL không được rỗng", result.isEmpty());
                latch.countDown();
            }

            @Override
            public void onError(String error) {
                logger.severe("❌ Error: " + error);
                Assert.fail("Test thất bại: " + error);
                latch.countDown();
            }
        });

        if (!latch.await(5, TimeUnit.SECONDS)) {
            Assert.fail("Test timed out");
        }
    }
}