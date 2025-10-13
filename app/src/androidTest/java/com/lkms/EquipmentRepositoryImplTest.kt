package com.lkms

import com.lkms.data.repository.IEquipmentRepository
import com.lkms.data.repository.implement.EquipmentRepositoryImpl
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.util.*

class EquipmentRepositoryImplTest {

    private val repo = EquipmentRepositoryImpl()

    // ✅ 1. Test lấy toàn bộ thiết bị
    @Test
    fun testGetAllEquipment() = runBlocking {
        repo.getAllEquipment(object : IEquipmentRepository.EquipmentListCallback {
            override fun onSuccess(equipmentList: List<com.lkms.data.model.Equipment>) {
                println("✅ Equipment list size: ${equipmentList.size}")
                equipmentList.forEach {
                    println(" - ${it.equipmentName} (${it.equipmentId})")
                }
            }

            override fun onError(error: String) {
                println("❌ Error: $error")
            }
        })
        Thread.sleep(3000)
    }

    // ✅ 2. Test lấy thông tin thiết bị theo ID
    @Test
    fun testGetEquipmentById() = runBlocking {
        val id = 1
        repo.getEquipmentById(id, object : IEquipmentRepository.EquipmentCallback {
            override fun onSuccess(equipment: com.lkms.data.model.Equipment) {
                println("✅ Equipment: ${equipment.equipmentName}")
            }

            override fun onError(error: String) {
                println("❌ Error: $error")
            }
        })
        Thread.sleep(3000)
    }

    // ✅ 3. Test lấy danh sách booking theo thiết bị + khoảng thời gian
    @Test
    fun testGetEquipmentBookings() = runBlocking {
        val startDate = Date(System.currentTimeMillis() - 86400000) // hôm qua
        val endDate = Date(System.currentTimeMillis() + 86400000)  // ngày mai

        repo.getEquipmentBookings(
            equipmentId = 1,
            startDate = startDate,
            endDate = endDate,
            callback = object : IEquipmentRepository.BookingListCallback {
                override fun onSuccess(bookings: List<com.lkms.data.model.Booking>) {
                    println("✅ Bookings: ${bookings.size}")
                    bookings.forEach {
                        println(" - Booking ID ${it.bookingId} | Start ${it.startTime} | End ${it.endTime}")
                    }
                }

                override fun onError(error: String) {
                    println("❌ Error: $error")
                }
            }
        )
        Thread.sleep(3000)
    }

    // ✅ 4. Test tạo booking mới
    @Test
    fun testCreateBooking() = runBlocking {
        val start = Date()
        val end = Date(System.currentTimeMillis() + 3600000) // +1h

        repo.createBooking(
            userId = 1,
            equipmentId = 1,
            experimentId = 1,
            startTime = start,
            endTime = end,
            callback = object : IEquipmentRepository.BookingIdCallback {
                override fun onSuccess(id: Int) {
                    println("✅ Booking created with ID: $id")
                }

                override fun onError(error: String) {
                    println("❌ Error: $error")
                }
            }
        )
        Thread.sleep(3000)
    }

    // ✅ 5. Test lấy URL manual (hướng dẫn thiết bị)
    @Test
    fun testGetManualDownloadUrl() = runBlocking {
        repo.getManualDownloadUrl(1, object : IEquipmentRepository.StringCallback {
            override fun onSuccess(result: String) {
                println("✅ Manual URL: $result")
            }

            override fun onError(error: String) {
                println("❌ Error: $error")
            }
        })
        Thread.sleep(3000)
    }

    // ✅ 6. (Tùy chọn) Test lấy danh sách manual theo serialNumber = manualId
    @Test
    fun testManualIdEqualsSerialNumber() = runBlocking {
        // Giả định hàm getManualBySerialNumber được thêm trong repo
        repo.getManualBySerialNumber("EQ-001", object : IEquipmentRepository.StringCallback {
            override fun onSuccess(result: String) {
                println("✅ Manual URL for EQ-001: $result")
            }

            override fun onError(error: String) {
                println("❌ Error: $error")
            }
        })
        Thread.sleep(3000)
    }
}
