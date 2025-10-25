package com.lkms.domain.loginmaindashboardusecase;

import com.lkms.data.model.java.User;
import com.lkms.data.repository.IAuthRepository;
import com.lkms.data.repository.implement.java.AuthRepositoryImplJava;

public class SystemLoginUseCase {

    private final IAuthRepository authRepository;

    // ✅ Constructor — có thể inject repository từ ngoài (nếu cần test dễ hơn)
    public SystemLoginUseCase() {
        this.authRepository = new AuthRepositoryImplJava();
    }

    // ✅ Hoặc constructor có tham số (nếu bạn muốn truyền repo từ Activity)
    public SystemLoginUseCase(IAuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    // ✅ Hàm xử lý logic login
    public void execute(String email, String password, IAuthRepository.AuthCallback callback) {
        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            callback.onError("Vui lòng nhập đầy đủ thông tin");
            return;
        }

        // Gọi repo xử lý đăng nhập
        authRepository.login(email, password, new IAuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                callback.onSuccess(user);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }
}
