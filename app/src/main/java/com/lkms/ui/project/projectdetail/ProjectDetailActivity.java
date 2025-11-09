package com.lkms.ui.project.projectdetail;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.lkms.R;
import com.lkms.ui.project.projectmanage.ProjectViewModel;
import com.lkms.ui.project.projectmanage.ProjectViewModelFactory;

public class ProjectDetailActivity extends AppCompatActivity {

    public static final String PROJECT_ID_KEY = "PROJECT_ID";
    private int projectId;
    private ProjectViewModel viewModel;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_detail);

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, Math.max(systemBars.bottom, ime.bottom));
            return insets;
        });

        projectId = getIntent().getIntExtra(PROJECT_ID_KEY, -1);
        if (projectId == -1) {
            Toast.makeText(this, "Can't find project", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        ProjectViewModelFactory factory = new ProjectViewModelFactory();
        viewModel = new ViewModelProvider(this, factory).get(ProjectViewModel.class);

        setupToolbar();
        setupViewPagerAndTabs();

        viewModel.currentProjectDetails.observe(this, project -> {
            if (project != null) {
                getSupportActionBar().setTitle(project.getProjectTitle());
            }
        });
        viewModel.loadProjectDetailsScreen(projectId);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbarProjectDetail);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setupViewPagerAndTabs() {
        viewPager = findViewById(R.id.viewPagerProject);
        tabLayout = findViewById(R.id.tabLayoutProject);

        // 1. Tạo Adapter
        ProjectDetailAdapter adapter = new ProjectDetailAdapter(this, projectId);
        viewPager.setAdapter(adapter);

        // 2. Liên kết TabLout với ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Detail");
                    break;
                case 1:
                    tab.setText("Peer Review");
                    break;
                case 2:
                    tab.setText("Discussion");
                    break;
            }
        }).attach();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}