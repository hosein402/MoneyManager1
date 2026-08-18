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
import ir.moneymanager.app.db.DebtEntity;
import ir.moneymanager.app.util.PersianUtils;

public class DebtAdapter extends RecyclerView.Adapter<DebtAdapter.ViewHolder> {

    public interface Listener {
        void onSettle(DebtEntity item);
    }

    private List<DebtEntity> items;
    private final Context context;
    private final Listener listener;

    public DebtAdapter(Context context, List<DebtEntity> items, Listener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    public void updateItems(List<DebtEntity> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_debt, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DebtEntity item = items.get(position);

        holder.person.setText(item.getPersonName());

        String desc = item.getDescription();
        holder.description.setVisibility(desc == null || desc.trim().isEmpty() ? View.GONE : View.VISIBLE);
        holder.description.setText(desc);

        boolean settled = item.getIsSettled() == 1;
        String amountText = PersianUtils.formatAmount(item.getAmount()) + " " + context.getString(R.string.toman);
        holder.amount.setText(settled ? amountText + " (" + context.getString(R.string.settled_label) + ")" : amountText);

        holder.btnSettle.setVisibility(settled ? View.GONE : View.VISIBLE);
        holder.btnSettle.setOnClickListener(v -> {
            if (listener != null) listener.onSettle(item);
        });
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView person, description, amount;
        Button btnSettle;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            person = itemView.findViewById(R.id.tvDebtPerson);
            description = itemView.findViewById(R.id.tvDebtDescription);
            amount = itemView.findViewById(R.id.tvDebtAmount);
            btnSettle = itemView.findViewById(R.id.btnSettle);
        }
    }
}
