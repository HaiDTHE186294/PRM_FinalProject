package com.lkms

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lkms.data.dal.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import android.util.Log
import com.lkms.data.model.*

@RunWith(AndroidJUnit4::class)
class SupabaseConnectionTest
{
    @Test
    fun testSupabaseConnection() = runBlocking {
        try {
            val response = SupabaseClient.client
                .from("User")
                .select()
                .decodeList<User>()

            Log.d("SupabaseTest", "Successfully connected! Data: $response")
            assertTrue("Supabase returns empty data or an error occured!", response != null)
        } catch (e: Exception) {
            Log.e("SupabaseTest", "Something went wrong while trying to connect to Supabase:", e)
            assertTrue("Connecting to Supabase failed: ${e.message}", false)
        }
    }
}
