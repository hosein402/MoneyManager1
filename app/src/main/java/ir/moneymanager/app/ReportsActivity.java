package ir.moneymanager.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import ir.moneymanager.app.db.AppDatabase;
import ir.moneymanager.app.db.CategoryTotal;
import ir.moneymanager.app.db.TransactionDao;
import ir.moneymanager.app.db.TransactionEntity;
import ir.moneymanager.app.util.PersianUtils;

public class ReportsActivity extends AppCompatActivity {

    private static final int PERIOD_DAY = 0;
    private static final int PERIOD_MONTH = 1;
    private static final int PERIOD_YEAR = 2;

    private int currentPeriod = PERIOD_DAY;

    private Button btnDaily, btnMonthly, btnYearly;
    private TextView tvPeriodLabel, tvReportIncome, tvReportExpense, tvNoCategoryData;
    private View barIncomeFill, barExpenseFill;
    private LinearLayout categoryContainer;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        db = AppDatabase.getInstance(this);

        btnDaily = findViewById(R.id.btnDaily);
        btnMonthly = findViewById(R.id.btnMonthly);
        btnYearly = findViewById(R.id.btnYearly);
        tvPeriodLabel = findViewById(R.id.tvPeriodLabel);
        tvReportIncome = findViewById(R.id.tvReportIncome);
        tvReportExpense = findViewById(R.id.tvReportExpense);
        tvNoCategoryData = findViewById(R.id.tvNoCategoryData);
        barIncomeFill = findViewById(R.id.barIncomeFill);
        barExpenseFill = findViewById(R.id.barExpenseFill);
        categoryContainer = findViewById(R.id.categoryContainer);

        btnDaily.setOnClickListener(v -> selectPeriod(PERIOD_DAY));
        btnMonthly.setOnClickListener(v -> selectPeriod(PERIOD_MONTH));
        btnYearly.setOnClickListener(v -> selectPeriod(PERIOD_YEAR));

        selectPeriod(PERIOD_DAY);
    }

    private void selectPeriod(int period) {
        currentPeriod = period;

        btnDaily.setBackgroundTintList(getColorStateList(period == PERIOD_DAY ? R.color.primary : R.color.card_background));
        btnDaily.setTextColor(getResources().getColor(period == PERIOD_DAY ? R.color.white : R.color.text_primary));

        btnMonthly.setBackgroundTintList(getColorStateList(period == PERIOD_MONTH ? R.color.primary : R.color.card_background));
        btnMonthly.setTextColor(getResources().getColor(period == PERIOD_MONTH ? R.color.white : R.color.text_primary));

        btnYearly.setBackgroundTintList(getColorStateList(period == PERIOD_YEAR ? R.color.primary : R.color.card_background));
        btnYearly.setTextColor(getResources().getColor(period == PERIOD_YEAR ? R.color.white : R.color.text_primary));

        loadReport();
    }

    private void loadReport() {
        long[] range;
        String label;
        int[] j = PersianUtils.todayJalali();

        if (currentPeriod == PERIOD_DAY) {
            range = PersianUtils.getDayRange();
            label = PersianUtils.toPersianDigits(j[0] + "/" + j[1] + "/" + j[2]);
        } else if (currentPeriod == PERIOD_MONTH) {
            range = PersianUtils.getMonthRange();
            label = PersianUtils.jalaliMonthName(j[1]) + " " + PersianUtils.toPersianDigits(String.valueOf(j[0]));
        } else {
            range = PersianUtils.getYearRange();
            label = "سال " + PersianUtils.toPersianDigits(String.valueOf(j[0]));
        }

        tvPeriodLabel.setText(label);

        long start = range[0];
        long end = range[1];

        AppDatabase.databaseWriteExecutor.execute(() -> {
            TransactionDao dao = db.transactionDao();
            long income = dao.getIncomeByRange(start, end);
            long expense = dao.getExpenseByRange(start, end);
            List<CategoryTotal> categories = dao.getCategoryTotals(TransactionEntity.TYPE_EXPENSE, start, end);

            runOnUiThread(() -> {
                tvReportIncome.setText(PersianUtils.formatAmount(income) + " " + getString(R.string.toman));
                tvReportExpense.setText(PersianUtils.formatAmount(expense) + " " + getString(R.string.toman));

                long maxVal = Math.max(income, expense);
                int incomePercent = maxVal > 0 ? (int) (income * 100 / maxVal) : 0;
                int expensePercent = maxVal > 0 ? (int) (expense * 100 / maxVal) : 0;

                setBarWeight(barIncomeFill, incomePercent);
                setBarWeight(barExpenseFill, expensePercent);

                renderCategories(categories);
            });
        });
    }

    private void setBarWeight(View bar, int percent) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) bar.getLayoutParams();
        params.weight = percent;
        bar.setLayoutParams(params);
    }

    private void renderCategories(List<CategoryTotal> categories) {
        categoryContainer.removeAllViews();

        if (categories == null || categories.isEmpty()) {
            tvNoCategoryData.setVisibility(View.VISIBLE);
            return;
        }
        tvNoCategoryData.setVisibility(View.GONE);

        long maxTotal = categories.get(0).total;

        LayoutInflater inflater = LayoutInflater.from(this);
        for (CategoryTotal cat : categories) {
            View row = inflater.inflate(R.layout.item_category_report, categoryContainer, false);
            TextView tvName = row.findViewById(R.id.tvCategoryName);
            TextView tvAmount = row.findViewById(R.id.tvCategoryAmount);
            View barFill = row.findViewById(R.id.barFill);

            tvName.setText(cat.category);
            tvAmount.setText(PersianUtils.formatAmount(cat.total) + " " + getString(R.string.toman));

            int percent = maxTotal > 0 ? (int) (cat.total * 100 / maxTotal) : 0;
            setBarWeight(barFill, percent);

            categoryContainer.addView(row);
        }
    }
              }
