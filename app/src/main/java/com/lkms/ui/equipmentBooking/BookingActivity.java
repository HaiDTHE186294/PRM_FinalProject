package com.lkms.ui.equipmentBooking;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import android.app.DatePickerDialog;
import android.view.ViewGroup;


import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.lkms.R;
import com.lkms.data.model.java.Experiment;
import com.lkms.data.repository.IExperimentRepository;
import com.lkms.data.repository.implement.java.ExperimentRepositoryImplJava;

import java.time.LocalDate;

public class BookingActivity extends AppCompatActivity {

    public static final String EXTRA_EQUIPMENT_ID = "EQUIPMENT_ID";
    public static final String EXTRA_EQUIPMENT_NAME = "EQUIPMENT_NAME";

    private int equipmentId;
    private String equipmentName;

    private BookingViewModel viewModel;

    private Button btnSelectStartDate, btnSelectEndDate, btnBook;
    private TextView tvStartDate, tvEndDate;
    private Spinner spinnerExperiment;

    private List<Experiment> experiments; // danh sách experiment load từ repo

    CalendarView calendarView;
    List<Calendar> bookedDates = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        equipmentId = getIntent().getIntExtra(EXTRA_EQUIPMENT_ID, -1);
        equipmentName = getIntent().getStringExtra(EXTRA_EQUIPMENT_NAME);

        tvStartDate = findViewById(R.id.tvStartDate);
        tvEndDate = findViewById(R.id.tvEndDate);

        btnSelectStartDate = findViewById(R.id.btnSelectStartDate);
        btnSelectEndDate = findViewById(R.id.btnSelectEndDate);
        btnBook = findViewById(R.id.btnBook);
        spinnerExperiment = findViewById(R.id.spinnerExperiment);

        TextView tvTitle = findViewById(R.id.tvBookingTitle);
        tvTitle.setText("Đặt lịch cho: " + equipmentName);

        viewModel = new ViewModelProvider(this).get(BookingViewModel.class);
        viewModel.setEquipmentId(equipmentId);
        viewModel.loadBookedDays();

        observeViewModel();
        setupExperimentSpinner();

        btnSelectStartDate.setOnClickListener(v -> showCalendar(true));
        btnSelectEndDate.setOnClickListener(v -> showCalendar(false));
        btnBook.setOnClickListener(v -> viewModel.bookEquipment());

        calendarView = findViewById(R.id.calendarView);

    }

    private void observeViewModel() {
        viewModel.startDate.observe(this, date -> {
            if (date != null) tvStartDate.setText(date.toString());
        });

        viewModel.endDate.observe(this, date -> {
            if (date != null) tvEndDate.setText(date.toString());
        });

        viewModel.bookingResult.observe(this, success -> {
            if (success) Toast.makeText(this, "Đặt lịch thành công!", Toast.LENGTH_SHORT).show();
            else Toast.makeText(this, "Đặt lịch thất bại!", Toast.LENGTH_SHORT).show();
        });

        viewModel.error.observe(this, msg -> {
            if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        });

        viewModel.bookedDays.observe(this, list -> {
            if (list != null) {
                highlightBookedDates(list);
            } else {
                calendarView.setEvents(new ArrayList<>());
            }
        });

    }


    private void setupExperimentSpinner() {
        IExperimentRepository experimentRepo = new ExperimentRepositoryImplJava(); // hoặc repo của bạn
        int userId = 1; // hoặc lấy từ session/user hiện tại

        experimentRepo.getOngoingExperiments(userId, new IExperimentRepository.ExperimentListCallback() {
            @Override
            public void onSuccess(List<Experiment> experimentList) {
                runOnUiThread(() -> {
                    experiments = experimentList;

                    ArrayAdapter<Experiment> adapter = new ArrayAdapter<>(BookingActivity.this,
                            android.R.layout.simple_spinner_item, experiments);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerExperiment.setAdapter(adapter);

                    spinnerExperiment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            Experiment selected = experiments.get(position);
                            viewModel.selectExperiment(selected.getExperimentId());
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {}
                    });
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> Toast.makeText(BookingActivity.this,
                        "Lỗi khi tải danh sách experiment: " + errorMessage, Toast.LENGTH_LONG).show());
            }
        });
    }


    private void showCalendar(boolean isStart) {
        List<LocalDate> blocked = viewModel.bookedDays.getValue();

        Calendar now = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, day) -> {
                    LocalDate selected = null;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        selected = LocalDate.of(year, month + 1, day);
                    }

                    if (blocked != null && blocked.contains(selected)) {
                        Toast.makeText(this, "Ngày này đã được đặt!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (isStart) viewModel.selectStartDate(selected);
                    else viewModel.selectEndDate(selected);
                },
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );

        // ✅ Không cho chọn ngày quá khứ
        dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);

        dialog.show();
    }


    private void highlightBookedDates(List<java.time.LocalDate> dates) {
        if (dates == null || dates.isEmpty()) {
            calendarView.setEvents(new ArrayList<>()); // xoá event hiện có
            return;
        }

        List<EventDay> events = new ArrayList<>();
        List<Calendar> disabledDays = new ArrayList<>();

        for (java.time.LocalDate ld : dates) {
            Calendar cal = Calendar.getInstance();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                cal.set(ld.getYear(), ld.getMonthValue() - 1, ld.getDayOfMonth());
            }

            // EventDay hiển thị icon (dot). Dùng drawable bạn tạo ic_dot
            events.add(new EventDay(cal, R.drawable.ic_dot));

            // Nếu muốn disable (không cho chọn) cùng lúc
            disabledDays.add(cal);
        }

        // Hiển thị dot (events)
        calendarView.setEvents(events);

        // Nếu muốn disable ngày (không cho chọn) — phương thức tồn tại trong Applandeo
        calendarView.setDisabledDays(disabledDays);
    }



}
