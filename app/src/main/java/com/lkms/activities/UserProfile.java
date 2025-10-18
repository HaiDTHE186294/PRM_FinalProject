package com.lkms.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.lkms.R;
import com.lkms.data.repository.implement.UserRepositoryImpl;
import com.lkms.activities.viewmodel.UserProfileViewModel;
import com.lkms.activities.viewmodel.factory.UserProfileViewModelFactory;

//For debugging
//import android.util.Log;

public class UserProfile extends AppCompatActivity {

    private UserProfileViewModel userProfileViewModel;

    // UI components
    private TextView textViewName;
    private TextView textViewEmail;
    private TextView textViewRole;
    private FrameLayout roleBackground;

    private TextView changePasswordOption;
    private TextView profileSettingOption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        //Get user's name and email field
        textViewName = findViewById(R.id.user_name);
        textViewEmail = findViewById(R.id.user_email);
        textViewRole = findViewById(R.id.user_role);
        roleBackground = findViewById(R.id.role_background);

        //Change password option
        changePasswordOption = findViewById(R.id.option_change_password);
        changePasswordOption.setOnClickListener(v -> {
            Intent intent = new Intent(UserProfile.this, PasswordChange.class);
            startActivity(intent);
        });

        //Profile setting option
        profileSettingOption = findViewById(R.id.option_profile_setting);
        profileSettingOption.setOnClickListener(v -> {
            Intent intent = new Intent(UserProfile.this, ProfileSetting.class);
            startActivity(intent);
        });

        //Setup ViewModel
        UserRepositoryImpl userRepository = new UserRepositoryImpl();
        UserProfileViewModelFactory factory = new UserProfileViewModelFactory(userRepository);
        userProfileViewModel = new ViewModelProvider(this, factory).get(UserProfileViewModel.class);

        // Observe. Execute ONLY when the data is ready.
        userProfileViewModel.getUser().observe(this, user -> {
            if (user != null)
            {
                //Update name and email
                textViewName.setText(user.getName());
                textViewEmail.setText(user.getEmail());

                //Update role
                updateUserRoleUI(user.getRoleId());
            }
        });

        //Load user
        userProfileViewModel.loadUser(2);
    }

    private void updateUserRoleUI(int roleId) {
        String roleName = "UNKNOW";
        int backgroundColor = ContextCompat.getColor(this, android.R.color.darker_gray);

        switch (roleId) {
            case 1:
                roleName = "Lab Manager";
                backgroundColor = ContextCompat.getColor(this, R.color.role_lab_mng);
                break;
            case 2:
                roleName = "Researcher";
                backgroundColor = ContextCompat.getColor(this, R.color.role_researcher);
                break;
            case 3:
                roleName = "Technician";
                backgroundColor = ContextCompat.getColor(this, R.color.role_technician);
                break;
        }

        // Set the text and background color
        textViewRole.setText(roleName);
        roleBackground.setBackgroundColor(backgroundColor);
        roleBackground.setVisibility(View.VISIBLE); // Make sure it's visible
    }
}

