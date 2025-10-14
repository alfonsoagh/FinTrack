package com.pascm.fintrack.ui.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.pascm.fintrack.databinding.FragmentAdminUsuariosBinding;

public class AdminUsuariosFragment extends Fragment {

    private FragmentAdminUsuariosBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAdminUsuariosBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Botón volver
        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        // Botón más opciones
        binding.btnMore.setOnClickListener(v ->
            Toast.makeText(requireContext(), "Más opciones (demo)", Toast.LENGTH_SHORT).show()
        );

        // Campo de búsqueda
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Aquí puedes implementar la lógica de filtrado de usuarios
                Toast.makeText(requireContext(), "Buscando: " + s.toString(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Switches de activación/desactivación
        binding.switchUsuario1.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String estado = isChecked ? "activado" : "desactivado";
            Toast.makeText(requireContext(), "Usuario 1 " + estado, Toast.LENGTH_SHORT).show();
        });

        binding.switchUsuario2.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String estado = isChecked ? "activado" : "desactivado";
            Toast.makeText(requireContext(), "Usuario 2 " + estado, Toast.LENGTH_SHORT).show();
        });

        binding.switchUsuario3.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String estado = isChecked ? "activado" : "desactivado";
            Toast.makeText(requireContext(), "Usuario 3 " + estado, Toast.LENGTH_SHORT).show();
        });

        // Click en tarjetas de usuarios (para editar)
        binding.cardUsuario1.setOnClickListener(v ->
            Toast.makeText(requireContext(), "Editar Ana Martínez (demo)", Toast.LENGTH_SHORT).show()
        );

        binding.cardUsuario2.setOnClickListener(v ->
            Toast.makeText(requireContext(), "Editar Juan García (demo)", Toast.LENGTH_SHORT).show()
        );

        binding.cardUsuario3.setOnClickListener(v ->
            Toast.makeText(requireContext(), "Editar Carlos Rodriguez (demo)", Toast.LENGTH_SHORT).show()
        );

        // Botón flotante para agregar nuevo usuario
        binding.fabAdd.setOnClickListener(v ->
            Toast.makeText(requireContext(), "Agregar nuevo usuario (demo)", Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

