// File path: app/src/androidTest/java/com/lkms/ProtocolRepositoryImplTest.java
package com.lkms;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.lkms.data.model.java.Protocol;
import com.lkms.data.model.java.ProtocolItem;
import com.lkms.data.model.java.ProtocolStep;
import com.lkms.data.repository.IProtocolRepository;
// ✅ SỬA 1: IMPORT ENUM
import com.lkms.data.repository.enumPackage.java.LKMSConstantEnums.ProtocolApproveStatus;
import com.lkms.data.repository.implement.java.ProtocolRepositoryImplJava;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.*;

@RunWith(AndroidJUnit4.class)
public class ProtocolRepositoryImplTest {

    private final IProtocolRepository repository = new ProtocolRepositoryImplJava();
    private final long testTimeout = 5000L; // 5 giây

    // ✅ 1. Test UC3: Lấy danh sách tất cả protocol
    @Test
    public void testGetAllProtocols() throws InterruptedException {
        System.out.println("🧪 Bắt đầu test: Lấy tất cả protocol...");

        repository.getAllProtocols(new IProtocolRepository.ProtocolListCallback() {
            @Override
            public void onSuccess(List<Protocol> protocols) {
                System.out.println("✅ [Thành công] Lấy danh sách protocol thành công. Số lượng: " + protocols.size());
                assertTrue("Danh sách protocol không được rỗng", !protocols.isEmpty());
                for (Protocol p : protocols) {
                    // Hiển thị tên của Enum
                    System.out.println("   - ID: " + p.getProtocolId() + ", Title: " + p.getProtocolTitle() + ", Status: " + p.getApproveStatus().name());
                    assertNotNull(p.getProtocolId());
                }
            }

            @Override
            public void onError(String errorMessage) {
                System.out.println("❌ [Thất bại] Lỗi: " + errorMessage);
                fail("Thất bại khi lấy protocol: " + errorMessage);
            }
        });

        Thread.sleep(testTimeout);
    }

    // ✅ 2. Test UC4: Lấy chi tiết một protocol (gồm protocol, steps, items)
    @Test
    public void testGetProtocolDetails() throws InterruptedException {
        int protocolIdToTest = 1; // ⚠️ THAY BẰNG ID CÓ THẬT

        System.out.println("🧪 Bắt đầu test: Lấy chi tiết protocol ID = " + protocolIdToTest + "...");

        repository.getProtocolDetails(protocolIdToTest, new IProtocolRepository.ProtocolContentCallback() {
            @Override
            public void onProtocolReceived(Protocol protocol) {
                System.out.println("✅ [Thành công] Nhận được thông tin protocol: " + protocol.getProtocolTitle());
                assertNotNull(protocol);
                assertEquals(protocolIdToTest, (int) protocol.getProtocolId());
            }

            @Override
            public void onStepsReceived(List<ProtocolStep> steps) {
                System.out.println("✅ [Thành công] Nhận được " + steps.size() + " bước (steps).");
            }

            @Override
            public void onItemsReceived(List<ProtocolItem> items) {
                System.out.println("✅ [Thành công] Nhận được " + items.size() + " vật tư (items).");
            }

            @Override
            public void onError(String errorMessage) {
                System.out.println("❌ [Thất bại] Lỗi khi lấy chi tiết: " + errorMessage);
                fail("Thất bại khi lấy chi tiết protocol: " + errorMessage);
            }
        });

        Thread.sleep(testTimeout);
    }

