package com.lkms;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.FirebaseApp;
import com.lkms.data.model.java.Comment;
import com.lkms.data.model.java.User;
import com.lkms.data.repository.ICommentRepository;
import com.lkms.data.repository.enumPackage.java.LKMSConstantEnums;
import com.lkms.data.repository.implement.java.CommentRepositoryImplJava;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
/**
 * Test Tích Hợp (Integration Test) cho CommentRepositoryImplJava.
 * !! YÊU CẦU:
 * 1. Phải có kết nối Internet.
 * 2. Phải có dữ liệu thật trên Firebase và Supabase.
 * 3. Đã sửa lỗi Integer.parseInt() trong postComment.
 * 4. Đảm bảo HttpHelper đã được cấu hình với SUPABASE_KEY.
 */
@RunWith(AndroidJUnit4.class)
public class CommentRepositoryImplJavaTest {

    // Rule này bắt buộc phải có để test LiveData
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private CommentRepositoryImplJava repository;
    private CountDownLatch latch;

    private final Integer REAL_PROJECT_ID = 1; // ID của Project (DISCUSSION)
    private final Integer REAL_EXPERIMENT_ID = 1; // ID của Experiment (GENERAL)

    @Before
    public void setUp() {
        // Khởi tạo repository thật
        repository = new CommentRepositoryImplJava();
        // Mỗi test chờ 1 tín hiệu
        latch = new CountDownLatch(1);

        FirebaseDatabase.getInstance().setLogLevel(com.google.firebase.database.Logger.Level.DEBUG);
        Log.d("FirebaseTest", "Firebase Realtime Database Debug Logging enabled.");
    }

    @After
    public void tearDown() {
        // Hủy đăng ký listener sau mỗi test (nếu có)
        repository.unsubscribeFromComments();
    }

    @Test
    public void test_getRealtimeComments() throws InterruptedException {
        System.out.println("🧪 Bắt đầu test: getRealtimeComments...");

        Observer<List<Comment>> observer = new Observer<List<Comment>>() {
            @Override
            public void onChanged(List<Comment> comments) {
                if (comments != null && !comments.isEmpty()) {
                    System.out.println("✅ [getRealtimeComments] Thành công: Lấy được " + comments.size() + " comments.");
                    for (Comment c : comments) {
                        System.out.println("   - Comment: " + c.getCommentText()); // Giả sử có hàm getContent()
                    }
                    latch.countDown(); // Mở cổng
                    repository.unsubscribeFromComments(); // Ngừng observe
                } else if (comments != null && comments.isEmpty()) {
                    System.out.println("⚠️ [getRealtimeComments] Lấy được 0 comment (Có thể ID không có data).");
                    latch.countDown();
                }
            }
        };

        LiveData<List<Comment>> liveData = repository.getRealtimeComments(REAL_EXPERIMENT_ID, LKMSConstantEnums.CommentType.GENERAL);
        liveData.observeForever(observer);

        // Chờ tối đa 5 giây
        if (latch.await(180, TimeUnit.SECONDS)) {
            System.out.println("✅ Test getRealtimeComments hoàn thành.");
        } else {
            System.out.println("❌ Test getRealtimeComments thất bại (Timeout).");
        }
    }

