package com.lkms.ui.user_profile.view;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;

import com.lkms.R;
import com.lkms.data.repository.enumPackage.java.LKMSConstantEnums.UserRole;

//Displaying Roles as tag with name and unique color.
//Can be reuse for display user's role on User's profile panel
//and Lab Manager's user list
//Adding new role in the future should be easier with this
public class RoleTag extends AppCompatTextView {

//    String[] roleNames = {"Lab Manager", "Researcher", "Technician"};
//    int[] backgroundColors = {
//        ContextCompat.getColor(getContext(), R.color.role_lab_mng),
//        ContextCompat.getColor(getContext(), R.color.role_researcher),
//        ContextCompat.getColor(getContext(), R.color.role_technician)
//    };

    //region // Constructors required to support XML
    public RoleTag(@NonNull Context context) {
        super(context);
        init();
    }

    public RoleTag(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RoleTag(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    //endregion

    /**
     * Common initialization for the view.
     */
    private void init() {
        // Set default visual properties that will always apply to this tag
        setTextSize(12); // Example: set a consistent text size
        setPadding(dpToPx(12), dpToPx(4), dpToPx(12), dpToPx(4));
        setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));
//        setVisibility(com.google.ar.imp.view.View.GONE); // Hide by default until a role is set
    }

    /**
     * The main public method to set the role for this view.
     * This will automatically update the text and background color.
     *
     * @param role The UserRole enum value.
     */
    public void setRole(UserRole role) {
        if (role == null) {
            return;
        }

        String roleName = "UNKNOW";
        int backgroundColor = ContextCompat.getColor(getContext(), android.R.color.darker_gray);

        switch (role) {
            case LAB_MANAGER:
                roleName = "Lab Manager";
                backgroundColor = ContextCompat.getColor(getContext(), R.color.role_lab_mng);
                break;
            case RESEARCHER:
                roleName = "Researcher";
                backgroundColor = ContextCompat.getColor(getContext(), R.color.role_researcher);
                break;
            case TECHNICIAN:
                roleName = "Technician";
                backgroundColor = ContextCompat.getColor(getContext(), R.color.role_technician);
                break;
        }

        // Set the properties
        setText(roleName);
        setBackgroundColor(backgroundColor);
    }

    /**
     * Helper method to convert dp to pixels.
     */
    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}
