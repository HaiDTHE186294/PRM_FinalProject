package com.lkms.data.repository.implement.java;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
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
    private ChildEventListener currentCommentListener;
    private DatabaseReference currentCommentRef;
    private final MutableLiveData<List<Comment>> commentsLiveData = new MutableLiveData<>();
    private final List<Comment> mCommentList = new ArrayList<>();

    //Supabase
    private MutableLiveData<List<User>> mentionableUsersLiveData = new MutableLiveData<>();
    private Gson gson = new Gson();

    public CommentRepositoryImplJava() {
        this.database = FirebaseDatabase.getInstance();
    }

    // --- 1. LẤY COMMENT (FIREBASE) - ĐÃ SỬA ---
    @Override
    public LiveData<List<Comment>> getRealtimeComments(Integer targetId, LKMSConstantEnums.CommentType type) {
        unsubscribeFromComments();

        // Xóa list cũ khi lắng nghe 1 node mới
        mCommentList.clear();

        String nodePath = (type == LKMSConstantEnums.CommentType.DISCUSSION) ? "project" : "experiment";
        currentCommentRef = database.getReference("comment").child(nodePath).child(String.valueOf(targetId));

        // [ĐÃ SỬA] Dùng ChildEventListener thay vì ValueEventListener
        currentCommentListener = new ChildEventListener() {

            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Comment newComment = snapshot.getValue(Comment.class);
                if (newComment != null) {
                    // Thêm 1 comment mới vào list
                    mCommentList.add(newComment);
                    // Post 1 bản copy của list lên LiveData để UI cập nhật
                    commentsLiveData.postValue(new ArrayList<>(mCommentList));
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // (Nên xử lý) Logic khi 1 comment bị sửa
                Comment changedComment = snapshot.getValue(Comment.class);
                if (changedComment == null) return;

                // Tìm và thay thế comment cũ trong list
                for (int i = 0; i < mCommentList.size(); i++) {
                    if (mCommentList.get(i).getCommentId().equals(changedComment.getCommentId())) {
                        mCommentList.set(i, changedComment);
                        break;
                    }
                }
                commentsLiveData.postValue(new ArrayList<>(mCommentList));
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                // (Nên xử lý) Logic khi 1 comment bị xóa
                Comment removedComment = snapshot.getValue(Comment.class);
                if (removedComment == null) return;

                // Tìm và xóa comment khỏi list
                for (int i = 0; i < mCommentList.size(); i++) {
                    if (mCommentList.get(i).getCommentId().equals(removedComment.getCommentId())) {
                        mCommentList.remove(i);
                        break;
                    }
                }
                commentsLiveData.postValue(new ArrayList<>(mCommentList));
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // (Không cần xử lý nhiều cho comment)
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // [ĐÃ SỬA] Vẫn nên post list rỗng để UI biết là có lỗi
                commentsLiveData.postValue(new ArrayList<>());
            }
        };

        currentCommentRef.addChildEventListener((ChildEventListener) currentCommentListener);
        return commentsLiveData;
    }

    // --- 2. HỦY LẮNG NGHE (FIREBASE) ---
    @Override
    public void unsubscribeFromComments() {
        if (currentCommentRef != null && currentCommentListener != null) {
            // [ĐÃ SỬA] Phải ép kiểu đúng về ChildEventListener
            currentCommentRef.removeEventListener((ChildEventListener) currentCommentListener);
            currentCommentRef = null;
            currentCommentListener = null;
        }
    }

    // --- 3. POST COMMENT (FIREBASE) ---
    @Override
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

                Log.d("CommentRepositoryImpl", "JSON nhận được từ Supabase: " + json);

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
                    listener.onError(e);
                }
            }
        }).start();
    }

    private String endpointBuilder(Integer targetId, LKMSConstantEnums.CommentType type) {
        String baseUrl = SUPABASE_URL + "/rest/v1/User";

        if (type == LKMSConstantEnums.CommentType.DISCUSSION) {
            // --- LOGIC CHO PROJECT (DISCUSSION) ---
            // Ép INNER JOIN 2 cấp
            // Lấy User NÀO có Team, VÀ Team đó có Experiment
            return baseUrl + "?select=*,Team!inner(Experiment!inner(*))"
                    + "&Team.Experiment.projectId=eq." + targetId;

        } else if (type == LKMSConstantEnums.CommentType.GENERAL) {
            // --- LOGIC CHO EXPERIMENT (GENERAL) ---
            // Ép INNER JOIN 1 cấp
            // Lấy User NÀO có Team
            return baseUrl + "?select=*,Team!inner(*)"
                    + "&Team.experimentId=eq." + targetId;

        } else {
            // Trường hợp không xác định, trả về query rỗng
            Log.e("CommentRepositoryImpl", "Error during endpointBuilder: Unknown Command type" + type);
            return SUPABASE_URL + "/rest/v1/User?select=*&userId=eq.-1";
        }
    }
}