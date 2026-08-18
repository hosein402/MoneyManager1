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

import ir.moneymanager.app.adapter.DebtAdapter;
import ir.moneymanager.app.db.AppDatabase;
import ir.moneymanager.app.db.DebtDao;
import ir.moneymanager.app.db.DebtEntity;
import ir.moneymanager.app.db.TransactionEntity;

public class DebtsActivity extends AppCompatActivity implements DebtAdapter.Listener {

    private Button btnTabDebt, btnTabReceivable, btnAddDebt;
    private RecyclerView rvDebts;
    private TextView tvEmpty;
    private DebtAdapter adapter;
    private AppDatabase db;

    private String currentType = DebtEntity.TYPE_DEBT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debts);

        db = AppDatabase.getInstance(this);

        btnTabDebt = findViewById(R.id.btnTabDebt);
        btnTabReceivable = findViewById(R.id.btnTabReceivable);
        btnAddDebt = findViewById(R.id.btnAddDebt);
        rvDebts = findViewById(R.id.rvDebts);
        tvEmpty = findViewById(R.id.tvEmptyDebts);

        rvDebts.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DebtAdapter(this, new ArrayList<>(), this);
        rvDebts.setAdapter(adapter);

        btnTabDebt.setOnClickListener(v -> selectTab(DebtEntity.TYPE_DEBT));
        btnTabReceivable.setOnClickListener(v -> selectTab(DebtEntity.TYPE_RECEIVABLE));

        btnAddDebt.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddDebtActivity.class);
            intent.putExtra(AddDebtActivity.EXTRA_TYPE, currentType);
            startActivity(intent);
        });

        selectTab(DebtEntity.TYPE_DEBT);
    }

    private void selectTab(String type) {
        currentType = type;

        boolean isDebt = DebtEntity.TYPE_DEBT.equals(type);
        btnTabDebt.setBackgroundTintList(getColorStateList(isDebt ? R.color.expense_red : R.color.card_background));
        btnTabDebt.setTextColor(getResources().getColor(isDebt ? R.color.white : R.color.text_primary));

        btnTabReceivable.setBackgroundTintList(getColorStateList(!isDebt ? R.color.income_green : R.color.card_background));
        btnTabReceivable.setTextColor(getResources().getColor(!isDebt ? R.color.white : R.color.text_primary));

        btnAddDebt.setText(isDebt ? R.string.add_debt : R.string.add_receivable);

        loadData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        String type = currentType;
        AppDatabase.databaseWriteExecutor.execute(() -> {
            DebtDao dao = db.debtDao();
            List<DebtEntity> list = dao.getByType(type);

            runOnUiThread(() -> {
                adapter.updateItems(list);
                boolean empty = list.isEmpty();
                tvEmpty.setVisibility(empty ? android.view.View.VISIBLE : android.view.View.GONE);
                rvDebts.setVisibility(empty ? android.view.View.GONE : android.view.View.VISIBLE);
            });
        });
    }

    @Override
    public void onSettle(DebtEntity item) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            db.debtDao().markSettled(item.getId());

            boolean isDebt = DebtEntity.TYPE_DEBT.equals(item.getType());
            TransactionEntity transaction = new TransactionEntity(
                    item.getAmount(),
                    item.getPersonName(),
                    System.currentTimeMillis(),
                    isDebt ? TransactionEntity.TYPE_EXPENSE : TransactionEntity.TYPE_INCOME,
                    isDebt ? "بدهی" : "طلب",
                    ""
            );
            db.transactionDao().insert(transaction);

            runOnUiThread(this::loadData);
        });
    }
}
