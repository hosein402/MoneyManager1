package ir.moneymanager.app;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import ir.moneymanager.app.db.AppDatabase;
import ir.moneymanager.app.db.InstallmentEntity;

public class AddInstallmentActivity extends AppCompatActivity {

    private EditText etTitle, etAmount, etCount;
    private Spinner spBank;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_installment);

        etTitle = findViewById(R.id.etInstallmentTitle);
        etAmount = findViewById(R.id.etInstallmentAmount);
        etCount = findViewById(R.id.etInstallmentCount);
        spBank = findViewById(R.id.spInstallmentBank);

        ArrayAdapter<CharSequence> bankAdapter = ArrayAdapter.createFromResource(
                this, R.array.bank_list, android.R.layout.simple_spinner_item);
        bankAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spBank.setAdapter(bankAdapter);

        Button btnSave = findViewById(R.id.btnSaveInstallment);
        Button btnCancel = findViewById(R.id.btnCancelInstallment);

        btnCancel.setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String amountStr = etAmount.getText().toString().trim();
            String countStr = etCount.getText().toString().trim();

            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(amountStr) || TextUtils.isEmpty(countStr)) {
                Toast.makeText(this, R.string.amount_required, Toast.LENGTH_SHORT).show();
                return;
            }

            long amount;
            int count;
            try {
                amount = Long.parseLong(amountStr);
                count = Integer.parseInt(countStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, R.string.amount_required, Toast.LENGTH_SHORT).show();
                return;
            }

            if (count <= 0) {
                Toast.makeText(this, R.string.amount_required, Toast.LENGTH_SHORT).show();
                return;
            }

            String bank = spBank.getSelectedItem() != null ? spBank.getSelectedItem().toString() : "";

            InstallmentEntity entity = new InstallmentEntity(title, amount, count, 0, System.currentTimeMillis(), bank);

            AppDatabase db = AppDatabase.getInstance(this);
            AppDatabase.databaseWriteExecutor.execute(() -> {
                db.installmentDao().insert(entity);
                runOnUiThread(this::finish);
            });
        });
    }
}
