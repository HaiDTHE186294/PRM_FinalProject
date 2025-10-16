package com.lkms.data.repository.implement

import androidx.core.graphics.set
import com.lkms.data.dal.SupabaseClient
import com.lkms.data.model.User
import com.lkms.data.repository.IUserRepository
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Returning // <-- Import added
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserRepositoryImpl : IUserRepository {

    //DAO to get data from database
    private val client = SupabaseClient.client

    //Control lifetime of a process (According to my research)
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun getUserById (userId: Int, callback: IUserRepository.UserCallback)
    {
        scope.launch {
            try {
                //Get user data's from Supabase
                val getUser = client.from("User").select {
                    filter {
                        eq("userId", userId)
                    }
                }.decodeSingleOrNull<User>()

                if (getUser != null)
                    callback.onSuccess(getUser)
                else
                    callback.onError("Không tìm thấy người dùng ID=$userId")

            } catch (e: Exception) {
                callback.onError(e.message ?: "Lỗi không xác định")
            }
        }
    }

    override fun updateUserProfile(
        userId: Int,
        name: String?,
        contactInfo: String?,
        callback: IUserRepository.UserCallback?
    ) {
        scope.launch {
            try {
                // Create a map of the fields to update, ignoring nulls
                val newData = mutableMapOf<String, Any>().apply {
                    name?.let { put("name", it) }
                    contactInfo?.let { put("contactInfo", it) }
                }

//                // Proceed only if there's something to update
//                if (updates.isNotEmpty()) {

                    client.postgrest["User"].update(newData) {
                        filter {
                            eq("userId", userId)
                        }
                    }.decodeSingle<User>()

                    callback?.onSuccess(User())
//                }
            } catch (e: Exception) {
                callback?.onError(e.message ?: "Unknown error during profile update")
            }
        }
    }

    //Lab manager's specific methods
    //region

    override fun getAllUsers(callback: IUserRepository.UserListCallback?) {
        scope.launch {
            try {
                val response = client.postgrest["User"].select()
                val userList = response.decodeList<User>()
                callback?.onSuccess(userList)
            } catch (e: Exception) {
                callback?.onError(e.message ?: "Unknown error")
            }
        }
    }

    override fun updateUserRole(
        targetUserId: Int,
        newRoleId: Int,
        callback: IUserRepository.UserCallback?
    ) {
        scope.launch {
            try {
                val user = client.postgrest["User"].update(
                    {
                        set("roleId", newRoleId)
                    }
                ) {
                    filter {
                        eq("userId", targetUserId)
                    }
                }.decodeSingle<User>()
                callback?.onSuccess(user)
            } catch (e: Exception) {
                callback?.onError(e.message ?: "Unknown error during role update")
            }
        }
    }

    //endregion
}
