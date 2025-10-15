package com.pascm.fintrack.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.pascm.fintrack.R;

public class AdminDashboardFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Referencias a las nuevas tarjetas
        View cardUsuarios = view.findViewById(R.id.cardUsuarios);
        View cardCategorias = view.findViewById(R.id.cardCategorias);
        View cardMetricas = view.findViewById(R.id.cardMetricas);
        View btnTheme = view.findViewById(R.id.btnTheme);

        // Navegar a Usuarios
        if (cardUsuarios != null) {
            cardUsuarios.setOnClickListener(v ->
                    Navigation.findNavController(view).navigate(R.id.action_adminDashboard_to_adminUsuarios)
            );
        }

        // Navegar a Categorías (usando el existente Catálogo)
        if (cardCategorias != null) {
            cardCategorias.setOnClickListener(v ->
                    Navigation.findNavController(view).navigate(R.id.action_adminDashboard_to_adminCatalogo)
            );
        }

        // Navegar a Métricas
        if (cardMetricas != null) {
            cardMetricas.setOnClickListener(v ->
                    Navigation.findNavController(view).navigate(R.id.action_adminDashboard_to_adminMetricas)
            );
        }

        // Botón de configuración de cuenta
        if (btnTheme != null) {
            btnTheme.setOnClickListener(v ->
                    Navigation.findNavController(view).navigate(R.id.action_adminDashboard_to_adminAccount)
            );
        }
    }
}
