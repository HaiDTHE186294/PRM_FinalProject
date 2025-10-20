//package com.lkms.data.repository.implement
//
//import com.lkms.data.model.Item
//import com.lkms.data.repository.IInventoryRepository
//import kotlinx.coroutines.CoroutineScope
//import com.lkms.data.dal.SupabaseClient
//import com.lkms.data.model.SDS
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.*
//import io.github.jan.supabase.storage.storage
//import io.github.jan.supabase.postgrest.postgrest
//import java.io.File
//
//class InventoryRepositoryImpl : IInventoryRepository {
//
//    private val client = SupabaseClient.client
//    private val scope = CoroutineScope(Dispatchers.IO)
//
//    override fun getAllInventoryItems(callback: IInventoryRepository.InventoryListCallback?) {
//        scope.launch {
//            try {
//                val result = client.postgrest["Item"]
//                    .select()
//                    .decodeList<Item>()
//
//                withContext(Dispatchers.Main) {
//                    callback?.onSuccess(result)
//                }
//            } catch (e: Exception) {
//                withContext(Dispatchers.Main) {
//                    callback?.onError(e.message ?: "Failed to load inventory items")
//                }
//            }
//        }
//    }
//
//    override fun searchInventory(
//        query: String?,
//        callback: IInventoryRepository.InventoryListCallback?
//    ) {
//        scope.launch {
//            try {
//                val result = if (query.isNullOrBlank()) {
//                    client.postgrest["Item"].select().decodeList<Item>()
//                } else {
//                    client.postgrest["Item"].select {
//                        filter {
//                            or {
//                                like("itemName", "%$query%")
//                                like("casNumber", "%$query%")
//                            }
//                        }
//                    }.decodeList<Item>()
//                }
//
//                withContext(Dispatchers.Main) {
//                    if (result.isNotEmpty()) {
//                        callback?.onSuccess(result)
//                    } else {
//                        callback?.onError("No results found for '$query'")
//                    }
//                }
//            } catch (e: Exception) {
//                withContext(Dispatchers.Main) {
//                    callback?.onError(e.message ?: "Search failed")
//                }
//            }
//        }
//    }
//
//    override fun getSdsUrl(
//        casNumber: String?,
//        callback: IInventoryRepository.StringCallback?
//    ) {
//        scope.launch {
//            try {
//                // 1️⃣ Kiểm tra input
//                if (casNumber.isNullOrBlank()) {
//                    callback?.onError("CAS number is required.")
//                    return@launch
//                }
//
//                // 2️⃣ Lấy CAS number từ bảng Item (xác minh tồn tại)
//                val itemResponse = client.postgrest["Item"].select {
//                    filter {
//                        eq("casNumber", casNumber)
//                    }
//                }
//                val item = itemResponse.decodeSingleOrNull<Item>()
//
//                if (item == null) {
//                    withContext(Dispatchers.Main) {
//                        callback?.onError("Item not found with CAS number: $casNumber")
//                    }
//                    return@launch
//                }
//
//                // 3️⃣ Truy vấn bảng SDS để lấy URL tương ứng với CAS number
//                val sdsResponse = client.postgrest["SDS"].select {
//                    filter {
//                        eq("sdsId", casNumber)
//                    }
//                }
//                val sds = sdsResponse.decodeSingleOrNull<SDS>()
//
//                // 4️⃣ Trả kết quả về callback
//                if (sds != null && !sds.url.isNullOrBlank()) {
//                    callback?.onSuccess(sds.url)
//                } else {
//                    callback?.onError("No SDS found for CAS number: $casNumber")
//                }
//
//            } catch (e: Exception) {
//                withContext(Dispatchers.Main) {
//                    callback?.onError(e.message ?: "Error retrieving SDS URL")
//                }
//            }
//        }
//    }
//
//    override fun addNewInventoryItem(
//        itemData: Item?,
//        callback: IInventoryRepository.InventoryItemCallback?
//    ) {
//        scope.launch {
//            try {
//                val result = client.postgrest["Item"]
//                    .insert(itemData!!){
//                        select()
//                    }
//                    .decodeSingle<Item>()
//
//                withContext(Dispatchers.Main) {
//                    callback?.onSuccess(result)
//                }
//            } catch (e: Exception) {
//                withContext(Dispatchers.Main) {
//                    callback?.onError(e.message ?: "Failed to add new item")
//                }
//            }
//        }
//    }
//
//    override fun logInventoryTransaction(
//        itemId: Int,
//        userId: Int,
//        quantityChange: Int,
//        transactionType: String?,
//        callback: IInventoryRepository.TransactionIdCallback?
//    ) {
//
//    }
//
//    override fun processInventoryApproval(
//        transactionId: Int,
//        approve: Boolean,
//        rejectReason: String?,
//        callback: IInventoryRepository.GenericCallback?
//    ) {
//
//    }
//
//    override fun updateInventoryItem(
//        itemId: Int,
//        updatedData: Item?,
//        callback: IInventoryRepository.InventoryItemCallback?
//    ) {
//        if (updatedData == null) {
//            callback?.onError("Updated data cannot be null.")
//            return
//        }
//
//        scope.launch {
//            try {
//                // 1️⃣ Cập nhật trước
//                client.postgrest["Item"]
//                    .update(updatedData) {
//                        filter {
//                            eq("itemId", itemId)
//                        }
//                    }
//
//                // 2️⃣ Sau khi update thành công, truy vấn lại item vừa cập nhật
//                val updatedItem = client.postgrest["Item"]
//                    .select {
//                        filter {
//                            eq("itemId", itemId)
//                        }
//                    }
//                    .decodeList<Item>()
//
//                // 3️⃣ Gửi kết quả qua callback
//                if (updatedItem.isNotEmpty()) {
//                    callback?.onSuccess(updatedItem.first())
//                } else {
//                    callback?.onError("No item found with ID: $itemId (after update).")
//                }
//
//            } catch (e: Exception) {
//                callback?.onError("Failed to update item: ${e.message}")
//            }
//        }
//    }
//
//    override fun uploadFileSdsToStorage(
//        file: File?,
//        callback: IInventoryRepository.StringCallback?
//    ) {
//        if (file == null) {
//            callback?.onError("File is null")
//            return
//        }
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                val bucket = client.storage.from("SDS")
//                val path = "${System.currentTimeMillis()}_${file.name}"
//                val response = bucket.upload(path, file.readBytes())
//
//                val publicUrl = bucket.publicUrl(path)
//                callback?.onSuccess(publicUrl)
//
//            } catch (e: Exception) {
//                callback?.onError(e.message ?: "Unknown error while uploading file")
//            }
//        }
//    }
//
//    override fun addSds(
//        casNumber: String?,
//        fileUrl: String?,
//        callback: IInventoryRepository.IdCallback?
//    ) {
//        scope.launch {
//            try {
//                if (casNumber.isNullOrBlank() || fileUrl.isNullOrBlank()) {
//                    callback?.onError("CAS Number and File URL cannot be null.")
//                    return@launch
//                }
//
//                // 1️⃣ Kiểm tra xem CAS Number có tồn tại trong bảng Item không
//                val itemCheck = client.postgrest["Item"].select {
//                     filter {
//                        eq("casNumber", casNumber)
//                    }
//                }
//                    .decodeList<Item>()
//
//                if (itemCheck.isEmpty()) {
//                    callback?.onError("CAS Number $casNumber not found in Item table.")
//                    return@launch
//                }
//
//                // 2️⃣ Tạo bản ghi SDS với sdsId = casNumber
//                val newSds = SDS(
//                    sdsId = casNumber,
//                    url = fileUrl,
//                )
//
//                // 🔹 Thêm vào bảng SDS
//                val created = client.postgrest["SDS"]
//                    .insert(newSds) {
//                        select()
//                    }
//                    .decodeSingleOrNull<SDS>()
//
//                // 🔹 Kiểm tra kết quả
//                if (created != null) {
//                    callback?.onSuccess(created.sdsId)
//                } else {
//                    callback?.onError("Failed to add SDS for CAS: $casNumber")
//                }
//
//            } catch (e: Exception) {
//                callback?.onError("Error while adding SDS: ${e.message}")
//            }
//        }
//    }
//}