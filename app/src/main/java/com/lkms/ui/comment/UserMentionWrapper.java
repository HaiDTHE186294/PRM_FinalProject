package com.lkms.ui.comment; // (Hoặc package model của chủ nhân)

import android.os.Parcel;
import androidx.annotation.NonNull;
import com.linkedin.android.spyglass.mentions.Mentionable;
import com.lkms.data.model.java.User;


public class UserMentionWrapper implements Mentionable {

    private final User user;

    public UserMentionWrapper(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    // --- Triển khai các hàm chính của Mentionable ---

    @NonNull
    @Override
    public String getSuggestiblePrimaryText() {
        // Dùng getName() như chủ nhân yêu cầu
        return this.user.getName();
    }

    @Override
    public int getSuggestibleId() {
        return this.user.getUserId();
    }

    @NonNull
    @Override
    public MentionDeleteStyle getDeleteStyle() {
        return Mentionable.MentionDeleteStyle.PARTIAL_NAME_DELETE;
    }

    // [THÊM HÀM CÒN THIẾU]
    // Hàm này quyết định text hiển thị trong EditText
    @NonNull
    @Override
    public String getTextForDisplayMode(@NonNull MentionDisplayMode mode) {
        switch (mode) {
            case FULL:
                return "@" + user.getName();
            case PARTIAL:
            case NONE:
            default:
                // 2. Đây là text dùng cho danh sách gợi ý (khi đang gõ),
                //    trả về tên không (không có @) là đúng
                return user.getName();
        }
    }


    // --- Triển khai các hàm bắt buộc từ Parcelable ---
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        // Nếu User là Parcelable, hãy dùng:
        // dest.writeParcelable(user, flags);
    }

    public static final Creator<UserMentionWrapper> CREATOR = new Creator<UserMentionWrapper>() {
        @Override
        public UserMentionWrapper createFromParcel(Parcel in) {
            // Nếu User là Parcelable, hãy dùng:
            // User user = in.readParcelable(User.class.getClassLoader());
            // return new UserMentionWrapper(user);
            // Nếu không, trả về null là đủ cho Spyglass
            return null;
        }

        @Override
        public UserMentionWrapper[] newArray(int size) {
            return new UserMentionWrapper[size];
        }
    };
}