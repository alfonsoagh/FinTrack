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
import com.pascm.fintrack.data.TripPrefs;

public class AdminAccountFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Botón cerrar
        view.findViewById(R.id.btnClose).setOnClickListener(v ->
                Navigation.findNavController(view).navigateUp()
        );

        // Botón cerrar sesión
        view.findViewById(R.id.btnLogout).setOnClickListener(v -> {
            // Limpiar preferencias
            TripPrefs.clearAll(requireContext());
            // Navegar al login
            Navigation.findNavController(view).navigate(R.id.action_global_logout_to_login);
        });
    }
}
