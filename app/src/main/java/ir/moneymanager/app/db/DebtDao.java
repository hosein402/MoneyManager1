package ir.moneymanager.app.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DebtDao {

    @Insert
    void insert(DebtEntity debt);

    @Query("SELECT * FROM debts WHERE type = :type ORDER BY isSettled ASC, date DESC")
    List<DebtEntity> getByType(String type);

    @Query("SELECT * FROM debts")
    List<DebtEntity> getAllDebts();

    @Query("UPDATE debts SET isSettled = 1 WHERE id = :id")
    void markSettled(int id);

    @Query("DELETE FROM debts WHERE id = :id")
    void delete(int id);

    @Query("DELETE FROM debts")
    void deleteAll();

    @Query("SELECT * FROM debts WHERE id = :id")
    DebtEntity getById(int id);
}
