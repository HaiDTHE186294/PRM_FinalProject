package com.lkms.ui.user_profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
//import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.lkms.R;
import com.lkms.data.repository.enumPackage.java.LKMSConstantEnums;
import com.lkms.data.repository.implement.java.UserRepositoryImplJava;
import com.lkms.ui.user_profile.view.RoleTag;
import com.lkms.ui.user_profile.view.UserProfileHeader;
import com.lkms.ui.user_profile.viewmodel.UserProfileViewModel;
import com.lkms.ui.user_profile.viewmodel.factory.UserProfileViewModelFactory;

//For debugging
//import android.util.Log;

public class UserProfile extends AppCompatActivity {

    private UserProfileViewModel userProfileViewModel;
    private UserProfileHeader userProfileHeader;

    private TextView manageTeamOption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        //Get user's name and email field
        userProfileHeader = findViewById(R.id.user_profile_header);

        //region //OPTIONS

        //Profile setting option
        TextView profileSettingOption = findViewById(R.id.option_profile_setting);
        profileSettingOption.setOnClickListener(v -> {
            Intent intent = new Intent(UserProfile.this, ProfileSetting.class);

            //Passing user's id to ProfileSetting
            intent.putExtra("UserId", userProfileViewModel.getUser().getValue().getUserId());
            startActivity(intent);
        });

        //Lab Manager's manage team option
        manageTeamOption = findViewById(R.id.option_manage_team);
        manageTeamOption.setOnClickListener(v -> {
            Intent intent = new Intent(UserProfile.this, MemberList.class);

            //Passing user's id to ProfileSetting
            intent.putExtra("UserId", userProfileViewModel.getUser().getValue().getUserId());
            startActivity(intent);
        });


        //endregion

        //Setup ViewModel
        UserRepositoryImplJava userRepository = new UserRepositoryImplJava();
        UserProfileViewModelFactory factory = new UserProfileViewModelFactory(userRepository);
        userProfileViewModel = new ViewModelProvider(this, factory).get(UserProfileViewModel.class);

        // Observe. Execute ONLY when the data is ready.
        userProfileViewModel.getUser().observe(this, user -> {
            if (user != null)
            {
                userProfileHeader.setUser(user);

                //Get role
                LKMSConstantEnums.UserRole role = LKMSConstantEnums.UserRole.values()[
                    //Subtract by one since the first role (Lab Manager) start with 1
                    user.getRoleId() - 1
                ];

                //Update Manage Team option (depends on wether the user's role is Lab Manager or not)
                if (role == LKMSConstantEnums.UserRole.LAB_MANAGER)
                    manageTeamOption.setVisibility(View.VISIBLE);
                else
                    manageTeamOption.setVisibility(View.INVISIBLE);
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

