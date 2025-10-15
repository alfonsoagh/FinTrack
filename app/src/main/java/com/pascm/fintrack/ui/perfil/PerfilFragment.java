package com.pascm.fintrack.ui.perfil;

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
import com.pascm.fintrack.databinding.FragmentPerfilBinding;
import com.pascm.fintrack.data.TripPrefs;
import com.pascm.fintrack.data.repository.UserRepository;
import com.pascm.fintrack.data.local.entity.User;
import com.pascm.fintrack.data.local.entity.UserProfile;
import com.pascm.fintrack.util.SessionManager;

public class PerfilFragment extends Fragment {

    private FragmentPerfilBinding binding;
    private UserRepository userRepository;
    private User currentUser;
    private UserProfile userProfile;

    public PerfilFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPerfilBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize repository
        userRepository = new UserRepository(requireContext());

        // Load user data
        loadUserData();

        // Seleccionar item Perfil en el bottom nav
        binding.bottomNavigation.setSelectedItemId(R.id.nav_perfil);

        // Botón atrás
        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        // Bottom navigation funcional
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                Navigation.findNavController(view).navigate(R.id.action_perfil_to_home);
                return true;
            } else if (id == R.id.nav_viajes) {
                Navigation.findNavController(view).navigate(R.id.action_perfil_to_modo_viaje);
                return true;
            } else if (id == R.id.nav_lugares) {
                Navigation.findNavController(view).navigate(R.id.action_perfil_to_lugares);
                return true;
            } else if (id == R.id.nav_reportes) {
                Navigation.findNavController(view).navigate(R.id.action_perfil_to_reportes);
                return true;
            } else if (id == R.id.nav_perfil) {
                return true; // ya estamos aquí
            }
            return false;
        });

        // Acciones botones
        binding.btnGuardar.setOnClickListener(v -> saveUserChanges());

        binding.btnCerrarSesion.setOnClickListener(v -> {
            SessionManager.logout(requireContext());
            TripPrefs.clearAll(requireContext());
            Navigation.findNavController(view).navigate(R.id.action_global_logout_to_login);
        });

        // Acciones de tarjetas (opcional)
        binding.tvNombreValor.setOnClickListener(v ->
                Toast.makeText(requireContext(), getString(R.string.editar), Toast.LENGTH_SHORT).show()
        );
    }

    private void loadUserData() {
        long userId = SessionManager.getUserId(requireContext());

        if (userId == -1) {
            // User not logged in, redirect to login
            Navigation.findNavController(requireView()).navigate(R.id.action_global_logout_to_login);
            return;
        }

        // Load user data with LiveData observers
        userRepository.getUserById(userId).observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                currentUser = user;
                updateUserUI(user);
            }
        });

        userRepository.getUserProfile(userId).observe(getViewLifecycleOwner(), profile -> {
            if (profile != null) {
                userProfile = profile;
                updateProfileUI(profile);
            }
        });
    }

    private void updateUserUI(User user) {
        // Update email (locked field)
        binding.tvCorreo.setText(user.getEmail());
        binding.tvCorreoValor.setText(user.getEmail());
    }

    private void updateProfileUI(UserProfile profile) {
        // Update name
        String fullName = profile.getFullName() != null ? profile.getFullName() : "Usuario";
        binding.tvNombre.setText(fullName);
        binding.tvNombreValor.setText(fullName);

        // Update currency
        String currency = profile.getDefaultCurrency() != null ? profile.getDefaultCurrency() : "MXN";
        binding.tvMonedaValor.setText(getCurrencyDisplay(currency));

        // Update notification preferences
        binding.switchNotificaciones.setChecked(profile.isNotificationsEnabled());

        // Update GPS privacy
        binding.switchGps.setChecked(profile.isLocationEnabled());
    }

    private String getCurrencyDisplay(String currencyCode) {
        switch (currencyCode) {
            case "MXN":
                return "MXN - Peso Mexicano";
            case "USD":
                return "USD - Dólar Estadounidense";
            case "EUR":
                return "EUR - Euro";
            default:
                return currencyCode;
        }
    }

    private void saveUserChanges() {
        if (userProfile == null) {
            Toast.makeText(requireContext(), "Error al cargar perfil", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get current switch states
        boolean notificationsEnabled = binding.switchNotificaciones.isChecked();
        boolean locationEnabled = binding.switchGps.isChecked();

        // Update profile
        userProfile.setNotificationsEnabled(notificationsEnabled);
        userProfile.setLocationEnabled(locationEnabled);

        // Save to database
        userRepository.updateUserProfile(userProfile);

        Toast.makeText(requireContext(), "Cambios guardados exitosamente", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