    // ✅ 3. Test UC3: Tạo một protocol mới (cùng với steps và items)
    @Test
    public void testCreateNewProtocol() throws InterruptedException {
        System.out.println("🧪 Bắt đầu test: Tạo protocol mới...");

        int creatorId = 1; // ⚠️ Thay cho phù hợp
        long timestamp = System.currentTimeMillis();

        // Protocol chính
        Protocol newProtocol = new Protocol();
        newProtocol.setProtocolTitle("Protocol Test từ Android " + timestamp);
        newProtocol.setVersionNumber("1.0");
        newProtocol.setIntroduction("Giới thiệu được tạo tự động.");
        newProtocol.setSafetyWarning("Cảnh báo an toàn: Luôn cẩn thận.");

        // Steps
        List<ProtocolStep> newSteps = new ArrayList<>();
        newSteps.add(new ProtocolStep(null, 1, 1, null));
        newSteps.add(new ProtocolStep(null, 2, 2, null));
        newSteps.add(new ProtocolStep(null, 3, 3, null));

        // Items
        List<ProtocolItem> newItems = new ArrayList<>();
        newItems.add(new ProtocolItem(1, 1, 1)); // ⚠️ Thay itemId thật
        newItems.add(new ProtocolItem(1, 2, 2));
        newItems.add(new ProtocolItem(2, 3, 3));

        repository.createNewProtocol(newProtocol, newSteps, newItems, creatorId, new IProtocolRepository.ProtocolIdCallback() {
            @Override
            public void onSuccess(int protocolId) {
                System.out.println("✅ [Thành công] Đã tạo protocol mới thành công với ID = " + protocolId);
                assertTrue("ID trả về phải lớn hơn 0", protocolId > 0);
            }

            @Override
            public void onError(String errorMessage) {
                System.out.println("❌ [Thất bại] Lỗi khi tạo protocol: " + errorMessage);
                fail("Thất bại khi tạo protocol: " + errorMessage);
            }
        });

        Thread.sleep(testTimeout);
    }

    // ✅ 4. Test UC20/UC21: Phê duyệt protocol
    @Test
    public void testApproveProtocol() throws InterruptedException {
        int protocolIdToApprove = 4; // ⚠️ Thay ID thật
        int approverId = 1; // ⚠️ Lab Manager ID

        System.out.println("🧪 Bắt đầu test: Phê duyệt protocol ID = " + protocolIdToApprove + "...");

        // ✅ SỬA 2: Gọi hàm với Enum.APPROVED thay vì boolean `true`
        repository.approveProtocol(protocolIdToApprove, approverId, ProtocolApproveStatus.APPROVED, null, new IProtocolRepository.GenericCallback() {
            @Override
            public void onSuccess() {
                System.out.println("✅ [Thành công] Đã phê duyệt protocol ID " + protocolIdToApprove + "!");
            }

            @Override
            public void onError(String errorMessage) {
                System.out.println("❌ [Thất bại] Lỗi khi phê duyệt: " + errorMessage);
                fail("Thất bại khi phê duyệt protocol: " + errorMessage);
            }
        });

        Thread.sleep(testTimeout);
    }

    // ✅ 5. Test UC20/UC21: Từ chối protocol
    @Test
    public void testRejectProtocol() throws InterruptedException {
        int protocolIdToReject = 10; // ⚠️ Thay ID thật
        int approverId = 1;
        String reason = "Lý do từ chối: Các bước không đủ rõ ràng.";

        System.out.println("🧪 Bắt đầu test: Từ chối protocol ID = " + protocolIdToReject + "...");

        // ✅ SỬA 3: Gọi hàm với Enum.REJECTED thay vì boolean `false`
        repository.approveProtocol(protocolIdToReject, approverId, ProtocolApproveStatus.REJECTED, reason, new IProtocolRepository.GenericCallback() {
            @Override
            public void onSuccess() {
                System.out.println("✅ [Thành công] Đã từ chối protocol ID " + protocolIdToReject + "!");
            }

            @Override
            public void onError(String errorMessage) {
                System.out.println("❌ [Thất bại] Lỗi khi từ chối: " + errorMessage);
                fail("Thất bại khi từ chối protocol: " + errorMessage);
            }
        });

        Thread.sleep(testTimeout);
    }

    // ===============================================================================
    // CÁC HÀM TEST MỚI
    // ===============================================================================

    // ✅ Test mới: Lấy thư viện chính (đã duyệt và mới nhất)
    @Test
    public void testGetLatestApprovedProtocols() throws InterruptedException {
        System.out.println("🧪 Bắt đầu test: Lấy các protocol đã duyệt và mới nhất...");
        final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);

