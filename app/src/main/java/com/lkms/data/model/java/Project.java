package com.lkms.data.model.java;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Đại diện cho một hàng trong bảng "Project".
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Project {

    @SerializedName("projectId")
    private Integer projectId;

    @SerializedName("projectTitle")
    private String projectTitle;

    // Khóa ngoại, liên kết đến userId trong bảng User
    @SerializedName("projectLeaderId")
    private Integer projectLeaderId;
}
