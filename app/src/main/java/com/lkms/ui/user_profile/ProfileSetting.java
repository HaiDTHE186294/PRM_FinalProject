package com.lkms.ui.user_profile;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.lkms.R;
import com.lkms.data.model.java.User;
import com.lkms.data.repository.implement.java.UserRepositoryImplJava;
import com.lkms.ui.user_profile.viewmodel.UserProfileViewModel;
import com.lkms.ui.user_profile.viewmodel.factory.UserProfileViewModelFactory;

public class ProfileSetting extends AppCompatActivity {

    private UserProfileViewModel userProfileViewModel;

    TextInputEditText nameEditText;
    TextInputEditText contactInfoEditText;

    Button saveButton;
    Button cancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile_setting);

        //Editable text field
        nameEditText = findViewById(R.id.name_edit_text);
        contactInfoEditText = findViewById(R.id.contact_info_edit_text);

        //Setup ViewModel
        UserRepositoryImplJava userRepository = new UserRepositoryImplJava();
        UserProfileViewModelFactory factory = new UserProfileViewModelFactory(userRepository);
        userProfileViewModel = new ViewModelProvider(this, factory).get(UserProfileViewModel.class);

        // Observe. Execute ONLY when the data is ready.
        userProfileViewModel.getUser().observe(this, user -> {
            if (user != null)
            {
                nameEditText.setText(user.getName());
                contactInfoEditText.setText(user.getContactInfo());
            }
        });

        //Buttons
        saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(v -> {
            if (userProfileViewModel.getUser().getValue() == null){
                Toast.makeText(this, "Cannot save, user data not loaded.", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                userProfileViewModel.updateUser(
                        nameEditText.getText().toString(),
                        contactInfoEditText.getText().toString()
                );
                Toast.makeText(this, "Saved Successfully!", Toast.LENGTH_SHORT).show();
            }
            catch (Exception e)
            {
                Toast.makeText(this, "Something went wrong behind the scene", Toast.LENGTH_SHORT).show();
                Log.e("ProfileSettingERROR", "An error occurred while updating user:" + e.getMessage());
            }

            finish();
        });

        cancelButton = findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(v -> {
            finish();
        });

        //Load user
        int userId = getIntent().getIntExtra("UserId", -1);
        if (userId == -1) {
            Toast.makeText(this, "Error: User ID not found.", Toast.LENGTH_SHORT).show();
            finish();
        }
        else
            userProfileViewModel.loadUser(userId);
    }
}