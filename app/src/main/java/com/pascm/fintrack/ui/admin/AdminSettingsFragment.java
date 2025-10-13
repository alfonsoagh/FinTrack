package com.pascm.fintrack.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.pascm.fintrack.R;
import com.pascm.fintrack.data.TripPrefs;

public class AdminSettingsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View btnBack = view.findViewById(R.id.btnBack);
        View btnTemaClaro = view.findViewById(R.id.btnTemaClaro);
        View btnTemaOscuro = view.findViewById(R.id.btnTemaOscuro);
        View btnCerrarSesion = view.findViewById(R.id.btnCerrarSesion);

        if (btnBack != null) btnBack.setOnClickListener(v -> Navigation.findNavController(view).navigateUp());
        if (btnTemaClaro != null) btnTemaClaro.setOnClickListener(v -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO));
        if (btnTemaOscuro != null) btnTemaOscuro.setOnClickListener(v -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES));
        if (btnCerrarSesion != null) btnCerrarSesion.setOnClickListener(v -> {
            TripPrefs.clearAll(requireContext());
            Navigation.findNavController(view).navigate(R.id.action_global_logout_to_login);
        });
    }
}

