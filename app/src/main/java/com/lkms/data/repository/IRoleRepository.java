package com.lkms.data.repository;

import com.lkms.data.model.java.Role;

import java.util.List;

public interface IRoleRepository {
    interface RoleListCallback {
        void onSuccess(List<Role> role);
        void onError(String errorMessage);
    }

    /**
     * Lấy hết role từ cơ sở dữ liệu
     *
     * @param callback
     */
    void getAllRoles(IRoleRepository.RoleListCallback callback);
}
