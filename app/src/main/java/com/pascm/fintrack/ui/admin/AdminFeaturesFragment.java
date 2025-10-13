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

import com.pascm.fintrack.R;

public class AdminFeaturesFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_features, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View btnBack = view.findViewById(R.id.btnBack);
        View btnGuardar = view.findViewById(R.id.btnGuardar);
        View btnHealthCheck = view.findViewById(R.id.btnHealthCheck);

        if (btnBack != null) btnBack.setOnClickListener(v -> Navigation.findNavController(view).navigateUp());
        if (btnGuardar != null) btnGuardar.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Configuración de features guardada (demo)", Toast.LENGTH_SHORT).show()
        );
        if (btnHealthCheck != null) btnHealthCheck.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Health check OK (simulado)", Toast.LENGTH_SHORT).show()
        );
    }
}
