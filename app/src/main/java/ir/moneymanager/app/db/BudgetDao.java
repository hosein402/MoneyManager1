package ir.moneymanager.app.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface BudgetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(BudgetEntity budget);

    @Query("SELECT * FROM budgets")
    List<BudgetEntity> getAll();
}
