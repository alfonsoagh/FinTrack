package com.pascm.fintrack.ui.reportes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pascm.fintrack.R;
import com.pascm.fintrack.model.AccountTypeReport;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AccountTypeReportAdapter extends RecyclerView.Adapter<AccountTypeReportAdapter.ViewHolder> {

    private List<AccountTypeReport> reports = new ArrayList<>();
    private final NumberFormat currencyFormat;

    public AccountTypeReportAdapter() {
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_account_type_report, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AccountTypeReport report = reports.get(position);

        // Set icon based on account type
        String icon = "💰";
        if ("CREDIT".equals(report.getAccountType())) {
            icon = "💳";
        } else if ("DEBIT".equals(report.getAccountType())) {
            icon = "🏧";
        }

        holder.tvAccountIcon.setText(icon);
        holder.tvAccountType.setText(report.getAccountTypeDisplay());
        holder.tvTransactionCount.setText(report.getTransactionCount() + " transacciones");
        holder.tvAmount.setText(currencyFormat.format(report.getAmount()));
        holder.tvPercentage.setText(String.format(Locale.getDefault(), "%.1f%%", report.getPercentage()));
    }

    @Override
    public int getItemCount() {
        return reports.size();
    }

    public void setReports(List<AccountTypeReport> reports) {
        this.reports = reports != null ? reports : new ArrayList<>();
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAccountIcon, tvAccountType, tvTransactionCount, tvAmount, tvPercentage;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAccountIcon = itemView.findViewById(R.id.tv_account_icon);
            tvAccountType = itemView.findViewById(R.id.tv_account_type);
            tvTransactionCount = itemView.findViewById(R.id.tv_transaction_count);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvPercentage = itemView.findViewById(R.id.tv_percentage);
        }
    }
}
