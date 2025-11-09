package com.lkms.ui.equipment;

import android.app.DatePickerDialog;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.lkms.R;
import com.lkms.data.model.java.Experiment;
// BỎ import các repository không cần thiết
// import com.lkms.data.repository.IExperimentRepository;
// import com.lkms.data.repository.implement.java.ExperimentRepositoryImplJava;
import com.lkms.util.AuthHelper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class BookingActivity extends AppCompatActivity {

    public static final String EXTRA_EQUIPMENT_ID = "EQUIPMENT_ID";
    public static final String EXTRA_EQUIPMENT_NAME = "EQUIPMENT_NAME";

    private int equipmentId;
    private String equipmentName;

    private BookingViewModel viewModel; // Khai báo

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

        int userId = AuthHelper.getLoggedInUserId(getApplicationContext());
        if (userId == -1) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        BookingViewModelFactory factory = new BookingViewModelFactory(userId);
        this.viewModel = new ViewModelProvider(this, factory).get(BookingViewModel.class);

        initUI();
        setupViewModel();
        setupActions();
        this.viewModel.loadExperiments();

        startBookingRefresh();
    }

    private void initUI() {
        equipmentId = getIntent().getIntExtra(EXTRA_EQUIPMENT_ID, -1);
        equipmentName = getIntent().getStringExtra(EXTRA_EQUIPMENT_NAME);

        ((TextView) findViewById(R.id.tvBookingTitle))
                .setText("Booking for: " + equipmentName);

        tvStartDate = findViewById(R.id.tvStartDate);
        tvEndDate = findViewById(R.id.tvEndDate);
        btnSelectStartDate = findViewById(R.id.btnSelectStartDate);
        btnSelectEndDate = findViewById(R.id.btnSelectEndDate);
        btnBook = findViewById(R.id.btnBook);
        spinnerExperiment = findViewById(R.id.spinnerExperiment);
        calendarView = findViewById(R.id.calendarView);
        viewModel.setEquipmentId(equipmentId);
    }

    private void setupViewModel() {
               viewModel.loadBookedDays();

        viewModel.startDate.observe(this, date ->
                tvStartDate.setText(date != null ? date.toString() : "")
        );

        viewModel.endDate.observe(this, date ->
                tvEndDate.setText(date != null ? date.toString() : "")
        );

        viewModel.experiments.observe(this, this::setupExperimentSpinner);

        viewModel.bookedDays.observe(this, this::highlightBookedDates);

        viewModel.bookingResult.observe(this, success ->
                Toast.makeText(this,
                        success ? "Booking Success!" : "Booking Fail!",
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

    private void setupExperimentSpinner(List<Experiment> list) {
        if (list == null || list.isEmpty()) {
            Toast.makeText(this, "No ongoing experiments found.", Toast.LENGTH_LONG).show();
            return;
        }

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
                if (pos >= 0 && pos < experiments.size()) {
                    viewModel.selectExperiment(experiments.get(pos).getExperimentId());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }

        });

        if (!experiments.isEmpty()) {
            viewModel.selectExperiment(experiments.get(0).getExperimentId());
        }
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
                        Toast.makeText(this, "This date is booked!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (selected.isBefore(LocalDate.now())) {
                        Toast.makeText(this, "Cannot select past date!", Toast.LENGTH_SHORT).show();
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
            calendarView.setDisabledDays(new ArrayList<>());
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

    private void startBookingRefresh() {
        final Handler handler = new Handler(Looper.getMainLooper());
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                viewModel.loadBookedDays();
                handler.postDelayed(this, 3000);
            }
        };
        handler.post(runnable);
    }
}