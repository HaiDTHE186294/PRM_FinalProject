package com.lkms.data.model.java;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Đại diện cho một hàng trong bảng "Role".
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    @SerializedName("roleId")
    private Integer roleId;

    @SerializedName("roleName")
    private String roleName;

    @SerializedName("roleStatus")
    private String roleStatus;
}
