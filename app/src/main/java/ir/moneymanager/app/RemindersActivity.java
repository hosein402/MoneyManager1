package ir.moneymanager.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ir.moneymanager.app.adapter.ReminderAdapter;
import ir.moneymanager.app.db.AppDatabase;
import ir.moneymanager.app.db.ReminderDao;
import ir.moneymanager.app.db.ReminderEntity;

public class RemindersActivity extends AppCompatActivity implements ReminderAdapter.Listener {

    private RecyclerView rvReminders;
    private TextView tvEmpty;
    private ReminderAdapter adapter;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminders);

        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 100);
            }
        }

        db = AppDatabase.getInstance(this);

        rvReminders = findViewById(R.id.rvReminders);
        tvEmpty = findViewById(R.id.tvEmptyReminders);
        Button btnAdd = findViewById(R.id.btnAddReminder);

        rvReminders.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReminderAdapter(this, new ArrayList<>(), this);
        rvReminders.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> startActivity(new Intent(this, AddReminderActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            ReminderDao dao = db.reminderDao();
            List<ReminderEntity> list = dao.getAll();

            runOnUiThread(() -> {
                adapter.updateItems(list);
                boolean empty = list.isEmpty();
                tvEmpty.setVisibility(empty ? android.view.View.VISIBLE : android.view.View.GONE);
                rvReminders.setVisibility(empty ? android.view.View.GONE : android.view.View.VISIBLE);
            });
        });
    }

    @Override
    public void onDelete(ReminderEntity item) {
        android.app.AlarmManager alarmManager = (android.app.AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, ReminderReceiver.class);
        android.app.PendingIntent pendingIntent = android.app.PendingIntent.getBroadcast(
                this, item.getId(), intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE);
        alarmManager.cancel(pendingIntent);

        AppDatabase.databaseWriteExecutor.execute(() -> {
            db.reminderDao().delete(item.getId());
            runOnUiThread(this::loadData);
        });
    }
          }
