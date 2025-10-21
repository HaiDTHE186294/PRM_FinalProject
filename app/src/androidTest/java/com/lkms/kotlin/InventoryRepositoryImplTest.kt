//package com.lkms
//
//import com.lkms.data.repository.implement.InventoryRepositoryImpl
//import com.lkms.data.repository.IInventoryRepository
//import kotlinx.coroutines.runBlocking
//import com.lkms.data.model.Item
//import org.junit.Test
//import java.util.*
//import java.io.File
//import androidx.test.platform.app.InstrumentationRegistry
//class InventoryRepositoryImplTest {
//    private val repo = InventoryRepositoryImpl()
//
//    // ✅ 1. Test lấy tất cả các Item trong kho
//    @Test
//    fun testGetAllInventoryItems() = runBlocking {
//        repo.getAllInventoryItems(object : IInventoryRepository.InventoryListCallback {
//            override fun onSuccess(result: List<Item>) {
//                println("✅ Loaded ${result.size} inventory items.")
//                result.forEach {
//                    println(" - ${it.itemName} | ID: ${it.itemId}")
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
//    // ✅ 2. Test tìm kiếm Item trong kho (theo tên hoặc CAS)
//    @Test
//    fun testSearchInventory() = runBlocking {
//        val query = "1" // 🔍 thử với từ khóa hoặc CAS thật
//        repo.searchInventory(query, object : IInventoryRepository.InventoryListCallback {
//            override fun onSuccess(result: List<Item>) {
//                println("✅ Found ${result.size} items for query '$query'")
//                result.forEach {
//                    println(" - ${it.itemName} | CAS: ${it.casNumber}")
//                }
//            }
//
//            override fun onError(error: String) {
//                println("❌ No items found: $error")
//            }
//        })
//        Thread.sleep(3000)
//    }
//
//    // ✅ 3. Test lấy SDS URL theo CAS number
//    @Test
//    fun testGetSdsUrl() = runBlocking {
//        val casNumber = "S1" // 🔹 thay bằng CAS thật có trong DB của bạn
//        repo.getSdsUrl(casNumber, object : IInventoryRepository.StringCallback {
//            override fun onSuccess(result: String) {
//                println("✅ SDS URL for $casNumber: $result")
//            }
//
//            override fun onError(error: String) {
//                println("❌ Failed to get SDS: $error")
//            }
//        })
//        Thread.sleep(3000)
//    }
//
//    // ✅ 4. Test thêm mới Item vào kho
//    @Test
//    fun testAddNewInventoryItem() = runBlocking {
//        val newItem = Item(
//            itemId = null, // 👈 auto-increment
//            itemName = "Test Chemical",
//            casNumber = "1234-56-7",
//            lotNumber = "LOT-001",
//            quantity = 50,
//            unit = "g",
//            location = "Shelf A3",
//            expirationDate = "2026-12-31"
//        )
//
//        repo.addNewInventoryItem(newItem, object : IInventoryRepository.InventoryItemCallback {
//            override fun onSuccess(result: Item) {
//                println("✅ Added new item successfully: ${result.itemName} | ID: ${result.itemId}")
//            }
//
//            override fun onError(error: String) {
//                println("❌ Failed to add new item: $error")
//            }
//        })
//        Thread.sleep(3000)
//    }
//
//
//    // ✅ 5. Test update Item đã có trong kho
//    @Test
//    fun testUpdateInventoryItem() = runBlocking {
//        // 👇 Giả sử item có sẵn trong DB với ID = 1
//        val itemIdToUpdate = 3
//
//        // 👇 Tạo dữ liệu cập nhật
//        val updatedItem = Item(
//            itemId = itemIdToUpdate,
//            itemName = "Test Chemical (Updated)",
//            casNumber = "1234-56-7",
//            lotNumber = "LOT-001",
//            quantity = 75, // cập nhật số lượng
//            unit = "g",
//            location = "Shelf B2", // đổi vị trí
//            expirationDate = "2027-01-15" // đổi hạn
//        )
//
//        repo.updateInventoryItem(itemIdToUpdate, updatedItem, object : IInventoryRepository.InventoryItemCallback {
//            override fun onSuccess(result: Item) {
//                println("✅ Item updated successfully:")
//                println("   ID: ${result.itemId}")
//                println("   Name: ${result.itemName}")
//                println("   Quantity: ${result.quantity}")
//                println("   Location: ${result.location}")
//                println("   Expiry: ${result.expirationDate}")
//            }
//
//            override fun onError(error: String) {
//                println("❌ Failed to update item: $error")
//            }
//        })
//
//        // Đợi coroutine hoàn tất (chỉ dùng trong test)
//        Thread.sleep(3000)
//    }
//
//
//    // 6. ✅ Test upload file SDS lên Supabase Storage.
//    @Test
//    fun testUploadFileSdsToStorage() = runBlocking {
//        val context = InstrumentationRegistry.getInstrumentation().targetContext
//
//        // --- 1️⃣ Tạo file mẫu ---
//        val testFile = File(context.filesDir, "sds_sample.txt")
//        if (testFile.exists()) testFile.delete() // tránh file cache cũ
//        testFile.writeText("This is a dummy SDS file for upload test.")
//
//        println("📂 File created: ${testFile.absolutePath}")
//        println("📏 Size: ${testFile.length()} bytes")
//
//        // --- 2️⃣ Gọi upload ---
//        repo.uploadFileSdsToStorage(
//            file = testFile,
//            callback = object : IInventoryRepository.StringCallback {
//                override fun onSuccess(result: String) {
//                    println("✅ Upload successful!")
//                    println("🌐 Public URL: $result")
//                }
//
//                override fun onError(error: String) {
//                    println("❌ Upload failed: $error")
//                    assert(false) // fail test
//                }
//            }
//        )
//
//        // --- 3️⃣ Chờ coroutine hoàn tất ---
//        Thread.sleep(5000)
//    }
//
//    // 7. ✅  Test add casNumber và url vào bảng SDS
//    @Test
//    fun testAddSds() = runBlocking {
//        val casNumber = "S2"
//        val url = "https://fpt-team-pw6hkgsc.atlassian.net/jira/software/projects/PF/boards/34"
//
//        repo.addSds(casNumber, url, object : IInventoryRepository.IdCallback {
//            override fun onSuccess(result: String) {
//                println("✅ SDS added successfully with sdsId: $result")
//            }
//
//            override fun onError(error: String) {
//                println("❌ Failed to add SDS: $error")
//            }
//        })
//
//        // Đợi một chút để coroutine thực thi xong
//        Thread.sleep(3000)
//    }
//
//}