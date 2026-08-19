package ir.moneymanager.app.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ReminderDao {

    @Insert
    long insert(ReminderEntity reminder);

    @Query("SELECT * FROM reminders ORDER BY triggerAt ASC")
    List<ReminderEntity> getAll();

    @Query("DELETE FROM reminders WHERE id = :id")
    void delete(int id);

    @Query("DELETE FROM reminders")
    void deleteAll();
}
