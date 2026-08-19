package ir.moneymanager.app;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import ir.moneymanager.app.db.AppDatabase;
import ir.moneymanager.app.db.BudgetEntity;
import ir.moneymanager.app.db.DebtEntity;
import ir.moneymanager.app.db.InstallmentEntity;
import ir.moneymanager.app.db.TransactionEntity;

public class BackupActivity extends AppCompatActivity {

    private static final int REQUEST_RESTORE = 501;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup);

        db = AppDatabase.getInstance(this);

        Button btnBackup = findViewById(R.id.btnBackup);
        Button btnRestore = findViewById(R.id.btnRestore);

        btnBackup.setOnClickListener(v -> doBackup());
        btnRestore.setOnClickListener(v -> pickRestoreFile());
    }

    private void doBackup() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                JSONObject root = new JSONObject();

                JSONArray transactionsArr = new JSONArray();
                for (TransactionEntity t : db.transactionDao().getAllTransactions()) {
                    JSONObject o = new JSONObject();
                    o.put("amount", t.getAmount());
                    o.put("description", t.getDescription());
                    o.put("date", t.getDate());
                    o.put("type", t.getType());
                    o.put("category", t.getCategory());
                    o.put("bank", t.getBank());
                    transactionsArr.put(o);
                }
                root.put("transactions", transactionsArr);

                JSONArray installmentsArr = new JSONArray();
                for (InstallmentEntity i : db.installmentDao().getAll()) {
                    JSONObject o = new JSONObject();
                    o.put("title", i.getTitle());
                    o.put("amountPerInstallment", i.getAmountPerInstallment());
                    o.put("installmentCount", i.getInstallmentCount());
                    o.put("paidCount", i.getPaidCount());
                    o.put("startDate", i.getStartDate());
                    o.put("bank", i.getBank());
                    installmentsArr.put(o);
                }
                root.put("installments", installmentsArr);

                JSONArray debtsArr = new JSONArray();
                for (DebtEntity d : db.debtDao().getAllDebts()) {
                    JSONObject o = new JSONObject();
                    o.put("personName", d.getPersonName());
                    o.put("amount", d.getAmount());
                    o.put("description", d.getDescription());
                    o.put("type", d.getType());
                    o.put("date", d.getDate());
                    o.put("isSettled", d.getIsSettled());
                    debtsArr.put(o);
                }
                root.put("debts", debtsArr);

                JSONArray budgetsArr = new JSONArray();
                for (BudgetEntity b : db.budgetDao().getAll()) {
                    JSONObject o = new JSONObject();
                    o.put("category", b.getCategory());
                    o.put("amount", b.getAmount());
                    budgetsArr.put(o);
                }
                root.put("budgets", budgetsArr);

                File dir = getExternalFilesDir(null);
                File file = new File(dir, "money_manager_backup.json");
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(root.toString().getBytes());
                fos.close();

                runOnUiThread(() -> shareFile(file));
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, R.string.restore_failed, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void shareFile(File file) {
        Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Toast.makeText(this, R.string.backup_success, Toast.LENGTH_SHORT).show();
        startActivity(Intent.createChooser(intent, getString(R.string.backup_now)));
    }

    private void pickRestoreFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, REQUEST_RESTORE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_RESTORE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri == null) return;

            new AlertDialog.Builder(this)
                    .setMessage(R.string.restore_confirm)
                    .setPositiveButton(R.string.save, (dialog, which) -> doRestore(uri))
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        }
    }

    private void doRestore(Uri uri) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                InputStream is = getContentResolver().openInputStream(uri);
                if (is == null) throw new Exception("cannot open");

                StringBuilder sb = new StringBuilder();
                byte[] buffer = new byte[4096];
                int len;
                while ((len = is.read(buffer)) != -1) {
                    sb.append(new String(buffer, 0, len));
                }
                is.close();

                JSONObject root = new JSONObject(sb.toString());

                List<TransactionEntity> newTransactions = new ArrayList<>();
                JSONArray tArr = root.optJSONArray("transactions");
                if (tArr != null) {
                    for (int idx = 0; idx < tArr.length(); idx++) {
                        JSONObject o = tArr.getJSONObject(idx);
                        newTransactions.add(new TransactionEntity(
                                o.optLong("amount"),
                                o.optString("description"),
                                o.optLong("date"),
                                o.optString("type"),
                                o.optString("category"),
                                o.optString("bank")
                        ));
                    }
                }

                List<InstallmentEntity> newInstallments = new ArrayList<>();
                JSONArray iArr = root.optJSONArray("installments");
                if (iArr != null) {
                    for (int idx = 0; idx < iArr.length(); idx++) {
                        JSONObject o = iArr.getJSONObject(idx);
                        newInstallments.add(new InstallmentEntity(
                                o.optString("title"),
                                o.optLong("amountPerInstallment"),
                                o.optInt("installmentCount"),
                                o.optInt("paidCount"),
                                o.optLong("startDate"),
                                o.optString("bank")
                        ));
                    }
                }

                List<DebtEntity> newDebts = new ArrayList<>();
                JSONArray dArr = root.optJSONArray("debts");
                if (dArr != null) {
                    for (int idx = 0; idx < dArr.length(); idx++) {
                        JSONObject o = dArr.getJSONObject(idx);
                        newDebts.add(new DebtEntity(
                                o.optString("personName"),
                                o.optLong("amount"),
                                o.optString("description"),
                                o.optString("type"),
                                o.optLong("date"),
                                o.optInt("isSettled")
                        ));
                    }
                }

                List<BudgetEntity> newBudgets = new ArrayList<>();
                JSONArray bArr = root.optJSONArray("budgets");
                if (bArr != null) {
                    for (int idx = 0; idx < bArr.length(); idx++) {
                        JSONObject o = bArr.getJSONObject(idx);
                        newBudgets.add(new BudgetEntity(
                                o.optString("category"),
                                o.optLong("amount")
                        ));
                    }
                }

                db.transactionDao().deleteAll();
                db.installmentDao().deleteAll();
                db.debtDao().deleteAll();
                db.budgetDao().deleteAll();

                for (TransactionEntity t : newTransactions) db.transactionDao().insert(t);
                for (InstallmentEntity i : newInstallments) db.installmentDao().insert(i);
                for (DebtEntity d : newDebts) db.debtDao().insert(d);
                for (BudgetEntity b : newBudgets) db.budgetDao().insertOrUpdate(b);

                runOnUiThread(() -> {
                    Toast.makeText(this, R.string.restore_success, Toast.LENGTH_SHORT).show();
                    finish();
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, R.string.restore_failed, Toast.LENGTH_SHORT).show());
            }
        });
    }
        }
