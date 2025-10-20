// File path: app/src/androidTest/java/com/lkms/ProtocolRepositoryImplTest.kt
package com.lkms

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lkms.data.model.Protocol
import com.lkms.data.model.ProtocolItem
import com.lkms.data.model.ProtocolStep
import com.lkms.data.repository.IProtocolRepository
import com.lkms.data.repository.implement.ProtocolRepositoryImpl
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Lớp này dùng để kiểm thử các chức năng của ProtocolRepositoryImpl.
 * Mỗi hàm @Test sẽ kiểm tra một chức năng được định nghĩa trong IProtocolRepository.
 *
 * QUAN TRỌNG:
 * - Hãy đảm bảo bạn có dữ liệu mẫu trong các bảng Supabase để các bài test 'get' có thể thành công.
 * - Thay đổi các giá trị ID (ví dụ: protocolId, userId) để khớp với dữ liệu của bạn trong DB.
 * - Chạy từng bài test bằng cách nhấn vào nút ▶ màu xanh lá cây bên cạnh mỗi hàm @Test.
 */
@RunWith(AndroidJUnit4::class)
class ProtocolRepositoryImplTest {

    private val repository: IProtocolRepository = ProtocolRepositoryImpl()

    // Thời gian chờ cho mỗi request mạng để đảm bảo coroutine có đủ thời gian hoàn thành.
    private val testTimeout = 5000L // 5 giây

    // ✅ 1. Test UC3: Lấy danh sách tất cả protocol
    @Test
    fun testGetAllProtocols() = runBlocking {
        println("🧪 Bắt đầu test: Lấy tất cả protocol...")

        repository.getAllProtocols(object : IProtocolRepository.ProtocolListCallback {
            override fun onSuccess(protocols: List<Protocol>) {
                println("✅ [Thành công] Lấy danh sách protocol thành công. Số lượng: ${protocols.size}")
                assertTrue("Danh sách protocol không được rỗng", protocols.isNotEmpty())
                protocols.forEach {
                    println("   - ID: ${it.protocolId}, Title: ${it.protocolTitle}, Status: ${it.approveStatus}")
                    assertNotNull(it.protocolId)
                }
            }

            override fun onError(errorMessage: String) {
                println("❌ [Thất bại] Lỗi: $errorMessage")
                assertTrue("Thất bại khi lấy protocol: $errorMessage", false)
            }
        })
        Thread.sleep(testTimeout) // Chờ cho coroutine hoàn thành
    }

    // ✅ 2. Test UC4: Lấy chi tiết một protocol (gồm protocol, steps, items)
    @Test
    fun testGetProtocolDetails() = runBlocking {
        val protocolIdToTest = 1 // ⚠️ THAY ĐỔI ID này thành một ID protocol TỒN TẠI trong DB của bạn

        println("🧪 Bắt đầu test: Lấy chi tiết protocol ID = $protocolIdToTest...")

        repository.getProtocolDetails(protocolIdToTest, object : IProtocolRepository.ProtocolContentCallback {
            override fun onProtocolReceived(protocol: Protocol) {
                println("✅ [Thành công] Nhận được thông tin protocol: ${protocol.protocolTitle}")
                assertNotNull(protocol)
                assertEquals(protocolIdToTest, protocol.protocolId)
            }

            override fun onStepsReceived(steps: List<ProtocolStep>) {
                println("✅ [Thành công] Nhận được ${steps.size} bước (steps).")
                // Bạn có thể thêm assert ở đây để kiểm tra steps không rỗng nếu protocol đó có steps
            }

            override fun onItemsReceived(items: List<ProtocolItem>) {
                println("✅ [Thành công] Nhận được ${items.size} vật tư (items).")
                // Bạn có thể thêm assert ở đây để kiểm tra items không rỗng nếu protocol đó có items
            }

            override fun onError(errorMessage: String) {
                println("❌ [Thất bại] Lỗi khi lấy chi tiết: $errorMessage")
                assertTrue("Thất bại khi lấy chi tiết protocol: $errorMessage", false)
            }
        })
        Thread.sleep(testTimeout)
    }

