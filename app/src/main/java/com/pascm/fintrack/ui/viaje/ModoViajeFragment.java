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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.pascm.fintrack.R;
import com.pascm.fintrack.databinding.FragmentModoViajeBinding;
import com.pascm.fintrack.data.repository.TripRepository;
import com.pascm.fintrack.data.repository.TransactionRepository;
import com.pascm.fintrack.data.local.entity.Trip;
import com.pascm.fintrack.data.local.entity.Transaction;
import com.pascm.fintrack.util.SessionManager;
import com.pascm.fintrack.util.CsvExporter;

import android.net.Uri;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

public class ModoViajeFragment extends Fragment {

    private FragmentModoViajeBinding binding;
    private TripRepository tripRepository;
    private TransactionRepository transactionRepository;
    private TripTransactionAdapter transactionAdapter;
    private long userId;

    // Launcher para solicitar permisos de ubicación
    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean coarseGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);

                if (Boolean.TRUE.equals(fineGranted) || Boolean.TRUE.equals(coarseGranted)) {
                    // Permisos concedidos, navegar al mapa
                    Navigation.findNavController(requireView()).navigate(R.id.action_modo_viaje_to_tripMap);
                } else {
                    Toast.makeText(requireContext(),
                        "Permiso de ubicación necesario para ver el mapa",
                        Toast.LENGTH_LONG).show();
                }
            });

    public ModoViajeFragment() { }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentModoViajeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize repositories
        tripRepository = new TripRepository(requireContext());
        transactionRepository = new TransactionRepository(requireContext());
        userId = SessionManager.getUserId(requireContext());

        binding.bottomNavigation.setSelectedItemId(R.id.nav_viajes);

        // Setup RecyclerView for transactions
        setupRecyclerView();

        // Setup listeners
        setupListeners(view);

        // Load trip data
        loadTripData();
    }

    private void setupRecyclerView() {
        transactionAdapter = new TripTransactionAdapter();
        binding.rvRecentTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvRecentTransactions.setAdapter(transactionAdapter);
    }

    private void setupListeners(View view) {
        // Add trip button
        binding.btnAddTrip.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_modo_viaje_to_nuevo_viaje)
        );

        // End trip button
        binding.btnEndTrip.setOnClickListener(v -> endTrip());

        // View map button -> navegar al fragment de mapa del viaje
        binding.btnViewMap.setOnClickListener(v -> {
            // Verificar permisos de ubicación antes de navegar al mapa
            if (hasLocationPermissions()) {
                Navigation.findNavController(v).navigate(R.id.action_modo_viaje_to_tripMap);
            } else {
                requestLocationPermissions();
            }
        });

        // Export CSV button
        binding.btnExportCsv.setOnClickListener(v -> exportTripToCsv());

        // Historial de viajes button
        binding.btnHistorialViajes.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_modo_viaje_to_historico_viajes)
        );

        // Bottom navigation
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                Navigation.findNavController(view).navigate(R.id.action_modo_viaje_to_home);
                return true;
            } else if (itemId == R.id.nav_viajes) {
                return true;
            } else if (itemId == R.id.nav_lugares) {
                Navigation.findNavController(view).navigate(R.id.action_modo_viaje_to_lugares);
                return true;
            } else if (itemId == R.id.nav_reportes) {
                Navigation.findNavController(view).navigate(R.id.action_modo_viaje_to_reportes);
                return true;
            } else if (itemId == R.id.nav_perfil) {
                Navigation.findNavController(view).navigate(R.id.action_modo_viaje_to_perfil);
                return true;
            }
            return false;
        });
    }

    private void loadTripData() {
        tripRepository.getActiveTrip(userId).observe(getViewLifecycleOwner(), trip -> {
            if (trip != null) {
                // Show active trip view
                binding.noTripView.setVisibility(View.GONE);
                binding.activeTripView.setVisibility(View.VISIBLE);

                // Update trip info
                updateTripInfo(trip);

                // Load transactions for this trip
                loadTripTransactions(trip.getTripId());
            } else {
                // Show no trip view
                binding.noTripView.setVisibility(View.VISIBLE);
                binding.activeTripView.setVisibility(View.GONE);
            }
        });
    }

    private void updateTripInfo(Trip trip) {
        // Itinerario
        String route = (trip.getOrigin() != null && trip.getDestination() != null) ?
                trip.getOrigin() + " → " + trip.getDestination() :
                trip.getName();
        binding.txtTripRoute.setText(route);

        // Fechas
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", new Locale("es", "MX"));
        String dates = trip.getStartDate().format(formatter) + " - " + trip.getEndDate().format(formatter);
        binding.txtTripDates.setText(dates);

        // Días restantes
        long daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), trip.getEndDate());
        binding.txtDaysRemaining.setText(String.valueOf(Math.max(0, daysRemaining)));

        // Presupuesto (si tiene)
        if (trip.hasBudget()) {
            binding.txtBudgetTotal.setText(formatCurrency(trip.getBudgetAmount()));
            // Las transacciones actualizarán el gastado
        } else {
            binding.txtBudgetTotal.setText("Sin presupuesto");
            binding.txtBudgetSpent.setText("$0");
            binding.txtBudgetRemaining.setText("N/A");
        }
    }

    private void loadTripTransactions(long tripId) {
        transactionRepository.getTransactionsByTrip(userId, tripId).observe(getViewLifecycleOwner(), transactions -> {
            if (transactions != null) {
                // Update transaction count
                binding.txtTransactionCount.setText(String.valueOf(transactions.size()));

                // Update adapter with recent transactions (max 5)
                List<Transaction> recentTransactions = transactions.size() > 5 ?
                        transactions.subList(0, 5) : transactions;
                transactionAdapter.setTransactions(recentTransactions);

                // Calculate totals and top category
                double spent = 0;
                int expenseCount = 0;
                java.util.Map<Long, Integer> categoryCounts = new java.util.HashMap<>();
                for (Transaction t : transactions) {
                    if (t.getType() == Transaction.TransactionType.EXPENSE && t.getAmount() > 0) {
                        spent += t.getAmount();
                        expenseCount++;
                        Long catId = t.getCategoryId();
                        if (catId != null) {
                            Integer c = categoryCounts.get(catId);
                            categoryCounts.put(catId, c == null ? 1 : c + 1);
                        }
                    }
                }

                final double totalSpent = spent;
                binding.txtBudgetSpent.setText(formatCurrency(totalSpent));

                // Update budget progress
                tripRepository.getActiveTrip(userId).observe(getViewLifecycleOwner(), trip -> {
                    if (trip != null && trip.hasBudget()) {
                        double remaining = trip.getBudgetAmount() - totalSpent;
                        binding.txtBudgetRemaining.setText(formatCurrency(Math.max(0, remaining)));

                        int progress = trip.getBudgetAmount() > 0 ? (int) ((totalSpent / trip.getBudgetAmount()) * 100) : 0;
                        binding.progressBudget.setProgress(Math.min(Math.max(progress, 0), 100));
                    } else {
                        binding.txtBudgetRemaining.setText(getString(R.string.valor_na));
                        binding.progressBudget.setProgress(0);
                    }
                });

                // Top categoría: mostrar nombre si hay al menos un gasto; de lo contrario '---'
                if (expenseCount == 0 || categoryCounts.isEmpty()) {
                    binding.txtTopCategory.setText("---");
                } else {
                    // encontrar categoría con mayor conteo
                    Long topCategoryId = null; int max = -1;
                    for (java.util.Map.Entry<Long, Integer> e : categoryCounts.entrySet()) {
                        if (e.getValue() > max) { max = e.getValue(); topCategoryId = e.getKey(); }
                    }
                    final Long finalTopCategoryId = topCategoryId;
                    // Obtener nombre de categoría en background
                    new Thread(() -> {
                        try {
                            com.pascm.fintrack.data.local.FinTrackDatabase db = com.pascm.fintrack.data.local.FinTrackDatabase.getDatabase(requireContext());
                            com.pascm.fintrack.data.local.entity.Category cat = finalTopCategoryId != null ? db.categoryDao().getByIdSync(finalTopCategoryId) : null;
                            requireActivity().runOnUiThread(() -> {
                                if (binding != null) {
                                    binding.txtTopCategory.setText(cat != null ? cat.getName() : "---");
                                }
                            });
                        } catch (Exception ex) {
                            requireActivity().runOnUiThread(() -> {
                                if (binding != null) binding.txtTopCategory.setText("---");
                            });
                        }
                    }).start();
                }
            } else {
                // Sin transacciones
                binding.txtTransactionCount.setText("0");
                binding.txtTopCategory.setText("---");
                binding.txtBudgetSpent.setText(formatCurrency(0));
                binding.progressBudget.setProgress(0);
            }
        });
    }

    private void endTrip() {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Finalizar viaje")
                .setMessage("¿Estás seguro de que deseas finalizar este viaje?")
                .setPositiveButton("Finalizar", (dialog, which) -> {
                    tripRepository.endActiveTrip(userId);
                    Toast.makeText(requireContext(), "Viaje finalizado", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private String formatCurrency(double amount) {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));
        return format.format(amount);
    }

    private void exportTripToCsv() {
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
        binding = null;
    }
}
