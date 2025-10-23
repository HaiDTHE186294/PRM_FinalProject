package com.lkms.ui.user_profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import com.lkms.R;
import com.lkms.data.model.java.User;
import com.lkms.data.repository.implement.java.UserRepositoryImplJava;
import com.lkms.ui.user_profile.viewmodel.MemberListViewModel;
import com.lkms.ui.user_profile.viewmodel.factory.MemberListViewModelFactory;

import java.util.ArrayList;
import java.util.List;

public class MemberList extends AppCompatActivity {

    private MemberListViewModel viewModel;
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

        //Setup ViewModel
        UserRepositoryImplJava userRepository = new UserRepositoryImplJava(); // You need to pass your Postgrest client
        MemberListViewModelFactory factory = new MemberListViewModelFactory(userRepository);
        viewModel = new ViewModelProvider(this, factory).get(MemberListViewModel.class);

        //Setup Observers for async stuff
        viewModel.getMemberList().observe(
            this,
            members -> {
                memberList.clear();
                memberList.addAll(members);
                memberAdapter.notifyDataSetChanged();
        });

        //Load Data
        viewModel.loadAllUsers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.loadAllUsers();
    }
}
