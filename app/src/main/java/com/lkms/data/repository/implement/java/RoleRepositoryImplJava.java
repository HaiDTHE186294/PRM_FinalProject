package com.lkms.data.repository.implement.java;

import static com.lkms.BuildConfig.SUPABASE_URL;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lkms.data.model.java.Role;
import com.lkms.data.model.java.User;
import com.lkms.data.repository.IRoleRepository;
import com.lkms.data.repository.IUserRepository;

import java.lang.reflect.Type;
import java.util.List;

public class RoleRepositoryImplJava implements IRoleRepository {

    private static final Gson gson = new Gson();

    @Override
    public void getAllRoles(IRoleRepository.RoleListCallback callback) {
        new Thread(() -> {
            try {
                String endpoint = SUPABASE_URL + "/rest/v1/Role?select=*";
                String json = HttpHelper.getJson(endpoint);

                Type listType = new TypeToken<List<Role>>() {}.getType();
                List<Role> role = gson.fromJson(json, listType);
                callback.onSuccess(role);

            } catch (Exception e) {
                callback.onError("Lỗi khi tải danh sách role: " + e.getMessage());
            }
        }).start();
    }
}
