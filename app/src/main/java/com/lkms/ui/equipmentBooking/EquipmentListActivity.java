package com.lkms.ui.equipmentBooking;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lkms.R;
import com.lkms.data.repository.IEquipmentRepository;
import com.lkms.data.repository.implement.java.EquipmentRepositoryImplJava;
import com.lkms.domain.EquipmentBookingUseCase;

public class EquipmentListActivity extends AppCompatActivity {

    private RecyclerView rvEquipment;
    private EquipmentAdapter adapter;
    private EquipmentListViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equipment_list);

        rvEquipment = findViewById(R.id.rvEquipment);
        rvEquipment.setLayoutManager(new LinearLayoutManager(this));

        IEquipmentRepository repository = new EquipmentRepositoryImplJava();
        EquipmentBookingUseCase useCase = new EquipmentBookingUseCase(repository);

        // Khởi tạo ViewModel (không dùng DI ở đây - Không dùng DI)
        viewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @SuppressWarnings("unchecked")
            @Override
            public <T extends androidx.lifecycle.ViewModel> T create(Class<T> modelClass) {
                return (T) new EquipmentListViewModel(useCase);
            }
        }).get(EquipmentListViewModel.class);


        observeViewModel();
        viewModel.loadEquipmentList();
    }

    private void observeViewModel() {
        viewModel.equipmentList.observe(this, list -> {
            if (adapter == null) {
                adapter = new EquipmentAdapter(list, equipment ->
                        Toast.makeText(this,
                                "Chọn thiết bị: " + equipment.getEquipmentName(),
                                Toast.LENGTH_SHORT).show());
                rvEquipment.setAdapter(adapter);
            } else {
                adapter.notifyDataSetChanged(); // update nếu danh sách thay đổi
            }
        });

        viewModel.error.observe(this, message ->
                Toast.makeText(this, "Lỗi tải danh sách: " + message, Toast.LENGTH_LONG).show());
    }
}
