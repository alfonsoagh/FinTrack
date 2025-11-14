package com.pascm.fintrack.ui.movimiento;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

/**
 * Dialog para elegir opciones de ubicación al agregar un gasto:
 * - Usar ubicación actual (GPS)
 * - Elegir un lugar frecuente guardado
 * - Registrar nueva ubicación personalizada
 */
public class LocationOptionsDialogFragment extends DialogFragment {

    public interface LocationOptionListener {
        void onUseCurrentLocation();
        void onChooseFrequentPlace();
        void onRegisterNewPlace();
    }

    private LocationOptionListener listener;

    public static LocationOptionsDialogFragment newInstance(LocationOptionListener listener) {
        LocationOptionsDialogFragment fragment = new LocationOptionsDialogFragment();
        fragment.listener = listener;
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        String[] options = {
                "Usar ubicación actual (GPS)",
                "Elegir lugar guardado",
                "Registrar nueva ubicación"
        };

        return new AlertDialog.Builder(requireContext())
                .setTitle("Ubicación del gasto")
                .setItems(options, (dialog, which) -> {
                    if (listener != null) {
                        switch (which) {
                            case 0:
                                listener.onUseCurrentLocation();
                                break;
                            case 1:
                                listener.onChooseFrequentPlace();
                                break;
                            case 2:
                                listener.onRegisterNewPlace();
                                break;
                        }
                    }
                })
                .setNegativeButton("Cancelar", null)
                .create();
    }
}
