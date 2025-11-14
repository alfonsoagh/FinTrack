package com.pascm.fintrack.ui.movimiento;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.pascm.fintrack.BuildConfig;
import com.pascm.fintrack.R;
import com.pascm.fintrack.data.local.entity.Merchant;
import com.pascm.fintrack.databinding.DialogRegisterPlaceBinding;
import com.pascm.fintrack.util.LocationPermissionHelper;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

/**
 * Dialog para registrar una nueva ubicación/lugar personalizado
 * Permite ingresar: nombre, ubicación GPS (lat/lng), y foto
 */
public class RegisterPlaceDialogFragment extends DialogFragment {

    public interface PlaceRegistrationListener {
        void onPlaceRegistered(Merchant place, Uri photoUri);
    }

    private DialogRegisterPlaceBinding binding;
    private PlaceRegistrationListener listener;
    private Double latitude = null;
    private Double longitude = null;
    private Uri photoUri = null;
    private File photoFile = null;
    private FusedLocationProviderClient fusedLocationClient;

    private ActivityResultLauncher<Intent> takePictureLauncher;
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 2001;

    public static RegisterPlaceDialogFragment newInstance(PlaceRegistrationListener listener,
                                                          Double initialLat, Double initialLng) {
        RegisterPlaceDialogFragment fragment = new RegisterPlaceDialogFragment();
        fragment.listener = listener;
        fragment.latitude = initialLat;
        fragment.longitude = initialLng;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActivityResultLaunchers();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());
    }

    private void setupActivityResultLaunchers() {
        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        if (photoFile != null && photoFile.exists()) {
                            photoUri = Uri.fromFile(photoFile);
                            // Mostrar preview de la imagen
                            binding.ivPlacePhoto.setImageURI(photoUri);
                        }
                    }
                }
        );

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        photoUri = result.getData().getData();
                        if (photoUri != null) {
                            // Mostrar preview de la imagen
                            binding.ivPlacePhoto.setImageURI(photoUri);
                        }
                    }
                }
        );
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = DialogRegisterPlaceBinding.inflate(LayoutInflater.from(requireContext()));

        // Mostrar ubicación actual si está disponible
        updateLocationFields();

        // Botones de foto
        binding.btnTakePhoto.setOnClickListener(v -> takePicture());
        binding.btnSelectPhoto.setOnClickListener(v -> pickImageFromGallery());

        // Botón para buscar en mapa
        binding.btnSearchMap.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Función de mapa próximamente", Toast.LENGTH_SHORT).show();
        });

        // Botón para usar ubicación actual
        binding.btnUseLocation.setOnClickListener(v -> getCurrentLocationWithPermission());

        return new AlertDialog.Builder(requireContext())
                .setTitle("Registrar nueva ubicación")
                .setView(binding.getRoot())
                .setPositiveButton("Guardar", (dialog, which) -> savePlace())
                .setNegativeButton("Cancelar", null)
                .create();
    }

    private void savePlace() {
        String name = binding.etPlaceName.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(requireContext(), "Ingresa un nombre para el lugar", Toast.LENGTH_SHORT).show();
            return;
        }

        // Obtener latitud y longitud de los campos
        String latStr = binding.etLatitud.getText().toString().trim();
        String lngStr = binding.etLongitud.getText().toString().trim();

        if (latStr.isEmpty() || lngStr.isEmpty()) {
            Toast.makeText(requireContext(), "Ingresa las coordenadas o usa el botón de ubicación", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            latitude = Double.parseDouble(latStr);
            longitude = Double.parseDouble(lngStr);
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Coordenadas inválidas", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear merchant
        Merchant merchant = new Merchant();
        merchant.setName(name);
        merchant.setLatitude(latitude);
        merchant.setLongitude(longitude);

        if (listener != null) {
            listener.onPlaceRegistered(merchant, photoUri);
        }
    }

    private void updateLocationFields() {
        if (latitude != null && longitude != null) {
            binding.etLatitud.setText(String.format(Locale.US, "%.6f", latitude));
            binding.etLongitud.setText(String.format(Locale.US, "%.6f", longitude));
        }
    }

    public void updateLocation(Double lat, Double lng) {
        this.latitude = lat;
        this.longitude = lng;
        if (binding != null) {
            updateLocationFields();
        }
    }

    private void takePicture() {
        if (requireContext().checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
            return;
        }

        try {
            photoFile = createImageFile();
            if (photoFile != null) {
                Uri photoUriTemp = FileProvider.getUriForFile(
                        requireContext(),
                        BuildConfig.APPLICATION_ID + ".fileprovider",
                        photoFile
                );

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUriTemp);
                takePictureLauncher.launch(takePictureIntent);
            }
        } catch (IOException e) {
            Toast.makeText(requireContext(), "Error al crear archivo de foto", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String imageFileName = "PLACE_" + System.currentTimeMillis();
        File storageDir = requireContext().getCacheDir();
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    /**
     * Get current location with permission check
     */
    private void getCurrentLocationWithPermission() {
        if (!LocationPermissionHelper.hasFineLocationPermission(requireContext())) {
            if (LocationPermissionHelper.shouldShowLocationRationale(requireActivity())) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Permiso de ubicación necesario")
                        .setMessage(LocationPermissionHelper.getLocationPermissionExplanation(requireContext()))
                        .setPositiveButton("Conceder permiso", (dialog, which) ->
                                LocationPermissionHelper.requestLocationPermission(requireActivity()))
                        .setNegativeButton("Cancelar", null)
                        .show();
            } else {
                LocationPermissionHelper.requestLocationPermission(requireActivity());
            }
            return;
        }

        getCurrentLocationHighAccuracy();
    }

    /**
     * Get current location with high accuracy
     */
    @SuppressLint("MissingPermission")
    private void getCurrentLocationHighAccuracy() {
        Toast.makeText(requireContext(), "Obteniendo ubicación...", Toast.LENGTH_SHORT).show();

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        updateLocationFields();
                        Toast.makeText(requireContext(), "Ubicación obtenida", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Error al obtener ubicación: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                getCurrentLocationHighAccuracy();
            } else {
                Toast.makeText(requireContext(), "Permiso de ubicación denegado", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
