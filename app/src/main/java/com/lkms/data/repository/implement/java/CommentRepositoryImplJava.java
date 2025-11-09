package com.lkms.data.repository.implement.java;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.reflect.TypeToken;
import com.lkms.data.model.java.Comment;
import com.lkms.data.model.java.User;
import com.lkms.data.repository.ICommentRepository;
import com.lkms.data.repository.enumPackage.java.LKMSConstantEnums;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.lkms.BuildConfig.SUPABASE_URL;

import android.util.Log;

import com.google.gson.Gson;

/**
 * Implement ICommentRepository (Đã sửa để dùng Integer ID).
 * - Dùng Firebase Realtime Database cho Comment và Mention.
 * - Dùng HttpHelper (Supabase) cho Get Users.
 */
public class CommentRepositoryImplJava implements ICommentRepository {

    private final FirebaseDatabase database;
    private ValueEventListener currentCommentListener;
    private DatabaseReference currentCommentRef;
    private final MutableLiveData<List<Comment>> commentsLiveData = new MutableLiveData<>();

    //Supabase
    private MutableLiveData<List<User>> mentionableUsersLiveData = new MutableLiveData<>();
    private Gson gson = new Gson();

    public CommentRepositoryImplJava() {
        this.database = FirebaseDatabase.getInstance();
    }

    // --- 1. LẤY COMMENT (FIREBASE) ---
    @Override
    public LiveData<List<Comment>> getRealtimeComments(Integer targetId, LKMSConstantEnums.CommentType type) {
        unsubscribeFromComments();

        // [ĐÃ SỬA] Đổi tên enum cho đúng (DISCUSSION -> PROJECT)
        String nodePath = (type == LKMSConstantEnums.CommentType.DISCUSSION) ? "project" : "experiment";

        // [ĐÃ SỬA] Phải chuyển Integer thành String cho Firebase path
        currentCommentRef = database.getReference("comment").child(nodePath).child(String.valueOf(targetId));

        currentCommentListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Comment> newList = new ArrayList<>();
                for (DataSnapshot commentSnapshot : snapshot.getChildren()) {
                    Comment comment = commentSnapshot.getValue(Comment.class);
                    if (comment != null) {
                        newList.add(comment);
                    }
                }
                commentsLiveData.postValue(newList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                commentsLiveData.postValue(new ArrayList<>());
            }
        };

        currentCommentRef.addValueEventListener(currentCommentListener);
        return commentsLiveData;
    }

    // --- 2. HỦY LẮNG NGHE (FIREBASE) ---
    @Override
    public void unsubscribeFromComments() {
        if (currentCommentRef != null && currentCommentListener != null) {
            currentCommentRef.removeEventListener(currentCommentListener);
            currentCommentRef = null;
            currentCommentListener = null;
        }
    }

    // --- 3. POST COMMENT (FIREBASE) ---
    @Override
    // [ĐÃ SỬA] List<Integer> mentionedUserIds
    public void postComment(Comment newComment, List<Integer> mentionedUserIds, OnPostResultListener listener) {

        String nodePath = (newComment.getCommentType().equals(LKMSConstantEnums.CommentType.DISCUSSION.toString())) ? "project" : "experiment";
        Integer targetId = newComment.getTargetId(); // Giả sử model trả về Integer

        DatabaseReference commentsRef = database.getReference("comment").child(nodePath).child(String.valueOf(targetId));

        String newCommentId = commentsRef.push().getKey();
        if (newCommentId == null) {
            listener.onError(new Exception("Không thể tạo ID cho comment"));
            return;
        }
        newComment.setCommentId(newCommentId);

        commentsRef.child(newCommentId).setValue(newComment).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                listener.onError(task.getException());
                return;
            }

            if (mentionedUserIds == null || mentionedUserIds.isEmpty()) {
                listener.onSuccess();
                return;
            }

            // 3. Có mention, lưu vào node /mentions/comment_id/
            DatabaseReference mentionsRef = database.getReference("mention").child(newCommentId);

            Map<String, Object> userMap = new HashMap<>();
            for (Integer userId : mentionedUserIds) {
                userMap.put(String.valueOf(userId), true);
            }

            mentionsRef.setValue(userMap).addOnCompleteListener(mentionTask -> {
                if (mentionTask.isSuccessful()) {
                    listener.onSuccess();
                } else {
                    listener.onError(mentionTask.getException());
                }
            });
        });
    }

    // --- 4. LẤY USER ĐỂ MENTION (SUPABASE) ---
    @Override
    // (Giữ nguyên, đã là Integer)
    public LiveData<List<User>> getMentionableUsers(Integer targetId, LKMSConstantEnums.CommentType type) {
        return mentionableUsersLiveData;
    }

    // --- 5. LÀM MỚI DANH SÁCH USER (SUPABASE) ---
    @Override
    // (Giữ nguyên, đã là Integer)
    public void refreshMentionableUsers(Integer targetId, LKMSConstantEnums.CommentType type, OnRefreshListener listener) {

        new Thread(() -> {
            try {


                String endpoint = endpointBuilder(targetId, type);
                String json = HttpHelper.getJson(endpoint);

                // Parse JSON thành List<User>
                Type listType = new TypeToken<List<User>>() {}.getType();
                List<User> userList = gson.fromJson(json, listType);

                mentionableUsersLiveData.postValue(userList);

                if (listener != null) {
                    listener.onRefreshComplete();
                }

            } catch (Exception e) {
                // Xử lý lỗi
                Log.e("CommentRepositoryImpl", "Error fetching mentionable users", e);
                mentionableUsersLiveData.postValue(null);

                if (listener != null) {
                    listener.onRefreshComplete();
                }
            }
        }).start();
    }

    private String endpointBuilder(Integer targetId, LKMSConstantEnums.CommentType type) {
        // Gốc của query: Chúng ta luôn muốn lấy User
        String baseUrl = SUPABASE_URL + "/rest/v1/User?select=*";

        if (type == LKMSConstantEnums.CommentType.DISCUSSION) {
            // --- LOGIC CHO PROJECT (DISCUSSION) ---
            // targetId ở đây là projectId
            // Supabase/PostgREST cho phép join lồng nhau: Team.Experiment.projectId
            // (Với 'Team' là bảng join từ User, và 'Experiment' là bảng join từ Team)

            return baseUrl + "&Team.Experiment.projectId=eq." + targetId;

        } else if (type == LKMSConstantEnums.CommentType.GENERAL) {
            // --- LOGIC CHO EXPERIMENT (GENERAL) ---
            // targetId ở đây là experimentId
            // Query này đơn giản hơn, chỉ cần join 1 cấp: Team.experimentId

            return baseUrl + "&Team.experimentId=eq." + targetId;

        } else {
            // Trường hợp không xác định, trả về một query không có kết quả
            Log.e("CommentRepositoryImpl", "Error during endpointBuilder: Unknown Command type" + type);
            // Trả về một query luôn rỗng (vd: userId = -1)
            // để tránh lỗi và đảm bảo an toàn.
            return baseUrl + "&userId=eq.-1";
        }
    }
}