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

import ir.moneymanager.app.adapter.InstallmentAdapter;
import ir.moneymanager.app.db.AppDatabase;
import ir.moneymanager.app.db.InstallmentDao;
import ir.moneymanager.app.db.InstallmentEntity;
import ir.moneymanager.app.db.TransactionEntity;

public class InstallmentsActivity extends AppCompatActivity implements InstallmentAdapter.Listener {

    private RecyclerView rvInstallments;
    private TextView tvEmpty;
    private InstallmentAdapter adapter;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_installments);

        db = AppDatabase.getInstance(this);

        rvInstallments = findViewById(R.id.rvInstallments);
        tvEmpty = findViewById(R.id.tvEmptyInstallments);
        Button btnAdd = findViewById(R.id.btnAddInstallment);

        rvInstallments.setLayoutManager(new LinearLayoutManager(this));
        adapter = new InstallmentAdapter(this, new ArrayList<>(), this);
        rvInstallments.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> startActivity(new Intent(this, AddInstallmentActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            InstallmentDao dao = db.installmentDao();
            List<InstallmentEntity> list = dao.getAll();

            runOnUiThread(() -> {
                adapter.updateItems(list);
                boolean empty = list.isEmpty();
                tvEmpty.setVisibility(empty ? android.view.View.VISIBLE : android.view.View.GONE);
                rvInstallments.setVisibility(empty ? android.view.View.GONE : android.view.View.VISIBLE);
            });
        });
    }

    @Override
    public void onPay(InstallmentEntity item) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            db.installmentDao().markOnePaid(item.getId());

            TransactionEntity expense = new TransactionEntity(
                    item.getAmountPerInstallment(),
                    item.getTitle(),
                    System.currentTimeMillis(),
                    TransactionEntity.TYPE_EXPENSE,
                    "اقساط",
                    item.getBank()
            );
            db.transactionDao().insert(expense);

            runOnUiThread(this::loadData);
        });
    }

    @Override
    public void onDelete(InstallmentEntity item) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            db.installmentDao().delete(item.getId());
            runOnUiThread(this::loadData);
        });
    }
}
