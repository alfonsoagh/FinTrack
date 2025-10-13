package com.pascm.fintrack.ui.cards;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.pascm.fintrack.R;
import com.pascm.fintrack.util.CardsManager;

public class AddCardFragment extends Fragment {

    private String type;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_card, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle args = getArguments();
        if (args != null) type = args.getString("type", CardsManager.TYPE_CREDIT);
        else type = CardsManager.TYPE_CREDIT;

        View btnBack = view.findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> Navigation.findNavController(view).navigateUp());

        EditText edtName = view.findViewById(R.id.edtCardName);
        MaterialButton btnSave = view.findViewById(R.id.btnSaveCard);
        MaterialButton btnCancel = view.findViewById(R.id.btnCancel);

        if (btnCancel != null) btnCancel.setOnClickListener(v -> Navigation.findNavController(view).navigateUp());
        if (btnSave != null) btnSave.setOnClickListener(v -> {
            String name = edtName != null ? edtName.getText().toString() : "";
            if (name.trim().isEmpty()) {
                Toast.makeText(requireContext(), R.string.nombre_tarjeta_requerido, Toast.LENGTH_SHORT).show();
                return;
            }
            CardsManager.addCard(requireContext(), type, name);
            Toast.makeText(requireContext(), R.string.tarjeta_guardada, Toast.LENGTH_SHORT).show();
            Navigation.findNavController(view).navigateUp();
        });
    }
}

