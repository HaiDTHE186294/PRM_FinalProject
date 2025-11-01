package com.lkms.ui.project.projectdetail;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.lkms.ui.project.peerreview.ProjectPeerReviewFragment;

// import com.lkms.ui.project.tabs.ProjectDiscussionFragment; // Sẽ tạo sau
// import com.lkms.ui.project.tabs.ProjectPeerReviewFragment; // Sẽ tạo sau
// KHÔNG CẦN import ProjectInfoFragment vì nó cùng package

public class ProjectDetailAdapter extends FragmentStateAdapter {

    private final int projectId;
    private static final int NUM_TABS = 3;

    public ProjectDetailAdapter(@NonNull FragmentActivity fragmentActivity, int projectId) {
        super(fragmentActivity);
        this.projectId = projectId;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return ProjectInfoFragment.newInstance(projectId);
            case 1:
                return ProjectPeerReviewFragment.newInstance(projectId);
            case 2:
                return new Fragment(); // TẠM THỜI
            default:
                return new Fragment();
        }
    }

    @Override
    public int getItemCount() {
        return NUM_TABS;
    }
}