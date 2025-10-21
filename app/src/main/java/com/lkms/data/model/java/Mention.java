package com.lkms.data.model.java;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Đại diện cho một hàng trong bảng "Mention".
 * Bảng này có khóa chính phức hợp (commentId, userId).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Mention {

    // Khóa ngoại đến Comment
    @SerializedName("commentId")
    private Integer commentId;

    // Khóa ngoại đến User
    @SerializedName("userId")
    private Integer userId;
}
