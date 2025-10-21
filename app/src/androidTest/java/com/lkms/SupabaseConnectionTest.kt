//package com.lkms
//
//import androidx.test.ext.junit.runners.AndroidJUnit4
//import com.lkms.data.dal.SupabaseClient
//import io.github.jan.supabase.postgrest.from
//import kotlinx.coroutines.runBlocking
//import org.junit.Assert.assertTrue
//import org.junit.Test
//import org.junit.runner.RunWith
//import android.util.Log
//import com.lkms.data.model.Role
//
//@RunWith(AndroidJUnit4::class)
//class SupabaseConnectionTest {
//
//    @Test
//    fun testSupabaseConnection() = runBlocking {
//        try {
//            val response = SupabaseClient.client
//                .from("Role")
//                .select()
//                .decodeList<Role>()
//
//            Log.d("SupabaseTest", "✅ Kết nối thành công! Dữ liệu: $response")
//            assertTrue("Supabase trả về dữ liệu rỗng hoặc lỗi!", true)
//        } catch (e: Exception) {
//            Log.e("SupabaseTest", "❌ Lỗi khi kết nối Supabase", e)
//            assertTrue("Kết nối Supabase thất bại: ${e.message}", false)
//        }
//    }
//}
