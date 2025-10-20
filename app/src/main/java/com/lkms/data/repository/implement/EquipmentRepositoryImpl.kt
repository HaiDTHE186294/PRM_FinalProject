//package com.lkms.data.repository.implement
//import kotlinx.serialization.json.Json
//import kotlinx.serialization.json.encodeToJsonElement
//import com.lkms.data.dal.SupabaseClient
//import com.lkms.data.model.Booking
//import com.lkms.data.model.Equipment
//import com.lkms.data.model.MaintenanceLog
//import com.lkms.data.model.UserManual
//import com.lkms.data.repository.IEquipmentRepository
//import io.github.jan.supabase.postgrest.postgrest
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import java.text.SimpleDateFormat
//import java.util.Date
//import java.util.Locale
//import java.util.TimeZone
//class EquipmentRepositoryImpl : IEquipmentRepository {
//
//    private val client = SupabaseClient.client
//    private val scope = CoroutineScope(Dispatchers.IO)
//
//    private fun toIsoString(date: Date?): String? {
//        if (date == null) return null
//        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
//        sdf.timeZone = TimeZone.getTimeZone("UTC")
//        return sdf.format(date)
//    }
//
//    override fun getAllEquipment(callback: IEquipmentRepository.EquipmentListCallback) {
//        scope.launch {
//            try {
//                val response = client.postgrest["Equipment"].select()
//                val equipmentList = response.decodeList<Equipment>()
//                callback.onSuccess(equipmentList)
//            } catch (e: Exception) {
//                callback.onError(e.message ?: "Unknown error") //ghi rõ lỗi
//            }
//        }
//    }
//
//    override fun getEquipmentDetails(equipmentId: Int, callback: IEquipmentRepository.EquipmentCallback) {
//        scope.launch {
//            try {
//                val response = client.postgrest["Equipment"].select {
//                    filter {
//                        eq("equipmentId", equipmentId)
//                    }
//                }
//                val equipment = response.decodeSingleOrNull<Equipment>()
//                if (equipment != null) {
//                    callback.onSuccess(equipment)
//                } else {
//                    callback.onError("Equipment not found with ID: $equipmentId")
//                }
//            } catch (e: Exception) {
//                callback.onError(e.message ?: "Unknown error")
//            }
//        }
//    }
//
//    override fun addEquipment(newEquipment: Equipment, callback: IEquipmentRepository.EquipmentIdCallback) {
//        scope.launch {
//            try {
//                val response = client.postgrest["Equipment"].insert(newEquipment)
//                val insertedEquipment = response.decodeSingleOrNull<Equipment>()
//                if (insertedEquipment != null && insertedEquipment.equipmentId != null) {
//                    callback.onSuccess(insertedEquipment.equipmentId)
//                } else {
//                    callback.onError("Failed to add equipment or get returned ID.")
//                }
//            } catch (e: Exception) {
//                callback.onError(e.message ?: "Unknown error")
//            }
//        }
//    }
//
//
//    override fun getMaintenanceLogs(equipmentId: Int, callback: IEquipmentRepository.MaintenanceLogCallback) {
//        scope.launch {
//            try {
//                val response = client.postgrest["MaintenanceLog"].select {
//                    filter {
//                        eq("equipmentId", equipmentId)
//                    }
//                }
//                val logs = response.decodeList<MaintenanceLog>()
//                callback.onSuccess(logs)
//            } catch (e: Exception) {
//                callback.onError(e.message ?: "Unknown error")
//            }
//        }
//    }
//
//    override fun getManualDownloadUrl(
//        equipmentId: Int,
//        callback: IEquipmentRepository.StringCallback?
//    ) {
//        scope.launch {
//            try {
//                // 1️⃣ Lấy serialNumber từ bảng Equipment
//                val equipmentResponse = client.postgrest["Equipment"].select {
//                    filter {
//                        eq("equipmentId", equipmentId)
//                    }
//                }
//                val equipment = equipmentResponse.decodeSingleOrNull<Equipment>()
//
//                if (equipment == null) {
//                    callback?.onError("Equipment not found with ID: $equipmentId")
//                    return@launch
//                }
//
//                val serialNumber = equipment.serialNumber
//                if (serialNumber.isNullOrBlank()) {
//                    callback?.onError("Equipment has no serial number.")
//                    return@launch
//                }
//
//                // 2️⃣ Dùng serialNumber để tìm manual trong bảng UserManual
//                val manualResponse = client.postgrest["UserManual"].select {
//                    filter {
//                        eq("manualId", serialNumber)
//                    }
//                }
//
//                val manual = manualResponse.decodeSingleOrNull<UserManual>()
//
//                if (manual != null && !manual.url.isNullOrBlank()) {
//                    callback?.onSuccess(manual.url)
//                } else {
//                    callback?.onError("No manual found for serial number: $serialNumber")
//                }
//
//            } catch (e: Exception) {
//                callback?.onError(e.message ?: "Unknown error")
//            }
//        }
//    }
//
//    override fun getEquipmentBookings(
//        equipmentId: Int,
//        startDate: String,
//        endDate: String,
//        callback: IEquipmentRepository.BookingListCallback
//    ) {
//        scope.launch {
//            try {
//                val response = client.postgrest["Booking"].select {
//                    filter {
//                        eq("equipmentId", equipmentId)
//                        lte("startTime", endDate ?: "")
//                        gte("endTime", startDate ?: "")
//                    }
//                }
//                val bookings = response.decodeList<Booking>()
//                callback.onSuccess(bookings)
//            } catch (e: Exception) {
//                callback.onError(e.message ?: "Unknown error")
//            }
//        }
//    }
//
//    override fun createBooking(
//        userId: Int,
//        equipmentId: Int,
//        experimentId: Int,
//        startTime: String,
//        endTime: String,
//        callback: IEquipmentRepository.BookingIdCallback
//
//    ) {
//        scope.launch {
//            try {
//
//                // ✅ Tạo object có @Serializable thay vì Map<String, Any>
//                val newBooking = Booking(
//                    bookingId = null, // Không cần set ID
//                    userId = userId,
//                    equipmentId = equipmentId,
//                    experimentId = experimentId,
//                    startTime = startTime,
//                    endTime = endTime,
//                    bookingStatus = "Pending"
//                )
//
//
//                val response = client.postgrest["Booking"].insert(newBooking)
//                val created = response.decodeSingleOrNull<Booking>()
//
//                if (created?.bookingId != null) {
//                    callback.onSuccess(created.bookingId)
//                } else {
//                    callback.onError("Booking ID is null after insert.")
//                }
//            } catch (e: Exception) {
//                callback.onError(e.message ?: "Unknown error")
//            }
//        }
//    }
//
//
//
//    override fun processBookingApproval(
//        bookingId: Int,
//        approve: Boolean,
//        rejectReason: String?,
//        callback: IEquipmentRepository.GenericCallback
//    ) {
//        scope.launch {
//            try {
//                // ✅ Tạo data class có @Serializable để thay Map<String, Any>
//                @kotlinx.serialization.Serializable
//                data class BookingUpdate(
//                    val bookingStatus: String,
//                    val rejectReason: String? = null
//                )
//
//                val updateData = BookingUpdate(
//                    bookingStatus = if (approve) "Approved" else "Rejected",
//                    rejectReason = if (!approve) rejectReason else null
//                )
//
//                client.postgrest["Booking"].update(updateData) {
//                    filter {
//                        eq("bookingId", bookingId)
//                    }
//                }
//
//                callback.onSuccess()
//            } catch (e: Exception) {
//                callback.onError(e.message ?: "Unknown error")
//            }
//        }
//    }
//
//
//    override fun getEquipmentById(
//        equipmentId: Int,
//        callback: IEquipmentRepository.EquipmentCallback
//    ) {
//        scope.launch {
//            try {
//                val response = client.postgrest["Equipment"].select {
//                    filter {
//                        eq("equipmentId", equipmentId)
//                    }
//                }
//                val equipment = response.decodeSingleOrNull<Equipment>()
//                if (equipment != null) {
//                    callback.onSuccess(equipment)
//                } else {
//                    callback.onError("Không tìm thấy thiết bị ID=$equipmentId")
//                }
//            } catch (e: Exception) {
//                callback.onError(e.message ?: "Lỗi không xác định")
//            }
//        }
//    }
//
//    override fun getManualBySerialNumber(
//        serialNumber: String,
//        callback: IEquipmentRepository.StringCallback
//    ) {
//        scope.launch {
//            try {
//                // manualId = serialNumber
//                val manualResponse = client.postgrest["UserManual"].select {
//                    filter {
//                        eq("manualId", serialNumber)
//                    }
//                }
//
//                val manual = manualResponse.decodeSingleOrNull<com.lkms.data.model.UserManual>()
//                if (manual != null && !manual.url.isNullOrBlank()) {
//                    callback.onSuccess(manual.url)
//                } else {
//                    callback.onError("Không tìm thấy manual cho serialNumber = $serialNumber")
//                }
//            } catch (e: Exception) {
//                callback.onError(e.message ?: "Lỗi không xác định")
//            }
//        }
//    }
//
//}
