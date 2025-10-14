package com.pascm.fintrack.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.pascm.fintrack.R;

public class AdminCatalogoFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_catalogo, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Botón volver
        ImageButton btnBack = view.findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> Navigation.findNavController(view).navigateUp());
        }

        // Botón flotante para agregar
        FloatingActionButton fabAdd = view.findViewById(R.id.fabAdd);
        if (fabAdd != null) {
            fabAdd.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Agregar nueva categoría (demo)", Toast.LENGTH_SHORT).show()
            );
        }

        // Botones de edición - Gastos
        setupCategoryButtons(view, R.id.btnEditComida, R.id.btnDeleteComida, "Comida");
        setupCategoryButtons(view, R.id.btnEditTransporte, R.id.btnDeleteTransporte, "Transporte");
        setupCategoryButtons(view, R.id.btnEditOcio, R.id.btnDeleteOcio, "Ocio");
        setupCategoryButtons(view, R.id.btnEditHogar, R.id.btnDeleteHogar, "Hogar");
        setupCategoryButtons(view, R.id.btnEditSalud, R.id.btnDeleteSalud, "Salud");

        // Botones de edición - Ingresos
        setupCategoryButtons(view, R.id.btnEditSalario, R.id.btnDeleteSalario, "Salario");
        setupCategoryButtons(view, R.id.btnEditBonos, R.id.btnDeleteBonos, "Bonos");
        setupCategoryButtons(view, R.id.btnEditFreelance, R.id.btnDeleteFreelance, "Freelance");

        // Clicks en las tarjetas
        setupCardClick(view, R.id.cardComida, "Comida");
        setupCardClick(view, R.id.cardTransporte, "Transporte");
        setupCardClick(view, R.id.cardOcio, "Ocio");
        setupCardClick(view, R.id.cardHogar, "Hogar");
        setupCardClick(view, R.id.cardSalud, "Salud");
        setupCardClick(view, R.id.cardSalario, "Salario");
        setupCardClick(view, R.id.cardBonos, "Bonos");
        setupCardClick(view, R.id.cardFreelance, "Freelance");
    }

    private void setupCategoryButtons(View parent, int editButtonId, int deleteButtonId, String categoryName) {
        ImageButton btnEdit = parent.findViewById(editButtonId);
        ImageButton btnDelete = parent.findViewById(deleteButtonId);

        if (btnEdit != null) {
            btnEdit.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Editar " + categoryName + " (demo)", Toast.LENGTH_SHORT).show()
            );
        }

        if (btnDelete != null) {
            btnDelete.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Eliminar " + categoryName + " (demo)", Toast.LENGTH_SHORT).show()
            );
        }
    }

    private void setupCardClick(View parent, int cardId, String categoryName) {
        View card = parent.findViewById(cardId);
        if (card != null) {
            card.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Ver detalles de " + categoryName + " (demo)", Toast.LENGTH_SHORT).show()
            );
        }
    }
}

