package com.pascm.fintrack.ui.viaje;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.pascm.fintrack.R;
import com.pascm.fintrack.data.local.entity.Trip;
import com.pascm.fintrack.data.repository.TripRepository;
import com.pascm.fintrack.databinding.FragmentNuevoViajeBinding;
import com.pascm.fintrack.ui.lugar.MapPickerActivity;
import com.pascm.fintrack.util.LocationPermissionHelper;
import com.pascm.fintrack.util.SessionManager;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class NuevoViajeFragment extends Fragment {

    private FragmentNuevoViajeBinding binding;
    private TripRepository tripRepository;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationManager locationManager;

    private LocalDate startDate;
    private LocalDate endDate;
    private String selectedCurrency = "MXN";

    // Coordenadas de origen y destino
    private Double originLatitude;
    private Double originLongitude;
    private Double destinationLatitude;
    private Double destinationLongitude;

    private ActivityResultLauncher<Intent> originMapPickerLauncher;
    private ActivityResultLauncher<Intent> destinationMapPickerLauncher;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    public NuevoViajeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize map picker launchers
        originMapPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    double latitude = result.getData().getDoubleExtra(MapPickerActivity.EXTRA_LATITUDE, 0);
                    double longitude = result.getData().getDoubleExtra(MapPickerActivity.EXTRA_LONGITUDE, 0);
                    String placeName = result.getData().getStringExtra(MapPickerActivity.EXTRA_PLACE_NAME);

                    // Guardar coordenadas
                    originLatitude = latitude;
                    originLongitude = longitude;

                    // Set location as coordinates or place name
                    String locationText = placeName != null && !placeName.isEmpty() ?
                            placeName : String.format(Locale.US, "%.6f, %.6f", latitude, longitude);
                    binding.etOrigin.setText(locationText);

                    // Actualizar campos de coordenadas
                    binding.etOriginLat.setText(String.format(Locale.US, "%.6f", latitude));
                    binding.etOriginLng.setText(String.format(Locale.US, "%.6f", longitude));
                }
            }
        );

        destinationMapPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    double latitude = result.getData().getDoubleExtra(MapPickerActivity.EXTRA_LATITUDE, 0);
                    double longitude = result.getData().getDoubleExtra(MapPickerActivity.EXTRA_LONGITUDE, 0);
                    String placeName = result.getData().getStringExtra(MapPickerActivity.EXTRA_PLACE_NAME);

                    // Guardar coordenadas
                    destinationLatitude = latitude;
                    destinationLongitude = longitude;

                    // Set location as coordinates or place name
                    String locationText = placeName != null && !placeName.isEmpty() ?
                            placeName : String.format(Locale.US, "%.6f, %.6f", latitude, longitude);
                    binding.etDestination.setText(locationText);

                    // Actualizar campos de coordenadas
                    binding.etDestinationLat.setText(String.format(Locale.US, "%.6f", latitude));
                    binding.etDestinationLng.setText(String.format(Locale.US, "%.6f", longitude));
                }
            }
        );
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentNuevoViajeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize repository and location services
        tripRepository = new TripRepository(requireContext());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());
        locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);

        setupCurrencySpinner();
        setupDatePickers();
        setupListeners();
    }

    private void setupCurrencySpinner() {
        String[] currencies = getResources().getStringArray(R.array.currencies);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, currencies);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCurrency.setAdapter(adapter);

        // Set MXN as default
        int mxnPosition = 0;
        for (int i = 0; i < currencies.length; i++) {
            if (currencies[i].contains("MXN")) {
                mxnPosition = i;
                break;
            }
        }
        binding.spinnerCurrency.setSelection(mxnPosition);
    }

    private void setupDatePickers() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", new Locale("es", "MX"));

        // Start date picker
        binding.etStartDate.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Fecha de inicio")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build();

            datePicker.addOnPositiveButtonClickListener(selection -> {
                startDate = Instant.ofEpochMilli(selection)
                        .atZone(ZoneId.of("UTC"))
                        .toLocalDate();
                binding.etStartDate.setText(startDate.format(formatter));

                // Validar: si la fecha de inicio es posterior a la de fin, limpiar fecha de fin
                if (endDate != null && startDate.isAfter(endDate)) {
                    endDate = null;
                    binding.etEndDate.setText("");
                    Toast.makeText(requireContext(),
                            "La fecha de inicio es posterior a la de fin. Por favor, selecciona nuevamente la fecha de fin.",
                            Toast.LENGTH_LONG).show();
                }
            });

            datePicker.show(getParentFragmentManager(), "START_DATE_PICKER");
        });

        // End date picker
        binding.etEndDate.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Fecha de fin")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build();

            datePicker.addOnPositiveButtonClickListener(selection -> {
                LocalDate selectedEndDate = Instant.ofEpochMilli(selection)
                        .atZone(ZoneId.of("UTC"))
                        .toLocalDate();

                // Validar: fecha de fin puede ser igual o posterior a la de inicio
                if (startDate != null && selectedEndDate.isBefore(startDate)) {
                    Toast.makeText(requireContext(),
                            "La fecha de fin debe ser igual o posterior a la de inicio",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                endDate = selectedEndDate;
                binding.etEndDate.setText(endDate.format(formatter));
            });

            datePicker.show(getParentFragmentManager(), "END_DATE_PICKER");
        });
    }

    private void setupListeners() {
        // Botón cerrar (X) - regresa a Modo Viaje
        binding.btnClose.setOnClickListener(v ->
                Navigation.findNavController(v).navigateUp()
        );

        // Botón cancelar
        binding.btnCancel.setOnClickListener(v ->
                Navigation.findNavController(v).navigateUp()
        );

        // Botón seleccionar origen en mapa
        binding.btnSelectOriginMap.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), MapPickerActivity.class);
            originMapPickerLauncher.launch(intent);
        });

        // Botón seleccionar destino en mapa
        binding.btnSelectDestinationMap.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), MapPickerActivity.class);
            destinationMapPickerLauncher.launch(intent);
        });

        // Botón guardar viaje
        binding.btnSaveTrip.setOnClickListener(v -> validateAndSaveTrip());
    }

    /**
     * Muestra opciones para obtener ubicación actual o ingresar manualmente
     */
    private void showLocationOptions(boolean isOrigin) {
        new AlertDialog.Builder(requireContext())
                .setTitle(isOrigin ? "Ubicación de origen" : "Ubicación de destino")
                .setMessage("¿Cómo deseas ingresar la ubicación?")
                .setPositiveButton("Usar ubicación actual", (dialog, which) -> {
                    getCurrentLocationWithPermission(isOrigin);
                })
                .setNegativeButton("Ingresar manualmente", (dialog, which) -> {
                    // El usuario puede escribir directamente
                })
                .setNeutralButton("Cancelar", null)
                .show();
    }

    /**
     * Obtiene la ubicación actual con verificación de permisos
     * Usa Google Play Services (FusedLocationProviderClient) para ALTA PRECISIÓN
     */
    private void getCurrentLocationWithPermission(boolean isOrigin) {
        // Verificar si tiene permisos de ubicación de alta precisión
        if (!LocationPermissionHelper.hasFineLocationPermission(requireContext())) {
            // Mostrar explicación si es necesario
            if (LocationPermissionHelper.shouldShowLocationRationale(requireActivity())) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Permiso de ubicación necesario")
                        .setMessage(LocationPermissionHelper.getLocationPermissionExplanation(requireContext()))
                        .setPositiveButton("Conceder permiso", (dialog, which) -> {
                            LocationPermissionHelper.requestLocationPermission(requireActivity());
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
            } else {
                // Solicitar permisos directamente
                LocationPermissionHelper.requestLocationPermission(requireActivity());
            }
            return;
        }

        // Tenemos permisos, obtener ubicación de ALTA PRECISIÓN
        getCurrentLocationHighAccuracy(isOrigin);
    }

    /**
     * Obtiene la ubicación actual con ALTA PRECISIÓN usando FusedLocationProviderClient
     * Prioridad: PRIORITY_HIGH_ACCURACY (GPS + WiFi + Cellular)
     */
    @SuppressLint("MissingPermission")
    private void getCurrentLocationHighAccuracy(boolean isOrigin) {
        // Usar FusedLocationProviderClient con prioridad de ALTA PRECISIÓN
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        String locationText = String.format(Locale.US, "%.6f, %.6f",
                                location.getLatitude(), location.getLongitude());

                        if (isOrigin) {
                            binding.etOrigin.setText(locationText);
                        } else {
                            binding.etDestination.setText(locationText);
                        }

                        Toast.makeText(requireContext(),
                                "Ubicación obtenida con alta precisión (GPS)",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        // Fallback: intentar con último conocido
                        getLastKnownLocation(isOrigin);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(),
                            "Error al obtener ubicación: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    // Fallback
                    getLastKnownLocation(isOrigin);
                });
    }

    /**
     * Fallback: Obtiene la última ubicación conocida
     */
    @SuppressLint("MissingPermission")
    private void getLastKnownLocation(boolean isOrigin) {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        String locationText = String.format(Locale.US, "%.6f, %.6f",
                                location.getLatitude(), location.getLongitude());

                        if (isOrigin) {
                            binding.etOrigin.setText(locationText);
                        } else {
                            binding.etDestination.setText(locationText);
                        }

                        Toast.makeText(requireContext(),
                                "Ubicación obtenida (última conocida)",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(),
                                "No se pudo obtener la ubicación. Asegúrate de que el GPS esté activado.",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (LocationPermissionHelper.handlePermissionResult(requestCode, grantResults)) {
                Toast.makeText(requireContext(),
                        "Permiso de ubicación concedido. Nivel de precisión: " +
                        LocationPermissionHelper.getLocationAccuracyLevel(requireContext()),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(),
                        "Permiso de ubicación denegado. Puedes ingresar la ubicación manualmente.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Valida y guarda el viaje en la base de datos
     */
    private void validateAndSaveTrip() {
        // Validar nombre del viaje
        String tripName = binding.etTripName.getText().toString().trim();
        if (tripName.isEmpty()) {
            Toast.makeText(requireContext(), "Ingresa el nombre del viaje", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar fechas
        if (startDate == null || endDate == null) {
            Toast.makeText(requireContext(), "Selecciona las fechas del viaje", Toast.LENGTH_SHORT).show();
            return;
        }

        if (endDate.isBefore(startDate)) {
            Toast.makeText(requireContext(), "La fecha de fin debe ser posterior a la de inicio", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar presupuesto (OBLIGATORIO)
        String budgetStr = binding.etBudget.getText().toString().trim();
        if (budgetStr.isEmpty()) {
            Toast.makeText(requireContext(), "Ingresa el presupuesto del viaje", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar origen (OBLIGATORIO)
        String origin = binding.etOrigin.getText().toString().trim();
        if (origin.isEmpty()) {
            Toast.makeText(requireContext(), "Selecciona el origen del viaje", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar destino (OBLIGATORIO)
        String destination = binding.etDestination.getText().toString().trim();
        if (destination.isEmpty()) {
            Toast.makeText(requireContext(), "Selecciona el destino del viaje", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar coordenadas de origen (OBLIGATORIAS)
        try {
            String originLatStr = binding.etOriginLat.getText().toString().trim();
            String originLngStr = binding.etOriginLng.getText().toString().trim();
            if (originLatStr.isEmpty() || originLngStr.isEmpty()) {
                Toast.makeText(requireContext(), "Las coordenadas de origen son obligatorias. Usa el mapa para seleccionar el origen.", Toast.LENGTH_LONG).show();
                return;
            }
            originLatitude = Double.parseDouble(originLatStr);
            originLongitude = Double.parseDouble(originLngStr);
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Formato de coordenadas de origen inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar coordenadas de destino (OBLIGATORIAS)
        try {
            String destLatStr = binding.etDestinationLat.getText().toString().trim();
            String destLngStr = binding.etDestinationLng.getText().toString().trim();
            if (destLatStr.isEmpty() || destLngStr.isEmpty()) {
                Toast.makeText(requireContext(), "Las coordenadas de destino son obligatorias. Usa el mapa para seleccionar el destino.", Toast.LENGTH_LONG).show();
                return;
            }
            destinationLatitude = Double.parseDouble(destLatStr);
            destinationLongitude = Double.parseDouble(destLngStr);
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Formato de coordenadas de destino inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        // Parsear presupuesto (ya validado como obligatorio)
        Double budget = null;
        try {
            budget = Double.parseDouble(budgetStr);
            if (budget <= 0) {
                Toast.makeText(requireContext(), "El presupuesto debe ser mayor a cero", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Formato de presupuesto inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        // Obtener moneda seleccionada
        String currencySelection = binding.spinnerCurrency.getSelectedItem().toString();
        selectedCurrency = currencySelection.substring(0, 3); // Extraer código (MXN, USD, etc.)

        // Crear objeto Trip
        Trip trip = new Trip();
        trip.setUserId(SessionManager.getUserId(requireContext()));
        trip.setName(tripName);
        trip.setOrigin(origin); // Ya validado como obligatorio
        trip.setDestination(destination); // Ya validado como obligatorio

        // Guardar coordenadas (ya validadas como obligatorias)
        trip.setOriginLatitude(originLatitude);
        trip.setOriginLongitude(originLongitude);
        trip.setDestinationLatitude(destinationLatitude);
        trip.setDestinationLongitude(destinationLongitude);

        trip.setStartDate(startDate);
        trip.setEndDate(endDate);
        trip.setBudgetAmount(budget); // Ya validado como obligatorio
        trip.setCurrencyCode(selectedCurrency);
        trip.setStatus(Trip.TripStatus.ACTIVE);

        // Guardar en base de datos
        tripRepository.insertTrip(trip, tripId -> {
            requireActivity().runOnUiThread(() -> {
                Toast.makeText(requireContext(),
                        "Viaje guardado: " + tripName,
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
