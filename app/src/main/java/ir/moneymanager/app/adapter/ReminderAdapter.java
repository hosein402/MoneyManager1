package ir.moneymanager.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ir.moneymanager.app.R;
import ir.moneymanager.app.db.ReminderEntity;
import ir.moneymanager.app.util.PersianUtils;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ViewHolder> {

    public interface Listener {
        void onDelete(ReminderEntity item);
    }

    private List<ReminderEntity> items;
    private final Listener listener;

    public ReminderAdapter(Context context, List<ReminderEntity> items, Listener listener) {
        this.items = items;
        this.listener = listener;
    }

    public void updateItems(List<ReminderEntity> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reminder, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReminderEntity item = items.get(position);
        holder.title.setText(item.getTitle());
        holder.time.setText(PersianUtils.toJalaliDateString(item.getTriggerAt()));
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(item);
        });
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, time;
        Button btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvReminderTitle);
            time = itemView.findViewById(R.id.tvReminderTime);
            btnDelete = itemView.findViewById(R.id.btnDeleteReminder);
        }
    }
}
