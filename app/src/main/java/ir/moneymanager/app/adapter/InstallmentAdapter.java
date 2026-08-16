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
import ir.moneymanager.app.db.InstallmentEntity;
import ir.moneymanager.app.util.PersianUtils;

public class InstallmentAdapter extends RecyclerView.Adapter<InstallmentAdapter.ViewHolder> {

    public interface Listener {
        void onPay(InstallmentEntity item);
        void onDelete(InstallmentEntity item);
    }

    private List<InstallmentEntity> items;
    private final Context context;
    private final Listener listener;

    public InstallmentAdapter(Context context, List<InstallmentEntity> items, Listener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    public void updateItems(List<InstallmentEntity> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_installment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        InstallmentEntity item = items.get(position);

        holder.title.setText(item.getTitle());

        boolean completed = item.getPaidCount() >= item.getInstallmentCount();
        String progress = PersianUtils.toPersianDigits(item.getPaidCount() + " / " + item.getInstallmentCount());
        holder.progress.setText(context.getString(R.string.installment_progress) + ": " + progress
                + (completed ? " (" + context.getString(R.string.installment_completed) + ")" : ""));

        holder.amount.setText(PersianUtils.formatAmount(item.getAmountPerInstallment()) + " " + context.getString(R.string.toman));

        holder.btnPay.setEnabled(!completed);
        holder.btnPay.setAlpha(completed ? 0.5f : 1f);

        holder.btnPay.setOnClickListener(v -> {
            if (listener != null) listener.onPay(item);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(item);
        });
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, progress, amount;
        Button btnPay, btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvInstallmentTitle);
            progress = itemView.findViewById(R.id.tvInstallmentProgress);
            amount = itemView.findViewById(R.id.tvInstallmentAmount);
            btnPay = itemView.findViewById(R.id.btnPayInstallment);
            btnDelete = itemView.findViewById(R.id.btnDeleteInstallment);
        }
    }
}
