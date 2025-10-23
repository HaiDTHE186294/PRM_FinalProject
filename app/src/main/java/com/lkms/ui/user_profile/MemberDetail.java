package com.lkms.ui.user_profile;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.lkms.R;
import com.lkms.data.repository.enumPackage.java.LKMSConstantEnums;
import com.lkms.data.repository.implement.java.UserRepositoryImplJava;
import com.lkms.ui.user_profile.view.UserProfileHeader;
import com.lkms.ui.user_profile.viewmodel.UserProfileViewModel;
import com.lkms.ui.user_profile.viewmodel.factory.UserProfileViewModelFactory;

public class MemberDetail extends AppCompatActivity {

    private UserProfileViewModel userProfileViewModel;
    private UserProfileHeader userProfileHeader;
    private RadioButton labManagerRadioButton, researcherRadioButton, technicianRadioButton;
    private Button confirmButton, cancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_member_detail);

        //Get header
        userProfileHeader = findViewById(R.id.user_profile_header);

        //Get radio buttons
        labManagerRadioButton = findViewById(R.id.radio_button_lab_mng);
        researcherRadioButton = findViewById(R.id.radio_button_researcher);
        technicianRadioButton = findViewById(R.id.radio_button_technician);

        //Get buttons
        confirmButton = findViewById(R.id.save_button);
        confirmButton.setOnClickListener(v -> {
            if (userProfileViewModel.getUser().getValue() == null){
                Toast.makeText(this, "Cannot save, user data not loaded.", Toast.LENGTH_SHORT).show();
                return;
            }

            try {

                LKMSConstantEnums.UserRole role = LKMSConstantEnums.UserRole.TECHNICIAN;
                if (labManagerRadioButton.isChecked())
                    role = LKMSConstantEnums.UserRole.LAB_MANAGER;
                else if (researcherRadioButton.isChecked())
                    role = LKMSConstantEnums.UserRole.RESEARCHER;

                userProfileViewModel.updateUserRole(role);
                Toast.makeText(this, "Role updated successfully!", Toast.LENGTH_SHORT).show();
            }
            catch (Exception e)
            {
                Toast.makeText(this, "Something went wrong behind the scene", Toast.LENGTH_SHORT).show();
                Log.e("MemberDetailERROR", "An error occurred while updating user:" + e.getMessage());
            }

            finish();
        });

        cancelButton = findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(v -> {
            finish();
        });


        //Setup ViewModel
        UserRepositoryImplJava userRepository = new UserRepositoryImplJava();
        UserProfileViewModelFactory factory = new UserProfileViewModelFactory(userRepository);
        userProfileViewModel = new ViewModelProvider(this, factory).get(UserProfileViewModel.class);

        // Observe async stuff
        userProfileViewModel.getUser().observe(this, user -> {
            if (user == null)
                return;

            userProfileHeader.setUser(user);

            switch (LKMSConstantEnums.UserRole.values()[user.getRoleId() - 1])
            {
                case LAB_MANAGER:
                    labManagerRadioButton.setChecked(true);
                    break;
                case RESEARCHER:
                    researcherRadioButton.setChecked(true);
                    break;
                case TECHNICIAN:
                    technicianRadioButton.setChecked(true);
                    break;
            }
        });

        //Load user
        userProfileViewModel.loadUser(getIntent().getIntExtra("MemberId", -1));
    }
}