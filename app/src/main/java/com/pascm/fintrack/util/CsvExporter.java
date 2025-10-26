package com.pascm.fintrack.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.core.content.FileProvider;

import com.pascm.fintrack.data.local.entity.Transaction;
import com.pascm.fintrack.data.local.entity.Trip;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Utility class for exporting data to CSV format
 */
public class CsvExporter {

    private static final String TAG = "CsvExporter";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy", new Locale("es", "MX"));

    /**
     * Export trip transactions to CSV file
     *
     * @param context        Application context
     * @param trip           Trip entity
     * @param transactions   List of transactions
     * @return Uri of the created CSV file, or null if failed
     */
    public static Uri exportTripToCSV(Context context, Trip trip, List<Transaction> transactions) {
        try {
            // Create CSV file
            File csvFile = createCsvFile(context, "viaje_" + trip.getName().replaceAll("[^a-zA-Z0-9]", "_"));
            FileWriter writer = new FileWriter(csvFile);

            // Write trip header
            writer.append("REPORTE DE VIAJE\n");
            writer.append("Nombre:,").append(escapeCSV(trip.getName())).append("\n");
            writer.append("Origen:,").append(escapeCSV(trip.getOrigin() != null ? trip.getOrigin() : "N/A")).append("\n");
            writer.append("Destino:,").append(escapeCSV(trip.getDestination() != null ? trip.getDestination() : "N/A")).append("\n");
            writer.append("Fecha inicio:,").append(trip.getStartDate().format(DATE_FORMATTER)).append("\n");
            writer.append("Fecha fin:,").append(trip.getEndDate().format(DATE_FORMATTER)).append("\n");

            if (trip.hasBudget()) {
                writer.append("Presupuesto:,").append(String.format(Locale.US, "%.2f", trip.getBudgetAmount())).append(" ").append(trip.getCurrencyCode()).append("\n");
            }

            writer.append("\n");

            // Write transactions header
            writer.append("TRANSACCIONES\n");
            writer.append("Fecha,Tipo,Monto,Categoría,Notas,Ubicación\n");

            // Calculate totals
            double totalIncome = 0;
            double totalExpense = 0;

            // Write transactions
            for (Transaction transaction : transactions) {
                writer.append(transaction.getTransactionDate().toString().substring(0, 10));
                writer.append(",");
                writer.append(getTransactionTypeText(transaction.getType()));
                writer.append(",");
                writer.append(String.format(Locale.US, "%.2f", transaction.getAmount()));
                writer.append(",");
                writer.append(""); // Category (TODO: add when category names are available)
                writer.append(",");
                writer.append(escapeCSV(transaction.getNotes() != null ? transaction.getNotes() : ""));
                writer.append(",");
                if (transaction.hasLocation()) {
                    writer.append(String.format(Locale.US, "%.6f,%.6f", transaction.getLatitude(), transaction.getLongitude()));
                } else {
                    writer.append("");
                }
                writer.append("\n");

                // Update totals
                if (transaction.getType() == Transaction.TransactionType.INCOME) {
                    totalIncome += transaction.getAmount();
                } else if (transaction.getType() == Transaction.TransactionType.EXPENSE) {
                    totalExpense += transaction.getAmount();
                }
            }

            // Write summary
            writer.append("\n");
            writer.append("RESUMEN\n");
            writer.append("Total ingresos:,").append(String.format(Locale.US, "%.2f", totalIncome)).append("\n");
            writer.append("Total gastos:,").append(String.format(Locale.US, "%.2f", totalExpense)).append("\n");
            writer.append("Balance:,").append(String.format(Locale.US, "%.2f", totalIncome - totalExpense)).append("\n");

            if (trip.hasBudget()) {
                double remaining = trip.getBudgetAmount() - totalExpense;
                writer.append("Presupuesto restante:,").append(String.format(Locale.US, "%.2f", remaining)).append("\n");
            }

            writer.flush();
            writer.close();

            // Return file URI
            return FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", csvFile);

        } catch (IOException e) {
            Log.e(TAG, "Error exporting trip to CSV", e);
            return null;
        }
    }

