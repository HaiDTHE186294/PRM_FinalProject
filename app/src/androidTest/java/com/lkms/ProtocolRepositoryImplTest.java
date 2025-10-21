// File path: app/src/androidTest/java/com/lkms/ProtocolRepositoryImplTest.java
package com.lkms;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.lkms.data.model.java.Protocol;
import com.lkms.data.model.java.ProtocolItem;
import com.lkms.data.model.java.ProtocolStep;
import com.lkms.data.repository.IProtocolRepository;
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
                    System.out.println("   - ID: " + p.getProtocolId() + ", Title: " + p.getProtocolTitle() + ", Status: " + p.getApproveStatus());
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

        repository.approveProtocol(protocolIdToApprove, approverId, true, null, new IProtocolRepository.GenericCallback() {
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

        repository.approveProtocol(protocolIdToReject, approverId, false, reason, new IProtocolRepository.GenericCallback() {
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
}
