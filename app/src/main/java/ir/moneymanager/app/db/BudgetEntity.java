package ir.moneymanager.app.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "budgets")
public class BudgetEntity {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "category")
    private String category;

    @ColumnInfo(name = "amount")
    private long amount;

    public BudgetEntity(@NonNull String category, long amount) {
        this.category = category;
        this.amount = amount;
    }

    @NonNull
    public String getCategory() {
        return category;
    }

    public void setCategory(@NonNull String category) {
        this.category = category;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }
}
