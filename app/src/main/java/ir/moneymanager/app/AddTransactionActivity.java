package ir.moneymanager.app;

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

    private EditText etAmount, etDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        String type = getIntent().getStringExtra(EXTRA_TYPE);
        if (type == null) type = TransactionEntity.TYPE_EXPENSE;
        final String transactionType = type;

        TextView tvTitle = findViewById(R.id.tvFormTitle);
        tvTitle.setText(TransactionEntity.TYPE_INCOME.equals(transactionType)
                ? R.string.new_income : R.string.new_expense);

        etAmount = findViewById(R.id.etAmount);
        etDescription = findViewById(R.id.etDescription);

        Button btnSave = findViewById(R.id.btnSave);
        Button btnCancel = findViewById(R.id.btnCancel);

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
            long now = System.currentTimeMillis();

            TransactionEntity entity = new TransactionEntity(amount, description, now, transactionType);

            AppDatabase db = AppDatabase.getInstance(this);
            AppDatabase.databaseWriteExecutor.execute(() -> {
                db.transactionDao().insert(entity);
                runOnUiThread(this::finish);
            });
        });
    }
}
