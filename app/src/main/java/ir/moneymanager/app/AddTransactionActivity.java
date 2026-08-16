package ir.moneymanager.app;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import ir.moneymanager.app.db.AppDatabase;
import ir.moneymanager.app.db.TransactionEntity;

public class AddTransactionActivity extends AppCompatActivity {

    public static final String EXTRA_TYPE = "extra_type";
    public static final String EXTRA_ID = "extra_id";

    private EditText etAmount, etDescription;
    private int existingId = -1;
    private String transactionType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        transactionType = getIntent().getStringExtra(EXTRA_TYPE);
        if (transactionType == null) transactionType = TransactionEntity.TYPE_EXPENSE;
        existingId = getIntent().getIntExtra(EXTRA_ID, -1);

        TextView tvTitle = findViewById(R.id.tvFormTitle);
        etAmount = findViewById(R.id.etAmount);
        etDescription = findViewById(R.id.etDescription);
        Button btnSave = findViewById(R.id.btnSave);
        Button btnCancel = findViewById(R.id.btnCancel);
        Button btnDelete = findViewById(R.id.btnDelete);

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
            tvTitle.setText(TransactionEntity.TYPE_INCOME.equals(transactionType)
                    ? R.string.new_income : R.string.new_expense);
        }

        btnCancel.setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> {
            String amountStr = etAmount.getText().toString().trim();
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

            if (existingId != -1) {
                AppDatabase.databaseWriteExecutor.execute(() -> {
                    TransactionEntity existing = db.transactionDao().getById(existingId);
                    if (existing != null) {
                        existing.setAmount(amount);
                        existing.setDescription(description);
                        db.transactionDao().update(existing);
                    }
                    runOnUiThread(this::finish);
                });
            } else {
                long now = System.currentTimeMillis();
                TransactionEntity entity = new TransactionEntity(amount, description, now, transactionType);
                AppDatabase.databaseWriteExecutor.execute(() -> {
                    db.transactionDao().insert(entity);
                    runOnUiThread(this::finish);
                });
            }
        });
    }
}
