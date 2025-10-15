package com.pascm.fintrack.ui.lugares;

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
import com.pascm.fintrack.databinding.FragmentLugaresBinding;
import com.pascm.fintrack.util.PlacesManager;

public class LugaresFragment extends Fragment {

    private FragmentLugaresBinding binding;

    public LugaresFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLugaresBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ensure correct selected item in bottom nav when on Lugares
        binding.bottomNavigation.setSelectedItemId(R.id.nav_lugares);

        // Toggle views based on whether there are places
        updateViewVisibility();

        // Back button
        binding.btnBack.setOnClickListener(v ->
                Navigation.findNavController(v).navigateUp()
        );

        // No places view - Add new place button
        binding.btnAddPlaceEmpty.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_lugares_to_agregar_lugar)
        );

        // With places view - Add new place button (at bottom)
        binding.btnAddPlace.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_lugares_to_agregar_lugar)
        );

        // Bottom navigation
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                // Navigate to Home
                Navigation.findNavController(view).navigate(R.id.action_lugares_to_home);
                return true;
            } else if (itemId == R.id.nav_viajes) {
                // Navigate to Viajes
                Navigation.findNavController(view).navigate(R.id.action_lugares_to_modo_viaje);
                return true;
            } else if (itemId == R.id.nav_lugares) {
                // Already on lugares
                return true;
            } else if (itemId == R.id.nav_reportes) {
                // Navigate to Reportes
                Navigation.findNavController(view).navigate(R.id.action_lugares_to_reportes);
                return true;
            } else if (itemId == R.id.nav_perfil) {
                // Navigate to Perfil
                Navigation.findNavController(view).navigate(R.id.action_lugares_to_perfil);
                return true;
            }
            return false;
        });
    }

    private void updateViewVisibility() {
        boolean hasPlaces = PlacesManager.hasPlaces(requireContext());
        if (hasPlaces) {
            binding.noPlacesView.setVisibility(View.GONE);
            binding.placesListView.setVisibility(View.VISIBLE);
            binding.btnAddPlaceContainer.setVisibility(View.VISIBLE);
        } else {
            binding.noPlacesView.setVisibility(View.VISIBLE);
            binding.placesListView.setVisibility(View.GONE);
            binding.btnAddPlaceContainer.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Update view visibility when returning from AgregarLugar
        updateViewVisibility();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
