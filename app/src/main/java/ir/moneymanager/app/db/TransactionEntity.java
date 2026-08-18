package ir.moneymanager.app.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "transactions")
public class TransactionEntity {

    public static final String TYPE_INCOME = "INCOME";
    public static final String TYPE_EXPENSE = "EXPENSE";

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "amount")
    private long amount;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "date")
    private long date;

    @NonNull
    @ColumnInfo(name = "type")
    private String type;

    @ColumnInfo(name = "category")
    private String category;

    @ColumnInfo(name = "bank")
    private String bank;

    public TransactionEntity(long amount, String description, long date, @NonNull String type, String category, String bank) {
        this.amount = amount;
        this.description = description;
        this.date = date;
        this.type = type;
        this.category = category;
        this.bank = bank;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    @NonNull
    public String getType() {
        return type;
    }

    public void setType(@NonNull String type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getBank() {
        return bank;
    }

    public void setBank(String bank) {
        this.bank = bank;
    }
}
