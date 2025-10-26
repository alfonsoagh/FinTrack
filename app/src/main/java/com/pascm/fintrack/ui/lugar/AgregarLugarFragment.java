package com.pascm.fintrack.ui.lugar;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.pascm.fintrack.BuildConfig;
import com.pascm.fintrack.R;
import com.pascm.fintrack.data.local.entity.Merchant;
import com.pascm.fintrack.data.repository.PlaceRepository;
import com.pascm.fintrack.databinding.FragmentAgregarLugarBinding;
import com.pascm.fintrack.util.ImageHelper;
import com.pascm.fintrack.util.LocationPermissionHelper;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;

public class AgregarLugarFragment extends Fragment {

    private FragmentAgregarLugarBinding binding;
    private PlaceRepository placeRepository;
    private FusedLocationProviderClient fusedLocationClient;

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1002;

    // Photo management
    private Uri selectedPhotoUri = null;
    private File photoFile = null;

    // Location
    private Double currentLatitude = null;
    private Double currentLongitude = null;

    // Activity result launchers
    private ActivityResultLauncher<Intent> takePictureLauncher;
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private ActivityResultLauncher<Intent> placesAutocompleteLauncher;
    private ActivityResultLauncher<Intent> mapPickerLauncher;

    public AgregarLugarFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Places API if not already initialized
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), getString(R.string.google_maps_key));
        }

        // Initialize activity result launchers
        setupActivityResultLaunchers();
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

        // Initialize repository and location services
        placeRepository = new PlaceRepository(requireContext());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        setupListeners();
    }

    /**
     * Setup activity result launchers for camera, gallery, and places
     */
    private void setupActivityResultLaunchers() {
        // Take picture launcher
        takePictureLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    if (photoFile != null && photoFile.exists()) {
                        selectedPhotoUri = Uri.fromFile(photoFile);
                        binding.ivPlacePhoto.setImageURI(selectedPhotoUri);
                        Toast.makeText(requireContext(), "Foto capturada", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        );

        // Pick image launcher
        pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedPhotoUri = result.getData().getData();
                    if (selectedPhotoUri != null) {
                        binding.ivPlacePhoto.setImageURI(selectedPhotoUri);
                        Toast.makeText(requireContext(), "Foto seleccionada", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        );

        // Places Autocomplete launcher
        placesAutocompleteLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Place place = Autocomplete.getPlaceFromIntent(result.getData());

                    // Set place name if not already filled
                    if (binding.etNombreLugar.getText().toString().trim().isEmpty()) {
                        binding.etNombreLugar.setText(place.getName());
                    }

                    // Set address
                    if (place.getAddress() != null) {
                        binding.etNota.setText(place.getAddress());
                    }

                    // Set coordinates
                    if (place.getLatLng() != null) {
                        currentLatitude = place.getLatLng().latitude;
                        currentLongitude = place.getLatLng().longitude;
                        binding.etLatitud.setText(String.format(Locale.US, "%.6f", currentLatitude));
                        binding.etLongitud.setText(String.format(Locale.US, "%.6f", currentLongitude));
                    }

                    Toast.makeText(requireContext(), "Lugar seleccionado: " + place.getName(),
                        Toast.LENGTH_SHORT).show();
                } else if (result.getResultCode() == AutocompleteActivity.RESULT_ERROR && result.getData() != null) {
                    Status status = Autocomplete.getStatusFromIntent(result.getData());
                    Toast.makeText(requireContext(), "Error: " + status.getStatusMessage(),
                        Toast.LENGTH_SHORT).show();
                }
            }
        );

        // Map Picker launcher
        mapPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    double latitude = result.getData().getDoubleExtra(MapPickerActivity.EXTRA_LATITUDE, 0);
                    double longitude = result.getData().getDoubleExtra(MapPickerActivity.EXTRA_LONGITUDE, 0);

                    currentLatitude = latitude;
                    currentLongitude = longitude;

                    binding.etLatitud.setText(String.format(Locale.US, "%.6f", currentLatitude));
                    binding.etLongitud.setText(String.format(Locale.US, "%.6f", currentLongitude));

                    Toast.makeText(requireContext(), "Ubicación seleccionada en el mapa",
                        Toast.LENGTH_SHORT).show();
                }
            }
        );
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
        binding.btnUseLocation.setOnClickListener(v -> getCurrentLocationWithPermission());

        // Botón buscar en mapa - Mostrar opciones
        binding.btnSearchMap.setOnClickListener(v -> showMapOptions());

        // Botón tomar foto
        binding.btnTakePhoto.setOnClickListener(v -> takePicture());

        // Botón seleccionar de galería
        binding.btnSelectPhoto.setOnClickListener(v -> pickImageFromGallery());

        // Botón guardar lugar
        binding.btnSavePlace.setOnClickListener(v -> validateAndSavePlace());
    }

    /**
     * Show map options: Search places or pick location on map
     */
    private void showMapOptions() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Seleccionar ubicación")
                .setMessage("¿Cómo deseas seleccionar la ubicación?")
                .setPositiveButton("Buscar lugar", (dialog, which) -> openPlacesPicker())
                .setNegativeButton("Marcar en mapa", (dialog, which) -> openMapPicker())
                .setNeutralButton("Cancelar", null)
                .show();
    }

    /**
     * Open Google Places Picker
     */
    private void openPlacesPicker() {
        try {
            // Define place fields to return
            java.util.List<Place.Field> fields = Arrays.asList(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.LAT_LNG,
                Place.Field.TYPES
            );

            // Create autocomplete intent
            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                .build(requireContext());

            placesAutocompleteLauncher.launch(intent);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error al abrir búsqueda: " + e.getMessage(),
                Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Open map picker to select location manually
     */
    private void openMapPicker() {
        Intent intent = new Intent(requireContext(), MapPickerActivity.class);
        mapPickerLauncher.launch(intent);
    }

    /**
     * Take picture with camera
     */
    private void takePicture() {
        // Check camera permission
        if (requireContext().checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
            return;
        }

        try {
            // Create temp file for photo
            photoFile = createImageFile();

            if (photoFile != null) {
                Uri photoUri = FileProvider.getUriForFile(
                    requireContext(),
                    BuildConfig.APPLICATION_ID + ".fileprovider",
                    photoFile
                );

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                takePictureLauncher.launch(takePictureIntent);
            }
        } catch (IOException e) {
            Toast.makeText(requireContext(), "Error al crear archivo de foto", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Create temp image file
     */
    private File createImageFile() throws IOException {
        String imageFileName = "PLACE_" + System.currentTimeMillis();
        File storageDir = requireContext().getCacheDir();
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    /**
     * Pick image from gallery
     */
    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    /**
     * Get current location with permission check
     */
    private void getCurrentLocationWithPermission() {
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
        getCurrentLocationHighAccuracy();
    }

    /**
     * Get current location with high accuracy using FusedLocationProviderClient
     */
    @SuppressLint("MissingPermission")
    private void getCurrentLocationHighAccuracy() {
        // Usar FusedLocationProviderClient con prioridad de ALTA PRECISIÓN
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        currentLatitude = location.getLatitude();
                        currentLongitude = location.getLongitude();

                        binding.etLatitud.setText(String.format(Locale.US, "%.6f", currentLatitude));
                        binding.etLongitud.setText(String.format(Locale.US, "%.6f", currentLongitude));

                        Toast.makeText(requireContext(),
                                "Ubicación obtenida con alta precisión (GPS)",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        // Fallback: intentar con último conocido
                        getLastKnownLocation();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(),
                            "Error al obtener ubicación: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    // Fallback
                    getLastKnownLocation();
                });
    }

    /**
     * Fallback: Get last known location
     */
    @SuppressLint("MissingPermission")
    private void getLastKnownLocation() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        currentLatitude = location.getLatitude();
                        currentLongitude = location.getLongitude();

                        binding.etLatitud.setText(String.format(Locale.US, "%.6f", currentLatitude));
                        binding.etLongitud.setText(String.format(Locale.US, "%.6f", currentLongitude));

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

        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePicture();
            } else {
                Toast.makeText(requireContext(), "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == LocationPermissionHelper.LOCATION_PERMISSION_REQUEST_CODE) {
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
     * Validate and save place to database
     */
    private void validateAndSavePlace() {
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

        // Guardar en base de datos con callback para guardar la foto después
        placeRepository.insertPlace(place, placeId -> {
            // Save photo if selected
            if (selectedPhotoUri != null) {
                String photoPath = ImageHelper.savePlacePhoto(requireContext(), selectedPhotoUri, placeId);

                if (photoPath != null) {
                    // Update place with photo path
                    place.setMerchantId(placeId);
                    place.setPhotoUrl(photoPath);
                    placeRepository.updatePlace(place);
                }
            }

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
