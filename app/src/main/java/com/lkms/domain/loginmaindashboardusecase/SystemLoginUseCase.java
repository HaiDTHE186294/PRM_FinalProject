package com.lkms.domain.loginmaindashboardusecase;

import com.lkms.data.model.java.AuthResult;
import com.lkms.data.model.java.User;
import com.lkms.data.repository.IAuthRepository;
import com.lkms.data.repository.implement.java.AuthRepositoryImplJava;

public class SystemLoginUseCase {

    private final IAuthRepository authRepository;

    public SystemLoginUseCase() {
        this.authRepository = new AuthRepositoryImplJava();
    }

    public SystemLoginUseCase(IAuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    // ✅ Hàm xử lý logic login
    public void execute(String email, String password, IAuthRepository.AuthCallback callback) {
        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            callback.onError("Please enter all required information.");
            return;
        }

        // Gọi repo xử lý đăng nhập
        authRepository.login(email, password, new IAuthRepository.AuthCallback() {
//            @Override
//            public void onSuccess(User user) {
//                callback.onSuccess(user);
//            }

            @Override
            public void onSuccess(AuthResult result) {
                callback.onSuccess(result);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }
}
