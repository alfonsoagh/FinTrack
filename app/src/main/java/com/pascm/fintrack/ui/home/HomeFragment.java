package com.pascm.fintrack.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.pascm.fintrack.R;
import com.pascm.fintrack.data.repository.HomeRepository;
import com.pascm.fintrack.databinding.FragmentHomeBinding;

import java.text.NumberFormat;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeRepository homeRepository;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize repository
        homeRepository = new HomeRepository(requireContext());

        // Ensure correct selected item in bottom nav when on Home
        binding.bottomNavigation.setSelectedItemId(R.id.nav_home);

        setupListeners();
        loadData();
    }

    private void setupListeners() {
        // Notification button -> navigate to Recordatorios
        binding.btnNotifications.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_home_to_recordatorios)
        );

        // Account cards - navegar a listas de tarjetas
        // Efectivo - sin acción
        binding.cardEfectivo.setOnClickListener(null);

        binding.cardCredito.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_home_to_credit_cards)
        );

        binding.cardDebito.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_home_to_debit_cards)
        );

        // Suggested actions
        binding.cardModoViaje.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_home_to_modo_viaje)
        );

        binding.cardReportes.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_home_to_reportes)
        );

        binding.cardLugaresFrecuentes.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_home_to_lugares)
        );

        // Bottom navigation
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                // Already on home
                return true;
            } else if (itemId == R.id.nav_viajes) {
                // Navigate to Modo Viaje
                Navigation.findNavController(requireView()).navigate(R.id.action_home_to_modo_viaje);
                return true;
            } else if (itemId == R.id.nav_lugares) {
                // Navigate to Lugares
                Navigation.findNavController(requireView()).navigate(R.id.action_home_to_lugares);
                return true;
            } else if (itemId == R.id.nav_reportes) {
                // Navigate to Reportes
                Navigation.findNavController(requireView()).navigate(R.id.action_home_to_reportes);
                return true;
            } else if (itemId == R.id.nav_perfil) {
                // Navigate to Perfil
                Navigation.findNavController(requireView()).navigate(R.id.action_home_to_perfil);
                return true;
            }
            return false;
        });

        // FAB - Add new movement
        binding.fabAddMovement.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_home_to_agregar_movimiento)
        );
    }

    private void loadData() {
        // Cargar balance total
        homeRepository.getTotalBalance().observe(getViewLifecycleOwner(), total -> {
            if (total != null) {
                updateTotalBalance(total);
            }
        });

        // Cargar balance de efectivo
        homeRepository.getCashBalance().observe(getViewLifecycleOwner(), info -> {
            if (info != null) {
                binding.txtCashBalance.setText(formatCurrency(info.balance));
                binding.txtCashCount.setText(info.getCountText());
            }
        });

        // Cargar balance de crédito
        homeRepository.getCreditBalance().observe(getViewLifecycleOwner(), info -> {
            if (info != null) {
                binding.txtCreditBalance.setText(formatCurrency(info.balance));
                binding.txtCreditCount.setText(info.getCountText());
            }
        });

        // Cargar balance de débito
        homeRepository.getDebitBalance().observe(getViewLifecycleOwner(), info -> {
            if (info != null) {
                binding.txtDebitBalance.setText(formatCurrency(info.balance));
                binding.txtDebitCount.setText(info.getCountText());
            }
        });
    }

    /**
     * Actualiza el balance total separando pesos y centavos
     */
    private void updateTotalBalance(double total) {
        int pesos = (int) total;
        int centavos = (int) Math.round((total - pesos) * 100);

        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
        String pesosFormatted = "$" + numberFormat.format(pesos);
        String centavosFormatted = String.format(Locale.US, ".%02d", centavos);

        binding.txtTotalBalance.setText(pesosFormatted);
        binding.txtTotalCents.setText(centavosFormatted);
    }

    /**
     * Formatea una cantidad como moneda
     */
    private String formatCurrency(double amount) {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));
        return format.format(amount);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
