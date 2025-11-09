package com.lkms.ui.comment;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.lkms.data.model.java.Comment;
import com.lkms.data.model.java.User;
import com.lkms.data.repository.ICommentRepository;
import com.lkms.data.repository.enumPackage.java.LKMSConstantEnums;
import com.lkms.domain.comment.GetMentionableUsersUseCase;
import com.lkms.domain.comment.GetRealtimeCommentsUseCase;
import com.lkms.domain.comment.PostCommentUseCase;
import com.lkms.domain.comment.RefreshMentionableUsersUseCase;
import com.lkms.domain.comment.UnsubscribeFromCommentsUseCase;
import com.lkms.util.NotificationHelper;

import java.util.ArrayList;
import java.util.List;

// (Import các UseCase và Model của chủ nhân)

public class CommentViewModel extends AndroidViewModel {

    // Use Cases
    private final GetRealtimeCommentsUseCase getRealtimeCommentsUseCase;
    private final PostCommentUseCase postCommentUseCase;
    private final GetMentionableUsersUseCase getMentionableUsersUseCase;
    private final RefreshMentionableUsersUseCase refreshMentionableUsersUseCase;
    private final UnsubscribeFromCommentsUseCase unsubscribeFromCommentsUseCase;

    // Dữ liệu nội bộ
    private Integer targetId;
    private LKMSConstantEnums.CommentType commentType;
    private List<User> localMentionableUsers = new ArrayList<>(); // Danh sách tạm thời

    // LiveData cho Fragment quan sát
    private LiveData<List<Comment>> commentsLiveData;
    private final LiveData<List<User>> mentionableUsersLiveData;
    private final MutableLiveData<Boolean> isLoadingUsers = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> postSuccess = new MutableLiveData<>();

    // Constructor (Dùng DI để tiêm UseCase)
    public CommentViewModel(@NonNull Application application,
                            GetRealtimeCommentsUseCase getRealtimeCommentsUseCase,
                            PostCommentUseCase postCommentUseCase,
                            GetMentionableUsersUseCase getMentionableUsersUseCase,
                            RefreshMentionableUsersUseCase refreshMentionableUsersUseCase,
                            UnsubscribeFromCommentsUseCase unsubscribeFromCommentsUseCase) {
        super(application);
        this.getRealtimeCommentsUseCase = getRealtimeCommentsUseCase;
        this.postCommentUseCase = postCommentUseCase;
        this.getMentionableUsersUseCase = getMentionableUsersUseCase;
        this.refreshMentionableUsersUseCase = refreshMentionableUsersUseCase;
        this.unsubscribeFromCommentsUseCase = unsubscribeFromCommentsUseCase;

        // Khởi tạo LiveData từ UseCase
        // (Lưu ý: LiveData này sẽ rỗng cho đến khi loadData được gọi)
        this.mentionableUsersLiveData = getMentionableUsersUseCase.execute(null, null);
    }

    // Fragment sẽ gọi hàm này đầu tiên
    public void loadInitialData(Integer targetId, LKMSConstantEnums.CommentType type) {
        this.targetId = targetId;
        this.commentType = type;

        // 1. Bắt đầu lắng nghe comment (Realtime)
        this.commentsLiveData = getRealtimeCommentsUseCase.execute(targetId, type);

        // 2. Tải danh sách user về
        refreshMentionableUsers();
    }

    // Lấy danh sách comment
    public LiveData<List<Comment>> getComments() {
        return commentsLiveData;
    }

    // Lấy danh sách user (để @mention)
    public LiveData<List<User>> getMentionableUsers() {
        // Quan sát LiveData này để cập nhật danh sách tạm thời
        return mentionableUsersLiveData;
    }

    // Lấy trạng thái loading
    public LiveData<Boolean> isLoadingUsers() {
        return isLoadingUsers;
    }

    // Lấy lỗi
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    // Lấy trạng thái gửi
    public LiveData<Boolean> getPostSuccess() {
        return postSuccess;
    }


    // 1. Tải (làm mới) danh sách user từ Supabase
    private void refreshMentionableUsers() {
        isLoadingUsers.setValue(true);
        refreshMentionableUsersUseCase.execute(targetId, commentType, new ICommentRepository.OnRefreshListener() {
            @Override
            public void onRefreshComplete() {
                isLoadingUsers.postValue(false);
            }

            @Override
            public void onError(Exception e) {
                isLoadingUsers.postValue(false);
                errorMessage.postValue("Lỗi tải danh sách người dùng: " + e.getMessage());
            }
        });
    }

