package com.pascm.fintrack.ui.perfil;

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
import com.pascm.fintrack.databinding.FragmentPerfilBinding;
import com.pascm.fintrack.data.TripPrefs;

public class PerfilFragment extends Fragment {

    private FragmentPerfilBinding binding;

    public PerfilFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPerfilBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Seleccionar item Perfil en el bottom nav
        binding.bottomNavigation.setSelectedItemId(R.id.nav_perfil);

        // Botón atrás
        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        // Bottom navigation funcional
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                Navigation.findNavController(view).navigate(R.id.action_perfil_to_home);
                return true;
            } else if (id == R.id.nav_viajes) {
                Navigation.findNavController(view).navigate(R.id.action_perfil_to_modo_viaje);
                return true;
            } else if (id == R.id.nav_lugares) {
                Navigation.findNavController(view).navigate(R.id.action_perfil_to_lugares);
                return true;
            } else if (id == R.id.nav_reportes) {
                Navigation.findNavController(view).navigate(R.id.action_perfil_to_reportes);
                return true;
            } else if (id == R.id.nav_perfil) {
                return true; // ya estamos aquí
            }
            return false;
        });

        // Acciones botones
        binding.btnGuardar.setOnClickListener(v ->
                Toast.makeText(requireContext(), getString(R.string.guardar_cambios), Toast.LENGTH_SHORT).show()
        );
        binding.btnCerrarSesion.setOnClickListener(v -> {
            TripPrefs.clearAll(requireContext());
            Navigation.findNavController(view).navigate(R.id.action_global_logout_to_login);
        });

        // Acciones de tarjetas (opcional)
        binding.tvNombreValor.setOnClickListener(v ->
                Toast.makeText(requireContext(), getString(R.string.editar), Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
