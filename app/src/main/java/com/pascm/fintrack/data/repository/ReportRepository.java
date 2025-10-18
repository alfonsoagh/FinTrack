package com.pascm.fintrack.data.repository;

import android.content.Context;

import com.pascm.fintrack.data.local.FinTrackDatabase;
import com.pascm.fintrack.data.local.dao.TransactionDao;
import com.pascm.fintrack.model.AccountTypeReport;
import com.pascm.fintrack.model.CategoryReport;
import com.pascm.fintrack.model.ReportData;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository para generar reportes y estadísticas
 */
public class ReportRepository {

    private final TransactionDao transactionDao;
    private final FinTrackDatabase database;

    public ReportRepository(Context context) {
        this.database = FinTrackDatabase.getDatabase(context);
        this.transactionDao = database.transactionDao();
    }

    /**
     * Obtener reporte general para un rango de fechas
     */
    public void getReportData(long userId, long startDate, long endDate, ReportCallback callback) {
        FinTrackDatabase.databaseWriteExecutor.execute(() -> {
            try {
                double totalIncome = transactionDao.getTotalIncomeForRangeSync(userId, startDate, endDate);
                double totalExpenses = transactionDao.getTotalExpensesForRangeSync(userId, startDate, endDate);
                int transactionCount = transactionDao.getTransactionCountForRangeSync(userId, startDate, endDate);
                double balance = totalIncome - totalExpenses;

                ReportData reportData = new ReportData(totalIncome, totalExpenses, balance, transactionCount);
                callback.onSuccess(reportData);

            } catch (Exception e) {
                android.util.Log.e("ReportRepository", "Error getting report data", e);
                callback.onError("Error al generar reporte: " + e.getMessage());
            }
        });
    }

    /**
     * Obtener reporte por categorías
     */
    public void getCategoryReport(long userId, long startDate, long endDate, CategoryReportCallback callback) {
        FinTrackDatabase.databaseWriteExecutor.execute(() -> {
            try {
                List<TransactionDao.CategoryReportData> rawData =
                        transactionDao.getCategoryReportForDateRange(userId, startDate, endDate);

                // Calcular el total para porcentajes
                double total = 0;
                for (TransactionDao.CategoryReportData data : rawData) {
                    if (data.total_amount != null) {
                        total += data.total_amount;
                    }
                }

                // Convertir a modelo de reporte
                List<CategoryReport> reports = new ArrayList<>();
                for (TransactionDao.CategoryReportData data : rawData) {
                    if (data.category_id != null) {
                        CategoryReport report = new CategoryReport();
                        report.setCategoryId(data.category_id);
                        report.setCategoryName(data.category_name != null ? data.category_name : "Sin categoría");
                        report.setCategoryIcon(data.category_icon);
                        report.setCategoryColor(data.category_color != null ? data.category_color : 0);
                        report.setAmount(data.total_amount != null ? data.total_amount : 0);
                        report.setTransactionCount(data.transaction_count != null ? data.transaction_count : 0);

                        // Calcular porcentaje
                        if (total > 0 && data.total_amount != null) {
                            report.setPercentage((data.total_amount / total) * 100);
                        } else {
                            report.setPercentage(0);
                        }

                        reports.add(report);
                    }
                }

                callback.onSuccess(reports);

            } catch (Exception e) {
                android.util.Log.e("ReportRepository", "Error getting category report", e);
                callback.onError("Error al generar reporte por categorías: " + e.getMessage());
            }
        });
    }

    /**
     * Obtener reporte por tipo de cuenta
     */
    public void getAccountTypeReport(long userId, long startDate, long endDate, AccountTypeReportCallback callback) {
        FinTrackDatabase.databaseWriteExecutor.execute(() -> {
            try {
                List<TransactionDao.AccountTypeReportData> rawData =
                        transactionDao.getAccountTypeReportForDateRange(userId, startDate, endDate);

                // Calcular el total para porcentajes
                double total = 0;
                for (TransactionDao.AccountTypeReportData data : rawData) {
                    if (data.total_amount != null) {
                        total += data.total_amount;
                    }
                }

                // Convertir a modelo de reporte
                List<AccountTypeReport> reports = new ArrayList<>();
                for (TransactionDao.AccountTypeReportData data : rawData) {
                    AccountTypeReport report = new AccountTypeReport();
                    report.setAccountType(data.account_type);
                    report.setAmount(data.total_amount != null ? data.total_amount : 0);
                    report.setTransactionCount(data.transaction_count != null ? data.transaction_count : 0);

                    // Calcular porcentaje
                    if (total > 0 && data.total_amount != null) {
                        report.setPercentage((data.total_amount / total) * 100);
                    } else {
                        report.setPercentage(0);
                    }

                    reports.add(report);
                }

                callback.onSuccess(reports);

            } catch (Exception e) {
                android.util.Log.e("ReportRepository", "Error getting account type report", e);
                callback.onError("Error al generar reporte por tipo de cuenta: " + e.getMessage());
            }
        });
    }

    // ========== Callbacks ==========

    public interface ReportCallback {
        void onSuccess(ReportData reportData);
        void onError(String errorMessage);
    }

    public interface CategoryReportCallback {
        void onSuccess(List<CategoryReport> reports);
        void onError(String errorMessage);
    }

    public interface AccountTypeReportCallback {
        void onSuccess(List<AccountTypeReport> reports);
        void onError(String errorMessage);
    }
}
