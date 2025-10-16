package com.lkms

import android.util.Log
import com.lkms.data.dal.SupabaseClient
import com.lkms.data.repository.implement.UserRepositoryImpl
import com.lkms.data.repository.IUserRepository
import com.lkms.data.model.User
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import org.junit.Test
import kotlinx.coroutines.runBlocking
import kotlin.io.path.Path

class UserRepositoryImplTest {
    val repo = UserRepositoryImpl()

    //region //Common user test

    //Broken
    @Test
    fun testGetUserById() = runBlocking {
        Log.d("UserRepositoryImplTest", "We do a bit test of retrieving user's data from database here")
        var getUser: User?
        repo.getUserById(1, object : IUserRepository.UserCallback {
            override fun onSuccess(user: User?) {
                getUser = user
                Log.d("UserRepositoryImplTest", "Retrieve data from database: $getUser")
            }
            override fun onError(errorMessage: String) {
                Log.e("UserRepositoryImplTest", "No user's data retrieved from the database or there's something wrong happened")
            }
        })

        Thread.sleep(3000)
    }

    @Test
    fun testGetUserById2() = runBlocking {
        Log.d("UserRepositoryImplTest", "We do a bit test of retrieving user's data from database here")
        val user = SupabaseClient.client.from("User")
            .select {
                filter {
                    eq("userId", 1)
                }
            }
            .decodeSingleOrNull<User>()

        if (user == null)
            Log.e("UserRepositoryImplTest", "No user's data retreived from the database or there's something wrong happened")
        else
            Log.d("UserRepositoryImplTest", "Retrieve data from database: $user")
        Thread.sleep(3000)
    }



    @Test
    fun testUpdateUserProfile() = runBlocking {
        repo.updateUserProfile(
            1, "Paul", "Arrakis",
            object: IUserRepository.UserCallback {

                override fun onSuccess(user: User?) {
                    Log.d("UserRepositoryImplTest", "Retrieve data from database: $user")
                }

                override fun onError(errorMessage: String) {
                    Log.e("UserRepositoryImplTest", "An error has occurred: $errorMessage")
                }

        })
    }

    //endregion

    //region //Lab manager test

    @Test
    fun testGetAllUsers() = runBlocking {
        repo.getAllUsers(object : IUserRepository.UserListCallback {

            override fun onSuccess(users: List<User?>?) {
                if (users == null) {
                    Log.e("UserRepositoryImplTest", "ERROR: User list is null")
                    return
                }
                Log.i("UserRepositoryImplTest", "Retrieve data from database:")
                for (user in users) Log.i("TEST", "$user")
            }

            override fun onError(errorMessage: String) {
                Log.e("UserRepositoryImplTest", "An error has occurred: $errorMessage")
            }

        })
    }

    @Test
    fun testUpdateUserRole() = runBlocking {
        repo.updateUserRole(
            1, 2,
            object : IUserRepository.UserCallback {

                override fun onSuccess(user: User?) {
                    Log.d("UserRepositoryImplTest", "Retrieve data from database: $user")
                }

                override fun onError(errorMessage: String) {
                    Log.e("UserRepositoryImplTest", "An error has occurred: $errorMessage")
                }

            }
        )
    }

    //endregion

}