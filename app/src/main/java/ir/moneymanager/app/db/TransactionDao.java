package ir.moneymanager.app.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface TransactionDao {

    @Insert
    void insert(TransactionEntity transaction);

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    List<TransactionEntity> getAllTransactions();

    @Query("SELECT * FROM transactions ORDER BY date DESC LIMIT :limit")
    List<TransactionEntity> getRecentTransactions(int limit);

    @Query("SELECT IFNULL(SUM(amount), 0) FROM transactions WHERE type = 'INCOME'")
    long getTotalIncome();

    @Query("SELECT IFNULL(SUM(amount), 0) FROM transactions WHERE type = 'EXPENSE'")
    long getTotalExpense();
}
