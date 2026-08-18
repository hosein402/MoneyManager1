package ir.moneymanager.app;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import ir.moneymanager.app.db.AppDatabase;
import ir.moneymanager.app.db.DebtEntity;

public class AddDebtActivity extends AppCompatActivity {

    public static final String EXTRA_TYPE = "extra_type";

    private EditText etPerson, etAmount, etDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_debt);

        String type = getIntent().getStringExtra(EXTRA_TYPE);
        if (type == null) type = DebtEntity.TYPE_DEBT;
        final String debtType = type;

        TextView tvTitle = findViewById(R.id.tvDebtFormTitle);
        tvTitle.setText(DebtEntity.TYPE_DEBT.equals(debtType) ? R.string.new_debt : R.string.new_receivable);

        etPerson = findViewById(R.id.etPersonName);
        etAmount = findViewById(R.id.etDebtAmount);
        etDescription = findViewById(R.id.etDebtDescription);

        Button btnSave = findViewById(R.id.btnSaveDebt);
        Button btnCancel = findViewById(R.id.btnCancelDebt);

        btnCancel.setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> {
            String person = etPerson.getText().toString().trim();
            String amountStr = etAmount.getText().toString().trim();

            if (TextUtils.isEmpty(person) || TextUtils.isEmpty(amountStr)) {
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

            DebtEntity entity = new DebtEntity(person, amount, description, debtType, System.currentTimeMillis(), 0);

            AppDatabase db = AppDatabase.getInstance(this);
            AppDatabase.databaseWriteExecutor.execute(() -> {
                db.debtDao().insert(entity);
                runOnUiThread(this::finish);
            });
        });
    }
}