    // 2. Gửi comment
    public void sendComment(String commentText, List<User> mentionedUsers) {
        if (commentText == null || commentText.trim().isEmpty()) {
            errorMessage.setValue("Vui lòng nhập bình luận!"); // Sửa lại error message
            return;
        }

        Integer currentUserId = getUserIdFromSession();
        if (currentUserId == -1) {
            errorMessage.setValue("Lỗi xác thực người dùng!");
            return;
        }

        List<Integer> mentionedUserIds = new ArrayList<>();
        if (mentionedUsers != null) {
            for (User user : mentionedUsers) {
                mentionedUserIds.add(user.getUserId());
            }
        }

        Comment newComment = new Comment();
        newComment.setCommentType(commentType.toString());

        if(commentType == LKMSConstantEnums.CommentType.DISCUSSION) {
            newComment.setProjectId(targetId);
        } else if(commentType == LKMSConstantEnums.CommentType.GENERAL) {
            newComment.setExperimentId(targetId);
        } else {
            Log.e("CommentViewModel", "Unknown comment type: " + commentType);
            errorMessage.setValue("Unknown comment type: " + commentType);
            return;
        }

        newComment.setCommentText(commentText.trim());
        newComment.setUserId(currentUserId);

        long timestamp = System.currentTimeMillis();
        newComment.setTimeStamp(String.valueOf(timestamp));

        // Gọi UseCase để post
        postCommentUseCase.execute(newComment, mentionedUserIds, new ICommentRepository.OnPostResultListener() {
            @Override
            public void onSuccess() {
                postSuccess.postValue(true);

                if (mentionedUsers != null && !mentionedUsers.isEmpty()) {

                    NotificationHelper notificationHelper = new NotificationHelper(getApplication());
                    notificationHelper.createNotificationChannel();

                    // 1. Lấy tên "A" (Người gửi)
                    String userNameA = findUserNameById(currentUserId);

                    // 2. Lặp qua "B" (Những người được mention)
                    for (User userB : mentionedUsers) {

                        // Bỏ qua nếu tự mention chính mình
                        if (userB.getUserId() == currentUserId) {
                            continue;
                        }

                        String title = "You have been mentioned!";

                        // 3. MEOW! TẠO CONTENT MỚI
                        String content = userNameA + " mentioned you in "; // "User A mentioned you in "

                        if (commentType == LKMSConstantEnums.CommentType.DISCUSSION) {
                            content += "project with id: " + targetId;
                        } else { // (Giả định là GENERAL)
                            content += "experiment with id: " + targetId;
                        }

                        // 4. Gửi thông báo!
                        notificationHelper.sendMentionNotification(title, content, userB.getUserId());
                    }
                }
            }

            @Override
            public void onError(Exception e) {
                errorMessage.postValue("Gửi bình luận thất bại: " + e.getMessage());
            }
        });
    }

    // 3. Dọn dẹp
    @Override
    protected void onCleared() {
        super.onCleared();
        unsubscribeFromCommentsUseCase.execute();
    }

    // Hàm này sẽ được observer gọi để cập nhật danh sách tạm thời
    public void updateLocalMentionableUsers(List<User> users) {
        if (users != null) {
            this.localMentionableUsers.clear();
            this.localMentionableUsers.addAll(users);
        }
    }

    private int getUserIdFromSession() {
        try {
            MasterKey masterKey = new MasterKey.Builder(getApplication().getApplicationContext()).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build();

            SharedPreferences sharedPreferences = EncryptedSharedPreferences.create(getApplication().getApplicationContext(), "secure_prefs", masterKey, EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);

            return sharedPreferences.getInt("user_id", -1); // -1 nếu chưa lưu
        } catch (Exception e) {
            Log.d("get userId failed:", e.toString() );
            return -1;
        }
    }

    private String findUserNameById(int userId) {
        for (User user : localMentionableUsers) {
            if (user.getUserId() == userId) {
                return user.getName();
            }
        }
        // Nếu không tìm thấy (hiếm khi), trả về ID
        return "User " + userId;
    }
}
