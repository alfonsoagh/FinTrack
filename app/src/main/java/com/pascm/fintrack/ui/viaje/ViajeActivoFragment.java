package com.pascm.fintrack.ui.viaje;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.pascm.fintrack.R;
import com.pascm.fintrack.databinding.FragmentViajeActivoBinding;
import com.pascm.fintrack.data.repository.TripRepository;
import com.pascm.fintrack.data.repository.TransactionRepository;
import com.pascm.fintrack.util.SessionManager;
import com.pascm.fintrack.util.CsvExporter;
import com.pascm.fintrack.data.local.FinTrackDatabase;
import com.pascm.fintrack.data.local.entity.Category;

import android.net.Uri;

public class ViajeActivoFragment extends Fragment {

    private FragmentViajeActivoBinding binding;
    private TripRepository tripRepository;
    private TransactionRepository transactionRepository;

    private int topCategoryCalcVersion = 0; // versión para invalidar cálculos asíncronos previos
    private java.util.List<com.pascm.fintrack.data.local.entity.Transaction> lastTransactions; // referencia para comparaciones

    // Launcher para solicitar permisos de ubicación
    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean coarseGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);

                if (Boolean.TRUE.equals(fineGranted) || Boolean.TRUE.equals(coarseGranted)) {
                    // Permisos concedidos, navegar al mapa
                    androidx.navigation.fragment.NavHostFragment.findNavController(this)
                            .navigate(R.id.action_viajeActivo_to_tripMap);
                } else {
                    Toast.makeText(requireContext(),
                        "Permiso de ubicación necesario para ver el mapa",
                        Toast.LENGTH_LONG).show();
                }
            });

    public ViajeActivoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentViajeActivoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize repositories
        tripRepository = new TripRepository(requireContext());
        transactionRepository = new TransactionRepository(requireContext());

        // Ensure correct selected item in bottom nav when on Viajes
        binding.bottomNavigation.setSelectedItemId(R.id.nav_viajes);

        // Back button
        binding.btnBack.setOnClickListener(v ->
                Navigation.findNavController(v).navigateUp()
        );

        // Finalizar viaje button
        binding.btnEndTrip.setOnClickListener(v -> {
            long userId = SessionManager.getUserId(requireContext());
            tripRepository.endActiveTrip(userId);
            Toast.makeText(requireContext(), "Viaje finalizado", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(v).navigate(R.id.modoViajeFragment);
        });

        // Ver mapa button (aseguramos navController del fragment)
        binding.btnViewMap.setOnClickListener(v -> {
            // Verificar permisos de ubicación antes de navegar al mapa
            if (hasLocationPermissions()) {
                androidx.navigation.fragment.NavHostFragment.findNavController(this)
                        .navigate(R.id.action_viajeActivo_to_tripMap);
            } else {
                requestLocationPermissions();
            }
        });

        // Exportar CSV button
        binding.btnExportCsv.setOnClickListener(v -> exportTripToCsv());

        // Cargar datos del viaje
        loadTripData();

        // Bottom navigation
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                // Navigate to Home
                Navigation.findNavController(view).navigate(R.id.homeFragment);
                return true;
            } else if (itemId == R.id.nav_viajes) {
                // Already on viajes
                return true;
            } else if (itemId == R.id.nav_lugares) {
                // Navigate to Lugares
                Navigation.findNavController(view).navigate(R.id.lugaresFragment);
                return true;
            } else if (itemId == R.id.nav_reportes) {
                // Navigate to Reportes
                Navigation.findNavController(view).navigate(R.id.reportesFragment);
                return true;
            } else if (itemId == R.id.nav_perfil) {
                // Navigate to Perfil
                Navigation.findNavController(view).navigate(R.id.perfilFragment);
                return true;
            }
            return false;
        });
    }

    private void loadTripData() {
        long userId = SessionManager.getUserId(requireContext());
        // Reset visual antes de observar
        binding.tvTopCategory.setText("---");
        tripRepository.getActiveTrip(userId).observe(getViewLifecycleOwner(), trip -> {
            if (trip != null) {
                // Cargar información del viaje
                updateTripInfo(trip);

                // Cargar transacciones del viaje
                transactionRepository.getTransactionsByTrip(userId, trip.getTripId())
                        .observe(getViewLifecycleOwner(), transactions -> {
                            lastTransactions = transactions;
                            updateTransactionInfo(trip, transactions);
                        });
            } else {
                // Sin viaje activo
                binding.tvTopCategory.setText("---");
            }
        });
    }

    private void updateTripInfo(com.pascm.fintrack.data.local.entity.Trip trip) {
        // Itinerario (origen - destino)
        String route = "";
        if (trip.getOrigin() != null && trip.getDestination() != null) {
            route = trip.getOrigin() + " - " + trip.getDestination();
        } else if (trip.getName() != null) {
            route = trip.getName();
        } else {
            route = "Viaje sin nombre";
        }
        binding.tvTripRoute.setText(route);

        // Fechas del viaje
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd MMM",
            new java.util.Locale("es", "MX"));
        String dates = trip.getStartDate().format(formatter) + " - " + trip.getEndDate().format(formatter);
        binding.tvTripDates.setText(dates);

        // Presupuesto
        if (trip.getBudgetAmount() != null && trip.getBudgetAmount() > 0) {
            java.text.NumberFormat currencyFormat = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("es", "MX"));
            binding.tvBudget.setText(currencyFormat.format(trip.getBudgetAmount()));
        } else {
            binding.tvBudget.setText(getString(R.string.sin_presupuesto));
        }

        // Días restantes
        long daysRemaining = java.time.temporal.ChronoUnit.DAYS.between(
            java.time.LocalDate.now(), trip.getEndDate());
        binding.tvDaysRemaining.setText(String.valueOf(Math.max(0, daysRemaining)));
    }

    private void updateTransactionInfo(com.pascm.fintrack.data.local.entity.Trip trip,
                                       java.util.List<com.pascm.fintrack.data.local.entity.Transaction> transactions) {
        java.text.NumberFormat currencyFormat = java.text.NumberFormat.getCurrencyInstance(
            new java.util.Locale("es", "MX"));

        int transactionCount = transactions != null ? transactions.size() : 0;
        binding.tvTransactionCount.setText(String.valueOf(transactionCount));

        double totalSpent = 0;
        int expenseCount = 0;
        if (transactions != null) {
            for (com.pascm.fintrack.data.local.entity.Transaction transaction : transactions) {
                if (transaction.getType() == com.pascm.fintrack.data.local.entity.Transaction.TransactionType.EXPENSE
                        && transaction.getAmount() > 0) {
                    totalSpent += transaction.getAmount();
                    expenseCount++;
                }
            }
        }
        binding.tvSpent.setText(currencyFormat.format(totalSpent));

        if (trip.getBudgetAmount() != null && trip.getBudgetAmount() > 0) {
            double remaining = trip.getBudgetAmount() - totalSpent;
            binding.tvRemaining.setText(currencyFormat.format(Math.max(0, remaining)));
            int progress = (int) ((totalSpent / trip.getBudgetAmount()) * 100);
            binding.progressBudget.setProgress(Math.min(100, Math.max(0, progress)));
        } else {
            binding.tvRemaining.setText(getString(R.string.valor_na));
            binding.progressBudget.setProgress(0);
        }

        // Top categoría: solo si hay al menos 1 gasto real
        if (expenseCount > 0) {
            calculateTopCategory(transactions);
        } else {
            // Invalidar cálculos previos incrementando versión
            topCategoryCalcVersion++;
            binding.tvTopCategory.setText("---");
        }
    }

    private void calculateTopCategory(java.util.List<com.pascm.fintrack.data.local.entity.Transaction> transactions) {
        topCategoryCalcVersion++; // nuevo cálculo
        final int calcVersion = topCategoryCalcVersion;
        binding.tvTopCategory.setText("---");
        if (transactions == null || transactions.isEmpty()) return;
        java.util.Map<Long, Integer> categoryCounts = new java.util.HashMap<>();
        for (com.pascm.fintrack.data.local.entity.Transaction t : transactions) {
            if (t.getType() == com.pascm.fintrack.data.local.entity.Transaction.TransactionType.EXPENSE && t.getAmount() > 0) {
                Long catId = t.getCategoryId();
                if (catId != null) {
                    Integer current = categoryCounts.get(catId);
                    categoryCounts.put(catId, current == null ? 1 : current + 1);
                }
            }
        }
        if (categoryCounts.isEmpty()) return;
        Long topCategoryId = null; int max = 0;
        for (java.util.Map.Entry<Long, Integer> e : categoryCounts.entrySet()) {
            if (e.getValue() > max) { max = e.getValue(); topCategoryId = e.getKey(); }
        }
        if (topCategoryId == null) return;
        final Long finalTopCategoryId = topCategoryId;
        new Thread(() -> {
            try {
                FinTrackDatabase db = FinTrackDatabase.getDatabase(requireContext());
                Category category = db.categoryDao().getByIdSync(finalTopCategoryId);
                requireActivity().runOnUiThread(() -> {
                    // Verificar que versión siga vigente y lista de transacciones no haya cambiado a vacía
                    if (binding != null && calcVersion == topCategoryCalcVersion && lastTransactions != null && !lastTransactions.isEmpty()) {
                        if (category != null) {
                            binding.tvTopCategory.setText(category.getName());
                        } else {
                            binding.tvTopCategory.setText("---");
                        }
                    }
                });
            } catch (Exception ex) {
                requireActivity().runOnUiThread(() -> {
                    if (binding != null && calcVersion == topCategoryCalcVersion) {
                        binding.tvTopCategory.setText("---");
                    }
                });
            }
        }).start();
    }

    private void exportTripToCsv() {
        long userId = SessionManager.getUserId(requireContext());
        tripRepository.getActiveTrip(userId).observe(getViewLifecycleOwner(), trip -> {
            if (trip != null) {
                transactionRepository.getTransactionsByTrip(userId, trip.getTripId()).observe(getViewLifecycleOwner(), transactions -> {
                    if (transactions != null) {
                        // Export on background thread
                        new Thread(() -> {
                            Uri csvUri = CsvExporter.exportTripToCSV(requireContext(), trip, transactions);

                            requireActivity().runOnUiThread(() -> {
                                if (csvUri != null) {
                                    CsvExporter.shareCsv(requireContext(), csvUri);
                                    Toast.makeText(requireContext(), "CSV exportado exitosamente", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(requireContext(), "Error al exportar CSV", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }).start();
                    }
                });
            } else {
                Toast.makeText(requireContext(), "No hay viaje activo", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Verifica si la aplicación tiene permisos de ubicación
     */
    private boolean hasLocationPermissions() {
        boolean fineGranted = ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean coarseGranted = ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        return fineGranted || coarseGranted;
    }

    /**
     * Solicita permisos de ubicación al usuario
     */
    private void requestLocationPermissions() {
        locationPermissionLauncher.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // invalidar cálculos pendientes
        topCategoryCalcVersion++;
        binding = null;
    }
}
