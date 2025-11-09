package com.lkms.data.repository;


import androidx.lifecycle.LiveData;

import com.lkms.data.model.java.Comment;
import com.lkms.data.model.java.User;
import com.lkms.data.repository.enumPackage.java.LKMSConstantEnums;

import java.util.List;

/**
 * Interface Repository đã được sửa đổi để đồng bộ
 * logic giữa Comment và Mention.
 */
public interface ICommentRepository {
    // --- Các Interface Callback (Listener) ---

    /**
     * Callback cho các hành động (commands) như Post, Update, Delete.
     */
    interface OnPostResultListener {
        void onSuccess();
        void onError(Exception e);
    }

    /**
     * Callback cho hành động "refresh" (tải lại dữ liệu).
     */
    interface OnRefreshListener {
        void onRefreshComplete();
        void onError(Exception e);
    }

    /**
     * Lấy danh sách comment của một project/experiment và tự động
     * đăng ký lắng nghe realtime.
     *
     * @param targetId  ID của project/experiment.
     * @param type      Kiểu comment (PROJECT hoặc EXPERIMENT).
     * @return LiveData chứa danh sách comment.
     */
    LiveData<List<Comment>> getRealtimeComments(Integer targetId, LKMSConstantEnums.CommentType type);

    /**
     * Hủy đăng ký tất cả các kênh (channel) realtime đang lắng nghe.
     * Được gọi trong ViewModel.onCleared().
     */
    void unsubscribeFromComments();

    /**
     * Gửi một comment mới lên server.
     *
     * @param newComment        Đối tượng Comment mới.
     * @param mentionedUserIds  Danh sách các User ID đã được mention.
     * @param listener          Callback thông báo kết quả.
     */
    void postComment(Comment newComment, List<Integer> mentionedUserIds, OnPostResultListener listener);


    // --- Chức năng 3: Lấy danh sách Mention (@) [ĐÃ SỬA ĐỔI] ---

    /**
     * Lấy "cái hộp" LiveData chứa danh sách User có thể mention.
     * Hàm này KHÔNG gọi API, chỉ trả về LiveData đang cache.
     *
     * @param targetId  ID của project/experiment để lấy user. [ĐÃ SỬA]
     * @param type      Phạm vi (PROJECT/EXPERIMENT) của user. [ĐÃ SỬA]
     * @return LiveData chứa danh sách User (List<User>).
     */
    LiveData<List<User>> getMentionableUsers(Integer targetId, LKMSConstantEnums.CommentType type);

    /**
     * Yêu cầu Repository tải lại (refresh) danh sách User từ server.
     * Được gọi khi onResume.
     *
     * @param targetId  ID của project/experiment. [ĐÃ SỬA]
     * @param type      Phạm vi (PROJECT/EXPERIMENT). [ĐÃ SỬA]
     * @param listener  Callback thông báo khi refresh hoàn tất.
     */
    void refreshMentionableUsers(Integer targetId, LKMSConstantEnums.CommentType type, OnRefreshListener listener);

}

