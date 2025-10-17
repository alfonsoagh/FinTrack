package com.pascm.fintrack.ui.lugar;

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
import com.pascm.fintrack.data.local.entity.Merchant;
import com.pascm.fintrack.data.repository.PlaceRepository;
import com.pascm.fintrack.databinding.FragmentLugaresBinding;

public class LugaresFragment extends Fragment {

    private FragmentLugaresBinding binding;
    private PlaceRepository placeRepository;
    private PlaceAdapter placeAdapter;

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

        // Initialize repository
        placeRepository = new PlaceRepository(requireContext());

        // Set selected item in bottom navigation
        binding.bottomNavigation.setSelectedItemId(R.id.nav_lugares);

        setupRecyclerView();
        setupListeners();
        loadPlaces();
    }

    private void setupRecyclerView() {
        placeAdapter = new PlaceAdapter();
        binding.rvPlaces.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvPlaces.setAdapter(placeAdapter);

        placeAdapter.setOnPlaceActionListener(new PlaceAdapter.OnPlaceActionListener() {
            @Override
            public void onPlaceClick(Merchant place) {
                // Mostrar detalles del lugar
                String message = place.getName();
                if (place.hasLocation()) {
                    message += String.format("\nUbicación: %.6f, %.6f",
                            place.getLatitude(), place.getLongitude());
                }
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onEditClick(Merchant place) {
                // TODO: Navegar a pantalla de edición
                Toast.makeText(requireContext(), "Editar: " + place.getName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDeleteClick(Merchant place) {
                deletePlace(place);
            }
        });
    }

    private void setupListeners() {
        // Botón agregar lugar (cuando está vacío)
        binding.btnAddPlaceEmpty.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_lugares_to_agregar_lugar)
        );

        // Botón agregar lugar (cuando hay lugares)
        binding.btnAddPlace.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_lugares_to_agregar_lugar)
        );

        // Bottom navigation
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                Navigation.findNavController(requireView()).navigate(R.id.action_lugares_to_home);
                return true;
            } else if (itemId == R.id.nav_viajes) {
                Navigation.findNavController(requireView()).navigate(R.id.action_lugares_to_modo_viaje);
                return true;
            } else if (itemId == R.id.nav_reportes) {
                Navigation.findNavController(requireView()).navigate(R.id.action_lugares_to_reportes);
                return true;
            } else if (itemId == R.id.nav_perfil) {
                Navigation.findNavController(requireView()).navigate(R.id.action_lugares_to_perfil);
                return true;
            } else if (itemId == R.id.nav_lugares) {
                return true;
            }

            return false;
        });
    }

    private void loadPlaces() {
        placeRepository.getAllPlaces().observe(getViewLifecycleOwner(), places -> {
            if (places != null && !places.isEmpty()) {
                placeAdapter.setPlaces(places);
                binding.noPlacesView.setVisibility(View.GONE);
                binding.placesListView.setVisibility(View.VISIBLE);
                binding.btnAddPlaceContainer.setVisibility(View.VISIBLE);
            } else {
                binding.noPlacesView.setVisibility(View.VISIBLE);
                binding.placesListView.setVisibility(View.GONE);
                binding.btnAddPlaceContainer.setVisibility(View.GONE);
            }
        });
    }

    private void deletePlace(Merchant place) {
        // Mostrar confirmación antes de eliminar
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Eliminar lugar")
                .setMessage("¿Estás seguro de que deseas eliminar '" + place.getName() + "'?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    placeRepository.deletePlace(place);
                    Toast.makeText(requireContext(), "Lugar eliminado", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
