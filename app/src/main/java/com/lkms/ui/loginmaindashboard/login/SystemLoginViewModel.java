package com.lkms.ui.loginmaindashboard.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.lkms.data.model.java.User;
import com.lkms.data.repository.IAuthRepository;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.lkms.R;
import com.lkms.domain.loginmaindashboardusecase.SystemLoginUseCase;
import com.lkms.ui.loginmaindashboard.maindashboard.MainDashboardViewModel;
import com.lkms.ui.loginmaindashboard.maindashboard.ManagerMainDashboardViewModel;

public class SystemLoginViewModel extends AppCompatActivity {

    private EditText edtEmail, edtPassword;
    private Button btnLogin;
    private SystemLoginUseCase loginUseCase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_system_login);
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
                public void onSuccess(User user) {
                    runOnUiThread(() -> {
                        Toast.makeText(SystemLoginViewModel.this,
                                "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

                        try {
                            MasterKey masterKey = new MasterKey.Builder(getApplicationContext()).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build();

                            SharedPreferences sharedPreferences = EncryptedSharedPreferences.create(getApplicationContext(), "secure_prefs", masterKey, EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);

                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("jwt_token", user.getToken());
                            editor.putInt("user_role", user.getRoleId());
                            editor.putInt("user_id", user.getUserId());
                            editor.apply();

                            int savedId = sharedPreferences.getInt("user_id", -1);
                            int savedRole = sharedPreferences.getInt("user_role", -1);

                            if (savedRole == 1 || savedRole == 2) {
                                // research
                                startActivity(new Intent(SystemLoginViewModel.this, MainDashboardViewModel.class));
                            } else {
                                // Manager
                                startActivity(new Intent(SystemLoginViewModel.this, ManagerMainDashboardViewModel.class));
                            }
                            finish();

                        } catch (Exception e) {
                            Toast.makeText(SystemLoginViewModel.this, "Lưu token thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onError(String message) {
                    runOnUiThread(() -> {
                        Toast.makeText(SystemLoginViewModel.this, message, Toast.LENGTH_SHORT).show();
                    });
                }
            });
        });
    }
}