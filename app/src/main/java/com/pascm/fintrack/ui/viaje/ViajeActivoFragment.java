package com.pascm.fintrack.ui.viaje;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

        // Ver mapa button
        binding.btnViewMap.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_viajeActivo_to_tripMap)
        );

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
        tripRepository.getActiveTrip(userId).observe(getViewLifecycleOwner(), trip -> {
            if (trip != null) {
                // Cargar información del viaje
                updateTripInfo(trip);

                // Cargar transacciones del viaje
                transactionRepository.getTransactionsByTrip(userId, trip.getTripId())
                    .observe(getViewLifecycleOwner(), transactions -> {
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
            java.text.NumberFormat currencyFormat = java.text.NumberFormat.getCurrencyInstance(
                new java.util.Locale("es", "MX"));
            binding.tvBudget.setText(currencyFormat.format(trip.getBudgetAmount()));
        } else {
            binding.tvBudget.setText("Sin presupuesto");
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

        // Contar transacciones
        int transactionCount = transactions != null ? transactions.size() : 0;
        binding.tvTransactionCount.setText(String.valueOf(transactionCount));

        // Calcular total gastado
        double totalSpent = 0;
        if (transactions != null) {
            for (com.pascm.fintrack.data.local.entity.Transaction transaction : transactions) {
                if (transaction.getType() == com.pascm.fintrack.data.local.entity.Transaction.TransactionType.EXPENSE) {
                    totalSpent += transaction.getAmount();
                }
            }
        }
        binding.tvSpent.setText(currencyFormat.format(totalSpent));

        // Calcular restante y progreso
        if (trip.getBudgetAmount() != null && trip.getBudgetAmount() > 0) {
            double remaining = trip.getBudgetAmount() - totalSpent;
            binding.tvRemaining.setText(currencyFormat.format(Math.max(0, remaining)));

            // Progreso (porcentaje gastado)
            int progress = (int) ((totalSpent / trip.getBudgetAmount()) * 100);
            binding.progressBudget.setProgress(Math.min(100, Math.max(0, progress)));
        } else {
            binding.tvRemaining.setText("N/A");
            binding.progressBudget.setProgress(0);
        }

        // Top categoría
        if (transactions != null && !transactions.isEmpty()) {
            calculateTopCategory(transactions);
        } else {
            binding.tvTopCategory.setText("---");
        }
    }

    private void calculateTopCategory(java.util.List<com.pascm.fintrack.data.local.entity.Transaction> transactions) {
        // Inicializar con "---" por defecto
        binding.tvTopCategory.setText("---");

        if (transactions == null || transactions.isEmpty()) {
            return;
        }

        // Contar categorías (solo gastos)
        java.util.Map<Long, Integer> categoryCounts = new java.util.HashMap<>();
        for (com.pascm.fintrack.data.local.entity.Transaction transaction : transactions) {
            if (transaction.getType() == com.pascm.fintrack.data.local.entity.Transaction.TransactionType.EXPENSE) {
                Long categoryId = transaction.getCategoryId();
                if (categoryId != null) {
                    categoryCounts.put(categoryId, categoryCounts.getOrDefault(categoryId, 0) + 1);
                }
            }
        }

        if (categoryCounts.isEmpty()) {
            return;
        }

        // Encontrar la categoría más común
        Long topCategoryId = null;
        int maxCount = 0;
        for (java.util.Map.Entry<Long, Integer> entry : categoryCounts.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                topCategoryId = entry.getKey();
            }
        }

        if (topCategoryId == null) {
            return;
        }

        // Obtener el nombre de la categoría desde la base de datos
        final Long finalTopCategoryId = topCategoryId;
        new Thread(() -> {
            try {
                FinTrackDatabase database = FinTrackDatabase.getDatabase(requireContext());
                Category category = database.categoryDao().getByIdSync(finalTopCategoryId);

                requireActivity().runOnUiThread(() -> {
                    if (category != null && binding != null) {
                        binding.tvTopCategory.setText(category.getName());
                    } else if (binding != null) {
                        binding.tvTopCategory.setText("---");
                    }
                });
            } catch (Exception e) {
                requireActivity().runOnUiThread(() -> {
                    if (binding != null) {
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
