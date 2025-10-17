package com.lkms.data.repository.implement


import com.lkms.data.dal.SupabaseClient
import com.lkms.data.model.User
import com.lkms.data.repository.IUserRepository
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Returning // <-- Import added
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

class UserRepositoryImpl : IUserRepository {

    //DAO to get data from database
    private val client = SupabaseClient.client

    //Control lifetime of a process (According to my research)
    private val scope = CoroutineScope(Dispatchers.IO)

    //region //Common user's methods

    override fun getUserById (userId: Int, callback: IUserRepository.UserCallback)
    {
        scope.launch {
            try {
                //Get user data's from Supabase
                val getUser = client.postgrest["User"].select {
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

                //Update the user's data first
                client.postgrest["User"].update({
                    set("name", name)
                    set("contactInfo", contactInfo)
                }) {
                    filter {
                        eq("userId", userId)
                    }
                }

                //Then get the data for callback
                val updatedUser = client.postgrest["User"].select {
                    filter {
                        eq("userId", userId)
                    }
                }.decodeSingle<User>()
                callback?.onSuccess(updatedUser)

            } catch (e: Exception) {
                callback?.onError(e.message ?: "Unknown error during profile update")
            }
        }
    }

    //endregion

    //region //Lab manager's specific methods

    override fun getAllUsers(callback: IUserRepository.UserListCallback?) {
        scope.launch {
            try {
                val userList = client.postgrest["User"].select().decodeList<User>()
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

                client.postgrest["User"].update(
                    { set("roleId", newRoleId) }
                ) {
                    filter {
                        eq("userId", targetUserId)
                    }
                }

                //Split out to prevent that one JSON "EOF" error
                val updatedUser = client.postgrest["User"].select {
                    filter {
                        eq("userId", targetUserId)
                    }
                }.decodeSingle<User>()
                callback?.onSuccess(updatedUser)

            } catch (e: Exception) {
                callback?.onError(e.message ?: "Unknown error during role update")
            }
        }
    }

    //endregion

}
