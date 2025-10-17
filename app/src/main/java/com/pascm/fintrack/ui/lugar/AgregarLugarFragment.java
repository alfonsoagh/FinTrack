package com.pascm.fintrack.ui.lugar;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.pascm.fintrack.R;
import com.pascm.fintrack.data.local.entity.Merchant;
import com.pascm.fintrack.data.repository.PlaceRepository;
import com.pascm.fintrack.databinding.FragmentAgregarLugarBinding;

public class AgregarLugarFragment extends Fragment {

    private FragmentAgregarLugarBinding binding;
    private PlaceRepository placeRepository;
    private LocationManager locationManager;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private Double currentLatitude = null;
    private Double currentLongitude = null;

    public AgregarLugarFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAgregarLugarBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize repository
        placeRepository = new PlaceRepository(requireContext());
        locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);

        setupListeners();
    }

    private void setupListeners() {
        // Botón volver atrás
        binding.btnBack.setOnClickListener(v ->
                Navigation.findNavController(v).navigateUp()
        );

        // Botón cancelar
        binding.btnCancel.setOnClickListener(v ->
                Navigation.findNavController(v).navigateUp()
        );

        // Botón usar ubicación actual
        binding.btnUseLocation.setOnClickListener(v -> getCurrentLocation());

        // Botón buscar en mapa
        binding.btnSearchMap.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Buscar en mapa (próximamente)", Toast.LENGTH_SHORT).show()
        );

        // Botón guardar lugar
        binding.btnSavePlace.setOnClickListener(v -> savePlaceInBackground());
    }

    /**
     * Obtiene la ubicación actual del dispositivo usando LocationManager
     */
    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        // Verificar permisos de ubicación
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Solicitar permisos
            requestPermissions(
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                             Manifest.permission.ACCESS_COARSE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE
            );
            return;
        }

        try {
            // Intentar obtener la última ubicación conocida
            Location location = null;

            // Intentar primero con GPS
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }

            // Si no hay ubicación del GPS, intentar con Network
            if (location == null && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

            if (location != null) {
                currentLatitude = location.getLatitude();
                currentLongitude = location.getLongitude();

                // Actualizar campos de texto
                binding.etLatitud.setText(String.valueOf(currentLatitude));
                binding.etLongitud.setText(String.valueOf(currentLongitude));

                Toast.makeText(requireContext(),
                    "Ubicación obtenida: " + String.format("%.6f, %.6f", currentLatitude, currentLongitude),
                    Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(),
                    "No se pudo obtener la ubicación. Asegúrate de que el GPS esté activado.",
                    Toast.LENGTH_LONG).show();
            }
        } catch (SecurityException e) {
            Toast.makeText(requireContext(),
                "Error de permisos: " + e.getMessage(),
                Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(requireContext(),
                "Error al obtener ubicación: " + e.getMessage(),
                Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, intentar obtener ubicación nuevamente
                getCurrentLocation();
            } else {
                Toast.makeText(requireContext(),
                    "Permiso de ubicación denegado. Puedes ingresar las coordenadas manualmente.",
                    Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Valida y guarda el lugar en un thread de background
     */
    private void savePlaceInBackground() {
        // Validar nombre del lugar
        String placeName = binding.etNombreLugar.getText().toString().trim();
        if (placeName.isEmpty()) {
            Toast.makeText(requireContext(), "Ingresa el nombre del lugar", Toast.LENGTH_SHORT).show();
            return;
        }

        // Obtener datos opcionales
        String note = binding.etNota.getText().toString().trim();

        // Obtener coordenadas (de los campos de texto o de la ubicación obtenida)
        Double latitude = null;
        Double longitude = null;

        String latStr = binding.etLatitud.getText().toString().trim();
        String lngStr = binding.etLongitud.getText().toString().trim();

        if (!latStr.isEmpty() && !lngStr.isEmpty()) {
            try {
                latitude = Double.parseDouble(latStr);
                longitude = Double.parseDouble(lngStr);

                // Validar rangos de coordenadas
                if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
                    Toast.makeText(requireContext(), "Coordenadas inválidas", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Formato de coordenadas inválido", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Crear objeto Merchant
        Merchant place = new Merchant();
        place.setName(placeName);
        place.setAddress(note.isEmpty() ? null : note);
        place.setLatitude(latitude);
        place.setLongitude(longitude);
        place.setFrequent(false);
        place.setUsageCount(0);

        // Guardar en base de datos
        placeRepository.insertPlace(place, placeId -> {
            requireActivity().runOnUiThread(() -> {
                Toast.makeText(requireContext(),
                    "Lugar guardado: " + placeName,
                    Toast.LENGTH_SHORT).show();
                Navigation.findNavController(requireView()).navigateUp();
            });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
