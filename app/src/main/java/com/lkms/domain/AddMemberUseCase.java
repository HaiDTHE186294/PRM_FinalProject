package com.lkms.domain;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.lkms.data.model.java.User;
import com.lkms.data.repository.IUserRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * UseCase cho màn hình "Add Member".
 * Bao gồm logic tìm kiếm và lọc ra những user CHƯA CÓ trong team.
 */
public class AddMemberUseCase {

    private final IUserRepository userRepository;

    // Tên biến đã được sửa theo quy chuẩn SonarQube
    private final MutableLiveData<List<User>> searchResults = new MutableLiveData<>();
    public LiveData<List<User>> getSearchResults() {
        return searchResults;
    }

    private final MutableLiveData<String> error = new MutableLiveData<>();
    public LiveData<String> getError() {
        return error;
    }

    public AddMemberUseCase(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * ⭐ HÀM LOGIC CHÍNH ⭐
     * Thực hiện tìm kiếm người dùng và lọc ra những người chưa có trong team.
     * Độ phức tạp đã được giảm bằng cách tách logic ra các hàm con.
     */
    public void findAvailableUsers(String query, int experimentId) {
        userRepository.searchUsers(query, new IUserRepository.UserListCallback() {
            @Override
            public void onSuccess(List<User> users) {
                processFoundUsers(users, experimentId);
            }

            @Override
            public void onError(String errorMessage) {
                error.postValue(errorMessage);
                searchResults.postValue(new ArrayList<>()); // Đảm bảo danh sách rỗng khi có lỗi
            }
        });
    }

    /**
     * Xử lý danh sách người dùng tìm thấy, lọc ra những người chưa có trong team.
     * @param users Danh sách người dùng từ kết quả tìm kiếm.
     * @param experimentId ID của experiment để kiểm tra.
     */
    private void processFoundUsers(List<User> users, int experimentId) {
        if (users == null || users.isEmpty()) {
            searchResults.postValue(new ArrayList<>());
            return;
        }

        final List<User> filteredUsers = Collections.synchronizedList(new ArrayList<>());
        final AtomicInteger counter = new AtomicInteger(users.size());

        for (User user : users) {
            checkAndFilterSingleUser(user, experimentId, filteredUsers, counter);
        }
    }

    /**
     * Kiểm tra một người dùng duy nhất xem đã tồn tại trong team chưa và xử lý kết quả.
     * @param user Người dùng cần kiểm tra.
     * @param experimentId ID của experiment.
     * @param filteredUsers Danh sách (đã đồng bộ) để thêm người dùng nếu hợp lệ.
     * @param counter Bộ đếm để biết khi nào tất cả các tác vụ hoàn thành.
     */
    private void checkAndFilterSingleUser(User user, int experimentId, List<User> filteredUsers, AtomicInteger counter) {
        userRepository.checkIfMemberExists(user.getUserId(), experimentId, new IUserRepository.MemberExistsCallback() {
            @Override
            public void onResult(boolean exists) {
                if (!exists) {
                    // Nếu CHƯA TỒN TẠI, thêm vào danh sách kết quả
                    filteredUsers.add(user);
                }
                // Sau khi xử lý, kiểm tra xem đã xong hết chưa
                finalizeIfAllTasksDone(counter, filteredUsers);
            }

            @Override
            public void onError(String errorMessage) {
                // ⭐ SỬA: Xóa dòng code bị comment theo yêu cầu của SonarQube.
                // Ghi log lỗi ở đây nếu cần thiết cho việc debug:
                // Log.e(TAG, "Lỗi khi kiểm tra user " + user.getUserId() + ": " + errorMessage);

                // Bỏ qua lỗi và vẫn kiểm tra xem đã xong hết chưa
                finalizeIfAllTasksDone(counter, filteredUsers);
            }
        });
    }

    /**
     * Giảm bộ đếm và cập nhật LiveData nếu tất cả các tác vụ kiểm tra đã hoàn thành.
     * @param counter Bộ đếm.
     * @param filteredUsers Danh sách kết quả cuối cùng.
     */
    private void finalizeIfAllTasksDone(AtomicInteger counter, List<User> filteredUsers) {
        // Khi một user được kiểm tra xong (thành công hoặc lỗi), giảm bộ đếm
        if (counter.decrementAndGet() == 0) {
            // Nếu đã kiểm tra hết tất cả user, cập nhật LiveData
            searchResults.postValue(new ArrayList<>(filteredUsers)); // Tạo bản sao mới để tránh lỗi
        }
    }
}
