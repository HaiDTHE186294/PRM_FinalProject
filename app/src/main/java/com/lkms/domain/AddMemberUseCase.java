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

    private final MutableLiveData<List<User>> _searchResults = new MutableLiveData<>();
    public LiveData<List<User>> getSearchResults() {
        return _searchResults;
    }

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public LiveData<String> getError() {
        return _error;
    }

    public AddMemberUseCase(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * ⭐ HÀM LOGIC CHÍNH ⭐
     * Thực hiện tìm kiếm người dùng và lọc ra những người chưa có trong team.
     *
     * @param query Từ khóa tìm kiếm.
     * @param experimentId ID của experiment để kiểm tra.
     */
    public void findAvailableUsers(String query, int experimentId) {
        // 1. Tìm tất cả user khớp với query
        userRepository.searchUsers(query, new IUserRepository.UserListCallback() {
            @Override
            public void onSuccess(List<User> users) {
                if (users == null || users.isEmpty()) {
                    _searchResults.postValue(new ArrayList<>()); // Trả về ds rỗng nếu không tìm thấy ai
                    return;
                }

                // 2. Lọc danh sách user vừa nhận được
                // Dùng List đã được đồng bộ hóa để an toàn khi thêm từ nhiều luồng
                final List<User> filteredUsers = Collections.synchronizedList(new ArrayList<>());
                // Dùng AtomicInteger để đếm các tác vụ bất đồng bộ đã hoàn thành
                final AtomicInteger counter = new AtomicInteger(users.size());

                for (User user : users) {
                    // Với mỗi user, kiểm tra xem họ đã tồn tại trong team chưa
                    userRepository.checkIfMemberExists(user.getUserId(), experimentId, new IUserRepository.MemberExistsCallback() {
                        @Override
                        public void onResult(boolean exists) {
                            if (!exists) {
                                // Nếu CHƯA TỒN TẠI, thêm vào danh sách kết quả
                                filteredUsers.add(user);
                            }

                            // Khi một user được kiểm tra xong, giảm bộ đếm
                            if (counter.decrementAndGet() == 0) {
                                // Nếu đã kiểm tra hết tất cả user, cập nhật LiveData
                                _searchResults.postValue(new ArrayList<>(filteredUsers)); // Tạo bản sao mới để tránh lỗi
                            }
                        }

                        @Override
                        public void onError(String errorMessage) {
                            // Bỏ qua user bị lỗi và tiếp tục, nhưng vẫn phải giảm bộ đếm
                            // Bạn có thể post lỗi lên LiveData nếu muốn
                            // _error.postValue("Lỗi khi kiểm tra user " + user.getUserId() + ": " + errorMessage);

                            if (counter.decrementAndGet() == 0) {
                                _searchResults.postValue(new ArrayList<>(filteredUsers));
                            }
                        }
                    });
                }
            }

            @Override
            public void onError(String errorMessage) {
                _error.postValue(errorMessage);
                _searchResults.postValue(new ArrayList<>()); // Đảm bảo danh sách rỗng khi có lỗi
            }
        });
    }
}
