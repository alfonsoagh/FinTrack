package com.pascm.fintrack.ui.login;

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
import com.pascm.fintrack.data.repository.UserRepository;
import com.pascm.fintrack.databinding.FragmentLoginBinding;
import com.pascm.fintrack.util.SessionManager;
import com.pascm.fintrack.util.PlacesManager;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private UserRepository userRepository;

    public LoginFragment() { /* Required empty public constructor */ }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize repository
        userRepository = new UserRepository(requireContext());

        // Check if already logged in
        if (SessionManager.isLoggedIn(requireContext())) {
            Navigation.findNavController(view).navigate(R.id.action_login_to_home);
            return;
        }

        binding.btnLogin.setOnClickListener(v -> performLogin());

        binding.btnCreateAccount.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_login_to_registro)
        );
    }

    private void performLogin() {
        String email = String.valueOf(binding.edtEmail.getText()).trim();
        String password = String.valueOf(binding.edtPassword.getText()).trim();

        if (email.isEmpty()) {
            binding.edtEmail.setError("Ingresa tu email");
            binding.edtEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            binding.edtPassword.setError("Ingresa tu contraseña");
            binding.edtPassword.requestFocus();
            return;
        }

        binding.btnLogin.setEnabled(false);
        binding.btnLogin.setText("Iniciando sesión...");

        userRepository.loginUser(email, password, result -> {
            requireActivity().runOnUiThread(() -> {
                binding.btnLogin.setEnabled(true);
                binding.btnLogin.setText("Iniciar sesión");

                if (result.isSuccess()) {
                    SessionManager.login(requireContext(), result.getUser());

                    // Resetear Lugares para demo: siempre iniciar sin lugares
                    PlacesManager.setHasPlaces(requireContext(), false);

                    Toast.makeText(requireContext(), "Bienvenido " + result.getUser().getEmail(), Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).navigate(R.id.action_login_to_home);
                } else {
                    Toast.makeText(requireContext(), result.getErrorMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
