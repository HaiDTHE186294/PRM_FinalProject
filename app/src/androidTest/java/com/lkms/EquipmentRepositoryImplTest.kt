//package com.lkms
//
//import com.lkms.data.repository.IEquipmentRepository
//import com.lkms.data.repository.implement.EquipmentRepositoryImpl
//import kotlinx.coroutines.runBlocking
//import org.junit.Test
//
//class EquipmentRepositoryImplTest {
//
//    private val repo = EquipmentRepositoryImpl()
//
//    // ✅ 1. Test lấy toàn bộ thiết bị
//    @Test
//    fun testGetAllEquipment() = runBlocking {
//        repo.getAllEquipment(object : IEquipmentRepository.EquipmentListCallback {
//            override fun onSuccess(equipmentList: List<com.lkms.data.model.Equipment>) {
//                println("✅ Equipment list size: ${equipmentList.size}")
//                equipmentList.forEach {
//                    println(" - ${it.equipmentName} (ID=${it.equipmentId ?: "null"})")
//                }
//            }
//
//            override fun onError(error: String) {
//                println("❌ Error: $error")
//            }
//        })
//        Thread.sleep(3000)
//    }
//
//    // ✅ 2. Test lấy thông tin thiết bị theo ID
//    @Test
//    fun testGetEquipmentById() = runBlocking {
//        val id = 2
//        repo.getEquipmentById(id, object : IEquipmentRepository.EquipmentCallback {
//            override fun onSuccess(equipment: com.lkms.data.model.Equipment) {
//                println("✅ Equipment: ${equipment.equipmentName} (ID=${equipment.equipmentId})")
//            }
//
//            override fun onError(error: String) {
//                println("❌ Error: $error")
//            }
//        })
//        Thread.sleep(3000)
//    }
//
//    // ✅ 3. Test lấy danh sách booking theo thiết bị + khoảng thời gian
//    @Test
//    fun testGetEquipmentBookings() = runBlocking {
//        val startDate = "2024-01-01"
//        val endDate = "2124-01-01"
//
//        repo.getEquipmentBookings(
//            equipmentId = 1,
//            startDate = startDate,
//            endDate = endDate,
//            callback = object : IEquipmentRepository.BookingListCallback {
//                override fun onSuccess(bookings: List<com.lkms.data.model.Booking>) {
//                    println("✅ Bookings: ${bookings.size}")
//                    bookings.forEach {
//                        println(" - Booking ID=${it.bookingId ?: "null"} | ${it.startTime} → ${it.endTime}")
//                    }
//                }
//
//                override fun onError(error: String) {
//                    println("❌ Error: $error")
//                }
//            }
//        )
//        Thread.sleep(3000)
//    }
//
//    // ✅ 4. Test tạo booking mới (Supabase auto ID)
//    @Test
//    fun testCreateBooking() = runBlocking {
//        val start = "2004-09-24"
//        val end = "2204-09-23"
//
//        repo.createBooking(
//            userId = 1,
//            equipmentId = 1,
//            experimentId = 1,
//            startTime = start,
//            endTime = end,
//            callback = object : IEquipmentRepository.BookingIdCallback {
//                override fun onSuccess(bookingId: Int) { // 🔹 Đổi id → bookingId
//                    println("✅ Booking created with ID: $bookingId")
//                }
//
//                override fun onError(error: String) {
//                    println("❌ Error: $error")
//                }
//            }
//        )
//        Thread.sleep(3000)
//    }
//
//
//    // ✅ 5. Test lấy URL manual (hướng dẫn thiết bị)
//    @Test
//    fun testGetManualDownloadUrl() = runBlocking {
//        repo.getManualDownloadUrl(1, object : IEquipmentRepository.StringCallback {
//            override fun onSuccess(result: String) {
//                println("✅ Manual URL: $result")
//            }
//
//            override fun onError(error: String) {
//                println("❌ Error: $error")
//            }
//        })
//        Thread.sleep(3000)
//    }
//
//    // ✅ 6. Test lấy manual theo serialNumber = manualId
//    @Test
//    fun testManualIdEqualsSerialNumber() = runBlocking {
//        repo.getManualBySerialNumber("S1", object : IEquipmentRepository.StringCallback {
//            override fun onSuccess(result: String) {
//                println("✅ Manual URL for S1: $result")
//            }
//
//            override fun onError(error: String) {
//                println("❌ Error: $error")
//            }
//        })
//        Thread.sleep(3000)
//    }
//}
