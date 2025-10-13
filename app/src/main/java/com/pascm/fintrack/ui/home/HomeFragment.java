package com.pascm.fintrack.ui.home;

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
import com.pascm.fintrack.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ensure correct selected item in bottom nav when on Home
        binding.bottomNavigation.setSelectedItemId(R.id.nav_home);

        // Notification button -> navigate to Recordatorios
        binding.btnNotifications.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_home_to_recordatorios)
        );

        // Account cards - navigate to placeholder or show toast
        binding.cardEfectivo.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Cuenta Efectivo", Toast.LENGTH_SHORT).show()
        );

        binding.cardCredito.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Cuenta Crédito", Toast.LENGTH_SHORT).show()
        );

        binding.cardDebito.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Cuenta Débito", Toast.LENGTH_SHORT).show()
        );

        // Suggested actions
        binding.cardModoViaje.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_home_to_modo_viaje)
        );

        binding.cardReportes.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_home_to_reportes)
        );

        binding.cardLugaresFrecuentes.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Lugares frecuentes", Toast.LENGTH_SHORT).show()
        );

        // Bottom navigation
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                // Already on home
                return true;
            } else if (itemId == R.id.nav_viajes) {
                // Navigate to Modo Viaje
                Navigation.findNavController(view).navigate(R.id.modoViajeFragment);
                return true;
            } else if (itemId == R.id.nav_lugares) {
                // Navigate to Lugares
                Navigation.findNavController(view).navigate(R.id.lugaresFragment);
                return true;
            } else if (itemId == R.id.nav_reportes) {
                // Navigate to Reportes
                Navigation.findNavController(view).navigate(R.id.reportesFragment);
                return true;
            } else if (itemId == R.id.nav_perfil) {
                // Navigate to Perfil
                Navigation.findNavController(view).navigate(R.id.perfilFragment);
                return true;
            }
            return false;
        });

        // FAB - Add new movement
        binding.fabAddMovement.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_home_to_agregar_movimiento)
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
