package com.pascm.fintrack.ui.reportes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pascm.fintrack.R;
import com.pascm.fintrack.model.CategoryReport;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CategoryReportAdapter extends RecyclerView.Adapter<CategoryReportAdapter.ViewHolder> {

    private List<CategoryReport> reports = new ArrayList<>();
    private final NumberFormat currencyFormat;

    public CategoryReportAdapter() {
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_report, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CategoryReport report = reports.get(position);

        holder.tvCategoryIcon.setText(report.getCategoryIcon() != null ? report.getCategoryIcon() : "📊");
        holder.tvCategoryName.setText(report.getCategoryName());
        holder.tvTransactionCount.setText(report.getTransactionCount() + " transacciones");
        holder.tvAmount.setText(currencyFormat.format(report.getAmount()));
        holder.tvPercentage.setText(String.format(Locale.getDefault(), "%.1f%%", report.getPercentage()));
    }

    @Override
    public int getItemCount() {
        return reports.size();
    }

    public void setReports(List<CategoryReport> reports) {
        this.reports = reports != null ? reports : new ArrayList<>();
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryIcon, tvCategoryName, tvTransactionCount, tvAmount, tvPercentage;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryIcon = itemView.findViewById(R.id.tv_category_icon);
            tvCategoryName = itemView.findViewById(R.id.tv_category_name);
            tvTransactionCount = itemView.findViewById(R.id.tv_transaction_count);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvPercentage = itemView.findViewById(R.id.tv_percentage);
        }
    }
}
