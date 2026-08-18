package ir.moneymanager.app.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "debts")
public class DebtEntity {

    public static final String TYPE_DEBT = "DEBT";
    public static final String TYPE_RECEIVABLE = "RECEIVABLE";

    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    @ColumnInfo(name = "personName")
    private String personName;

    @ColumnInfo(name = "amount")
    private long amount;

    @ColumnInfo(name = "description")
    private String description;

    @NonNull
    @ColumnInfo(name = "type")
    private String type;

    @ColumnInfo(name = "date")
    private long date;

    @ColumnInfo(name = "isSettled")
    private int isSettled;

    public DebtEntity(@NonNull String personName, long amount, String description, @NonNull String type, long date, int isSettled) {
        this.personName = personName;
        this.amount = amount;
        this.description = description;
        this.type = type;
        this.date = date;
        this.isSettled = isSettled;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    public String getPersonName() {
        return personName;
    }

    public void setPersonName(@NonNull String personName) {
        this.personName = personName;
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

    @NonNull
    public String getType() {
        return type;
    }

    public void setType(@NonNull String type) {
        this.type = type;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public int getIsSettled() {
        return isSettled;
    }

    public void setIsSettled(int isSettled) {
        this.isSettled = isSettled;
    }
}
