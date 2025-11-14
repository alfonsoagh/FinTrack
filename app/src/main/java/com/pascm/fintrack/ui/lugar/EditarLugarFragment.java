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

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.pascm.fintrack.BuildConfig;
import com.pascm.fintrack.R;
import com.pascm.fintrack.data.local.entity.Merchant;
import com.pascm.fintrack.data.repository.PlaceRepository;
import com.pascm.fintrack.databinding.FragmentEditarLugarBinding;
import com.pascm.fintrack.util.ImageHelper;
import com.pascm.fintrack.util.LocationPermissionHelper;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

public class EditarLugarFragment extends Fragment {

    private FragmentEditarLugarBinding binding;
    private PlaceRepository placeRepository;
    private FusedLocationProviderClient fusedLocationClient;

    private long placeId;
    private Merchant currentPlace;
    private Uri photoUri = null;
    private File photoFile = null;
    private boolean photoChanged = false;

    private ActivityResultLauncher<Intent> takePictureLauncher;
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 3001;

    public EditarLugarFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActivityResultLaunchers();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEditarLugarBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        placeRepository = new PlaceRepository(requireContext());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        // Obtener el ID del lugar a editar
        if (getArguments() != null) {
            placeId = getArguments().getLong("placeId", -1);
            if (placeId != -1) {
                loadPlace();
            } else {
                Toast.makeText(requireContext(), "Error: lugar no encontrado", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(view).navigateUp();
            }
        }

        setupListeners();
    }

    private void setupActivityResultLaunchers() {
        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        if (photoFile != null && photoFile.exists()) {
                            photoUri = Uri.fromFile(photoFile);
                            photoChanged = true;
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
                            photoChanged = true;
                            binding.ivPlacePhoto.setImageURI(photoUri);
                        }
                    }
                }
        );
    }

    private void loadPlace() {
        placeRepository.getPlaceById(placeId).observe(getViewLifecycleOwner(), place -> {
            if (place != null) {
                currentPlace = place;
                populateFields();
            } else {
                Toast.makeText(requireContext(), "Lugar no encontrado", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(requireView()).navigateUp();
            }
        });
    }

    private void populateFields() {
        binding.etNombreLugar.setText(currentPlace.getName());

        if (currentPlace.hasLocation()) {
            binding.etLatitud.setText(String.format(Locale.US, "%.6f", currentPlace.getLatitude()));
            binding.etLongitud.setText(String.format(Locale.US, "%.6f", currentPlace.getLongitude()));
        }

        // Cargar foto si existe
        if (currentPlace.getPhotoUrl() != null && !currentPlace.getPhotoUrl().isEmpty()) {
            File photoFile = new File(currentPlace.getPhotoUrl());
            if (photoFile.exists()) {
                binding.ivPlacePhoto.setImageURI(Uri.fromFile(photoFile));
            }
        }
    }

    private void setupListeners() {
        // Botón volver
        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        // Botones de foto
        binding.btnTakePhoto.setOnClickListener(v -> takePicture());
        binding.btnSelectPhoto.setOnClickListener(v -> pickImageFromGallery());

        // Botón usar ubicación actual
        binding.btnUseLocation.setOnClickListener(v -> getCurrentLocationWithPermission());

        // Botón buscar en mapa
        binding.btnSearchMap.setOnClickListener(v ->
            Toast.makeText(requireContext(), "Función de mapa próximamente", Toast.LENGTH_SHORT).show()
        );

        // Botón guardar cambios
        binding.btnSavePlace.setOnClickListener(v -> saveChanges());

        // Botón eliminar lugar
        binding.btnDeletePlace.setOnClickListener(v -> confirmDelete());
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
        File storageDir = requireContext().getFilesDir();
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

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

    @SuppressLint("MissingPermission")
    private void getCurrentLocationHighAccuracy() {
        Toast.makeText(requireContext(), "Obteniendo ubicación...", Toast.LENGTH_SHORT).show();

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        binding.etLatitud.setText(String.format(Locale.US, "%.6f", location.getLatitude()));
                        binding.etLongitud.setText(String.format(Locale.US, "%.6f", location.getLongitude()));
                        Toast.makeText(requireContext(), "Ubicación obtenida", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                    Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void saveChanges() {
        String nombre = binding.etNombreLugar.getText().toString().trim();

        if (nombre.isEmpty()) {
            Toast.makeText(requireContext(), "El nombre es obligatorio", Toast.LENGTH_SHORT).show();
            return;
        }

        String latStr = binding.etLatitud.getText().toString().trim();
        String lngStr = binding.etLongitud.getText().toString().trim();

        Double latitude = null;
        Double longitude = null;

        if (!latStr.isEmpty() && !lngStr.isEmpty()) {
            try {
                latitude = Double.parseDouble(latStr);
                longitude = Double.parseDouble(lngStr);
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Coordenadas inválidas", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Actualizar el lugar
        currentPlace.setName(nombre);
        currentPlace.setLatitude(latitude);
        currentPlace.setLongitude(longitude);

        // Guardar foto si cambió
        if (photoChanged && photoUri != null) {
            try {
                String photoPath = ImageHelper.saveImageToInternalStorage(
                        requireContext(),
                        photoUri,
                        "place_" + placeId + "_" + System.currentTimeMillis() + ".jpg"
                );
                currentPlace.setPhotoUrl(photoPath);
            } catch (Exception e) {
                Toast.makeText(requireContext(), "Error al guardar foto", Toast.LENGTH_SHORT).show();
            }
        }

        // Actualizar en la base de datos
        placeRepository.updatePlace(currentPlace);

        Toast.makeText(requireContext(), "Lugar actualizado", Toast.LENGTH_SHORT).show();
        Navigation.findNavController(requireView()).navigateUp();
    }

    private void confirmDelete() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Eliminar lugar")
                .setMessage("¿Estás seguro de que deseas eliminar '" + currentPlace.getName() + "'?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    placeRepository.deletePlace(currentPlace);
                    Toast.makeText(requireContext(), "Lugar eliminado", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).navigateUp();
                })
                .setNegativeButton("Cancelar", null)
                .show();
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