        repository.getLatestApprovedProtocols(new IProtocolRepository.ProtocolListCallback() {
            @Override
            public void onSuccess(List<Protocol> protocols) {
                System.out.println("✅ [Thành công] Lấy danh sách protocol đã duyệt thành công. Số lượng: " + protocols.size());
                assertNotNull("Danh sách protocol không được là null", protocols);

                for (Protocol p : protocols) {
                    System.out.println("   - ID: " + p.getProtocolId() + ", Title: " + p.getProtocolTitle() + ", Status: " + p.getApproveStatus());
                    // ✅ SỬA 4: So sánh Enum với Enum, không phải với String
                    assertEquals("Mọi protocol trong danh sách này phải có trạng thái APPROVED", ProtocolApproveStatus.APPROVED, p.getApproveStatus());
                }
                latch.countDown();
            }

            @Override
            public void onError(String errorMessage) {
                fail("Thất bại khi lấy protocol đã duyệt: " + errorMessage);
                latch.countDown();
            }
        });

        if (!latch.await(10, java.util.concurrent.TimeUnit.SECONDS)) {
            fail("Test hết thời gian chờ mà không nhận được callback từ getLatestApprovedProtocols.");
        }
    }

    // ... (Các test khác giữ nguyên vì không bị ảnh hưởng trực tiếp bởi Enum)

    // ✅ Test mới: Tìm kiếm protocol theo tiêu đề
    @Test
    public void testSearchProtocolsByTitle() throws InterruptedException {
        String searchQuery = "Test"; // ⚠️ Đảm bảo có protocol chứa từ "Test" trong tiêu đề để test
        System.out.println("🧪 Bắt đầu test: Tìm kiếm protocol với tiêu đề chứa '" + searchQuery + "'...");
        final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);

        repository.searchProtocolsByTitle(searchQuery, new IProtocolRepository.ProtocolListCallback() {
            @Override
            public void onSuccess(List<Protocol> protocols) {
                System.out.println("✅ [Thành công] Tìm kiếm thành công. Số lượng kết quả: " + protocols.size());
                assertNotNull(protocols);
                //assertTrue("Phải tìm thấy ít nhất một kết quả cho từ khóa '" + searchQuery + "'", !protocols.isEmpty());

                for (Protocol p : protocols) {
                    System.out.println("   - ID: " + p.getProtocolId() + ", Title: " + p.getProtocolTitle());
                    assertTrue("Tiêu đề protocol phải chứa từ khóa tìm kiếm",
                            p.getProtocolTitle().toLowerCase().contains(searchQuery.toLowerCase()));
                }
                latch.countDown();
            }

            @Override
            public void onError(String errorMessage) {
                fail("Thất bại khi tìm kiếm protocol: " + errorMessage);
                latch.countDown();
            }
        });

        if (!latch.await(10, java.util.concurrent.TimeUnit.SECONDS)) {
            fail("Test hết thời gian chờ mà không nhận được callback từ searchProtocolsByTitle.");
        }
    }

    // ✅ Test mới: Lọc protocol theo nhiều điều kiện
    @Test
    public void testFilterProtocols() throws InterruptedException {
        Integer creatorIdToTest = 2;      // ⚠️ Thay bằng creator ID có thật trong DB của bạn
        String versionToTest = "3.0";   // ⚠️ Thay bằng version có thật

        System.out.println("🧪 Bắt đầu test: Lọc protocol với creator ID = " + creatorIdToTest + " và version = " + versionToTest + "...");
        final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);

        repository.filterProtocols(creatorIdToTest, versionToTest, new IProtocolRepository.ProtocolListCallback() {
            @Override
            public void onSuccess(List<Protocol> protocols) {
                System.out.println("✅ [Thành công] Lọc thành công. Số lượng kết quả: " + protocols.size());
                assertNotNull(protocols);
                //assertTrue("Phải tìm thấy ít nhất một protocol với điều kiện lọc này", !protocols.isEmpty());

                for (Protocol p : protocols) {
                    System.out.println("   - ID: " + p.getProtocolId() + ", Title: " + p.getProtocolTitle() + ", Version: " + p.getVersionNumber() + ", CreatorID: " + p.getCreatorUserId());
                    assertEquals("Creator ID của protocol phải khớp", creatorIdToTest, p.getCreatorUserId());
                    assertEquals("Version của protocol phải khớp", versionToTest, p.getVersionNumber());
                }
                latch.countDown();
            }

            @Override
            public void onError(String errorMessage) {
                fail("Thất bại khi lọc protocol: " + errorMessage);
                latch.countDown();
            }
        });

        if (!latch.await(10, java.util.concurrent.TimeUnit.SECONDS)) {
            fail("Test hết thời gian chờ mà không nhận được callback từ filterProtocols.");
        }
    }

}
