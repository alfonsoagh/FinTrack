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
import com.pascm.fintrack.databinding.FragmentAgregarLugarBinding;
import com.pascm.fintrack.util.PlacesManager;

public class AgregarLugarFragment extends Fragment {

    private FragmentAgregarLugarBinding binding;

    public AgregarLugarFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAgregarLugarBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Back button
        binding.btnBack.setOnClickListener(v ->
                Navigation.findNavController(v).navigateUp()
        );

        // Save place button
        binding.btnSavePlace.setOnClickListener(v -> {
            // Set that we now have places
            PlacesManager.setHasPlaces(requireContext(), true);
            Toast.makeText(requireContext(), "Lugar guardado", Toast.LENGTH_SHORT).show();
            // Navigate back to lugares list
            Navigation.findNavController(v).navigateUp();
        });

        // Search in map button
        binding.btnSearchMap.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Buscar en mapa", Toast.LENGTH_SHORT).show()
        );

        // Use current location button
        binding.btnUseLocation.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Usando ubicación actual...", Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
