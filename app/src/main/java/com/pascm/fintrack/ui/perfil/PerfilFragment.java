package com.pascm.fintrack.ui.perfil;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
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
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.pascm.fintrack.R;
import com.pascm.fintrack.databinding.FragmentPerfilBinding;
import com.pascm.fintrack.data.TripPrefs;
import com.pascm.fintrack.data.repository.UserRepository;
import com.pascm.fintrack.data.local.entity.User;
import com.pascm.fintrack.data.local.entity.UserProfile;
import com.pascm.fintrack.util.ImageHelper;
import com.pascm.fintrack.util.SessionManager;

import java.io.File;
import java.io.IOException;

public class PerfilFragment extends Fragment {

    private FragmentPerfilBinding binding;
    private UserRepository userRepository;
    private User currentUser;
    private UserProfile userProfile;

    // Image picker
    private Uri photoUri;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<String> cameraPermissionLauncher;
    private ActivityResultLauncher<String> storagePermissionLauncher;

    public PerfilFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize image picker launchers
        initializeImageLaunchers();
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

        // Setup avatar edit button
        binding.btnEditAvatar.setOnClickListener(v -> showImagePickerBottomSheet());

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

        // Currency change
        binding.tvMonedaValor.setOnClickListener(v -> showCurrencySelectionDialog());
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

        // Load avatar image if exists
        loadAvatarImage(profile.getAvatarUrl());
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

        // Update profile
        userProfile.setNotificationsEnabled(notificationsEnabled);

        // Save to database
        userRepository.updateUserProfile(userProfile);

        Toast.makeText(requireContext(), "Cambios guardados exitosamente", Toast.LENGTH_SHORT).show();
    }

    // ========== Image Picker Methods ==========

    private void initializeImageLaunchers() {
        // Camera launcher
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        if (photoUri != null) {
                            handleImageResult(photoUri);
                        }
                    }
                }
        );

        // Gallery launcher (usar la galería de Google Photos)
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            handleImageResult(selectedImageUri);
                        }
                    }
                }
        );

        // Camera permission launcher
        cameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openCamera();
                    } else {
                        Toast.makeText(requireContext(), "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Storage permission launcher
        storagePermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openGallery();
                    } else {
                        Toast.makeText(requireContext(), "Permiso de almacenamiento denegado", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void showImagePickerBottomSheet() {
        BottomSheetDialog bottomSheet = new BottomSheetDialog(requireContext());
        View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_image_picker, null);
        bottomSheet.setContentView(sheetView);

        // Camera option
        sheetView.findViewById(R.id.option_camera).setOnClickListener(v -> {
            bottomSheet.dismiss();
            checkCameraPermissionAndOpen();
        });

        // Gallery option
        sheetView.findViewById(R.id.option_gallery).setOnClickListener(v -> {
            bottomSheet.dismiss();
            checkStoragePermissionAndOpen();
        });

        // Cancel button
        sheetView.findViewById(R.id.btn_cancel).setOnClickListener(v -> bottomSheet.dismiss());

        bottomSheet.show();
    }

    private void checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void checkStoragePermissionAndOpen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ usa READ_MEDIA_IMAGES
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                storagePermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
            }
        } else {
            // Android 12 y anteriores
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                storagePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            // Create temporary file for photo
            File photoFile = createImageFile();
            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(
                        requireContext(),
                        requireContext().getPackageName() + ".fileprovider",
                        photoFile
                );
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                cameraLauncher.launch(cameraIntent);
            }
        } else {
            Toast.makeText(requireContext(), "No hay aplicación de cámara disponible", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        // Usar ACTION_PICK para usar la galería de Google Photos
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.setType("image/*");

        if (galleryIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            galleryLauncher.launch(galleryIntent);
        } else {
            Toast.makeText(requireContext(), "No hay aplicación de galería disponible", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() {
        try {
            String imageFileName = "profile_temp_" + System.currentTimeMillis();
            File storageDir = new File(requireContext().getFilesDir(), "profile_images");
            if (!storageDir.exists()) {
                storageDir.mkdirs();
            }
            return File.createTempFile(imageFileName, ".jpg", storageDir);
        } catch (IOException e) {
            android.util.Log.e("PerfilFragment", "Error creating image file", e);
            return null;
        }
    }

    private void handleImageResult(Uri imageUri) {
        if (userProfile == null) {
            Toast.makeText(requireContext(), "Error: perfil no cargado", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save image to internal storage
        String fileName = "profile_" + userProfile.getUserId() + ".jpg";
        String savedPath = ImageHelper.saveImageToInternalStorage(requireContext(), imageUri, fileName);

        if (savedPath != null) {
            // Update profile with new avatar URL
            userProfile.setAvatarUrl(savedPath);
            userRepository.updateUserProfile(userProfile);

            // Display image
            loadAvatarImage(savedPath);

            Toast.makeText(requireContext(), "Foto de perfil actualizada", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "Error al guardar la imagen", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadAvatarImage(String avatarUrl) {
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Bitmap bitmap = ImageHelper.loadBitmapFromPath(avatarUrl);
            if (bitmap != null) {
                binding.imgAvatar.setImageBitmap(bitmap);
                binding.imgAvatar.setVisibility(View.VISIBLE);
                binding.layoutAvatarPlaceholder.setVisibility(View.GONE);
            } else {
                // Show placeholder if image loading fails
                binding.imgAvatar.setVisibility(View.GONE);
                binding.layoutAvatarPlaceholder.setVisibility(View.VISIBLE);
            }
        } else {
            // No avatar, show placeholder
            binding.imgAvatar.setVisibility(View.GONE);
            binding.layoutAvatarPlaceholder.setVisibility(View.VISIBLE);
        }
    }

    private void showCurrencySelectionDialog() {
        if (userProfile == null) {
            Toast.makeText(requireContext(), "Error al cargar perfil", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] currencies = getResources().getStringArray(R.array.currencies);
        String currentCurrency = userProfile.getDefaultCurrency() != null ? userProfile.getDefaultCurrency() : "MXN";

        // Find current currency index
        int selectedIndex = 0;
        for (int i = 0; i < currencies.length; i++) {
            if (currencies[i].startsWith(currentCurrency)) {
                selectedIndex = i;
                break;
            }
        }

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Seleccionar moneda")
                .setSingleChoiceItems(currencies, selectedIndex, null)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    int selectedPosition = ((androidx.appcompat.app.AlertDialog) dialog).getListView().getCheckedItemPosition();
                    if (selectedPosition >= 0 && selectedPosition < currencies.length) {
                        String selectedCurrency = currencies[selectedPosition];
                        // Extract currency code (first 3 characters)
                        String currencyCode = selectedCurrency.substring(0, 3);

                        // Update profile
                        userProfile.setDefaultCurrency(currencyCode);
                        userRepository.updateUserProfile(userProfile);

                        // Update UI
                        binding.tvMonedaValor.setText(getCurrencyDisplay(currencyCode));

                        Toast.makeText(requireContext(), "Moneda actualizada a " + currencyCode, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
