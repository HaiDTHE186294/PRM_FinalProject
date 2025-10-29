package com.lkms.ui.user;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.lkms.R;
import com.lkms.data.repository.enumPackage.java.LKMSConstantEnums;
import com.lkms.domain.UserProfileUseCase;
import com.lkms.ui.user.view.UserProfileHeader;

public class MemberDetailActvity extends AppCompatActivity {

    private UserProfileUseCase userProfileUseCase = new UserProfileUseCase();
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
            if (userProfileUseCase.getUser().getValue() == null){
                Toast.makeText(this, "Cannot save, user data not loaded.", Toast.LENGTH_SHORT).show();
                return;
            }

            try {

                LKMSConstantEnums.UserRole role = LKMSConstantEnums.UserRole.TECHNICIAN;
                if (labManagerRadioButton.isChecked())
                    role = LKMSConstantEnums.UserRole.LAB_MANAGER;
                else if (researcherRadioButton.isChecked())
                    role = LKMSConstantEnums.UserRole.RESEARCHER;

                userProfileUseCase.updateUserRole(role);
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


        // Observe async stuff
        userProfileUseCase.getUser().observe(this, user -> {
            if (user == null)
                return;

            userProfileHeader.setUser(user);

            //TODO: Softcode this
            switch (LKMSConstantEnums.UserRole.values()[user.getRoleId()])
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
        userProfileUseCase.loadUser(getIntent().getIntExtra("MemberId", -1));
    }
}