package ir.moneymanager.app;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

import ir.moneymanager.app.db.AppDatabase;
import ir.moneymanager.app.db.TransactionEntity;

public class AddTransactionActivity extends AppCompatActivity {

    public static final String EXTRA_TYPE = "extra_type";
    public static final String EXTRA_ID = "extra_id";

    private EditText etAmount, etDescription;
    private Spinner spCategory;
    private int existingId = -1;
    private String transactionType;
    private boolean isFormattingAmount = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        transactionType = getIntent().getStringExtra(EXTRA_TYPE);
        existingId = getIntent().getIntExtra(EXTRA_ID, -1);

        TextView tvTitle = findViewById(R.id.tvFormTitle);
        etAmount = findViewById(R.id.etAmount);
        etDescription = findViewById(R.id.etDescription);
        spCategory = findViewById(R.id.spCategory);
        Button btnSave = findViewById(R.id.btnSave);
        Button btnCancel = findViewById(R.id.btnCancel);
        Button btnDelete = findViewById(R.id.btnDelete);

        setupAmountFormatting();

        AppDatabase db = AppDatabase.getInstance(this);

        if (existingId != -1) {
            tvTitle.setText(R.string.edit_transaction);
            btnDelete.setVisibility(android.view.View.VISIBLE);

            AppDatabase.databaseWriteExecutor.execute(() -> {
                TransactionEntity existing = db.transactionDao().getById(existingId);
                if (existing != null) {
                    runOnUiThread(() -> {
                        etAmount.setText(String.valueOf(existing.getAmount()));
                        etDescription.setText(existing.getDescription());
                        transactionType = existing.getType();
                        setupCategorySpinner(transactionType, existing.getCategory());
                    });
                }
            });

            btnDelete.setOnClickListener(v -> new AlertDialog.Builder(this)
                    .setMessage(R.string.delete_confirm_message)
                    .setPositiveButton(R.string.delete, (dialog, which) -> {
                        AppDatabase.databaseWriteExecutor.execute(() -> {
                            db.transactionDao().delete(existingId);
                            runOnUiThread(this::finish);
                        });
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show());
        } else {
            if (transactionType == null) transactionType = TransactionEntity.TYPE_EXPENSE;
            tvTitle.setText(TransactionEntity.TYPE_INCOME.equals(transactionType)
                    ? R.string.new_income : R.string.new_expense);
            setupCategorySpinner(transactionType, null);
        }

        btnCancel.setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> {
            String amountStr = etAmount.getText().toString().trim().replace(",", "");
            if (TextUtils.isEmpty(amountStr)) {
                Toast.makeText(this, R.string.amount_required, Toast.LENGTH_SHORT).show();
                return;
            }

            long amount;
            try {
                amount = Long.parseLong(amountStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, R.string.amount_required, Toast.LENGTH_SHORT).show();
                return;
            }

            String description = etDescription.getText().toString().trim();
            String category = spCategory.getSelectedItem() != null ? spCategory.getSelectedItem().toString() : "";

            if (existingId != -1) {
                AppDatabase.databaseWriteExecutor.execute(() -> {
                    TransactionEntity existing = db.transactionDao().getById(existingId);
                    if (existing != null) {
                        existing.setAmount(amount);
                        existing.setDescription(description);
                        existing.setCategory(category);
                        db.transactionDao().update(existing);
                    }
                    runOnUiThread(this::finish);
                });
            } else {
                long now = System.currentTimeMillis();
                TransactionEntity entity = new TransactionEntity(amount, description, now, transactionType, category);
                AppDatabase.databaseWriteExecutor.execute(() -> {
                    db.transactionDao().insert(entity);
                    runOnUiThread(this::finish);
                });
            }
        });
    }

    private void setupCategorySpinner(String type, String selectedCategory) {
        int arrayRes = TransactionEntity.TYPE_INCOME.equals(type)
                ? R.array.income_categories
                : R.array.expense_categories;

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, arrayRes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(adapter);

        if (selectedCategory != null) {
            String[] items = getResources().getStringArray(arrayRes);
            for (int i = 0; i < items.length; i++) {
                if (items[i].equals(selectedCategory)) {
                    spCategory.setSelection(i);
                    break;
                }
            }
        }
    }

    private void setupAmountFormatting() {
        etAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormattingAmount) return;

                String raw = s.toString().replace(",", "");
                isFormattingAmount = true;

                if (!raw.isEmpty()) {
                    try {
                        long value = Long.parseLong(raw);
                        String formatted = String.format(Locale.US, "%,d", value);
                        etAmount.setText(formatted);
                        etAmount.setSelection(formatted.length());
                    } catch (NumberFormatException e) {
                        etAmount.setText(raw);
                        etAmount.setSelection(raw.length());
                    }
                }

                isFormattingAmount = false;
            }
        });
    }
}
