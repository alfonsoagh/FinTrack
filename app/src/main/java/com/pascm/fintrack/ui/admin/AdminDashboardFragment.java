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

        View btnBack = view.findViewById(R.id.btnBack);
        View cardUsuarios = view.findViewById(R.id.cardUsuarios);
        View cardConfig = view.findViewById(R.id.cardConfig);
        View cardCatalogo = view.findViewById(R.id.cardCatalogo);
        View cardPresupuestos = view.findViewById(R.id.cardPresupuestos);
        View cardTextos = view.findViewById(R.id.cardTextos);
        View cardAjustes = view.findViewById(R.id.cardAjustes);

        if (btnBack != null) btnBack.setOnClickListener(v -> Navigation.findNavController(view).navigateUp());
        if (cardUsuarios != null) cardUsuarios.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_adminDashboard_to_adminUsuarios)
        );
        if (cardConfig != null) cardConfig.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_adminDashboard_to_adminConfig)
        );
        if (cardCatalogo != null) cardCatalogo.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_adminDashboard_to_adminCatalogo)
        );
        if (cardPresupuestos != null) cardPresupuestos.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_adminDashboard_to_adminPresupuestos)
        );
        if (cardTextos != null) cardTextos.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_adminDashboard_to_adminTextos)
        );
        if (cardAjustes != null) cardAjustes.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_adminDashboard_to_adminSettings)
        );
    }
}
