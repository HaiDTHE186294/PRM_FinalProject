// File: app/src/main/java/com/lkms/ui/activities/RegisterActivity.java
package com.lkms.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.lkms.R;
import com.lkms.data.repository.implement.java.AuthRepositoryImplJava;
import com.lkms.databinding.ActivityRegisterBinding;
import com.lkms.domain.UserProfileUseCase;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private UserProfileUseCase userProfileUseCase;

    TextInputEditText etFullName;
    TextInputEditText etEmail;
    TextInputEditText etPassword;

    Button btnRegister;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        userProfileUseCase = new UserProfileUseCase();

        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        btnRegister = findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(v -> {
            String fullName = etFullName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString();

            if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }


            userProfileUseCase.addNewUser(fullName, email, password);
            Toast.makeText(this, "Register successfully", Toast.LENGTH_SHORT).show();
        });
    }

}
