package com.lkms.ui.user_profile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
//import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.lkms.R;
import com.lkms.data.repository.enumPackage.java.LKMSConstantEnums;
import com.lkms.data.repository.implement.java.UserRepositoryImplJava;
import com.lkms.ui.user_profile.viewmodel.UserProfileViewModel;
import com.lkms.ui.user_profile.viewmodel.factory.UserProfileViewModelFactory;

//For debugging
//import android.util.Log;

public class UserProfile extends AppCompatActivity {

    private UserProfileViewModel userProfileViewModel;

    // UI components
    private TextView textViewName;
    private TextView textViewEmail;
    private RoleTag userRoleTag;

    private TextView changePasswordOption;
    private TextView profileSettingOption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        //Get user's name and email field
        textViewName = findViewById(R.id.user_name);
        textViewEmail = findViewById(R.id.user_email);
        userRoleTag = findViewById(R.id.user_role);

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

            //Passing user's id to ProfileSetting
            intent.putExtra("UserId", userProfileViewModel.getUser().getValue().getUserId());
            startActivity(intent);
        });

        //Setup ViewModel
        UserRepositoryImplJava userRepository = new UserRepositoryImplJava();
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
                LKMSConstantEnums.UserRole role = LKMSConstantEnums.UserRole.values()[
                    //Subtract by one since the first role (Lab Manager) start with 1
                    user.getRoleId() - 1
                ];
                userRoleTag.setRole(role);
            }
        });

        //Load user
        userProfileViewModel.loadUser(2);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Load user
        userProfileViewModel.reloadUser();
    }
}

