//// File path: app/src/main/java/com/lkms/data/repository/implement/ProtocolRepositoryImpl.kt
//package com.lkms.data.repository.implement
//
//import com.lkms.data.dal.SupabaseClient
//import com.lkms.data.model.Protocol
//import com.lkms.data.model.ProtocolItem
//import com.lkms.data.model.ProtocolStep
//import com.lkms.data.repository.IProtocolRepository
//import io.github.jan.supabase.postgrest.postgrest
//import io.github.jan.supabase.postgrest.query.Returning
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.async
//import kotlinx.coroutines.launch
//import kotlinx.serialization.Serializable
//
///**
// * Lớp triển khai của IProtocolRepository.
// * Chịu trách nhiệm thực hiện các lệnh gọi đến Supabase để quản lý dữ liệu Protocol,
// * dựa trên interface đã được định nghĩa bằng Java.
// */
//class ProtocolRepositoryImpl : IProtocolRepository {
//
//    private val client = SupabaseClient.client
//    private val scope = CoroutineScope(Dispatchers.IO)
//
//    /**
//     * UC3: Lấy danh sách tất cả các protocol.
//     * Thực hiện một lệnh gọi `select()` đơn giản đến bảng "Protocol".
//     */
//    override fun getAllProtocols(callback: IProtocolRepository.ProtocolListCallback) {
//        scope.launch {
//            try {
//                // Giả định tên bảng là "Protocol", khớp với tên model
//                val protocols = client.postgrest["Protocol"].select().decodeList<Protocol>()
//                callback.onSuccess(protocols)
//            } catch (e: Exception) {
//                callback.onError(e.message ?: "Failed to fetch protocols.")
//            }
//        }
//    }
//
//    /**
//     * UC4: Lấy nội dung chi tiết của một protocol.
//     * Thực hiện 3 lệnh gọi bất đồng bộ song song (sử dụng async) để tăng hiệu suất.
//     */
//    override fun getProtocolDetails(protocolId: Int, callback: IProtocolRepository.ProtocolContentCallback) {
//        scope.launch {
//            try {
//                // 1. Lấy thông tin chính của Protocol từ bảng "Protocol"
//                val protocolDataJob = async {
//                    client.postgrest["Protocol"].select {
//                        filter { eq("protocolId", protocolId) }
//                    }.decodeSingle<Protocol>()
//                }
//
//                // 2. Lấy danh sách các bước (steps) từ bảng "ProtocolStep"
//                val stepsDataJob = async {
//                    client.postgrest["ProtocolStep"].select {
//                        filter { eq("protocolId", protocolId) }
//                    }.decodeList<ProtocolStep>()
//                }
//
//                // 3. Lấy danh sách vật tư (items) từ bảng "ProtocolItem"
//                val itemsDataJob = async {
//                    client.postgrest["ProtocolItem"].select {
//                        filter { eq("protocolId", protocolId) }
//                    }.decodeList<ProtocolItem>()
//                }
//
//                // Chờ tất cả các lệnh gọi hoàn thành và gửi kết quả về qua các hàm của callback
//                callback.onProtocolReceived(protocolDataJob.await())
//                callback.onStepsReceived(stepsDataJob.await())
//                callback.onItemsReceived(itemsDataJob.await())
//
//            } catch (e: Exception) {
//                callback.onError(e.message ?: "Failed to fetch protocol details.")
//            }
//        }
//    }
//
//    /**
//     * UC3: Tạo một Protocol mới cùng với các bước và vật tư của nó.
//     * PHIÊN BẢN NÂNG CẤP: Đã thêm chức năng tạo ProtocolItem.
//     */
//    override fun createNewProtocol(
//        protocolData: Protocol,
//        steps: MutableList<ProtocolStep>,
//        items: MutableList<ProtocolItem>, // <<< THAY ĐỔI 1: Thêm 'items' vào tham số
//        creatorUserId: Int,
//        callback: IProtocolRepository.ProtocolIdCallback
//    ) {
//        scope.launch {
//            try {
//                // Gán creatorUserId và trạng thái ban đầu là "Pending"
//                val newProtocol = protocolData.copy(creatorUserId = creatorUserId, approveStatus = "Pending")
//
//                // Bước 1: Chỉ thực hiện lệnh insert đơn giản.
//                client.postgrest["Protocol"].insert(newProtocol)
//
//                // Bước 2: Truy vấn lại để tìm protocol vừa tạo.
//                val insertedProtocol = client.postgrest["Protocol"].select {
//                    filter {
//                        eq("protocolTitle", newProtocol.protocolTitle!!)
//                        eq("creatorUserId", newProtocol.creatorUserId!!)
//                    }
//                    limit(1) // Lấy bản ghi mới nhất
//                }.decodeSingle<Protocol>()
//
//
//                // Bước 3: Lấy ID và tiếp tục như cũ
//                val newProtocolId = insertedProtocol.protocolId
//                    ?: throw IllegalStateException("Failed to retrieve ID of the newly created protocol.")
//
//                // Bước 4: Gán protocolId cho từng bước và insert chúng
//                if (steps.isNotEmpty()) {
//                    val stepsWithId = steps.map { it.copy(protocolId = newProtocolId) }
//                    client.postgrest["ProtocolStep"].insert(stepsWithId)
//                }
//
//                // <<< THAY ĐỔI 2: Thêm khối code để xử lý 'items' >>>
//                // Bước 5: Gán protocolId cho từng vật tư và insert chúng
//                if (items.isNotEmpty()) {
//                    val itemsWithId = items.map { it.copy(protocolId = newProtocolId) }
//                    client.postgrest["ProtocolItem"].insert(itemsWithId)
//                }
//
//                // Nếu mọi thứ thành công, trả về ID
//                callback.onSuccess(newProtocolId)
//
//            } catch (e: Exception) {
//                e.printStackTrace()
//                callback.onError(e.message ?: "Failed to create new protocol.")
//            }
//        }
//    }
//
//
//    /**
//     * UC20, UC21: Phê duyệt hoặc từ chối một protocol.
//     * Hàm này cập nhật trạng thái, người duyệt và lý do từ chối (nếu có) của một protocol.
//     */
//    override fun approveProtocol(
//        protocolId: Int,
//        approverUserId: Int,
//        approved: Boolean,
//        reason: String?,
//        callback: IProtocolRepository.GenericCallback
//    ) {
//        scope.launch {
//            // Chuẩn bị dữ liệu cần cập nhật bằng một data class tạm thời.
//            // Điều này giúp mã sạch hơn và đảm bảo chỉ những trường cần thiết được gửi đi.
//            @Serializable
//            data class ProtocolApprovalUpdate(
//                val approveStatus: String,
//                val approverUserId: Int,
//                val rejectionReason: String? = null // Giả định tên cột trong DB là "rejectionReason"
//            )
//
//            val updateData = if (approved) {
//                ProtocolApprovalUpdate("Approved", approverUserId)
//            } else {
//                ProtocolApprovalUpdate("R ejected", approverUserId)
//            }
//
//            try {
//                client.postgrest["Protocol"].update(updateData) {
//                    filter { eq("protocolId", protocolId) }
//                }
//                callback.onSuccess()
//            } catch (e: Exception) {
//                e.printStackTrace()
//                callback.onError(e.message ?: "Failed to process protocol approval.")
//            }
//        }
//    }
//}
//
