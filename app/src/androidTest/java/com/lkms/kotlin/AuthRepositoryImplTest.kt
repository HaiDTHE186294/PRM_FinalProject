//package com.lkms
//import com.lkms.data.repository.implement.AuthRepositoryImpl
//import com.lkms.data.repository.IAuthRepository
//import com.lkms.data.model.User
//import org.junit.Test
//import kotlinx.coroutines.runBlocking
//
//class AuthRepositoryImplTest {
//    private val repo = AuthRepositoryImpl()
//
//    // ✅ 1️⃣ Test login thành công
//    @Test
//    fun testLoginSuccess() = runBlocking {
//        val email = "test1@gmail.com"
//        val password = "1"
//
//        repo.login(email, password, object : IAuthRepository.AuthCallback {
//            override fun onSuccess(user: User) {
//                println("✅ Login success: ${user.email}")
//            }
//
//            override fun onError(error: String) {
//                println("❌ Login failed: $error")
//            }
//        })
//
//        Thread.sleep(3000)
//    }
//}