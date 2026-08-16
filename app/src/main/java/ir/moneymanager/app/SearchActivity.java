package ir.moneymanager.app;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
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
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            Calendar picked = Calendar.getInstance();
            picked.set(year, month, dayOfMonth, isFrom ? 0 : 23, isFrom ? 0 : 59, isFrom ? 0 : 59);

            String label = PersianUtils.toPersianDigits(year + "/" + (month + 1) + "/" + dayOfMonth);

            if (isFrom) {
                fromDate = picked.getTimeInMillis();
                btnFromDate.setText(label);
            } else {
                toDate = picked.getTimeInMillis();
                btnToDate.setText(label);
            }
            runSearch();
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
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
