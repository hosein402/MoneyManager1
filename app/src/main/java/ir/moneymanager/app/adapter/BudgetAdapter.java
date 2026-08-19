package ir.moneymanager.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ir.moneymanager.app.R;
import ir.moneymanager.app.util.PersianUtils;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.ViewHolder> {

    public static class Row {
        public String category;
        public long budget;
        public long spent;
    }

    public interface Listener {
        void onSetBudget(Row row);
    }

    private List<Row> items;
    private final Context context;
    private final Listener listener;

    public BudgetAdapter(Context context, List<Row> items, Listener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    public void updateItems(List<Row> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_budget, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Row row = items.get(position);

        holder.category.setText(row.category);

        if (row.budget <= 0) {
            holder.status.setText(context.getString(R.string.no_budget_set));
            setBarWeight(holder.barFill, 0);
            holder.barFill.setBackgroundColor(context.getResources().getColor(R.color.text_secondary));
        } else {
            int percent = (int) Math.min(100, (row.spent * 100) / row.budget);
            String statusText = PersianUtils.formatAmount(row.spent) + " / " + PersianUtils.formatAmount(row.budget) + " " + context.getString(R.string.toman);
            if (row.spent > row.budget) {
                statusText = context.getString(R.string.budget_exceeded) + " (" + statusText + ")";
            }
            holder.status.setText(statusText);
            setBarWeight(holder.barFill, percent);

            int color;
            if (row.spent > row.budget) {
                color = context.getResources().getColor(R.color.expense_red);
            } else if (percent >= 70) {
                color = context.getResources().getColor(R.color.primary);
            } else {
                color = context.getResources().getColor(R.color.income_green);
            }
            holder.barFill.setBackgroundColor(color);
        }

        holder.btnSetBudget.setOnClickListener(v -> {
            if (listener != null) listener.onSetBudget(row);
        });
    }

    private void setBarWeight(View bar, int percent) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) bar.getLayoutParams();
        params.weight = percent;
        bar.setLayoutParams(params);
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView category, status;
        View barFill;
        Button btnSetBudget;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            category = itemView.findViewById(R.id.tvBudgetCategory);
            status = itemView.findViewById(R.id.tvBudgetStatus);
            barFill = itemView.findViewById(R.id.barBudgetFill);
            btnSetBudget = itemView.findViewById(R.id.btnSetBudget);
        }
    }
}
