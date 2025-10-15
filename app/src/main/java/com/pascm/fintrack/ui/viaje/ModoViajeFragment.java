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
import com.pascm.fintrack.databinding.FragmentModoViajeBinding;
import com.pascm.fintrack.data.TripPrefs;

public class ModoViajeFragment extends Fragment {

    private FragmentModoViajeBinding binding;
    private boolean hasActiveTrip = false;

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

        binding.bottomNavigation.setSelectedItemId(R.id.nav_viajes);

        // Leer estado desde preferencias
        hasActiveTrip = TripPrefs.isActiveTrip(requireContext());
        updateViewVisibility();

        binding.btnAddTrip.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_modo_viaje_to_nuevo_viaje)
        );

        binding.btnViewDetails.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_modo_viaje_to_viaje_activo)
        );

        binding.btnEndTrip.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Viaje finalizado", Toast.LENGTH_SHORT).show();
            hasActiveTrip = false;
            TripPrefs.setActiveTrip(requireContext(), false);
            updateViewVisibility();
        });

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

    private void updateViewVisibility() {
        if (binding == null) return;
        if (hasActiveTrip) {
            binding.noTripView.setVisibility(View.GONE);
            binding.activeTripView.setVisibility(View.VISIBLE);
        } else {
            binding.noTripView.setVisibility(View.VISIBLE);
            binding.activeTripView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Releer estado al volver a esta pestaña
        hasActiveTrip = TripPrefs.isActiveTrip(requireContext());
        updateViewVisibility();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
