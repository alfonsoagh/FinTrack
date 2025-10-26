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
                Toast.makeText(requireContext(), "Ver mapa", Toast.LENGTH_SHORT).show()
        );

        // Exportar CSV button
        binding.btnExportCsv.setOnClickListener(v -> exportTripToCsv());

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
