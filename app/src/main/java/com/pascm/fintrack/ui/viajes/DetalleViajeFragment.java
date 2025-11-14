package com.pascm.fintrack.ui.viajes;

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
import com.pascm.fintrack.data.local.entity.Transaction;
import com.pascm.fintrack.data.local.entity.Trip;
import com.pascm.fintrack.data.repository.TransactionRepository;
import com.pascm.fintrack.data.repository.TripRepository;
import com.pascm.fintrack.databinding.FragmentDetalleViajeBinding;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class DetalleViajeFragment extends Fragment {

    private FragmentDetalleViajeBinding binding;
    private TripRepository tripRepository;
    private long tripId;
    private Trip currentTrip;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", new Locale("es", "MX"));
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDetalleViajeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tripRepository = new TripRepository(requireContext());

        if (getArguments() != null) {
            tripId = getArguments().getLong("tripId", -1);
            if (tripId != -1) {
                setupUI();
                loadTripData();
            } else {
                Toast.makeText(requireContext(), "Viaje no encontrado", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(view).navigateUp();
            }
        }
    }

    private void setupUI() {
        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        binding.btnVerMapa.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putLong("tripId", tripId);
            Navigation.findNavController(v).navigate(R.id.action_detalle_viaje_to_trip_map_detail, bundle);
        });
    }

    private void loadTripData() {
        tripRepository.getTripById(tripId).observe(getViewLifecycleOwner(), trip -> {
            if (trip != null) {
                currentTrip = trip;
                displayTripInfo();
            }
        });
    }

    private void displayTripInfo() {
        binding.tvTripName.setText(currentTrip.getDestination());
        binding.tvTripDates.setText(
                currentTrip.getStartDate().format(dateFormatter) + " - " +
                        currentTrip.getEndDate().format(dateFormatter)
        );

        if (currentTrip.getBudgetAmount() != null) {
            binding.tvTripBudget.setText("Presupuesto: " + currencyFormat.format(currentTrip.getBudgetAmount()));
        } else {
            binding.tvTripBudget.setText("Presupuesto: No establecido");
        }

        binding.tvTripTotal.setText(currencyFormat.format(0.0));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
