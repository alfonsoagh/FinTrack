package com.pascm.fintrack.ui.movimiento;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.pascm.fintrack.R;
import com.pascm.fintrack.data.local.FinTrackDatabase;
import com.pascm.fintrack.data.local.entity.CreditCardEntity;
import com.pascm.fintrack.data.local.entity.DebitCardEntity;
import com.pascm.fintrack.data.local.entity.Transaction;
import com.pascm.fintrack.data.repository.CardRepository;
import com.pascm.fintrack.data.repository.TransactionRepository;
import com.pascm.fintrack.data.repository.TripRepository;
import com.pascm.fintrack.databinding.FragmentAgregarMovimientoBinding;
import com.pascm.fintrack.model.PaymentMethod;
import com.pascm.fintrack.util.SessionManager;

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

    private Transaction.TransactionType selectedType = Transaction.TransactionType.EXPENSE;
    private LocalDate selectedDate = LocalDate.now();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("d 'de' MMMM, yyyy", new Locale("es", "MX"));

    private List<PaymentMethod> paymentMethods = new ArrayList<>();
    private PaymentMethod selectedPaymentMethod;
    private PaymentMethod selectedPaymentMethodTo; // Para transferencias

    public AgregarMovimientoFragment() {
        // Required empty public constructor
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

        // Initialize repositories
        transactionRepository = new TransactionRepository(requireContext());
        tripRepository = new TripRepository(requireContext());
        cardRepository = new CardRepository(requireContext());

        setupTypeButtons();
        updateDateDisplay();
        loadPaymentMethods();

        // Botón cerrar (X) - regresa al Home
        binding.btnClose.setOnClickListener(v ->
                Navigation.findNavController(v).navigateUp()
        );

        // Botón guardar movimiento
        binding.btnSaveMovement.setOnClickListener(v -> saveTransaction());

        // Botones de adjuntar foto y ubicación (sin lógica por ahora)
        binding.btnAttachPhoto.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Adjuntar foto (próximamente)", Toast.LENGTH_SHORT).show()
        );

        binding.btnAddLocation.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Agregar ubicación (próximamente)", Toast.LENGTH_SHORT).show()
        );
    }

    private void setupTypeButtons() {
        // Configurar botones de tipo de transacción
        binding.btnIngreso.setOnClickListener(v -> {
            selectedType = Transaction.TransactionType.INCOME;
            updateTypeButtonsUI();
        });

        binding.btnGasto.setOnClickListener(v -> {
            selectedType = Transaction.TransactionType.EXPENSE;
            updateTypeButtonsUI();
        });

        binding.btnTransferencia.setOnClickListener(v -> {
            selectedType = Transaction.TransactionType.TRANSFER;
            updateTypeButtonsUI();
        });

        // Establecer estado inicial (gasto seleccionado por defecto)
        updateTypeButtonsUI();
    }

    private void updateTypeButtonsUI() {
        // Reset all buttons
        binding.btnIngreso.setBackgroundColor(getResources().getColor(android.R.color.transparent, null));
        binding.btnGasto.setBackgroundColor(getResources().getColor(android.R.color.transparent, null));
        binding.btnTransferencia.setBackgroundColor(getResources().getColor(android.R.color.transparent, null));

        // Highlight selected button
        int selectedColor = getResources().getColor(android.R.color.white, null);
        switch (selectedType) {
            case INCOME:
                binding.btnIngreso.setBackgroundColor(selectedColor);
                binding.cardAccountTo.setVisibility(View.GONE);
                binding.tvAccountLabel.setVisibility(View.GONE);
                break;
            case EXPENSE:
                binding.btnGasto.setBackgroundColor(selectedColor);
                binding.cardAccountTo.setVisibility(View.GONE);
                binding.tvAccountLabel.setVisibility(View.GONE);
                break;
            case TRANSFER:
                binding.btnTransferencia.setBackgroundColor(selectedColor);
                binding.cardAccountTo.setVisibility(View.VISIBLE);
                binding.tvAccountLabel.setVisibility(View.VISIBLE);
                binding.tvAccountLabel.setText("Desde");
                break;
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

    private void updatePaymentMethodSpinner() {
        ArrayAdapter<PaymentMethod> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                paymentMethods
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerAccount.setAdapter(adapter);
        binding.spinnerAccountTo.setAdapter(adapter);

        // Seleccionar el primer método por defecto (efectivo)
        if (!paymentMethods.isEmpty()) {
            selectedPaymentMethod = paymentMethods.get(0);
            selectedPaymentMethodTo = paymentMethods.get(0);
        }
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

            // Procesar transferencia
            performTransfer(amount);
            return;
        }

        // Obtener nota (opcional)
        String notes = binding.etNote.getText().toString().trim();

        // Obtener userId desde sesión
        long userId = SessionManager.getUserId(requireContext());

        // Crear transacción
        Transaction transaction = new Transaction();
        transaction.setUserId(userId);
        transaction.setAmount(amount);
        transaction.setType(selectedType);
        transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        transaction.setCurrencyCode("MXN");
        transaction.setNotes(notes.isEmpty() ? null : notes);

        // Convertir LocalDate a Instant para transactionDate
        Instant transactionDate = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        transaction.setTransactionDate(transactionDate);

        // Asociar método de pago
        switch (selectedPaymentMethod.getType()) {
            case CREDIT_CARD:
                transaction.setCardId(selectedPaymentMethod.getEntityId());
                transaction.setCardType("CREDIT");
                break;
            case DEBIT_CARD:
                transaction.setCardId(selectedPaymentMethod.getEntityId());
                transaction.setCardType("DEBIT");
                break;
            case CASH:
                // Para efectivo no se asigna card_id
                transaction.setCardType("CASH");
                break;
        }

        // TODO: Asociar con categoría seleccionada en spinner
        // String selectedCategory = binding.spinnerCategory.getSelectedItem().toString();
        // transaction.setCategoryId(getCategoryIdByName(selectedCategory));

        // Verificar si hay un viaje activo y asociar automáticamente
        FinTrackDatabase.databaseWriteExecutor.execute(() -> {
            try {
                var activeTrip = tripRepository.getActiveTripSync(userId);
                if (activeTrip != null) {
                    transaction.setTripId(activeTrip.getTripId());
                }

                // Guardar la transacción
                transactionRepository.insertTransaction(transaction);

                // Actualizar saldos según el método de pago
                updateBalances(transaction, selectedPaymentMethod);

                // Mostrar mensaje en main thread
                requireActivity().runOnUiThread(() -> {
                    String typeText = selectedType == Transaction.TransactionType.INCOME ? "Ingreso" :
                                    selectedType == Transaction.TransactionType.EXPENSE ? "Gasto" : "Transferencia";
                    String paymentText = selectedPaymentMethod.getDisplayName();
                    Toast.makeText(requireContext(), typeText + " guardado - " + paymentText, Toast.LENGTH_LONG).show();
                    Navigation.findNavController(requireView()).navigateUp();
                });
            } catch (Exception e) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Error al guardar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    /**
     * Actualiza los saldos de tarjetas o cuentas según el tipo de transacción
     */
    private void updateBalances(Transaction transaction, PaymentMethod paymentMethod) {
        double effectiveAmount = transaction.getAmount();

        // Para gastos, el monto es negativo en el saldo
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
                // Para efectivo no actualizamos nada por ahora
                // En el futuro se podría llevar un registro de efectivo disponible
                break;
        }
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
                        transferNote + " [Salida]"
                );

                // Crear transacción de entrada (destino)
                Transaction inTransaction = createTransferTransaction(
                        userId,
                        amount,
                        Transaction.TransactionType.INCOME,
                        selectedPaymentMethodTo,
                        transactionDate,
                        transferNote + " [Entrada]"
                );

                // Guardar ambas transacciones
                transactionRepository.insertTransaction(outTransaction);
                transactionRepository.insertTransaction(inTransaction);

                // Actualizar saldos
                updateBalances(outTransaction, selectedPaymentMethod);
                updateBalances(inTransaction, selectedPaymentMethodTo);

                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(),
                        "Transferencia realizada: " + selectedPaymentMethod.getDisplayName() + " → " + selectedPaymentMethodTo.getDisplayName(),
                        Toast.LENGTH_LONG).show();
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
        transaction.setCurrencyCode("MXN");
        transaction.setNotes(notes);
        transaction.setTransactionDate(date);

        // Asociar método de pago
        switch (method.getType()) {
            case CREDIT_CARD:
                transaction.setCardId(method.getEntityId());
                transaction.setCardType("CREDIT");
                break;
            case DEBIT_CARD:
                transaction.setCardId(method.getEntityId());
                transaction.setCardType("DEBIT");
                break;
            case CASH:
                transaction.setCardType("CASH");
                break;
        }

        return transaction;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
