package com.lkms.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
//import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.lkms.R;
import com.lkms.data.repository.enumPackage.java.LKMSConstantEnums;
import com.lkms.domain.UserProfileUseCase;
import com.lkms.ui.user.view.UserProfileHeader;

//For debugging
//import android.util.Log;

public class UserProfileActivity extends AppCompatActivity {

    private UserProfileUseCase userProfileUseCase;

    private UserProfileHeader userProfileHeader;
    private TextView manageTeamOption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        userProfileUseCase = new UserProfileUseCase();

        //Get user's name and email field
        userProfileHeader = findViewById(R.id.user_profile_header);

        //Profile setting option
        TextView profileSettingOption = findViewById(R.id.option_profile_setting);
        profileSettingOption.setOnClickListener(v -> {
            Intent intent = new Intent(UserProfileActivity.this, ProfileSettingActivity.class);

            //Passing user's id to ProfileSetting
            intent.putExtra("UserId", userProfileUseCase.getUser().getValue().getUserId());
            startActivity(intent);
        });

        //Lab Manager's manage team option
        manageTeamOption = findViewById(R.id.option_manage_team);
        manageTeamOption.setOnClickListener(v -> {
            Intent intent = new Intent(UserProfileActivity.this, MemberListActivity.class);

            //Passing user's id to ProfileSetting
            intent.putExtra("UserId", userProfileUseCase.getUser().getValue().getUserId());
            startActivity(intent);
        });

        // Setup observer to observe async method to get user's data
        userProfileUseCase.getUser().observe(this, user -> {

//            Log.d("UserProfileActivityDEBUG", "Get user: " + user);

            if (user != null)
            {
                userProfileHeader.setUser(user);

                //Get role
                LKMSConstantEnums.UserRole role = LKMSConstantEnums.UserRole.values()[user.getRoleId()];

                //Update Manage Team option (depends on wether the user's role is Lab Manager or not)
                if (role == LKMSConstantEnums.UserRole.LAB_MANAGER)
                    manageTeamOption.setVisibility(View.VISIBLE);
                else
                    manageTeamOption.setVisibility(View.INVISIBLE);
            }
        });

        //Load user
        userProfileUseCase.loadUser(2);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Load user
        userProfileUseCase.reloadUser();
    }
}

