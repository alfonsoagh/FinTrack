package com.pascm.fintrack.ui.reportes;

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
import com.pascm.fintrack.databinding.FragmentReportesBinding;

public class ReportesFragment extends Fragment {

    private FragmentReportesBinding binding;

    public ReportesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentReportesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ensure correct selected item in bottom nav when on Reportes
        binding.bottomNavigation.setSelectedItemId(R.id.nav_reportes);

        // Back button
        binding.btnBack.setOnClickListener(v ->
                Navigation.findNavController(v).navigateUp()
        );

        // Filtros button
        binding.btnFilters.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Filtros", Toast.LENGTH_SHORT).show()
        );

        // Export to CSV button
        binding.btnExportCsv.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Exportando a CSV...", Toast.LENGTH_SHORT).show()
        );

        // Bottom navigation
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                // Navigate to Home
                Navigation.findNavController(view).navigate(R.id.action_reportes_to_home);
                return true;
            } else if (itemId == R.id.nav_viajes) {
                // Navigate to Viajes
                Navigation.findNavController(view).navigate(R.id.action_reportes_to_modo_viaje);
                return true;
            } else if (itemId == R.id.nav_lugares) {
                // Navigate to Lugares
                Navigation.findNavController(view).navigate(R.id.action_reportes_to_lugares);
                return true;
            } else if (itemId == R.id.nav_reportes) {
                // Already on reportes
                return true;
            } else if (itemId == R.id.nav_perfil) {
                // Navigate to Perfil
                Navigation.findNavController(view).navigate(R.id.action_reportes_to_perfil);
                return true;
            }
            return false;
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
