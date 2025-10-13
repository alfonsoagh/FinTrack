package com.pascm.fintrack.ui.movimiento;

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
import com.pascm.fintrack.databinding.FragmentAgregarMovimientoBinding;

public class AgregarMovimientoFragment extends Fragment {

    private FragmentAgregarMovimientoBinding binding;

    public AgregarMovimientoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAgregarMovimientoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Botón cerrar (X) - regresa al Home
        binding.btnClose.setOnClickListener(v ->
                Navigation.findNavController(v).navigateUp()
        );

        // Botón guardar movimiento
        binding.btnSaveMovement.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Movimiento guardado", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(v).navigateUp();
        });

        // Botones de adjuntar foto y ubicación (sin lógica)
        binding.btnAttachPhoto.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Adjuntar foto", Toast.LENGTH_SHORT).show()
        );

        binding.btnAddLocation.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Agregar ubicación", Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
