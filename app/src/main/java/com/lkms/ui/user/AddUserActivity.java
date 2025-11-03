// File: app/src/main/java/com/lkms/ui/activities/RegisterActivity.java
package com.lkms.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.lkms.R;
import com.lkms.data.repository.enumPackage.java.LKMSConstantEnums;
import com.lkms.domain.UserProfileUseCase;
import com.lkms.ui.loginmaindashboard.login.SystemLoginActivity;

public class AddUserActivity extends AppCompatActivity {

    private UserProfileUseCase userProfileUseCase;

    TextInputEditText etFullName;
    TextInputEditText etEmail;
    TextInputEditText etPassword;

    AutoCompleteTextView actvRole;

    Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);

        userProfileUseCase = new UserProfileUseCase();

        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        String[] roles = {"Lab Manager", "Researcher", "Technician"};
        actvRole = findViewById(R.id.actvRole);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, roles);
        actvRole.setAdapter(adapter);

        btnRegister = findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(v -> {
            String fullName = etFullName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString();

            if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }


            LKMSConstantEnums.UserRole role = LKMSConstantEnums.UserRole.valueOf(
                    actvRole.getText()
                            .toString()
                            .replace(" ", "_")
                            .toUpperCase()
            );

            userProfileUseCase.addNewUser(fullName, email, password, role);
            Toast.makeText(this, "Register successfully", Toast.LENGTH_SHORT).show();

//            Intent intent = new Intent(AddUserActivity.this, SystemLoginActivity.class);
//            startActivity(intent);
            finish();
        });
    }

}
