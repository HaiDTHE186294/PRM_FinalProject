package com.lkms.ui.loginmaindashboard.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.lkms.BuildConfig;
import com.lkms.data.model.java.AuthResult;
import com.lkms.data.repository.IAuthRepository;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.lkms.R;
import com.lkms.domain.loginmaindashboardusecase.SystemLoginUseCase;
import com.lkms.ui.loginmaindashboard.maindashboard.MainDashboardActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Date;

public class SystemLoginActivity extends AppCompatActivity {

    private EditText edtEmail, edtPassword;
    private Button btnLogin;
    private SystemLoginUseCase loginUseCase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isUserLoggedIn()) {
            startActivity(new Intent(this, MainDashboardActivity.class));
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_system_login);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);

        loginUseCase = new SystemLoginUseCase();

        btnLogin.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            loginUseCase.execute(email, password, new IAuthRepository.AuthCallback() {
                @Override
                public void onSuccess(AuthResult result) {
                    runOnUiThread(() -> {
                        Toast.makeText(SystemLoginActivity.this,
                                "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

                        try {
                            MasterKey masterKey = new MasterKey.Builder(getApplicationContext()).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build();

                            SharedPreferences sharedPreferences = EncryptedSharedPreferences.create(getApplicationContext(), "secure_prefs", masterKey, EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);

                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("jwt_token", result.getAuthToken());
                            editor.putInt("user_role", result.getRoleId());
                            editor.putInt("user_id", result.getUserId());
                            int currentUserId = result.getUserId();
                            editor.apply();

                        //    int savedRole = sharedPreferences.getInt("user_role", -1);
                            saveFcmTokenToFirebase(currentUserId);

                            startActivity(new Intent(SystemLoginActivity.this, MainDashboardActivity.class));
                            finish();

                        } catch (Exception e) {
                            Toast.makeText(SystemLoginActivity.this, "Lưu token thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onError(String message) {
                    runOnUiThread(() -> {
                        Toast.makeText(SystemLoginActivity.this, message, Toast.LENGTH_SHORT).show();
                    });
                }
            });
        });
    }


    private boolean isUserLoggedIn() {
        try {
            MasterKey masterKey = new MasterKey.Builder(getApplicationContext()).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build();
            SharedPreferences sharedPreferences = EncryptedSharedPreferences.create(getApplicationContext(), "secure_prefs", masterKey, EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
            String token = sharedPreferences.getString("jwt_token", null);

            if (token == null || token.isEmpty()) {
                return false;
            }

            // 🔹 Xác minh token bằng Auth0 JWT
            String SECRET_KEY = BuildConfig.JWT_SECRET;
            Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);

            JWTVerifier verifier = JWT.require(algorithm).withIssuer("LKMS_APP").build();

            DecodedJWT decodedJWT = verifier.verify(token);

            // 🔹 Kiểm tra hạn sử dụng
            Date expiresAt = decodedJWT.getExpiresAt();
            if (expiresAt == null || expiresAt.before(new Date())) {
                return false; // Hết hạn
            }

            // ✅ Token hợp lệ → có thể đọc thông tin user nếu cần
            int userId = decodedJWT.getClaim("userId").asInt();
            int roleId = decodedJWT.getClaim("roleId").asInt();

            return true;

        } catch (JWTVerificationException e) {
            clearLoginSession();
            return false;
        } catch (Exception e) {
            Log.e("Error", "⚠️ Lỗi khi kiểm tra token: " + e.getMessage());
            return false;
        }
    }

    private void saveFcmTokenToFirebase(int userId) {
        // 1. Lấy token từ Firebase Messaging
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (!task.isSuccessful()) {
                    Log.w("FCM", "Lấy FcmToken thất bại", task.getException());
                    return;
                }

                // 2. Lấy được Token
                String token = task.getResult();
                Log.d("FCM", "FcmToken của máy này là: " + token);

                // 3. Lấy đường dẫn đến node "fcm_tokens" trên RTDB
                DatabaseReference databaseRef = FirebaseDatabase.getInstance()
                        .getReference("fcm_tokens");

                // 4. Ghi đè token vào fcm_tokens/{userId}
                databaseRef.child(String.valueOf(userId)).setValue(token)
                        .addOnSuccessListener(aVoid -> {
                            Log.d("FCM", "Đã lưu FcmToken lên RTDB cho user: " + userId);
                        })
                        .addOnFailureListener(e -> {
                            Log.w("FCM", "Lưu FcmToken lên RTDB thất bại!", e);
                        });
            }
        });
    }

    private void clearLoginSession() {
        try {
            MasterKey masterKey = new MasterKey.Builder(getApplicationContext()).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build();
            SharedPreferences sharedPreferences = EncryptedSharedPreferences.create(getApplicationContext(), "secure_prefs", masterKey, EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
            sharedPreferences.edit().clear().apply();

        } catch (Exception e) {
            Log.e("Error", "⚠️ Lỗi khi xoá session: " + e.getMessage());
        }
    }
}