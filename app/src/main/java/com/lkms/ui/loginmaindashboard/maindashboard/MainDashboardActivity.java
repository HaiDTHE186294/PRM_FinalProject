package com.lkms.ui.loginmaindashboard.maindashboard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.lkms.R;
import com.lkms.data.model.java.BookingDisplay;
import com.lkms.data.model.java.Experiment;
import com.lkms.data.model.java.InventoryDisplayItem;
import com.lkms.data.repository.IEquipmentRepository;
import com.lkms.data.repository.implement.java.ExperimentRepositoryImplJava;
import com.lkms.data.repository.implement.java.InventoryRepositoryImplJava;
import com.lkms.domain.loginmaindashboardusecase.MainDashboardUseCase;
import com.lkms.ui.equipment.EquipmentListActivity;
import com.lkms.ui.inventory.InventoryActivity;
import com.lkms.ui.loginmaindashboard.login.SystemLoginActivity;
import com.lkms.ui.protocol.ProtocolListActivity;
import com.lkms.ui.sds.SdsLookupActivity;
import com.lkms.ui.user_profile.MemberListActivity;
import com.lkms.ui.user_profile.UserProfileActivity;

import java.util.ArrayList;
import java.util.List;

public class MainDashboardActivity extends AppCompatActivity {

    private RecyclerView recyclerExperiments, recyclerAlerts, recyclerBookings;
    private OngoingExperimentAdapter experimentAdapter;
    private InventoryAlertAdapter alertAdapter;
    private BookingAdapter bookingAdapter;

    private List<Experiment> experimentList = new ArrayList<>();
    private List<InventoryDisplayItem> alertList = new ArrayList<>();
    private List<BookingDisplay> bookingList = new ArrayList<>();
    private MainDashboardUseCase useCase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Gắn Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Khởi tạo UseCase
        useCase = new MainDashboardUseCase();

        // === RecyclerView cho Experiments ===
        recyclerExperiments = findViewById(R.id.recyclerExperiments);
        recyclerExperiments.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        experimentAdapter = new OngoingExperimentAdapter(this, experimentList);
        recyclerExperiments.setAdapter(experimentAdapter);

        // === RecyclerView cho Inventory Alerts ===
        recyclerAlerts = findViewById(R.id.recyclerAlerts);
        recyclerAlerts.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        alertAdapter = new InventoryAlertAdapter(alertList);
        recyclerAlerts.setAdapter(alertAdapter);

        // === RecyclerView cho Bookings ===
        recyclerBookings = findViewById(R.id.recyclerBookings);
        recyclerBookings.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        bookingAdapter = new BookingAdapter(this, bookingList);
        recyclerBookings.setAdapter(bookingAdapter);


