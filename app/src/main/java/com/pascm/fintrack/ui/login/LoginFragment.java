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
import com.pascm.fintrack.databinding.FragmentLoginBinding;
import com.pascm.fintrack.data.TripPrefs;
import com.pascm.fintrack.util.PlacesManager;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;

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

        binding.btnLogin.setOnClickListener(v -> {
            String email = String.valueOf(binding.edtEmail.getText()).trim();
            String pass = String.valueOf(binding.edtPassword.getText()).trim();

            if ("user".equalsIgnoreCase(email) && "123".equals(pass)) {
                TripPrefs.setActiveTrip(requireContext(), false); // reset Modo Viaje
                PlacesManager.setHasPlaces(requireContext(), false); // reset Lugares
                Navigation.findNavController(v).navigate(R.id.action_login_to_home);
            } else if ("admin".equalsIgnoreCase(email) && "321".equals(pass)) {
                TripPrefs.setActiveTrip(requireContext(), false); // reset Modo Viaje
                PlacesManager.setHasPlaces(requireContext(), false); // reset Lugares
                Navigation.findNavController(v).navigate(R.id.action_login_to_admin);
            } else {
                Toast.makeText(requireContext(), "Credenciales inválidas", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnCreateAccount.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Acción simulada: Crear cuenta", Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