    /**
     * Export general report to CSV
     *
     * @param context      Application context
     * @param periodName   Name of the period (e.g., "Este mes")
     * @param startDate    Start date
     * @param endDate      End date
     * @param totalIncome  Total income
     * @param totalExpense Total expenses
     * @param transactions List of transactions
     * @return Uri of the created CSV file, or null if failed
     */
    public static Uri exportReportToCSV(Context context, String periodName, LocalDate startDate, LocalDate endDate,
                                         double totalIncome, double totalExpense, List<Transaction> transactions) {
        try {
            // Create CSV file
            File csvFile = createCsvFile(context, "reporte_" + periodName.replaceAll("[^a-zA-Z0-9]", "_"));
            FileWriter writer = new FileWriter(csvFile);

            // Write report header
            writer.append("REPORTE FINANCIERO\n");
            writer.append("Período:,").append(periodName).append("\n");
            writer.append("Fecha inicio:,").append(startDate.format(DATE_FORMATTER)).append("\n");
            writer.append("Fecha fin:,").append(endDate.format(DATE_FORMATTER)).append("\n");
            writer.append("Total ingresos:,").append(String.format(Locale.US, "%.2f", totalIncome)).append("\n");
            writer.append("Total gastos:,").append(String.format(Locale.US, "%.2f", totalExpense)).append("\n");
            writer.append("Balance:,").append(String.format(Locale.US, "%.2f", totalIncome - totalExpense)).append("\n");
            writer.append("\n");

            // Write transactions header
            writer.append("TRANSACCIONES\n");
            writer.append("Fecha,Tipo,Monto,Categoría,Método de pago,Notas\n");

            // Write transactions
            for (Transaction transaction : transactions) {
                writer.append(transaction.getTransactionDate().toString().substring(0, 10));
                writer.append(",");
                writer.append(getTransactionTypeText(transaction.getType()));
                writer.append(",");
                writer.append(String.format(Locale.US, "%.2f", transaction.getAmount()));
                writer.append(",");
                writer.append(""); // Category
                writer.append(",");
                writer.append(getPaymentMethodText(transaction.getCardType()));
                writer.append(",");
                writer.append(escapeCSV(transaction.getNotes() != null ? transaction.getNotes() : ""));
                writer.append("\n");
            }

            writer.flush();
            writer.close();

            // Return file URI
            return FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", csvFile);

        } catch (IOException e) {
            Log.e(TAG, "Error exporting report to CSV", e);
            return null;
        }
    }

    /**
     * Share CSV file via Intent
     *
     * @param context Application context
     * @param csvUri  Uri of CSV file
     */
    public static void shareCsv(Context context, Uri csvUri) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/csv");
        shareIntent.putExtra(Intent.EXTRA_STREAM, csvUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        Intent chooser = Intent.createChooser(shareIntent, "Compartir CSV");
        if (shareIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(chooser);
        }
    }

    /**
     * Create a CSV file in the app's cache directory
     */
    private static File createCsvFile(Context context, String baseName) throws IOException {
        File csvDir = new File(context.getCacheDir(), "exports");
        if (!csvDir.exists()) {
            csvDir.mkdirs();
        }

        String timestamp = LocalDate.now().toString().replace("-", "");
        String fileName = baseName + "_" + timestamp + ".csv";

        return new File(csvDir, fileName);
    }

    /**
     * Escape CSV special characters
     */
    private static String escapeCSV(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }

        // If value contains comma, quote, or newline, wrap in quotes and escape internal quotes
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }

        return value;
    }

    /**
     * Get transaction type text in Spanish
     */
    private static String getTransactionTypeText(Transaction.TransactionType type) {
        switch (type) {
            case INCOME:
                return "Ingreso";
            case EXPENSE:
                return "Gasto";
            case TRANSFER:
                return "Transferencia";
            default:
                return "Desconocido";
        }
    }

    /**
     * Get payment method text
     */
    private static String getPaymentMethodText(String cardType) {
        if (cardType == null) {
            return "N/A";
        }

        switch (cardType) {
            case "CREDIT":
                return "Tarjeta de crédito";
            case "DEBIT":
                return "Tarjeta de débito";
            case "CASH":
                return "Efectivo";
            default:
                return cardType;
        }
    }
}