        // === Load dữ liệu ===
        loadOngoingExperiments();
        loadInventoryAlerts();
        loadBookings();
    }

    private void loadOngoingExperiments() {
        int userId = getUserIdFromSecurePrefs();
        //android.util.Log.d("USER_CHECK", "User ID from EncryptedSharedPrefs: " + userId);
        if (userId == -1) {
            Toast.makeText(this, "Không tìm thấy userId — vui lòng đăng nhập lại!", Toast.LENGTH_SHORT).show();
            return;
        }
        useCase.getOngoingExperiments(userId, new ExperimentRepositoryImplJava.ExperimentListCallback() {
            @Override
            public void onSuccess(List<Experiment> experiments) {
                runOnUiThread(() -> {
                    experimentList.clear();
                    experimentList.addAll(experiments);
                    experimentAdapter.notifyDataSetChanged();
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() ->
                        Toast.makeText(MainDashboardActivity.this, "Lỗi tải dữ liệu: " + message, Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void loadInventoryAlerts() {
        useCase.getAllInventoryItems(new InventoryRepositoryImplJava.InventoryDisplayListCallback() {
            @Override
            public void onSuccess(List<InventoryDisplayItem> displayItems) {
                runOnUiThread(() -> {
                    alertList.clear();
                    alertList.addAll(displayItems);
                    alertAdapter.notifyDataSetChanged();
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() ->
                        Toast.makeText(MainDashboardActivity.this, "Lỗi tải tồn kho: " + message, Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void loadBookings() {
        int userId = getUserIdFromSecurePrefs();
        //android.util.Log.d("USER_CHECK1", "User ID from EncryptedSharedPrefs: " + userId);
        if (userId == -1) {
            Toast.makeText(this, "Không tìm thấy userId — vui lòng đăng nhập lại!", Toast.LENGTH_SHORT).show();
            return;
        }
        useCase.getUpcomingEquipmentBookings(userId, new IEquipmentRepository.BookingDisplayListCallback() {
            @Override
            public void onSuccess(List<BookingDisplay> bookings) {
                runOnUiThread(() -> {
                    bookingList.clear();
                    bookingList.addAll(bookings);
                    bookingAdapter.notifyDataSetChanged();
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() ->
                        Toast.makeText(MainDashboardActivity.this, "Lỗi tải booking: " + message, Toast.LENGTH_SHORT).show()
                );
            }
        });
    }


    private int getUserIdFromSecurePrefs() {
        try {
            MasterKey masterKey = new MasterKey.Builder(getApplicationContext()).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build();

            SharedPreferences sharedPreferences = EncryptedSharedPreferences.create(getApplicationContext(), "secure_prefs", masterKey, EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);

            return sharedPreferences.getInt("user_id", -1); // -1 nếu chưa lưu
        } catch (Exception e) {
            Log.d("get userId failed:", e.toString() );
            return -1;
        }
    }

    private int getRoleIdFromSecurePrefs() {
        try {
            MasterKey masterKey = new MasterKey.Builder(getApplicationContext()).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build();

            SharedPreferences sharedPreferences = EncryptedSharedPreferences.create(getApplicationContext(), "secure_prefs", masterKey, EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);

            return sharedPreferences.getInt("user_role", -1); // Trả về -1 nếu chưa lưu
        } catch (Exception e) {
            Log.e("SecurePrefs", "Lỗi khi lấy user_role: " + e);
            return -1;
        }
    }



    // Hiển thị menu trên Toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        int roleId = getRoleIdFromSecurePrefs();

        if (roleId == 2) {
            menu.findItem(R.id.menu_project).setVisible(false);
            menu.findItem(R.id.menu_new_experiment).setVisible(false);
        }

        if (roleId != 0) {
            menu.findItem(R.id.menu_approve).setVisible(false);
            menu.findItem(R.id.menu_role).setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_user_profile) {
            Intent intent = new Intent(this, UserProfileActivity.class);
            startActivity(intent);
            Log.d("MENU_ACTION", "Navigated to UserProfileActivity");
            return true;
        }

        if (id == R.id.menu_protocol) {
            startActivity(new Intent(this, ProtocolListActivity.class));
            Log.d("MENU_ACTION", "Navigated to ProtocolListActivity");
            return true;
        }

        if (id == R.id.menu_equipment) {
            startActivity(new Intent(this, EquipmentListActivity.class));
            Log.d("MENU_ACTION", "Navigated to EquipmentListActivity");
            return true;
        }

        if (id == R.id.menu_logout) {
            logout();
            return true;
        }

        if (id == R.id.menu_sds) {
            startActivity(new Intent(this, SdsLookupActivity.class));
            Log.d("MENU_ACTION", "Navigated to SDS");
            return true;
        }

        if (id == R.id.menu_role) {
            startActivity(new Intent(this, MemberListActivity.class));
            Log.d("MENU_ACTION", "Navigated to MemberListActivity");
            return true;
        }

        if (id == R.id.menu_inventory) {
            startActivity(new Intent(this, InventoryActivity.class));
            Log.d("MENU_ACTION", "Navigated to InventoryActivity");
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
            editor.clear();
            editor.apply();

            Toast.makeText(this, "Đăng xuất thành công!", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(this, SystemLoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            finish();

        } catch (Exception e) {
            Log.e("LOGOUT_ERROR", "Logout failed: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi khi đăng xuất: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

}