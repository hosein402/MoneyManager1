package ir.moneymanager.app.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "reminders")
public class ReminderEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "triggerAt")
    private long triggerAt;

    public ReminderEntity(@NonNull String title, long triggerAt) {
        this.title = title;
        this.triggerAt = triggerAt;
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

    public long getTriggerAt() {
        return triggerAt;
    }

    public void setTriggerAt(long triggerAt) {
        this.triggerAt = triggerAt;
    }
}
