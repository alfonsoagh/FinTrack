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

public class AdminPermisosFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_permisos, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View btnBack = view.findViewById(R.id.btnBack);
        View btnGuardar = view.findViewById(R.id.btnGuardar);

        if (btnBack != null) btnBack.setOnClickListener(v -> Navigation.findNavController(view).navigateUp());
        if (btnGuardar != null) btnGuardar.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Permisos guardados (demo)", Toast.LENGTH_SHORT).show()
        );
    }
}
