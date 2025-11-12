package com.pascm.fintrack.ui.movimiento;

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
import android.widget.ArrayAdapter;
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
import com.pascm.fintrack.data.local.FinTrackDatabase;
import com.pascm.fintrack.data.local.entity.Account;
import com.pascm.fintrack.data.local.entity.CreditCardEntity;
import com.pascm.fintrack.data.local.entity.DebitCardEntity;
import com.pascm.fintrack.data.local.entity.Transaction;
import com.pascm.fintrack.data.repository.CardRepository;
import com.pascm.fintrack.data.repository.TransactionRepository;
import com.pascm.fintrack.data.repository.TripRepository;
import com.pascm.fintrack.data.repository.UserRepository;
import com.pascm.fintrack.databinding.FragmentAgregarMovimientoBinding;
import com.pascm.fintrack.model.PaymentMethod;
import com.pascm.fintrack.util.ImageHelper;
import com.pascm.fintrack.util.LocationPermissionHelper;
import com.pascm.fintrack.util.SessionManager;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AgregarMovimientoFragment extends Fragment {

    private FragmentAgregarMovimientoBinding binding;
    private TransactionRepository transactionRepository;
    private TripRepository tripRepository;
    private CardRepository cardRepository;
    private UserRepository userRepository;
    private FusedLocationProviderClient fusedLocationClient;

    private Transaction.TransactionType selectedType = Transaction.TransactionType.EXPENSE;
    private LocalDate selectedDate = LocalDate.now();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("d 'de' MMMM, yyyy", new Locale("es", "MX"));

    private List<PaymentMethod> paymentMethods = new ArrayList<>();
    private PaymentMethod selectedPaymentMethod;
    private PaymentMethod selectedPaymentMethodTo; // Para transferencias

    // Mapping de categorías desde spinner a la base de datos
    private java.util.Map<String, Long> categoryNameToIdMap = new java.util.HashMap<>();

    // Photo management
    private Uri selectedPhotoUri = null;
    private File photoFile = null;
    private boolean hasPhoto = false;

    // Location management
    private Double currentLatitude = null;
    private Double currentLongitude = null;
    private boolean hasLocation = false;

    // Activity result launchers
    private ActivityResultLauncher<Intent> takePictureLauncher;
    private ActivityResultLauncher<Intent> pickImageLauncher;

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1002;

    // Default currency code for transactions
    private String userCurrencyCode = "MXN";

    public AgregarMovimientoFragment() {
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
        binding = FragmentAgregarMovimientoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize repositories and services
        transactionRepository = new TransactionRepository(requireContext());
        tripRepository = new TripRepository(requireContext());
        cardRepository = new CardRepository(requireContext());
        userRepository = new UserRepository(requireContext());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        // Obtener moneda por defecto del usuario
        long userId = SessionManager.getUserId(requireContext());
        userRepository.getUserProfile(userId).observe(getViewLifecycleOwner(), profile -> {
            if (profile != null && profile.getDefaultCurrency() != null && !profile.getDefaultCurrency().isEmpty()) {
                userCurrencyCode = profile.getDefaultCurrency();
            }
        });

        setupTypeButtons();
        updateDateDisplay();
        loadPaymentMethods();
        loadCategories();

        // Botón cerrar (X) - regresa al Home
        binding.btnClose.setOnClickListener(v ->
                Navigation.findNavController(v).navigateUp()
        );

        // Botón guardar movimiento
        binding.btnSaveMovement.setOnClickListener(v -> saveTransaction());

        // Botón adjuntar foto - ahora funcional
        binding.btnAttachPhoto.setOnClickListener(v -> showPhotoOptions());

        // Botón ubicación GPS - ahora funcional
        binding.btnAddLocation.setOnClickListener(v -> {
            if (hasLocation) {
                showLocationOptions();
            } else {
                getCurrentLocationWithPermission();
            }
        });
    }

    /**
     * Setup activity result launchers for camera and gallery
     */
    private void setupActivityResultLaunchers() {
        // Take picture launcher
        takePictureLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    if (photoFile != null && photoFile.exists()) {
                        selectedPhotoUri = Uri.fromFile(photoFile);
                        hasPhoto = true;
                        Toast.makeText(requireContext(), "✓ Foto capturada", Toast.LENGTH_SHORT).show();
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
                        hasPhoto = true;
                        Toast.makeText(requireContext(), "✓ Foto seleccionada", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        );
    }

    /**
     * Show photo options dialog
     */
    private void showPhotoOptions() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Adjuntar foto")
                .setMessage("¿Cómo deseas adjuntar la foto?")
                .setPositiveButton("Tomar foto", (dialog, which) -> takePicture())
                .setNegativeButton("Elegir de galería", (dialog, which) -> pickImageFromGallery())
                .setNeutralButton("Cancelar", null)
                .show();
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
        String imageFileName = "TRANSACTION_" + System.currentTimeMillis();
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
     * Show location options when location already exists
     */
    private void showLocationOptions() {
        String currentLocation = String.format(Locale.US, "Ubicación actual:\n%.6f, %.6f",
                currentLatitude, currentLongitude);

        new AlertDialog.Builder(requireContext())
                .setTitle("Ubicación GPS")
                .setMessage(currentLocation)
                .setPositiveButton("Actualizar", (dialog, which) -> getCurrentLocationWithPermission())
                .setNegativeButton("Eliminar", (dialog, which) -> {
                    currentLatitude = null;
                    currentLongitude = null;
                    hasLocation = false;
                    Toast.makeText(requireContext(), "Ubicación eliminada", Toast.LENGTH_SHORT).show();
                })
                .setNeutralButton("Cancelar", null)
                .show();
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
                        .setPositiveButton("Conceder permiso", (dialog, which) ->
                                LocationPermissionHelper.requestLocationPermission(requireActivity()))
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
        Toast.makeText(requireContext(), "Obteniendo ubicación...", Toast.LENGTH_SHORT).show();

        // Usar FusedLocationProviderClient con prioridad de ALTA PRECISIÓN
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        currentLatitude = location.getLatitude();
                        currentLongitude = location.getLongitude();
                        hasLocation = true;

                        Toast.makeText(requireContext(),
                                String.format(Locale.US, "✓ Ubicación GPS obtenida\n%.6f, %.6f",
                                        currentLatitude, currentLongitude),
                                Toast.LENGTH_LONG).show();
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
                        hasLocation = true;

                        Toast.makeText(requireContext(),
                                String.format(Locale.US, "✓ Ubicación obtenida (última conocida)\n%.6f, %.6f",
                                        currentLatitude, currentLongitude),
                                Toast.LENGTH_LONG).show();
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
                getCurrentLocationHighAccuracy();
            } else {
                Toast.makeText(requireContext(),
                        "Permiso de ubicación denegado",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void setupTypeButtons() {
        // Configurar botones de tipo de transacción
        binding.btnIngreso.setOnClickListener(v -> {
            selectedType = Transaction.TransactionType.INCOME;
            updateTypeButtonsUI();
            updateCategorySpinner();
            updateLocationVisibility();
            updatePaymentMethodSpinner(); // Actualizar métodos de pago según el tipo
        });

        binding.btnGasto.setOnClickListener(v -> {
            selectedType = Transaction.TransactionType.EXPENSE;
            updateTypeButtonsUI();
            updateCategorySpinner();
            updateLocationVisibility();
            updatePaymentMethodSpinner(); // Actualizar métodos de pago según el tipo
        });

        binding.btnTransferencia.setOnClickListener(v -> {
            selectedType = Transaction.TransactionType.TRANSFER;
            updateTypeButtonsUI();
            updateCategorySpinner();
            updateLocationVisibility();
            updatePaymentMethodSpinner(); // Actualizar métodos de pago según el tipo
        });

        // Establecer estado inicial (gasto seleccionado por defecto)
        updateTypeButtonsUI();
        updateCategorySpinner();
        updateLocationVisibility();
    }

    private void updateTypeButtonsUI() {
        // Reset all buttons to unselected/unactivated state
        binding.btnIngreso.setSelected(false);
        binding.btnGasto.setSelected(false);
        binding.btnTransferencia.setSelected(false);
        binding.btnIngreso.setActivated(false);
        binding.btnGasto.setActivated(false);
        binding.btnTransferencia.setActivated(false);

        // Set selected/activated button
        switch (selectedType) {
            case INCOME:
                binding.btnIngreso.setSelected(true);
                binding.btnIngreso.setActivated(true);
                binding.cardAccountTo.setVisibility(View.GONE);
                binding.tvAccountLabel.setVisibility(View.GONE);
                break;
            case EXPENSE:
                binding.btnGasto.setSelected(true);
                binding.btnGasto.setActivated(true);
                binding.cardAccountTo.setVisibility(View.GONE);
                binding.tvAccountLabel.setVisibility(View.GONE);
                break;
            case TRANSFER:
                binding.btnTransferencia.setSelected(true);
                binding.btnTransferencia.setActivated(true);
                binding.cardAccountTo.setVisibility(View.VISIBLE);
                binding.tvAccountLabel.setVisibility(View.VISIBLE);
                binding.tvAccountLabel.setText(R.string.desde);
                break;
        }
    }

    /**
     * Update category spinner based on transaction type
     */
    private void updateCategorySpinner() {
        int arrayResourceId;

        switch (selectedType) {
            case INCOME:
                arrayResourceId = R.array.categories_income;
                break;
            case TRANSFER:
                arrayResourceId = R.array.categories_transfer;
                break;
            case EXPENSE:
            default:
                arrayResourceId = R.array.categories_expense;
                break;
        }

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                arrayResourceId,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCategory.setAdapter(adapter);
    }

    /**
     * Actualiza la visibilidad de la ubicación GPS
     * Solo se muestra para gastos (EXPENSE)
     */
    private void updateLocationVisibility() {
        if (selectedType == Transaction.TransactionType.EXPENSE) {
            binding.btnAddLocation.setVisibility(View.VISIBLE);
        } else {
            binding.btnAddLocation.setVisibility(View.GONE);
            // Limpiar ubicación si había una
            hasLocation = false;
            currentLatitude = null;
            currentLongitude = null;
        }
    }

    private void updateDateDisplay() {
        binding.tvDate.setText(selectedDate.format(dateFormatter));
    }

    private void loadPaymentMethods() {
        long userId = SessionManager.getUserId(requireContext());

        // Siempre agregar efectivo como primera opción
        paymentMethods.add(new PaymentMethod());

        // Cargar tarjetas de crédito
        cardRepository.getAllCreditCards(userId).observe(getViewLifecycleOwner(), creditCards -> {
            if (creditCards != null && !creditCards.isEmpty()) {
                for (CreditCardEntity card : creditCards) {
                    String displayName = card.getIssuer() + " - " + card.getLabel();
                    String details = "•••• " + (card.getPanLast4() != null ? card.getPanLast4() : "0000");
                    paymentMethods.add(new PaymentMethod(
                            PaymentMethod.Type.CREDIT_CARD,
                            card.getCardId(),
                            displayName,
                            details
                    ));
                }
            }
            updatePaymentMethodSpinner();
        });

        // Cargar tarjetas de débito
        cardRepository.getAllDebitCards(userId).observe(getViewLifecycleOwner(), debitCards -> {
            if (debitCards != null && !debitCards.isEmpty()) {
                for (DebitCardEntity card : debitCards) {
                    String displayName = card.getIssuer() + " - " + card.getLabel();
                    String details = "•••• " + (card.getPanLast4() != null ? card.getPanLast4() : "0000");
                    paymentMethods.add(new PaymentMethod(
                            PaymentMethod.Type.DEBIT_CARD,
                            card.getCardId(),
                            displayName,
                            details
                    ));
                }
            }
            updatePaymentMethodSpinner();
        });
    }

    private void loadCategories() {
        // Cargar todas las categorías de la base de datos en background
        FinTrackDatabase.databaseWriteExecutor.execute(() -> {
            try {
                FinTrackDatabase db = FinTrackDatabase.getDatabase(requireContext());
                List<com.pascm.fintrack.data.local.entity.Category> categories = db.categoryDao().getAllByUserSync();

                if (categories != null) {
                    categoryNameToIdMap.clear();
                    for (com.pascm.fintrack.data.local.entity.Category cat : categories) {
                        categoryNameToIdMap.put(cat.getName(), cat.getCategoryId());
                    }
                }
            } catch (Exception e) {
                android.util.Log.e("AgregarMovimiento", "Error loading categories: " + e.getMessage());
            }
        });
    }

    private void updatePaymentMethodSpinner() {
        // Filtrar métodos según el tipo de transacción
        List<PaymentMethod> filteredMethods = filterPaymentMethodsByType(selectedType);

        ArrayAdapter<PaymentMethod> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                filteredMethods
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerAccount.setAdapter(adapter);
        binding.spinnerAccountTo.setAdapter(adapter);

        // Seleccionar el primer método por defecto (efectivo)
        if (!filteredMethods.isEmpty()) {
            selectedPaymentMethod = filteredMethods.get(0);
            selectedPaymentMethodTo = filteredMethods.get(0);
        }
    }

    private List<PaymentMethod> filterPaymentMethodsByType(Transaction.TransactionType type) {
        List<PaymentMethod> filtered = new ArrayList<>();

        for (PaymentMethod method : paymentMethods) {
            // Para ingresos, solo permitir débito y efectivo
            if (type == Transaction.TransactionType.INCOME) {
                if (method.getType() == PaymentMethod.Type.CASH ||
                    method.getType() == PaymentMethod.Type.DEBIT_CARD) {
                    filtered.add(method);
                }
            } else {
                // Para gastos y transferencias, permitir todos
                filtered.add(method);
            }
        }

        return filtered;
    }

    private void saveTransaction() {
        // Validar monto
        String amountStr = binding.etAmount.getText().toString().trim();
        if (amountStr.isEmpty()) {
            Toast.makeText(requireContext(), "Ingresa un monto", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                Toast.makeText(requireContext(), "El monto debe ser mayor a 0", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Monto inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        // Obtener método de pago seleccionado
        int selectedPosition = binding.spinnerAccount.getSelectedItemPosition();
        if (selectedPosition >= 0 && selectedPosition < paymentMethods.size()) {
            selectedPaymentMethod = paymentMethods.get(selectedPosition);
        }

        if (selectedPaymentMethod == null) {
            Toast.makeText(requireContext(), "Selecciona un método de pago", Toast.LENGTH_SHORT).show();
            return;
        }

        // Si es transferencia, validar cuenta destino
        if (selectedType == Transaction.TransactionType.TRANSFER) {
            int selectedPositionTo = binding.spinnerAccountTo.getSelectedItemPosition();
            if (selectedPositionTo >= 0 && selectedPositionTo < paymentMethods.size()) {
                selectedPaymentMethodTo = paymentMethods.get(selectedPositionTo);
            }

            if (selectedPaymentMethodTo == null) {
                Toast.makeText(requireContext(), "Selecciona cuenta destino", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validar que no sean iguales
            if (selectedPaymentMethod.getType() == selectedPaymentMethodTo.getType() &&
                selectedPaymentMethod.getEntityId() == selectedPaymentMethodTo.getEntityId()) {
                Toast.makeText(requireContext(), "Origen y destino deben ser diferentes", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validar que si el destino es tarjeta de crédito, el origen sea débito o efectivo
            if (selectedPaymentMethodTo.getType() == PaymentMethod.Type.CREDIT_CARD) {
                if (selectedPaymentMethod.getType() == PaymentMethod.Type.CREDIT_CARD) {
                    Toast.makeText(requireContext(), "Solo puedes pagar tarjetas de crédito desde cuentas de débito o efectivo", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            // Validar saldo suficiente antes de transferir
            FinTrackDatabase.databaseWriteExecutor.execute(() -> {
                boolean hasEnoughBalance = checkBalanceAvailability(selectedPaymentMethod, amount);

                requireActivity().runOnUiThread(() -> {
                    if (!hasEnoughBalance) {
                        Toast.makeText(requireContext(), "Saldo insuficiente en la cuenta de origen", Toast.LENGTH_LONG).show();
                    } else {
                        // Procesar transferencia
                        performTransfer(amount);
                    }
                });
            });
            return;
        }

        // Validar saldo suficiente para gastos
        if (selectedType == Transaction.TransactionType.EXPENSE) {
            FinTrackDatabase.databaseWriteExecutor.execute(() -> {
                boolean hasEnoughBalance = checkBalanceAvailability(selectedPaymentMethod, amount);

                requireActivity().runOnUiThread(() -> {
                    if (!hasEnoughBalance) {
                        Toast.makeText(requireContext(), "Saldo insuficiente en la cuenta seleccionada", Toast.LENGTH_LONG).show();
                    } else {
                        // Continuar con el guardado de la transacción
                        proceedWithSaveTransaction(amount);
                    }
                });
            });
            return;
        }

        // Para ingresos, proceder directamente (no necesitan validación de saldo)
        proceedWithSaveTransaction(amount);
    }

    private boolean checkBalanceAvailability(PaymentMethod method, double amount) {
        switch (method.getType()) {
            case DEBIT_CARD:
                DebitCardEntity debitCard = cardRepository.getDebitCardByIdSync(method.getEntityId());
                if (debitCard != null) {
                    long accountId = debitCard.getAccountId();
                    FinTrackDatabase db = FinTrackDatabase.getDatabase(requireContext());
                    var account = db.accountDao().getByIdSync(accountId);
                    if (account != null) {
                        return account.getBalance() >= amount;
                    }
                }
                return false;
            case CASH:
                // Validar saldo de efectivo si es gasto o transferencia
                if (selectedType == Transaction.TransactionType.EXPENSE || selectedType == Transaction.TransactionType.TRANSFER) {
                    long userId = SessionManager.getUserId(requireContext());
                    FinTrackDatabase db = FinTrackDatabase.getDatabase(requireContext());
                    var accounts = db.accountDao().getAllByUserSync(userId);
                    double cashBalance = 0.0;
                    if (accounts != null) {
                        for (Account acc : accounts) {
                            if (acc.getType() == Account.AccountType.CASH && !acc.isArchived()) {
                                cashBalance += acc.getBalance();
                            }
                        }
                    }
                    return cashBalance >= amount;
                }
                return true;
            case CREDIT_CARD:
                CreditCardEntity creditCard = cardRepository.getCreditCardByIdSync(method.getEntityId());
                if (creditCard != null) {
                    return creditCard.getAvailableCredit() >= amount;
                }
                return false;
            default:
                return true;
        }
    }

    private void updateBalances(Transaction transaction, PaymentMethod paymentMethod) {
        double effectiveAmount = transaction.getAmount();
        if (transaction.getType() == Transaction.TransactionType.EXPENSE) {
            effectiveAmount = -effectiveAmount;
        }

        switch (paymentMethod.getType()) {
            case CREDIT_CARD:
                updateCreditCardBalance(paymentMethod.getEntityId(), effectiveAmount);
                break;
            case DEBIT_CARD:
                updateDebitCardBalance(paymentMethod.getEntityId(), effectiveAmount);
                break;
            case CASH:
                updateCashAccountBalance(effectiveAmount);
                break;
        }
    }

    private void updateCashAccountBalance(double delta) {
        long userId = SessionManager.getUserId(requireContext());
        FinTrackDatabase db = FinTrackDatabase.getDatabase(requireContext());
        var accountDao = db.accountDao();
        var accounts = accountDao.getAllByUserSync(userId);
        Account cash = null;
        if (accounts != null) {
            for (Account acc : accounts) {
                if (acc.getType() == Account.AccountType.CASH && !acc.isArchived()) {
                    cash = acc;
                    break;
                }
            }
        }
        if (cash == null) {
            // Crear cuenta de efectivo por defecto si no existe
            cash = new Account();
            cash.setUserId(userId);
            cash.setName("Efectivo");
            cash.setType(Account.AccountType.CASH);
            cash.setCurrencyCode(userCurrencyCode != null ? userCurrencyCode : "MXN");
            cash.setBalance(0.0);
            cash.setAvailable(0.0);
            long id = accountDao.insert(cash);
            cash.setAccountId(id);
        }
        double newBalance = Math.max(0, cash.getBalance() + delta);
        accountDao.updateBalance(cash.getAccountId(), newBalance, java.time.Instant.now().toEpochMilli());
    }

    private long getOrCreateCashAccountId() {
        long userId = SessionManager.getUserId(requireContext());
        FinTrackDatabase db = FinTrackDatabase.getDatabase(requireContext());
        var accountDao = db.accountDao();
        var accounts = accountDao.getAllByUserSync(userId);
        if (accounts != null) {
            for (Account acc : accounts) {
                if (acc.getType() == Account.AccountType.CASH && !acc.isArchived()) {
                    return acc.getAccountId();
                }
            }
        }
        // Crear si no existe
        Account cash = new Account();
        cash.setUserId(userId);
        cash.setName("Efectivo");
        cash.setType(Account.AccountType.CASH);
        cash.setCurrencyCode(userCurrencyCode != null ? userCurrencyCode : "MXN");
        long id = accountDao.insert(cash);
        return id;
    }

    private void proceedWithSaveTransaction(double amount) {
        // Obtener nota (opcional)
        String notes = binding.etNote.getText().toString().trim();

        // Agregar información de ubicación a las notas si existe
        if (hasLocation && currentLatitude != null && currentLongitude != null) {
            String locationInfo = String.format(Locale.US, "\n📍 GPS: %.6f, %.6f",
                    currentLatitude, currentLongitude);
            notes = notes.isEmpty() ? locationInfo.trim() : notes + locationInfo;
        }

        // Obtener categoría seleccionada del spinner
        String selectedCategoryName = binding.spinnerCategory.getSelectedItem() != null ?
                binding.spinnerCategory.getSelectedItem().toString() : null;
        Long categoryId = null;
        if (selectedCategoryName != null && categoryNameToIdMap.containsKey(selectedCategoryName)) {
            categoryId = categoryNameToIdMap.get(selectedCategoryName);
        }

        // Obtener userId desde sesión
        long userId = SessionManager.getUserId(requireContext());
        Transaction transaction = new Transaction();
        transaction.setUserId(userId);
        transaction.setAmount(amount);
        transaction.setType(selectedType);
        transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        transaction.setCurrencyCode(userCurrencyCode != null ? userCurrencyCode : "MXN");
        transaction.setNotes(notes.isEmpty() ? null : notes);
        transaction.setCategoryId(categoryId); // Asignar categoryId desde spinner

        // Guardar coordenadas GPS si existen (importante para mostrar en el mapa del viaje)
        if (hasLocation && currentLatitude != null && currentLongitude != null) {
            transaction.setLatitude(currentLatitude);
            transaction.setLongitude(currentLongitude);
        }

        // Convertir LocalDate a Instant para transactionDate
        Instant transactionDate = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        transaction.setTransactionDate(transactionDate);

        switch (selectedPaymentMethod.getType()) {
            case CREDIT_CARD:
                transaction.setCardId(selectedPaymentMethod.getEntityId());
                transaction.setCardType("CREDIT");
                break;
            case DEBIT_CARD:
                transaction.setCardId(selectedPaymentMethod.getEntityId());
                transaction.setCardType("DEBIT");
                // Asociar cuenta de dbito subyacente si es posible
                // AccountId para DEBIT se asignará en background thread
                break;
            case CASH:
                transaction.setCardType("CASH");
                // AccountId para CASH se asignará en background thread
                break;
        }

        // Verificar si hay viaje activo y preguntar solo para gastos
        if (selectedType == Transaction.TransactionType.EXPENSE) {
            FinTrackDatabase.databaseWriteExecutor.execute(() -> {
                try {
                    var activeTrip = tripRepository.getActiveTripSync(userId);
                    if (activeTrip != null) {
                        // Hay viaje activo - preguntar en UI thread
                        final String tripName = activeTrip.getName();
                        final long tripId = activeTrip.getTripId();

                        requireActivity().runOnUiThread(() -> {
                            showTripAssociationDialog(transaction, tripName, tripId);
                        });
                    } else {
                        // No hay viaje activo - guardar directamente
                        saveTransactionDirectly(transaction, selectedPaymentMethod);
                    }
                } catch (Exception e) {
                    requireActivity().runOnUiThread(() -> {
                        // Error al verificar viaje - guardar sin viaje
                        saveTransactionDirectly(transaction, selectedPaymentMethod);
                    });
                }
            });
        } else {
            // No es gasto - guardar directamente sin viaje
            saveTransactionDirectly(transaction, selectedPaymentMethod);
        }
    }

    private void showTripAssociationDialog(Transaction transaction, String tripName, long tripId) {
        new AlertDialog.Builder(requireContext())
            .setTitle("¿Asociar a viaje activo?")
            .setMessage("Tienes un viaje activo: \"" + tripName + "\"\n\n¿Deseas registrar este gasto en este viaje?")
            .setPositiveButton("Sí, asociar", (dialog, which) -> {
                // Si va a asociar, la ubicación es OBLIGATORIA
                if (!hasLocation || currentLatitude == null || currentLongitude == null) {
                    new AlertDialog.Builder(requireContext())
                        .setTitle("Ubicación requerida")
                        .setMessage("Para asociar el gasto al viaje debes agregar la ubicación del gasto.")
                        .setPositiveButton("Agregar ubicación", (d, w) -> {
                            // Intentar obtener ubicación; no guardamos todavía
                            getCurrentLocationWithPermission();
                            Toast.makeText(requireContext(), "Agrega la ubicación y vuelve a guardar el gasto", Toast.LENGTH_LONG).show();
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
                } else {
                    transaction.setTripId(tripId);
                    saveTransactionDirectly(transaction, selectedPaymentMethod);
                }
            })
            .setNegativeButton("No, solo guardar", (dialog, which) -> {
                // Guardar sin asociar
                saveTransactionDirectly(transaction, selectedPaymentMethod);
            })
            .setCancelable(false)
            .show();
    }

    /**
     * Actualiza el saldo de una tarjeta de crédito
     * Para gastos: incrementa el balance (deuda)
     * Para ingresos (pagos): disminuye el balance (deuda)
     */
    private void updateCreditCardBalance(long cardId, double amount) {
        CreditCardEntity card = cardRepository.getCreditCardByIdSync(cardId);
        if (card != null) {
            // En tarjetas de crédito:
            // - Gasto (amount negativo) aumenta la deuda: balance aumenta
            // - Pago (amount positivo) reduce la deuda: balance disminuye
            double newBalance = card.getCurrentBalance() - amount; // Invertimos porque amount ya tiene el signo
            newBalance = Math.max(0, newBalance); // No permitir balance negativo
            newBalance = Math.min(card.getCreditLimit(), newBalance); // No exceder el límite

            cardRepository.updateCardBalance(cardId, newBalance);
        }
    }

    /**
     * Actualiza el saldo de una cuenta vinculada a tarjeta de débito
     * Para gastos: disminuye el saldo de la cuenta
     * Para ingresos: aumenta el saldo de la cuenta
     */
    private void updateDebitCardBalance(long cardId, double amount) {
        DebitCardEntity card = cardRepository.getDebitCardByIdSync(cardId);
        if (card != null) {
            long accountId = card.getAccountId();

            // Obtener la cuenta y actualizar su saldo
            FinTrackDatabase db = FinTrackDatabase.getDatabase(requireContext());
            var account = db.accountDao().getByIdSync(accountId);

            if (account != null) {
                double newBalance = account.getBalance() + amount; // amount ya tiene el signo correcto
                newBalance = Math.max(0, newBalance); // No permitir balance negativo

                db.accountDao().updateBalance(accountId, newBalance, Instant.now().toEpochMilli());
            }
        }
    }

    /**
     * Procesa una transferencia entre cuentas propias del usuario
     */
    private void performTransfer(double amount) {
        // Obtener nota
        String notes = binding.etNote.getText().toString().trim();
        String transferNote = notes.isEmpty() ?
            "Transferencia de " + selectedPaymentMethod.getDisplayName() + " a " + selectedPaymentMethodTo.getDisplayName() :
            notes;

        // Agregar información de ubicación a las notas si existe
        if (hasLocation && currentLatitude != null && currentLongitude != null) {
            String locationInfo = String.format(Locale.US, "\n📍 GPS: %.6f, %.6f",
                    currentLatitude, currentLongitude);
            transferNote += locationInfo;
        }

        // Make final for lambda
        final String finalTransferNote = transferNote;

        long userId = SessionManager.getUserId(requireContext());
        Instant transactionDate = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant();

        // Ejecutar transferencia
        FinTrackDatabase.databaseWriteExecutor.execute(() -> {
            try {
                // Crear transacción de salida (origen)
                Transaction outTransaction = createTransferTransaction(
                        userId,
                        amount,
                        Transaction.TransactionType.EXPENSE,
                        selectedPaymentMethod,
                        transactionDate,
                        finalTransferNote + " [Salida]"
                );

                // Crear transacción de entrada (destino)
                Transaction inTransaction = createTransferTransaction(
                        userId,
                        amount,
                        Transaction.TransactionType.INCOME,
                        selectedPaymentMethodTo,
                        transactionDate,
                        finalTransferNote + " [Entrada]"
                );

                // Guardar ambas transacciones
                transactionRepository.insertTransaction(outTransaction);
                transactionRepository.insertTransaction(inTransaction);

                // Actualizar saldos
                updateBalances(outTransaction, selectedPaymentMethod);
                updateBalances(inTransaction, selectedPaymentMethodTo);

                requireActivity().runOnUiThread(() -> {
                    String successMessage = "Transferencia realizada: " + selectedPaymentMethod.getDisplayName() +
                                          " → " + selectedPaymentMethodTo.getDisplayName();
                    if (hasLocation) successMessage += "\n✓ Ubicación GPS";

                    Toast.makeText(requireContext(), successMessage, Toast.LENGTH_LONG).show();
                    Navigation.findNavController(requireView()).navigateUp();
                });
            } catch (Exception e) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Error en transferencia: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    /**
     * Crea una transacción para transferencia
     */
    private Transaction createTransferTransaction(long userId, double amount, Transaction.TransactionType type,
                                         PaymentMethod method, Instant date, String notes) {
        Transaction transaction = new Transaction();
        transaction.setUserId(userId);
        transaction.setAmount(amount);
        transaction.setType(type);
        transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        transaction.setCurrencyCode(userCurrencyCode != null ? userCurrencyCode : "MXN");
        transaction.setNotes(notes);
        transaction.setTransactionDate(date);

        switch (method.getType()) {
            case CREDIT_CARD:
                transaction.setCardId(method.getEntityId());
                transaction.setCardType("CREDIT");
                break;
            case DEBIT_CARD:
                transaction.setCardId(method.getEntityId());
                transaction.setCardType("DEBIT");
                DebitCardEntity dc = cardRepository.getDebitCardByIdSync(method.getEntityId());
                if (dc != null) transaction.setAccountId(dc.getAccountId());
                break;
            case CASH:
                transaction.setCardType("CASH");
                // AccountId para CASH se asignará en background thread
                break;
        }

        return transaction;
    }

    private void saveTransactionDirectly(Transaction transaction, PaymentMethod paymentMethod) {
        FinTrackDatabase.databaseWriteExecutor.execute(() -> {
            try {
                // Si es CASH o DEBIT, asignar el accountId ahora que estamos en background thread
                if (paymentMethod.getType() == PaymentMethod.Type.CASH && transaction.getAccountId() == null) {
                    transaction.setAccountId(getOrCreateCashAccountId());
                } else if (paymentMethod.getType() == PaymentMethod.Type.DEBIT_CARD && transaction.getAccountId() == null) {
                    DebitCardEntity dc = cardRepository.getDebitCardByIdSync(paymentMethod.getEntityId());
                    if (dc != null) transaction.setAccountId(dc.getAccountId());
                }

                long transactionId = transactionRepository.insertTransactionSync(transaction);

                // Guardar foto si existe
                if (hasPhoto && selectedPhotoUri != null) {
                    String photoPath = com.pascm.fintrack.util.ImageHelper.saveImageToInternalStorage(
                            requireContext(),
                            selectedPhotoUri,
                            "transaction_" + transactionId + "_" + System.currentTimeMillis() + ".jpg"
                    );
                    // TODO: persistir photoPath en la entidad (no implementado aún)
                }

                // Actualizar saldos según método de pago
                updateBalances(transaction, paymentMethod);

                requireActivity().runOnUiThread(() -> {
                    Transaction.TransactionType currentType = transaction.getType();
                    String typeText = currentType == Transaction.TransactionType.INCOME ? "Ingreso" :
                            currentType == Transaction.TransactionType.EXPENSE ? "Gasto" : "Transferencia";
                    String paymentText = paymentMethod.getDisplayName();
                    StringBuilder successMessage = new StringBuilder(typeText + " guardado - " + paymentText);
                    if (hasPhoto) successMessage.append("\n✓ Foto adjunta");
                    if (hasLocation && currentType == Transaction.TransactionType.EXPENSE) successMessage.append("\n✓ Ubicación GPS");
                    if (transaction.getTripId() != null) successMessage.append("\n✓ Asociado al viaje");

                    Toast.makeText(requireContext(), successMessage.toString(), Toast.LENGTH_LONG).show();
                    Navigation.findNavController(requireView()).navigateUp();
                });
            } catch (Exception e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Error al guardar: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
