package com.lkms.ui.loginmaindashboard.maindashboard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.lkms.R;
import com.lkms.ui.loginmaindashboard.login.SystemLoginViewModel;
import com.lkms.ui.user_profile.MemberListActivity;

public class ManagerMainDashboardViewModel extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manager_main_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ✅ Ánh xạ Toolbar
        Toolbar toolbar = findViewById(R.id.manager_toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.managermenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_role) {
            startActivity(new Intent(this, MemberListActivity.class));
            Log.d("MENU_ACTION", "Navigated to MemberListActivity (Role Management)");
            return true;
        }

        if (id == R.id.menu_logout) {
            logout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        try {
            MasterKey masterKey = new MasterKey.Builder(getApplicationContext()).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build();

            SharedPreferences sharedPreferences = EncryptedSharedPreferences.create(getApplicationContext(), "secure_prefs", masterKey, EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove("jwt_token");
            editor.remove("user_role");
            editor.remove("user_id");
            editor.apply();

            Intent intent = new Intent(this, SystemLoginViewModel.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            finish();

        } catch (Exception e) {
            Log.e("LOGOUT_ERROR", "Logout failed: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi khi đăng xuất: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}