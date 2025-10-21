//package com.lkms.data.repository.implement
//
//import android.content.Context
//import com.lkms.data.dal.SupabaseClient
//import com.lkms.data.repository.IAuthRepository
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import io.github.jan.supabase.postgrest.postgrest
//import com.lkms.data.model.kotlin.User
//import kotlinx.coroutines.*
//
//class AuthRepositoryImpl : IAuthRepository {
//    private val client = SupabaseClient.client
//    private val scope = CoroutineScope(Dispatchers.IO)
//
//    override fun login(
//        email: String?,
//        password: String?,
//        callback: IAuthRepository.AuthCallback?
//    ) {
//        if (email.isNullOrBlank() || password.isNullOrBlank()) {
//            callback?.onError("Email and Password is not blanked.")
//            return
//        }
//
//        scope.launch {
//            try {
//                val users = client.postgrest["User"]
//                    .select {
//                        filter {
//                            eq("email", email)
//                            eq("password", password)
//                        }
//                    }
//                    .decodeList<User>()
//
//                if (users.isNotEmpty()) {
//                    callback?.onSuccess(users.first())
//                } else {
//                    callback?.onError("Email or Password is wrong.")
//                }
//            } catch (e: Exception) {
//                callback?.onError("Login failed: ${e.message}")
//            }
//        }
//    }
//
//    override fun logout(callback: IAuthRepository.LogoutCallback?) {
//    }
//
//}