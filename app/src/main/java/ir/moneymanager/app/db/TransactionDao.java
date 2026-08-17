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

    @Query("SELECT * FROM transactions " +
            "WHERE (description LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%') " +
            "AND date BETWEEN :startDate AND :endDate " +
            "ORDER BY date DESC")
    List<TransactionEntity> search(String query, long startDate, long endDate);

    @Query("SELECT IFNULL(SUM(amount), 0) FROM transactions WHERE type = 'INCOME' AND date BETWEEN :start AND :end")
    long getIncomeByRange(long start, long end);

    @Query("SELECT IFNULL(SUM(amount), 0) FROM transactions WHERE type = 'EXPENSE' AND date BETWEEN :start AND :end")
    long getExpenseByRange(long start, long end);

    @Query("SELECT category, SUM(amount) as total FROM transactions " +
            "WHERE type = :type AND date BETWEEN :start AND :end AND category IS NOT NULL " +
            "GROUP BY category ORDER BY total DESC")
    List<CategoryTotal> getCategoryTotals(String type, long start, long end);
}
