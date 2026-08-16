package ir.moneymanager.app.adapter;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ir.moneymanager.app.R;
import ir.moneymanager.app.db.TransactionEntity;
import ir.moneymanager.app.util.PersianUtils;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private List<TransactionEntity> items;
    private final Context context;

    public TransactionAdapter(Context context, List<TransactionEntity> items) {
        this.context = context;
        this.items = items;
    }

    public void updateItems(List<TransactionEntity> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TransactionEntity item = items.get(position);
        boolean isIncome = TransactionEntity.TYPE_INCOME.equals(item.getType());

        String desc = item.getDescription();
        holder.description.setText((desc == null || desc.trim().isEmpty())
                ? (isIncome ? context.getString(R.string.total_income) : context.getString(R.string.total_expense))
                : desc);

        String sign = isIncome ? "+" : "-";
        holder.amount.setText(sign + " " + PersianUtils.formatAmount(item.getAmount()) + " " + context.getString(R.string.toman));
        holder.amount.setTextColor(context.getResources().getColor(
                isIncome ? R.color.income_green : R.color.expense_red));

        CharSequence dateStr = DateFormat.format("yyyy/MM/dd HH:mm", item.getDate());
        holder.date.setText(PersianUtils.toPersianDigits(dateStr.toString()));
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView description, amount, date;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            description = itemView.findViewById(R.id.tvItemDescription);
            amount = itemView.findViewById(R.id.tvItemAmount);
            date = itemView.findViewById(R.id.tvItemDate);
        }
    }
}
