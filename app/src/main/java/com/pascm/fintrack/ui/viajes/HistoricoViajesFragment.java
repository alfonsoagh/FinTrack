package com.pascm.fintrack.ui.viajes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.pascm.fintrack.R;
import com.pascm.fintrack.data.local.entity.Trip;
import com.pascm.fintrack.data.repository.TripRepository;
import com.pascm.fintrack.databinding.FragmentHistoricoViajesBinding;
import com.pascm.fintrack.util.SessionManager;

public class HistoricoViajesFragment extends Fragment {

    private FragmentHistoricoViajesBinding binding;
    private TripRepository tripRepository;
    private ViajesHistoricoAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHistoricoViajesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tripRepository = new TripRepository(requireContext());

        setupUI();
        setupRecyclerView();
        loadTrips();
    }

    private void setupUI() {
        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
    }

    private void setupRecyclerView() {
        adapter = new ViajesHistoricoAdapter(trip -> {
            // Al hacer click, navegar a detalle del viaje con mapa
            Bundle bundle = new Bundle();
            bundle.putLong("tripId", trip.getTripId());
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_historico_viajes_to_detalle_viaje, bundle);
        });

        binding.rvTrips.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvTrips.setAdapter(adapter);
    }

    private void loadTrips() {
        long userId = SessionManager.getUserId(requireContext());

        tripRepository.getAllTrips(userId).observe(getViewLifecycleOwner(), trips -> {
            if (trips != null && !trips.isEmpty()) {
                adapter.setTrips(trips);
                binding.emptyView.setVisibility(View.GONE);
                binding.rvTrips.setVisibility(View.VISIBLE);
            } else {
                binding.emptyView.setVisibility(View.VISIBLE);
                binding.rvTrips.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
