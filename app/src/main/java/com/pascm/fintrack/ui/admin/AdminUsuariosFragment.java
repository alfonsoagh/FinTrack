package com.pascm.fintrack.ui.admin;

import android.os.Bundle;
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

        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        binding.btnCrear.setOnClickListener(v -> Toast.makeText(requireContext(), "Alta de usuario (demo)", Toast.LENGTH_SHORT).show());
        binding.btnEditar.setOnClickListener(v -> Toast.makeText(requireContext(), "Edición de usuario (demo)", Toast.LENGTH_SHORT).show());
        binding.btnActivarDesactivar.setOnClickListener(v -> Toast.makeText(requireContext(), "Activar/Desactivar (demo)", Toast.LENGTH_SHORT).show());
        binding.btnResetPassword.setOnClickListener(v -> Toast.makeText(requireContext(), "Resetear contraseña (demo)", Toast.LENGTH_SHORT).show());
        binding.btnCerrarSesiones.setOnClickListener(v -> Toast.makeText(requireContext(), "Cerrar sesiones activas (demo)", Toast.LENGTH_SHORT).show());
        binding.btnSuplantar.setOnClickListener(v -> Toast.makeText(requireContext(), "Suplantar sesión (demo)", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

