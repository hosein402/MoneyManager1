package ir.moneymanager.app.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "installments")
public class InstallmentEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "amountPerInstallment")
    private long amountPerInstallment;

    @ColumnInfo(name = "installmentCount")
    private int installmentCount;

    @ColumnInfo(name = "paidCount")
    private int paidCount;

    @ColumnInfo(name = "startDate")
    private long startDate;

    public InstallmentEntity(@NonNull String title, long amountPerInstallment, int installmentCount, int paidCount, long startDate) {
        this.title = title;
        this.amountPerInstallment = amountPerInstallment;
        this.installmentCount = installmentCount;
        this.paidCount = paidCount;
        this.startDate = startDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    public void setTitle(@NonNull String title) {
        this.title = title;
    }

    public long getAmountPerInstallment() {
        return amountPerInstallment;
    }

    public void setAmountPerInstallment(long amountPerInstallment) {
        this.amountPerInstallment = amountPerInstallment;
    }

    public int getInstallmentCount() {
        return installmentCount;
    }

    public void setInstallmentCount(int installmentCount) {
        this.installmentCount = installmentCount;
    }

    public int getPaidCount() {
        return paidCount;
    }

    public void setPaidCount(int paidCount) {
        this.paidCount = paidCount;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }
}
