package com.lkms.ui.user.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.lkms.R;
import com.lkms.data.model.java.User;
import com.lkms.data.repository.enumPackage.java.LKMSConstantEnums.UserRole;

/**
 * A reusable custom view that displays a user's profile header information
 * (name, email, and role).
 */
public class UserProfileHeader extends LinearLayout {

    private TextView userName;
    private TextView userEmail;
    private RoleTag userRole;

    //region //Constructors
    public UserProfileHeader(Context context) {
        super(context);
        init(context);
    }

    public UserProfileHeader(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public UserProfileHeader(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }
    //endregion

    /**
     * Inflates the layout and finds the views.
     */
    private void init(Context context) {
        // Inflate the layout and attach it to this view
        LayoutInflater.from(context).inflate(R.layout.user_profile_header, this, true);

        // Find the child views from the inflated layout
        userName = findViewById(R.id.user_name);
        userEmail = findViewById(R.id.user_email);
        userRole = findViewById(R.id.user_role);
    }

    /**
     * Get the user object to display information
     *
     * @param user The user object containing the data to display.
     */
    public void setUser(User user) {
        if (user == null) {
            // Handle null user case, maybe hide the view or show placeholders
            userName.setText("User not found");
            userEmail.setText("");
            userRole.setRole(null);
            return;
        }

        userName.setText(user.getName());
        userEmail.setText(user.getEmail());

        userRole.setRole(UserRole.values()[user.getRoleId()]);
    }
}
