package ir.moneymanager.app;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ir.moneymanager.app.adapter.TransactionAdapter;
import ir.moneymanager.app.db.AppDatabase;
import ir.moneymanager.app.db.TransactionDao;
import ir.moneymanager.app.db.TransactionEntity;
import ir.moneymanager.app.util.PersianUtils;

public class SearchActivity extends AppCompatActivity {

    private EditText etQuery;
    private Button btnFromDate, btnToDate;
    private RecyclerView rvResults;
    private TextView tvEmpty;
    private TransactionAdapter adapter;
    private AppDatabase db;

    private long fromDate = 0;
    private long toDate = Long.MAX_VALUE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        db = AppDatabase.getInstance(this);

        etQuery = findViewById(R.id.etSearchQuery);
        btnFromDate = findViewById(R.id.btnFromDate);
        btnToDate = findViewById(R.id.btnToDate);
        Button btnClear = findViewById(R.id.btnClearFilter);
        rvResults = findViewById(R.id.rvSearchResults);
        tvEmpty = findViewById(R.id.tvSearchEmpty);

        rvResults.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TransactionAdapter(this, new ArrayList<>(), item -> {
            Intent intent = new Intent(this, AddTransactionActivity.class);
            intent.putExtra(AddTransactionActivity.EXTRA_ID, item.getId());
            startActivity(intent);
        });
        rvResults.setAdapter(adapter);

        etQuery.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                runSearch();
            }
        });

        btnFromDate.setOnClickListener(v -> pickDate(true));
        btnToDate.setOnClickListener(v -> pickDate(false));

        btnClear.setOnClickListener(v -> {
            fromDate = 0;
            toDate = Long.MAX_VALUE;
            btnFromDate.setText(R.string.from_date);
            btnToDate.setText(R.string.to_date);
            etQuery.setText("");
            runSearch();
        });

        runSearch();
    }

    @Override
    protected void onResume() {
        super.onResume();
        runSearch();
    }

    private void pickDate(boolean isFrom) {
        long baseMillis = isFrom
                ? (fromDate == 0 ? System.currentTimeMillis() : fromDate)
                : (toDate == Long.MAX_VALUE ? System.currentTimeMillis() : toDate);
        int[] jalali = PersianUtils.millisToJalali(baseMillis);

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_persian_date_picker, null);
        NumberPicker npYear = view.findViewById(R.id.npYear);
        NumberPicker npMonth = view.findViewById(R.id.npMonth);
        NumberPicker npDay = view.findViewById(R.id.npDay);

        npYear.setMinValue(1370);
        npYear.setMaxValue(1420);
        npYear.setValue(jalali[0]);

        String[] monthNames = new String[12];
        for (int i = 0; i < 12; i++) monthNames[i] = PersianUtils.jalaliMonthName(i + 1);
        npMonth.setMinValue(1);
        npMonth.setMaxValue(12);
        npMonth.setDisplayedValues(monthNames);
        npMonth.setValue(jalali[1]);

        npDay.setMinValue(1);
        npDay.setMaxValue(31);
        npDay.setValue(jalali[2]);

        new AlertDialog.Builder(this)
                .setView(view)
                .setPositiveButton(R.string.select_date, (dialog, which) -> {
                    int jy = npYear.getValue();
                    int jm = npMonth.getValue();
                    int jd = npDay.getValue();

                    long millis = PersianUtils.jalaliToMillis(jy, jm, jd, isFrom ? 0 : 23, isFrom ? 0 : 59);
                    String label = PersianUtils.toPersianDigits(jy + "/" + jm + "/" + jd);

                    if (isFrom) {
                        fromDate = millis;
                        btnFromDate.setText(label);
                    } else {
                        toDate = millis;
                        btnToDate.setText(label);
                    }
                    runSearch();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void runSearch() {
        String query = etQuery.getText().toString().trim();
        long from = fromDate;
        long to = toDate;

        AppDatabase.databaseWriteExecutor.execute(() -> {
            TransactionDao dao = db.transactionDao();
            List<TransactionEntity> results = dao.search(query, from, to);

            runOnUiThread(() -> {
                adapter.updateItems(results);
                boolean empty = results.isEmpty();
                tvEmpty.setVisibility(empty ? android.view.View.VISIBLE : android.view.View.GONE);
                rvResults.setVisibility(empty ? android.view.View.GONE : android.view.View.VISIBLE);
            });
        });
    }
}
