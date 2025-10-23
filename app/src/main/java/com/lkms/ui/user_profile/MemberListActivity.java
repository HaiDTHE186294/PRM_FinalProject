package com.lkms.ui.user_profile;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.lkms.R;
import com.lkms.data.model.java.User;
import com.lkms.domain.MemberManagementUseCase;

import java.util.ArrayList;
import java.util.List;

public class MemberListActivity extends AppCompatActivity {

    private MemberManagementUseCase memberManagementUseCase = new MemberManagementUseCase();
    private RecyclerView memberRecyclerView;
    private MemberAdapter memberAdapter;
    private List<User> memberList = new ArrayList<>(); // To store member data

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_list);

        // Initialize RecyclerView
        memberRecyclerView = findViewById(R.id.member_recycler_view);

        // Setup the adapter
        memberAdapter = new MemberAdapter(memberList);
        memberRecyclerView.setAdapter(memberAdapter);

        //Setup Observers for async stuff
        memberManagementUseCase.getAllMembers().observe(
            this,
            members -> {
                memberList.clear();
                memberList.addAll(members);
                memberAdapter.notifyDataSetChanged();
        });

        //Load Data
        memberManagementUseCase.loadAllMembers();
    }

    @Override
    protected void onResume() {
        super.onResume();

        memberManagementUseCase.loadAllMembers();
    }
}
