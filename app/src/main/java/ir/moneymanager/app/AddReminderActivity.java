package ir.moneymanager.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

import ir.moneymanager.app.db.AppDatabase;
import ir.moneymanager.app.db.ReminderEntity;
import ir.moneymanager.app.util.PersianUtils;

public class AddReminderActivity extends AppCompatActivity {

    private EditText etTitle;
    private Button btnPickDate, btnPickTime;

    private int selectedYear, selectedMonth, selectedDay;
    private int selectedHour = 9, selectedMinute = 0;
    private boolean dateChosen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_reminder);

        etTitle = findViewById(R.id.etReminderTitle);
        btnPickDate = findViewById(R.id.btnPickDate);
        btnPickTime = findViewById(R.id.btnPickTime);
        Button btnSave = findViewById(R.id.btnSaveReminder);
        Button btnCancel = findViewById(R.id.btnCancelReminder);

        int[] today = PersianUtils.todayJalali();
        selectedYear = today[0];
        selectedMonth = today[1];
        selectedDay = today[2];

        btnPickDate.setOnClickListener(v -> pickDate());
        btnPickTime.setOnClickListener(v -> pickTime());
        btnCancel.setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            if (TextUtils.isEmpty(title)) {
                Toast.makeText(this, R.string.amount_required, Toast.LENGTH_SHORT).show();
                return;
            }
            if (!dateChosen) {
                Toast.makeText(this, R.string.pick_date, Toast.LENGTH_SHORT).show();
                return;
            }

            long triggerAt = PersianUtils.jalaliToMillis(selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute);

            AppDatabase db = AppDatabase.getInstance(this);
            AppDatabase.databaseWriteExecutor.execute(() -> {
                ReminderEntity entity = new ReminderEntity(title, triggerAt);
                long insertedId = db.reminderDao().insert(entity);

                scheduleAlarm((int) insertedId, title, triggerAt);

                runOnUiThread(() -> {
                    Toast.makeText(this, R.string.reminder_saved, Toast.LENGTH_SHORT).show();
                    finish();
                });
            });
        });
    }

    private void pickDate() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_persian_date_picker, null);
        NumberPicker npYear = view.findViewById(R.id.npYear);
        NumberPicker npMonth = view.findViewById(R.id.npMonth);
        NumberPicker npDay = view.findViewById(R.id.npDay);

        npYear.setMinValue(1400);
        npYear.setMaxValue(1420);
        npYear.setValue(selectedYear);

        String[] monthNames = new String[12];
        for (int i = 0; i < 12; i++) monthNames[i] = PersianUtils.jalaliMonthName(i + 1);
        npMonth.setMinValue(1);
        npMonth.setMaxValue(12);
        npMonth.setDisplayedValues(monthNames);
        npMonth.setValue(selectedMonth);

        npDay.setMinValue(1);
        npDay.setMaxValue(31);
        npDay.setValue(selectedDay);

        new AlertDialog.Builder(this)
                .setView(view)
                .setPositiveButton(R.string.select_date, (dialog, which) -> {
                    selectedYear = npYear.getValue();
                    selectedMonth = npMonth.getValue();
                    selectedDay = npDay.getValue();
                    dateChosen = true;
                    btnPickDate.setText(PersianUtils.toPersianDigits(selectedYear + "/" + selectedMonth + "/" + selectedDay));
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void pickTime() {
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            selectedHour = hourOfDay;
            selectedMinute = minute;
            btnPickTime.setText(PersianUtils.toPersianDigits(
                    String.format("%02d:%02d", hourOfDay, minute)));
        }, selectedHour, selectedMinute, true).show();
    }

    private void scheduleAlarm(int id, String title, long triggerAt) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, ReminderReceiver.class);
        intent.putExtra(ReminderReceiver.EXTRA_TITLE, title);
        intent.putExtra(ReminderReceiver.EXTRA_ID, id);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, id, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
    }
              }
