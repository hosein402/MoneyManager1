package ir.moneymanager.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
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

public class MainActivity extends AppCompatActivity {

    private TextView tvBalance, tvIncome, tvExpense, tvEmpty;
    private RecyclerView rvTransactions;
    private TransactionAdapter adapter;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = AppDatabase.getInstance(this);

        tvBalance = findViewById(R.id.tvBalance);
        tvIncome = findViewById(R.id.tvIncomeTotal);
        tvExpense = findViewById(R.id.tvExpenseTotal);
        tvEmpty = findViewById(R.id.tvEmpty);
        rvTransactions = findViewById(R.id.rvTransactions);

        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TransactionAdapter(this, new ArrayList<>(), item -> {
            Intent intent = new Intent(this, AddTransactionActivity.class);
            intent.putExtra(AddTransactionActivity.EXTRA_ID, item.getId());
            startActivity(intent);
        });
        rvTransactions.setAdapter(adapter);

        Button btnAddIncome = findViewById(R.id.btnAddIncome);
        Button btnAddExpense = findViewById(R.id.btnAddExpense);
        Button btnInstallments = findViewById(R.id.btnInstallments);
        Button btnSearch = findViewById(R.id.btnSearch);

        btnAddIncome.setOnClickListener(v -> openAddTransaction(TransactionEntity.TYPE_INCOME));
        btnAddExpense.setOnClickListener(v -> openAddTransaction(TransactionEntity.TYPE_EXPENSE));
        btnInstallments.setOnClickListener(v -> startActivity(new Intent(this, InstallmentsActivity.class)));
        btnSearch.setOnClickListener(v -> startActivity(new Intent(this, SearchActivity.class)));
    }

    private void openAddTransaction(String type) {
        Intent intent = new Intent(this, AddTransactionActivity.class);
        intent.putExtra(AddTransactionActivity.EXTRA_TYPE, type);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            TransactionDao dao = db.transactionDao();
            long income = dao.getTotalIncome();
            long expense = dao.getTotalExpense();
            long balance = income - expense;
            List<TransactionEntity> recent = dao.getRecentTransactions(20);

            runOnUiThread(() -> {
                tvBalance.setText(PersianUtils.formatAmount(balance) + " " + getString(R.string.toman));
                tvIncome.setText(PersianUtils.formatAmount(income) + " " + getString(R.string.toman));
                tvExpense.setText(PersianUtils.formatAmount(expense) + " " + getString(R.string.toman));

                adapter.updateItems(recent);
                tvEmpty.setVisibility(recent.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
                rvTransactions.setVisibility(recent.isEmpty() ? android.view.View.GONE : android.view.View.VISIBLE);
            });
        });
    }
}
