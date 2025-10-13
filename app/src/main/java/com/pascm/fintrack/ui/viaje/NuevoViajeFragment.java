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
import com.pascm.fintrack.databinding.FragmentNuevoViajeBinding;
import com.pascm.fintrack.data.TripPrefs;

public class NuevoViajeFragment extends Fragment {

    private FragmentNuevoViajeBinding binding;

    public NuevoViajeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentNuevoViajeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Botón cerrar (X) - regresa a Modo Viaje
        binding.btnClose.setOnClickListener(v ->
                Navigation.findNavController(v).navigateUp()
        );

        // Botón cancelar
        binding.btnCancel.setOnClickListener(v ->
                Navigation.findNavController(v).navigateUp()
        );

        // Botón guardar viaje
        binding.btnSaveTrip.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Viaje guardado", Toast.LENGTH_SHORT).show();
            // Marcar viaje como activo y navegar a ViajeActivo
            TripPrefs.setActiveTrip(requireContext(), true);
            Navigation.findNavController(v).navigate(R.id.action_nuevo_viaje_to_viaje_activo);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
