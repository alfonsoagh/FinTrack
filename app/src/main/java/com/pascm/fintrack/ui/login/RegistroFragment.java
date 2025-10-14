package com.pascm.fintrack.ui.login;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.pascm.fintrack.R;
import com.pascm.fintrack.databinding.FragmentRegistroBinding;

public class RegistroFragment extends Fragment {

    private FragmentRegistroBinding binding;

    public RegistroFragment() { /* Required empty public constructor */ }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentRegistroBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Configurar el dropdown de monedas
        String[] currencies = {"MXN", "USD", "EUR"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                currencies
        );
        binding.actvMoneda.setAdapter(adapter);

        // Botón de regresar
        binding.btnBack.setOnClickListener(v ->
                Navigation.findNavController(v).navigateUp()
        );

        // Botón de crear cuenta
        binding.btnCrearCuenta.setOnClickListener(v -> {
            if (validarFormulario()) {
                crearCuenta();
            }
        });
    }

    private boolean validarFormulario() {
        String nombre = String.valueOf(binding.edtNombreCompleto.getText()).trim();
        String email = String.valueOf(binding.edtEmail.getText()).trim();
        String password = String.valueOf(binding.edtPassword.getText()).trim();
        String confirmPassword = String.valueOf(binding.edtConfirmPassword.getText()).trim();

        // Validar nombre completo
        if (TextUtils.isEmpty(nombre)) {
            binding.tilNombreCompleto.setError("Ingresa tu nombre completo");
            binding.edtNombreCompleto.requestFocus();
            return false;
        } else {
            binding.tilNombreCompleto.setError(null);
        }

        // Validar email
        if (TextUtils.isEmpty(email)) {
            binding.tilEmail.setError("Ingresa tu correo electrónico");
            binding.edtEmail.requestFocus();
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError("Ingresa un correo válido");
            binding.edtEmail.requestFocus();
            return false;
        } else {
            binding.tilEmail.setError(null);
        }

        // Validar contraseña
        if (TextUtils.isEmpty(password)) {
            binding.tilPassword.setError("Ingresa una contraseña");
            binding.edtPassword.requestFocus();
            return false;
        } else if (password.length() < 8) {
            binding.tilPassword.setError("La contraseña debe tener al menos 8 caracteres");
            binding.edtPassword.requestFocus();
            return false;
        } else if (!password.matches(".*[A-Z].*")) {
            binding.tilPassword.setError("Debe contener al menos una mayúscula");
            binding.edtPassword.requestFocus();
            return false;
        } else if (!password.matches(".*[a-z].*")) {
            binding.tilPassword.setError("Debe contener al menos una minúscula");
            binding.edtPassword.requestFocus();
            return false;
        } else if (!password.matches(".*\\d.*")) {
            binding.tilPassword.setError("Debe contener al menos un número");
            binding.edtPassword.requestFocus();
            return false;
        } else {
            binding.tilPassword.setError(null);
        }

        // Validar confirmación de contraseña
        if (TextUtils.isEmpty(confirmPassword)) {
            binding.tilConfirmPassword.setError("Confirma tu contraseña");
            binding.edtConfirmPassword.requestFocus();
            return false;
        } else if (!password.equals(confirmPassword)) {
            binding.tilConfirmPassword.setError("Las contraseñas no coinciden");
            binding.edtConfirmPassword.requestFocus();
            return false;
        } else {
            binding.tilConfirmPassword.setError(null);
        }

        return true;
    }

    private void crearCuenta() {
        String nombre = String.valueOf(binding.edtNombreCompleto.getText()).trim();
        String email = String.valueOf(binding.edtEmail.getText()).trim();
        String password = String.valueOf(binding.edtPassword.getText()).trim();
        String moneda = String.valueOf(binding.actvMoneda.getText()).trim();

        // TODO: Aquí implementarías la lógica real de registro
        // Por ahora, solo mostramos un mensaje y volvemos al login

        Toast.makeText(requireContext(),
                "Cuenta creada exitosamente para " + nombre,
                Toast.LENGTH_LONG).show();

        // Navegar de regreso al login
        Navigation.findNavController(requireView()).navigateUp();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