    // ✅ 3. Test UC3: Tạo một protocol mới (cùng với các bước và vật tư của nó)
    @Test
    fun testCreateNewProtocol() = runBlocking {
        println("🧪 Bắt đầu test: Tạo protocol mới...")
        // --- Chuẩn bị  dữ liệu ---
        val creatorId = 1 // ⚠️ THAY ĐỔI ID người tạo cho phù hợp với bảng User của bạn
        val timestamp = System.currentTimeMillis()

        // Dữ liệu cho bảng Protocol
        val newProtocol = Protocol(
            protocolTitle = "Protocol Test từ Android $timestamp",
            versionNumber = "1.0",
            introduction = "Giới thiệu được tạo tự động.",
            safetyWarning = "Cảnh báo an toàn: Luôn cẩn thận."
            // Không cần truyền creatorUserId, approveStatus vì hàm sẽ tự gán
        )

        // Dữ liệu cho bảng ProtocolStep
        val newSteps = mutableListOf(
            ProtocolStep(stepOrder = 1, instruction = "Bước 1: Chuẩn bị dụng cụ."),
            ProtocolStep(stepOrder = 2, instruction = "Bước 2: Thực hiện thí nghiệm."),
            ProtocolStep(stepOrder = 3, instruction = "Bước 3: Ghi lại kết quả và dọn dẹp.")
        )

        // <<< THAY ĐỔI QUAN TRỌNG: Chuẩn bị dữ liệu cho ProtocolItem >>>
        // Giả sử trong bảng "Item" của bạn đã có sẵn các vật tư với các ID sau:
        // - Vật tư có itemId = 1 (ví dụ: Cốc thủy tinh)
        // - Vật tư có itemId = 5 (ví dụ: Axit HCl)
        // - Vật tư có itemId = 12 (ví dụ: Giấy quỳ)
        // ⚠️ BẠN PHẢI THAY ĐỔI CÁC 'itemId' NÀY THÀNH CÁC ID CÓ THẬT TRONG BẢNG 'Item' CỦA BẠN
        val newItems = mutableListOf(
            ProtocolItem(itemId = 1, quantity = 1),
            ProtocolItem(itemId = 2, quantity = 2),
            ProtocolItem(itemId = 3, quantity = 3)
        )

        // --- Gọi hàm ---
        repository.createNewProtocol(newProtocol, newSteps, newItems, creatorId, object : IProtocolRepository.ProtocolIdCallback {
            override fun onSuccess(protocolId: Int) {
                println("✅ [Thành công] Đã tạo protocol mới thành công với ID = $protocolId")
                assertTrue("ID trả về phải lớn hơn 0", protocolId > 0)
            }

            override fun onError(errorMessage: String) {
                println("❌ [Thất bại] Lỗi khi tạo protocol: $errorMessage")
                assertTrue("Thất bại khi tạo protocol: $errorMessage", false)
            }
        })
        Thread.sleep(testTimeout)
    }





    // ✅ 4. Test UC20/UC21: Phê duyệt (Approve) một protocol
    @Test
    fun testApproveProtocol() = runBlocking {
        val protocolIdToApprove = 4 // ⚠️ THAY ĐỔI ID này thành một protocol có status "Pending"
        val approverId = 1 // ⚠️ THAY ĐỔI ID người duyệt (Lab Manager)

        println("🧪 Bắt đầu test: Phê duyệt protocol ID = $protocolIdToApprove...")

        repository.approveProtocol(protocolIdToApprove, approverId, true, null, object : IProtocolRepository.GenericCallback {
            override fun onSuccess() {
                println("✅ [Thành công] Đã phê duyệt (Approved) protocol ID $protocolIdToApprove!")
            }

            override fun onError(errorMessage: String) {
                println("❌ [Thất bại] Lỗi khi phê duyệt: $errorMessage")
                assertTrue("Thất bại khi phê duyệt protocol: $errorMessage", false)
            }
        })
        Thread.sleep(testTimeout)
    }

    // ✅ 5. Test UC20/UC21: Từ chối (Reject) một protocol
    @Test
    fun testRejectProtocol() = runBlocking {
        val protocolIdToReject = 10 // ⚠️ THAY ĐỔI ID này thành một protocol có status "Pending"
        val approverId = 1 // ⚠️ THAY ĐỔI ID người duyệt (Lab Manager)
        val rejectionReason = "Lý do từ chối: Các bước không đủ rõ ràng."

        println("🧪 Bắt đầu test: Từ chối protocol ID = $protocolIdToReject...")

        repository.approveProtocol(protocolIdToReject, approverId, false, rejectionReason, object : IProtocolRepository.GenericCallback {
            override fun onSuccess() {
                println("✅ [Thành công] Đã từ chối (Rejected) protocol ID $protocolIdToReject!")
            }

            override fun onError(errorMessage: String) {
                println("❌ [Thất bại] Lỗi khi từ chối: $errorMessage")
                assertTrue("Thất bại khi từ chối protocol: $errorMessage", false)
            }
        })
        Thread.sleep(testTimeout)
    }
}

