package ir.moneymanager.app;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ir.moneymanager.app.adapter.BudgetAdapter;
import ir.moneymanager.app.db.AppDatabase;
import ir.moneymanager.app.db.BudgetEntity;
import ir.moneymanager.app.db.CategoryTotal;
import ir.moneymanager.app.db.TransactionEntity;
import ir.moneymanager.app.util.PersianUtils;

public class BudgetsActivity extends AppCompatActivity implements BudgetAdapter.Listener {

    private RecyclerView rvBudgets;
    private BudgetAdapter adapter;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budgets);

        db = AppDatabase.getInstance(this);

        rvBudgets = findViewById(R.id.rvBudgets);
        rvBudgets.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BudgetAdapter(this, new ArrayList<>(), this);
        rvBudgets.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        String[] categories = getResources().getStringArray(R.array.expense_categories);
        long[] range = PersianUtils.getMonthRange();

        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<BudgetEntity> budgetList = db.budgetDao().getAll();
            Map<String, Long> budgetMap = new HashMap<>();
            for (BudgetEntity b : budgetList) budgetMap.put(b.getCategory(), b.getAmount());

            List<CategoryTotal> spentList = db.transactionDao().getCategoryTotals(TransactionEntity.TYPE_EXPENSE, range[0], range[1]);
            Map<String, Long> spentMap = new HashMap<>();
            for (CategoryTotal c : spentList) spentMap.put(c.category, c.total);

            List<BudgetAdapter.Row> rows = new ArrayList<>();
            for (String cat : categories) {
                BudgetAdapter.Row row = new BudgetAdapter.Row();
                row.category = cat;
                row.budget = budgetMap.containsKey(cat) ? budgetMap.get(cat) : 0;
                row.spent = spentMap.containsKey(cat) ? spentMap.get(cat) : 0;
                rows.add(row);
            }

            runOnUiThread(() -> adapter.updateItems(rows));
        });
    }

    @Override
    public void onSetBudget(BudgetAdapter.Row row) {
        EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        if (row.budget > 0) input.setText(String.valueOf(row.budget));

        new AlertDialog.Builder(this)
                .setTitle(row.category)
                .setView(input)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    String text = input.getText().toString().trim();
                    if (text.isEmpty()) return;
                    long amount;
                    try {
                        amount = Long.parseLong(text);
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, R.string.amount_required, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        db.budgetDao().insertOrUpdate(new BudgetEntity(row.category, amount));
                        runOnUiThread(this::loadData);
                    });
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
                                                  }
