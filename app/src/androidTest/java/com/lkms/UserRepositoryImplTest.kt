//package com.lkms
//
//import android.util.Log
//import com.lkms.data.dal.SupabaseClient
//import com.lkms.data.repository.implement.UserRepositoryImpl
//import com.lkms.data.repository.IUserRepository
//import com.lkms.data.model.User
//import io.github.jan.supabase.postgrest.from
//import io.github.jan.supabase.postgrest.postgrest
//import org.junit.Test
//import kotlinx.coroutines.runBlocking
//import kotlin.io.path.Path
//
//class UserRepositoryImplTest {
//    val repo = UserRepositoryImpl()
//
//    //region //Common user test
//
//    @Test
//    fun testGetUserById() = runBlocking {
//        Log.d("UserRepositoryImplTest", "We do a bit test of getting user's data from database here")
//
//        repo.getUserById(1, object : IUserRepository.UserCallback {
//            override fun onSuccess(user: User?) {
//                Log.d("UserRepositoryImplTest", "Retrieve data from database: $user")
//            }
//            override fun onError(errorMessage: String) {
//                Log.e("UserRepositoryImplTest", "There's something wrong happened: $errorMessage")
//            }
//        })
//
//        Thread.sleep(3000)
//    }
//
//    @Test
//    fun testUpdateUserProfile() = runBlocking {
//        Log.d("UserRepositoryImplTest", "Trying to update user's data to the database here")
//        repo.updateUserProfile(
//            2, "Ikari Shinji", "TOKYO-3",
//            object: IUserRepository.UserCallback {
//                override fun onSuccess(user: User?) {
//                    Log.d("UserRepositoryImplTest", "Updated data to database: $user")
//                }
//
//                override fun onError(errorMessage: String) {
//                    Log.e("UserRepositoryImplTest", "Unable to update user. An error has occurred:\n$errorMessage")
//                }
//
//        })
//        Thread.sleep(3000)
//    }
//
//    //endregion
//
//    //region //Lab manager test
//
//    @Test
//    fun testGetAllUsers() = runBlocking {
//        repo.getAllUsers(object : IUserRepository.UserListCallback {
//
//            override fun onSuccess(users: List<User?>?) {
//                if (users == null) {
//                    Log.e("UserRepositoryImplTest", "ERROR: User list is null")
//                    return
//                }
//                Log.d("UserRepositoryImplTest", "Retrieve data from database:")
//                for (user in users) Log.d("UserRepositoryImplTest", "$user")
//            }
//
//            override fun onError(errorMessage: String) {
//                Log.e("UserRepositoryImplTest", "An error has occurred: $errorMessage")
//            }
//
//        })
//        Thread.sleep(3000)
//    }
//
//    @Test
//    fun testUpdateUserRole() = runBlocking {
//        repo.updateUserRole(
//            2, 3,
//            object : IUserRepository.UserCallback {
//
//                override fun onSuccess(user: User?) {
//                    Log.d("UserRepositoryImplTest", "Retrieve data from database: $user")
//                }
//
//                override fun onError(errorMessage: String) {
//                    Log.e("UserRepositoryImplTest", "An error has occurred: $errorMessage")
//                }
//
//            }
//        )
//        Thread.sleep(3000)
//    }
//
//    //endregion
//
//}