package com.lkms.ui.equipment;

import android.app.DatePickerDialog;
import android.os.Build;
import android.os.Bundle;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.lkms.R;
import com.lkms.data.model.java.Experiment;
import com.lkms.data.repository.IExperimentRepository;
import com.lkms.data.repository.implement.java.ExperimentRepositoryImplJava;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class BookingActivity extends AppCompatActivity {

    public static final String EXTRA_EQUIPMENT_ID = "EQUIPMENT_ID";
    public static final String EXTRA_EQUIPMENT_NAME = "EQUIPMENT_NAME";

    private int equipmentId;
    private String equipmentName;

    private BookingViewModel viewModel;

    private Button btnSelectStartDate;
    private Button btnSelectEndDate;
    private Button btnBook;
    private TextView tvStartDate, tvEndDate;
    private Spinner spinnerExperiment;
    private CalendarView calendarView;

    private List<Experiment> experiments = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        initUI();
        setupViewModel();
        setupActions();
        setupExperimentSpinner();
    }

    private void initUI() {
        equipmentId = getIntent().getIntExtra(EXTRA_EQUIPMENT_ID, -1);
        equipmentName = getIntent().getStringExtra(EXTRA_EQUIPMENT_NAME);

        ((TextView) findViewById(R.id.tvBookingTitle))
                .setText("Đặt lịch cho: " + equipmentName);

        tvStartDate = findViewById(R.id.tvStartDate);
        tvEndDate = findViewById(R.id.tvEndDate);
        btnSelectStartDate = findViewById(R.id.btnSelectStartDate);
        btnSelectEndDate = findViewById(R.id.btnSelectEndDate);
        btnBook = findViewById(R.id.btnBook);
        spinnerExperiment = findViewById(R.id.spinnerExperiment);
        calendarView = findViewById(R.id.calendarView);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(BookingViewModel.class);
        viewModel.setEquipmentId(equipmentId);
        viewModel.loadBookedDays();

        viewModel.startDate.observe(this, date ->
                tvStartDate.setText(date != null ? date.toString() : "")
        );

        viewModel.endDate.observe(this, date ->
                tvEndDate.setText(date != null ? date.toString() : "")
        );

        viewModel.bookedDays.observe(this, this::highlightBookedDates);

        viewModel.bookingResult.observe(this, success ->
                Toast.makeText(this,
                        success ? "Đặt lịch thành công!" : "Đặt lịch thất bại!",
                        Toast.LENGTH_SHORT).show()
        );

        viewModel.error.observe(this, msg -> {
            if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        });
    }

    private void setupActions() {
        btnSelectStartDate.setOnClickListener(v -> showCalendar(true));
        btnSelectEndDate.setOnClickListener(v -> showCalendar(false));
        btnBook.setOnClickListener(v -> viewModel.bookEquipment());
    }

    private void setupExperimentSpinner() {
        IExperimentRepository repo = new ExperimentRepositoryImplJava();
        int userId = 1;

        repo.getOngoingExperiments(userId, new IExperimentRepository.ExperimentListCallback() {
            @Override
            public void onSuccess(List<Experiment> list) {
                runOnUiThread(() -> {
                    experiments = list;
                    ArrayAdapter<Experiment> adapter = new ArrayAdapter<>(
                            BookingActivity.this,
                            android.R.layout.simple_spinner_item,
                            experiments
                    );
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerExperiment.setAdapter(adapter);

                    spinnerExperiment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, android.view.View view, int pos, long id) {
                            viewModel.selectExperiment(experiments.get(pos).getExperimentId());
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                            // intentionally empty
                        }
                    });
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> Toast.makeText(
                        BookingActivity.this,
                        "Lỗi tải Experiment: " + message,
                        Toast.LENGTH_LONG).show()
                );
            }
        });
    }

    private void showCalendar(boolean isStart) {
        List<LocalDate> blocked = viewModel.bookedDays.getValue();
        Calendar now = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, day) -> {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

                    LocalDate selected = LocalDate.of(year, month + 1, day);

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

        dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        dialog.show();
    }

    private void highlightBookedDates(List<LocalDate> days) {
        if (days == null || days.isEmpty()) {
            calendarView.setEvents(new ArrayList<>());
            return;
        }

        List<EventDay> events = new ArrayList<>();
        List<Calendar> disabled = new ArrayList<>();

        for (LocalDate ld : days) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) continue;

            Calendar cal = Calendar.getInstance();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                cal.set(ld.getYear(), ld.getMonthValue() - 1, ld.getDayOfMonth());
            }

            events.add(new EventDay(cal, R.drawable.ic_dot));
            disabled.add(cal);
        }

        calendarView.setEvents(events);
        calendarView.setDisabledDays(disabled);
    }
}
