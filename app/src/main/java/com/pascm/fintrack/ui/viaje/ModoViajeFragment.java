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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.pascm.fintrack.R;
import com.pascm.fintrack.databinding.FragmentModoViajeBinding;
import com.pascm.fintrack.data.repository.TripRepository;
import com.pascm.fintrack.data.repository.TransactionRepository;
import com.pascm.fintrack.data.local.entity.Trip;
import com.pascm.fintrack.data.local.entity.Transaction;
import com.pascm.fintrack.util.SessionManager;

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

        // View map button
        binding.btnViewMap.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Ver mapa", Toast.LENGTH_SHORT).show()
        );

        // Export CSV button
        binding.btnExportCsv.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Exportando CSV...", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(requireContext(), "Reportes", Toast.LENGTH_SHORT).show();
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

                // Calculate total spent
                double spent = 0;
                for (Transaction t : transactions) {
                    if (t.getType() == Transaction.TransactionType.EXPENSE) {
                        spent += t.getAmount();
                    }
                }

                final double totalSpent = spent;
                binding.txtBudgetSpent.setText(formatCurrency(totalSpent));

                // Update budget progress
                tripRepository.getActiveTrip(userId).observe(getViewLifecycleOwner(), trip -> {
                    if (trip != null && trip.hasBudget()) {
                        double remaining = trip.getBudgetAmount() - totalSpent;
                        binding.txtBudgetRemaining.setText(formatCurrency(remaining));

                        int progress = (int) ((totalSpent / trip.getBudgetAmount()) * 100);
                        binding.progressBudget.setProgress(Math.min(progress, 100));
                    }
                });

                // Find top category (simple implementation)
                if (!transactions.isEmpty()) {
                    binding.txtTopCategory.setText("Gastos");
                }
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
