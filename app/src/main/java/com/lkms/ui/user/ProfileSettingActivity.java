package com.lkms.ui.user;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.lkms.R;
import com.lkms.domain.UserProfileUseCase;

public class ProfileSettingActivity extends AppCompatActivity {

    private final UserProfileUseCase userProfileUseCase = new UserProfileUseCase();

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

        // Observe. Execute ONLY when the data is ready.
        userProfileUseCase.getUser().observe(this, user -> {
            if (user != null)
            {
                nameEditText.setText(user.getName());
                contactInfoEditText.setText(user.getContactInfo());
            }
        });

        //Buttons
        saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(v -> {
            if (userProfileUseCase.getUser().getValue() == null){
                Toast.makeText(this, "Cannot save, user data not loaded.", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                userProfileUseCase.updateUser(
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
            userProfileUseCase.loadUser(userId);
    }
}