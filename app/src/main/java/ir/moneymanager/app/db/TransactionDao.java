package ir.moneymanager.app.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TransactionDao {

    @Insert
    void insert(TransactionEntity transaction);

    @Update
    void update(TransactionEntity transaction);

    @Query("DELETE FROM transactions WHERE id = :id")
    void delete(int id);

    @Query("SELECT * FROM transactions WHERE id = :id")
    TransactionEntity getById(int id);

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    List<TransactionEntity> getAllTransactions();

    @Query("SELECT * FROM transactions ORDER BY date DESC LIMIT :limit")
    List<TransactionEntity> getRecentTransactions(int limit);

    @Query("SELECT IFNULL(SUM(amount), 0) FROM transactions WHERE type = 'INCOME'")
    long getTotalIncome();

    @Query("SELECT IFNULL(SUM(amount), 0) FROM transactions WHERE type = 'EXPENSE'")
    long getTotalExpense();
}
