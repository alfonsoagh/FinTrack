package com.pascm.fintrack.ui.transacciones;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pascm.fintrack.R;
import com.pascm.fintrack.data.local.entity.Transaction;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TransaccionesAdapter extends RecyclerView.Adapter<TransaccionesAdapter.TransactionViewHolder> {

    private List<Transaction> transactions = new ArrayList<>();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", new Locale("es", "MX"));
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);
        holder.bind(transaction, dateFormatter, currencyFormat);
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvCategory;
        private final TextView tvNote;
        private final TextView tvDate;
        private final TextView tvAmount;
        private final View colorIndicator;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tv_transaction_category);
            tvNote = itemView.findViewById(R.id.tv_transaction_note);
            tvDate = itemView.findViewById(R.id.tv_transaction_date);
            tvAmount = itemView.findViewById(R.id.tv_transaction_amount);
            colorIndicator = itemView.findViewById(R.id.transaction_color_indicator);
        }

        public void bind(Transaction transaction, DateTimeFormatter dateFormatter, NumberFormat currencyFormat) {
            // Categoría o tipo
            String category = getTypeString(transaction.getType());
            tvCategory.setText(category);

            // Nota
            if (transaction.getNotes() != null && !transaction.getNotes().isEmpty()) {
                tvNote.setVisibility(View.VISIBLE);
                tvNote.setText(transaction.getNotes());
            } else {
                tvNote.setVisibility(View.GONE);
            }

            // Fecha
            tvDate.setText(transaction.getTransactionDate().atZone(java.time.ZoneId.systemDefault()).toLocalDate().format(dateFormatter));

            // Monto y color
            String amountText = currencyFormat.format(transaction.getAmount());
            tvAmount.setText(amountText);

            int color;
            if (transaction.getType() == Transaction.TransactionType.INCOME) {
                color = itemView.getContext().getColor(R.color.success_green);
            } else if (transaction.getType() == Transaction.TransactionType.EXPENSE) {
                color = itemView.getContext().getColor(R.color.error_red);
            } else {
                color = itemView.getContext().getColor(R.color.primary);
            }

            tvAmount.setTextColor(color);
            colorIndicator.setBackgroundColor(color);
        }

        private String getTypeString(Transaction.TransactionType type) {
            switch (type) {
                case INCOME: return "Ingreso";
                case EXPENSE: return "Gasto";
                case TRANSFER: return "Transferencia";
                default: return "Transacción";
            }
        }
    }
}
