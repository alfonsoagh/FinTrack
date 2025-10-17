package com.pascm.fintrack.ui.viaje;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

public class TripTransactionAdapter extends RecyclerView.Adapter<TripTransactionAdapter.TransactionViewHolder> {

    private List<Transaction> transactions;

    public TripTransactionAdapter() {
        this.transactions = new ArrayList<>();
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions != null ? transactions : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_trip_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);
        holder.bind(transaction);
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imgCategory;
        private final TextView txtName;
        private final TextView txtCategoryDate;
        private final TextView txtAmount;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCategory = itemView.findViewById(R.id.img_category);
            txtName = itemView.findViewById(R.id.txt_name);
            txtCategoryDate = itemView.findViewById(R.id.txt_category_date);
            txtAmount = itemView.findViewById(R.id.txt_amount);
        }

        public void bind(Transaction transaction) {
            // Set transaction name/description
            String description = transaction.getNotes();
            if (description == null || description.isEmpty()) {
                description = "Gasto";
            }
            txtName.setText(description);

            // Set category and date
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM", new Locale("es", "MX"));
            String typeText = transaction.getType() == Transaction.TransactionType.EXPENSE ? "Gasto" : "Ingreso";
            String categoryDate = typeText + " • " +
                    transaction.getTransactionDate().toString().substring(0, 10);
            txtCategoryDate.setText(categoryDate);

            // Set amount
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));
            String amountStr = transaction.getType() == Transaction.TransactionType.EXPENSE ?
                    "-" + currencyFormat.format(transaction.getAmount()) :
                    "+" + currencyFormat.format(transaction.getAmount());
            txtAmount.setText(amountStr);

            // Set icon based on transaction type
            int iconRes = transaction.getType() == Transaction.TransactionType.EXPENSE ?
                    R.drawable.ic_shopping_bag : R.drawable.ic_add;
            imgCategory.setImageResource(iconRes);
        }
    }
}