    @Test
    public void test_getAndRefreshUsers() throws InterruptedException {
        System.out.println("🧪 Bắt đầu test: getAndRefreshUsers (Supabase)...");

        // 1. Observe LiveData trước
        Observer<List<User>> observer = new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> users) {
                if (users != null && !users.isEmpty()) {
                    System.out.println("✅ [getAndRefreshUsers] Thành công: Lấy được " + users.size() + " users.");
                    for (User u : users) {
                        System.out.println("   - User: " + u.getUserId()); // Giả sử có hàm getUserId()
                    }
                } else if (users != null && users.isEmpty()) {
                    System.out.println("⚠️ [getAndRefreshUsers] Lấy được 0 user (ID không có data, hoặc query sai).");
                } else {
                    System.out.println("❌ [getAndRefreshUsers] Lỗi: LiveData trả về null.");
                }
                // (Không countDown ở đây, vì Latch là của RefreshListener)
            }
        };
        repository.getMentionableUsers(REAL_EXPERIMENT_ID, LKMSConstantEnums.CommentType.GENERAL)
                .observeForever(observer);


        // 2. Gọi refresh
        repository.refreshMentionableUsers(
                REAL_EXPERIMENT_ID,
                LKMSConstantEnums.CommentType.GENERAL,
                new ICommentRepository.OnRefreshListener() {
                    @Override
                    public void onRefreshComplete() {
                        System.out.println("✅ [refreshMentionableUsers] Đã gọi onRefreshComplete.");
                        latch.countDown(); // Mở cổng
                    }
                    @Override
                    public void onError(Exception error) {
                        System.out.println("❌ [refreshMentionableUsers] Lỗi: " + error.getMessage());
                        latch.countDown();
                    }
                }
        );

        // Chờ tối đa 30 giây
        if (latch.await(180, TimeUnit.SECONDS)) {
            System.out.println("✅ Test getAndRefreshUsers hoàn thành.");
        } else {
            System.out.println("❌ Test getAndRefreshUsers thất bại (Timeout).");
        }
    }

    @Test
    public void test_getAndRefreshUsersProject() throws InterruptedException {
        System.out.println("🧪 Bắt đầu test: getAndRefreshUsers (Supabase)...");

        // 1. Observe LiveData trước
        Observer<List<User>> observer = new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> users) {
                if (users != null && !users.isEmpty()) {
                    System.out.println("✅ [getAndRefreshUsers] Thành công: Lấy được " + users.size() + " users.");
                    for (User u : users) {
                        System.out.println("   - User: " + u.getUserId()); // Giả sử có hàm getUserId()
                    }
                } else if (users != null && users.isEmpty()) {
                    System.out.println("⚠️ [getAndRefreshUsers] Lấy được 0 user (ID không có data, hoặc query sai).");
                } else {
                    System.out.println("❌ [getAndRefreshUsers] Lỗi: LiveData trả về null.");
                }
                // (Không countDown ở đây, vì Latch là của RefreshListener)
            }
        };
        repository.getMentionableUsers(REAL_PROJECT_ID, LKMSConstantEnums.CommentType.DISCUSSION)
                .observeForever(observer);


        // 2. Gọi refresh
        repository.refreshMentionableUsers(
                REAL_PROJECT_ID,
                LKMSConstantEnums.CommentType.DISCUSSION,
                new ICommentRepository.OnRefreshListener() {
                    @Override
                    public void onRefreshComplete() {
                        System.out.println("✅ [refreshMentionableUsers] Đã gọi onRefreshComplete.");
                        latch.countDown(); // Mở cổng
                    }
                    @Override
                    public void onError(Exception error) {
                        System.out.println("❌ [refreshMentionableUsers] Lỗi: " + error.getMessage());
                        latch.countDown();
                    }
                }
        );

        // Chờ tối đa 30 giây
        if (latch.await(180, TimeUnit.SECONDS)) {
            System.out.println("✅ Test getAndRefreshUsers hoàn thành.");
        } else {
            System.out.println("❌ Test getAndRefreshUsers thất bại (Timeout).");
        }
    }



    @Test
    public void test_FirebaseConnection() throws InterruptedException {
        System.out.println("🧪 Bắt đầu test: Kết nối Firebase (.info/connected)...");

        String YOUR_DATABASE_URL = "https://lkms-57852-default-rtdb.asia-southeast1.firebasedatabase.app/";

        // Dùng 1 Latch mới để chờ trạng thái TRUE
        CountDownLatch connectionLatch = new CountDownLatch(1);
        // Dùng 1 Latch khác để chờ ít nhất một lần callback
        CountDownLatch firstCallbackLatch = new CountDownLatch(1);


        DatabaseReference connectedRef = FirebaseDatabase.getInstance(YOUR_DATABASE_URL).getReference(".info/connected");

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean isConnected = snapshot.getValue(Boolean.class);
                System.out.println("✅ [Firebase Connection] Trạng thái: " + isConnected);

                if (Boolean.TRUE.equals(isConnected)) {
                    System.out.println("✅✅✅ KẾT NỐI FIREBASE THÀNH CÔNG! ✅✅✅");
                    connectionLatch.countDown(); // Mở cổng nếu kết nối thành công
                } else {
                    System.out.println("❌❌❌ KẾT NỐI FIREBASE THẤT BẠI (Status: false) ❌❌❌");
                    // KHÔNG countDown() ở đây nếu isConnected là false, để tiếp tục chờ true
                }
                firstCallbackLatch.countDown(); // Đảm bảo ít nhất một callback đã được gọi
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("❌ [Firebase Connection] Bị hủy (OnCancelled): " + error.getMessage());
                connectionLatch.countDown(); // Mở cổng nếu có lỗi để test không bị treo
                firstCallbackLatch.countDown(); // Đảm bảo ít nhất một callback đã được gọi
            }
        };

        // Gắn listener
        connectedRef.addValueEventListener(listener);

        // Đợi ít nhất một callback để tránh race condition nếu kết nối nhanh
        firstCallbackLatch.await(1, TimeUnit.SECONDS);

        // Chờ tối đa 10 giây cho kết nối thành công (true)
        if (connectionLatch.await(180, TimeUnit.SECONDS)) {
            System.out.println("✅ Test kết nối hoàn thành (trạng thái TRUE đã nhận).");
        } else {
            // Nếu timeout, có thể do không bao giờ nhận được true
            System.out.println("❌ Test kết nối thất bại (Timeout hoặc không nhận được trạng thái TRUE).");
        }

        // Dọn dẹp
        connectedRef.removeEventListener(listener);
    }

}