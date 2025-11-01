package com.lkms.ui.project.projectdetail;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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

        projectId = getIntent().getIntExtra(PROJECT_ID_KEY, -1);
        if (projectId == -1) {
            Toast.makeText(this, "Lỗi: Không tìm thấy Project ID", Toast.LENGTH_LONG).show();
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
        onBackPressed(); // Xử lý khi nhấn nút Back trên Toolbar
        return true;
    }
}