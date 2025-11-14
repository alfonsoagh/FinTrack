package com.pascm.fintrack.ui.reportes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.pascm.fintrack.R;
import com.pascm.fintrack.databinding.FragmentReportesBinding;
import com.pascm.fintrack.data.repository.ReportRepository;
import com.pascm.fintrack.data.repository.TransactionRepository;
import com.pascm.fintrack.model.AccountTypeReport;
import com.pascm.fintrack.model.CategoryReport;
import com.pascm.fintrack.model.ReportData;
import com.pascm.fintrack.util.SessionManager;
import com.pascm.fintrack.util.CsvExporter;

import android.net.Uri;

import java.text.NumberFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Locale;

public class ReportesFragment extends Fragment {

    private FragmentReportesBinding binding;
    private ReportRepository reportRepository;
    private TransactionRepository transactionRepository;
    private CategoryReportAdapter categoryAdapter;
    private AccountTypeReportAdapter accountTypeAdapter;
    private NumberFormat currencyFormat;

    private enum Period { DAILY, WEEKLY, MONTHLY, YEARLY, ALL_TIME }
    private Period currentPeriod = Period.MONTHLY;

    private double currentTotalIncome = 0;
    private double currentTotalExpense = 0;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentReportesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        reportRepository = new ReportRepository(requireContext());
        transactionRepository = new TransactionRepository(requireContext());
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));

        setupRecyclerViews();
        setupButtons();
        updatePeriodDisplay();
        loadReports();

        binding.bottomNavigation.setSelectedItemId(R.id.nav_reportes);
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                Navigation.findNavController(view).navigate(R.id.action_reportes_to_home);
                return true;
            } else if (id == R.id.nav_viajes) {
                Navigation.findNavController(view).navigate(R.id.action_reportes_to_modo_viaje);
                return true;
            } else if (id == R.id.nav_lugares) {
                Navigation.findNavController(view).navigate(R.id.action_reportes_to_lugares);
                return true;
            } else if (id == R.id.nav_perfil) {
                Navigation.findNavController(view).navigate(R.id.action_reportes_to_perfil);
                return true;
            }
            return id == R.id.nav_reportes;
        });
    }

    private void setupRecyclerViews() {
        categoryAdapter = new CategoryReportAdapter();
        binding.rvCategories.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvCategories.setAdapter(categoryAdapter);

        accountTypeAdapter = new AccountTypeReportAdapter();
        binding.rvAccountTypes.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvAccountTypes.setAdapter(accountTypeAdapter);
    }

    private void setupButtons() {
        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        binding.btnFilters.setOnClickListener(v -> showFilterDialog());
        binding.btnExportCsv.setOnClickListener(v -> exportReportToCsv());
        binding.btnHistorialTransacciones.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_reportes_to_transacciones_list)
        );
    }

    private void showFilterDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_report_filters, null);
        dialog.setContentView(sheetView);

        int selectedChipId;
        if (currentPeriod == Period.DAILY) selectedChipId = R.id.chip_daily;
        else if (currentPeriod == Period.WEEKLY) selectedChipId = R.id.chip_weekly;
        else if (currentPeriod == Period.YEARLY) selectedChipId = R.id.chip_yearly;
        else if (currentPeriod == Period.ALL_TIME) selectedChipId = R.id.chip_all_time;
        else selectedChipId = R.id.chip_monthly;

        sheetView.findViewById(selectedChipId).performClick();

        sheetView.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());
        sheetView.findViewById(R.id.btn_apply).setOnClickListener(v -> {
            if (sheetView.findViewById(R.id.chip_daily).isSelected()) currentPeriod = Period.DAILY;
            else if (sheetView.findViewById(R.id.chip_weekly).isSelected()) currentPeriod = Period.WEEKLY;
            else if (sheetView.findViewById(R.id.chip_monthly).isSelected()) currentPeriod = Period.MONTHLY;
            else if (sheetView.findViewById(R.id.chip_yearly).isSelected()) currentPeriod = Period.YEARLY;
            else if (sheetView.findViewById(R.id.chip_all_time).isSelected()) currentPeriod = Period.ALL_TIME;

            updatePeriodDisplay();
            loadReports();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void updatePeriodDisplay() {
        String periodText;
        if (currentPeriod == Period.DAILY) periodText = "Hoy";
        else if (currentPeriod == Period.WEEKLY) periodText = "Esta semana";
        else if (currentPeriod == Period.YEARLY) periodText = "Este año";
        else if (currentPeriod == Period.ALL_TIME) periodText = "Histórico";
        else periodText = "Este mes";

        binding.tvPeriod.setText(periodText);
    }

    private void loadReports() {
        long userId = SessionManager.getUserId(requireContext());
        if (userId == -1) {
            Navigation.findNavController(requireView()).navigate(R.id.action_global_logout_to_login);
            return;
        }

        long[] dateRange = getDateRange();
        long startDate = dateRange[0];
        long endDate = dateRange[1];

        loadSummary(userId, startDate, endDate);
        loadCategoryReport(userId, startDate, endDate);
        loadAccountTypeReport(userId, startDate, endDate);
    }

    private long[] getDateRange() {
        LocalDate now = LocalDate.now();
        LocalDate startDate, endDate = now;

        switch (currentPeriod) {
            case DAILY:
                startDate = now;
                break;
            case WEEKLY:
                startDate = now.minusDays(now.getDayOfWeek().getValue() - 1);
                break;
            case YEARLY:
                startDate = now.withDayOfYear(1);
                break;
            case ALL_TIME:
                startDate = LocalDate.of(2000, 1, 1);
                break;
            case MONTHLY:
            default:
                startDate = now.with(TemporalAdjusters.firstDayOfMonth());
                break;
        }

        long start = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long end = endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        return new long[]{start, end};
    }

    private void loadSummary(long userId, long startDate, long endDate) {
        reportRepository.getReportData(userId, startDate, endDate, new ReportRepository.ReportCallback() {
            @Override
            public void onSuccess(ReportData reportData) {
                requireActivity().runOnUiThread(() -> {
                    currentTotalIncome = reportData.getTotalIncome();
                    currentTotalExpense = reportData.getTotalExpenses();
                    binding.tvTotalIncome.setText(currencyFormat.format(currentTotalIncome));
                    binding.tvTotalExpenses.setText(currencyFormat.format(currentTotalExpense));
                    binding.tvBalance.setText(currencyFormat.format(reportData.getBalance()));
                });
            }

            @Override
            public void onError(String errorMessage) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void loadCategoryReport(long userId, long startDate, long endDate) {
        reportRepository.getCategoryReport(userId, startDate, endDate, new ReportRepository.CategoryReportCallback() {
            @Override
            public void onSuccess(List<CategoryReport> reports) {
                requireActivity().runOnUiThread(() -> {
                    if (reports.isEmpty()) {
                        binding.rvCategories.setVisibility(View.GONE);
                        binding.tvNoCategories.setVisibility(View.VISIBLE);
                    } else {
                        binding.rvCategories.setVisibility(View.VISIBLE);
                        binding.tvNoCategories.setVisibility(View.GONE);
                        categoryAdapter.setReports(reports);
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void loadAccountTypeReport(long userId, long startDate, long endDate) {
        reportRepository.getAccountTypeReport(userId, startDate, endDate, new ReportRepository.AccountTypeReportCallback() {
            @Override
            public void onSuccess(List<AccountTypeReport> reports) {
                requireActivity().runOnUiThread(() -> {
                    if (reports.isEmpty()) {
                        binding.rvAccountTypes.setVisibility(View.GONE);
                        binding.tvNoAccountTypes.setVisibility(View.VISIBLE);
                    } else {
                        binding.rvAccountTypes.setVisibility(View.VISIBLE);
                        binding.tvNoAccountTypes.setVisibility(View.GONE);
                        accountTypeAdapter.setReports(reports);
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void exportReportToCsv() {
        long userId = SessionManager.getUserId(requireContext());
        if (userId == -1) {
            Navigation.findNavController(requireView()).navigate(R.id.action_global_logout_to_login);
            return;
        }

        long[] dateRange = getDateRange();
        long startDate = dateRange[0];
        long endDate = dateRange[1];

        LocalDate startLocalDate = Instant.ofEpochMilli(startDate).atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endLocalDate = Instant.ofEpochMilli(endDate).atZone(ZoneId.systemDefault()).toLocalDate();

        String periodName = getPeriodName();

        // Get all transactions for the period
        transactionRepository.getTransactionsByDateRange(userId, startLocalDate, endLocalDate)
                .observe(getViewLifecycleOwner(), transactions -> {
                    if (transactions != null) {
                        // Export on background thread
                        new Thread(() -> {
                            Uri csvUri = CsvExporter.exportReportToCSV(
                                    requireContext(),
                                    periodName,
                                    startLocalDate,
                                    endLocalDate,
                                    currentTotalIncome,
                                    currentTotalExpense,
                                    transactions
                            );

                            requireActivity().runOnUiThread(() -> {
                                if (csvUri != null) {
                                    CsvExporter.shareCsv(requireContext(), csvUri);
                                    Toast.makeText(requireContext(), "Reporte exportado exitosamente", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(requireContext(), "Error al exportar reporte", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }).start();
                    }
                });
    }

    private String getPeriodName() {
        if (currentPeriod == Period.DAILY) return "Hoy";
        else if (currentPeriod == Period.WEEKLY) return "Esta_semana";
        else if (currentPeriod == Period.YEARLY) return "Este_año";
        else if (currentPeriod == Period.ALL_TIME) return "Historico";
        else return "Este_mes";
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
