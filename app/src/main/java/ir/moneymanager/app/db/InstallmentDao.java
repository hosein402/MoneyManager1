package ir.moneymanager.app.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface InstallmentDao {

    @Insert
    void insert(InstallmentEntity installment);

    @Query("SELECT * FROM installments ORDER BY id DESC")
    List<InstallmentEntity> getAll();

    @Query("UPDATE installments SET paidCount = paidCount + 1 WHERE id = :id")
    void markOnePaid(int id);

    @Query("DELETE FROM installments WHERE id = :id")
    void delete(int id);
}
