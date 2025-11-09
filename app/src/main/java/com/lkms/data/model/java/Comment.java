package com.lkms.data.model.java;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.gson.annotations.SerializedName;
import com.lkms.data.repository.enumPackage.java.LKMSConstantEnums;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Đại diện cho một hàng trong bảng "Comment".
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@IgnoreExtraProperties
public class Comment {

    @SerializedName("commentId")
    private String commentId;

    @SerializedName("commentType")
    private String commentType;

    @SerializedName("commentText")
    private String commentText;

    @SerializedName("timeStamp")
    private String timeStamp;

    // Khóa ngoại đến Experiment
    @SerializedName("experimentId")
    private Integer experimentId;

    // Khóa ngoại đến User
    @SerializedName("userId")
    private Integer userId;

    // Khóa ngoại đến LogEntry
    @SerializedName("projectId")
    private Integer projectId;

    public Integer getTargetId(){
        if(commentType.equals(LKMSConstantEnums.CommentType.GENERAL.toString())){
            return experimentId;
        }else if(commentType.equals(LKMSConstantEnums.CommentType.DISCUSSION.toString())){
            return projectId;
        }else  {
            return -1;
        }
    }
}
