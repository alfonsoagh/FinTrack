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
import com.pascm.fintrack.databinding.FragmentTransferenciaBinding;
import com.pascm.fintrack.model.PaymentMethod;
import com.pascm.fintrack.util.SessionManager;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TransferenciaFragment extends Fragment {

    private FragmentTransferenciaBinding binding;
    private TransactionRepository transactionRepository;
    private CardRepository cardRepository;

    private LocalDate selectedDate = LocalDate.now();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("d 'de' MMMM, yyyy", new Locale("es", "MX"));

    private List<PaymentMethod> paymentMethods = new ArrayList<>();
    private PaymentMethod fromMethod;
    private PaymentMethod toMethod;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTransferenciaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize repositories
        transactionRepository = new TransactionRepository(requireContext());
        cardRepository = new CardRepository(requireContext());

        updateDateDisplay();
        loadPaymentMethods();

        // Verificar si hay datos precargados desde el anuncio de pago
        if (getArguments() != null) {
            long creditCardId = getArguments().getLong("creditCardId", -1);
            double amount = getArguments().getDouble("amount", 0.0);
            String cardLabel = getArguments().getString("cardLabel", "");

            if (creditCardId != -1 && amount > 0) {
                // Precargar el monto
                binding.etAmount.setText(String.format(java.util.Locale.US, "%.2f", amount));

                // Precargar nota
                binding.etNote.setText("Pago de tarjeta " + cardLabel);

                // La tarjeta de crédito se seleccionará automáticamente cuando se carguen los métodos de pago
                // mediante el método loadPaymentMethods() que se llama arriba
            }
        }

        // Botón cerrar
        binding.btnClose.setOnClickListener(v ->
                Navigation.findNavController(v).navigateUp()
        );

        // Botón transferir
        binding.btnTransfer.setOnClickListener(v -> performTransfer());
    }

    private void updateDateDisplay() {
        binding.tvDate.setText(selectedDate.format(dateFormatter));
    }

    private void loadPaymentMethods() {
        long userId = SessionManager.getUserId(requireContext());

        // Agregar efectivo
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
            updatePaymentMethodSpinners();
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
            updatePaymentMethodSpinners();
        });
    }

    private void updatePaymentMethodSpinners() {
        // Configurar spinner "Desde"
        updateFromSpinnerList();

        // Configurar spinner "Hacia"
        updateToSpinnerList();

        // Listener para actualizar el spinner "Hacia" cuando cambia "Desde"
        binding.spinnerFrom.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                PaymentMethod selected = (PaymentMethod) parent.getItemAtPosition(position);
                if (selected != null) {
                    fromMethod = selected;
                    updateToSpinnerList();
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });

        // Listener para actualizar el spinner "Desde" cuando cambia "Hacia"
        binding.spinnerTo.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                PaymentMethod selected = (PaymentMethod) parent.getItemAtPosition(position);
                if (selected != null) {
                    toMethod = selected;
                    updateFromSpinnerList();
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });
    }

    private void updateFromSpinnerList() {
        List<PaymentMethod> filtered = new ArrayList<>();
        for (PaymentMethod method : paymentMethods) {
            // Excluir el método seleccionado en "Hacia"
            if (toMethod == null ||
                method.getType() != toMethod.getType() ||
                method.getEntityId() != toMethod.getEntityId()) {
                filtered.add(method);
            }
        }

        ArrayAdapter<PaymentMethod> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                filtered
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerFrom.setAdapter(adapter);

        // Seleccionar el primer elemento por defecto si fromMethod no está en la lista
        if (!filtered.isEmpty()) {
            if (fromMethod == null || !filtered.contains(fromMethod)) {
                fromMethod = filtered.get(0);
                binding.spinnerFrom.setSelection(0);
            }
        }
    }

    private void updateToSpinnerList() {
        List<PaymentMethod> filtered = new ArrayList<>();
        for (PaymentMethod method : paymentMethods) {
            // Excluir el método seleccionado en "Desde"
            if (fromMethod == null ||
                method.getType() != fromMethod.getType() ||
                method.getEntityId() != fromMethod.getEntityId()) {
                filtered.add(method);
            }
        }

        ArrayAdapter<PaymentMethod> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                filtered
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerTo.setAdapter(adapter);

        // Seleccionar el primer elemento por defecto si toMethod no está en la lista
        if (!filtered.isEmpty()) {
            if (toMethod == null || !filtered.contains(toMethod)) {
                toMethod = filtered.get(0);
                binding.spinnerTo.setSelection(0);
            }
        }
    }

    private void performTransfer() {
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

        // Obtener métodos seleccionados
        int fromPosition = binding.spinnerFrom.getSelectedItemPosition();
        int toPosition = binding.spinnerTo.getSelectedItemPosition();

        if (fromPosition >= 0 && fromPosition < paymentMethods.size()) {
            fromMethod = paymentMethods.get(fromPosition);
        }

        if (toPosition >= 0 && toPosition < paymentMethods.size()) {
            toMethod = paymentMethods.get(toPosition);
        }

        if (fromMethod == null || toMethod == null) {
            Toast.makeText(requireContext(), "Selecciona origen y destino", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar que no sean el mismo
        if (fromMethod.getType() == toMethod.getType() &&
            fromMethod.getEntityId() == toMethod.getEntityId()) {
            Toast.makeText(requireContext(), "Origen y destino deben ser diferentes", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar que si el destino es tarjeta de crédito, el origen sea débito o efectivo
        if (toMethod.getType() == PaymentMethod.Type.CREDIT_CARD) {
            if (fromMethod.getType() == PaymentMethod.Type.CREDIT_CARD) {
                Toast.makeText(requireContext(), "Solo puedes pagar tarjetas de crédito desde cuentas de débito o efectivo", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Validar saldo suficiente en cuenta origen
        final double finalAmount = amount;
        FinTrackDatabase.databaseWriteExecutor.execute(() -> {
            boolean hasEnoughBalance = checkBalanceAvailability(fromMethod, finalAmount);

            requireActivity().runOnUiThread(() -> {
                if (!hasEnoughBalance) {
                    Toast.makeText(requireContext(), "Saldo insuficiente en la cuenta de origen", Toast.LENGTH_LONG).show();
                } else {
                    // Continuar con la transferencia
                    proceedWithTransfer(finalAmount);
                }
            });
        });
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
                // Para efectivo siempre permitir por ahora
                // En el futuro se podría llevar registro de efectivo disponible
                return true;
            case CREDIT_CARD:
                // Las tarjetas de crédito no deberían ser origen para pagos de otras tarjetas
                // pero si se usa entre cuentas propias, verificar crédito disponible
                CreditCardEntity creditCard = cardRepository.getCreditCardByIdSync(method.getEntityId());
                if (creditCard != null) {
                    return creditCard.getAvailableCredit() >= amount;
                }
                return false;
            default:
                return true;
        }
    }

    private void proceedWithTransfer(double amount) {
        // Obtener nota
        String notes = binding.etNote.getText().toString().trim();
        String transferNote = notes.isEmpty() ?
            "Transferencia de " + fromMethod.getDisplayName() + " a " + toMethod.getDisplayName() :
            notes;

        long userId = SessionManager.getUserId(requireContext());
        Instant transactionDate = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant();

        // Ejecutar transferencia
        FinTrackDatabase.databaseWriteExecutor.execute(() -> {
            try {
                // Crear transacción de salida (origen)
                Transaction outTransaction = createTransaction(
                        userId,
                        amount,
                        Transaction.TransactionType.EXPENSE,
                        fromMethod,
                        transactionDate,
                        transferNote + " [Salida]"
                );

                // Crear transacción de entrada (destino)
                Transaction inTransaction = createTransaction(
                        userId,
                        amount,
                        Transaction.TransactionType.INCOME,
                        toMethod,
                        transactionDate,
                        transferNote + " [Entrada]"
                );

                // Guardar ambas transacciones
                transactionRepository.insertTransaction(outTransaction);
                transactionRepository.insertTransaction(inTransaction);

                // Actualizar saldos
                updateBalanceForTransfer(fromMethod, -amount); // Resta del origen
                updateBalanceForTransfer(toMethod, amount);    // Suma al destino

                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(),
                        "Transferencia realizada: " + fromMethod.getDisplayName() + " → " + toMethod.getDisplayName(),
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

    private Transaction createTransaction(long userId, double amount, Transaction.TransactionType type,
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

    private void updateBalanceForTransfer(PaymentMethod method, double amount) {
        switch (method.getType()) {
            case CREDIT_CARD:
                updateCreditCardBalance(method.getEntityId(), amount);
                break;
            case DEBIT_CARD:
                updateDebitCardBalance(method.getEntityId(), amount);
                break;
            case CASH:
                // No se actualiza efectivo por ahora
                break;
        }
    }

    private void updateCreditCardBalance(long cardId, double amount) {
        CreditCardEntity card = cardRepository.getCreditCardByIdSync(cardId);
        if (card != null) {
            // amount negativo = sale dinero = aumenta deuda
            // amount positivo = entra dinero = disminuye deuda
            double newBalance = card.getCurrentBalance() - amount;
            newBalance = Math.max(0, newBalance);
            newBalance = Math.min(card.getCreditLimit(), newBalance);

            cardRepository.updateCardBalance(cardId, newBalance);
        }
    }

    private void updateDebitCardBalance(long cardId, double amount) {
        DebitCardEntity card = cardRepository.getDebitCardByIdSync(cardId);
        if (card != null) {
            long accountId = card.getAccountId();

            FinTrackDatabase db = FinTrackDatabase.getDatabase(requireContext());
            var account = db.accountDao().getByIdSync(accountId);

            if (account != null) {
                double newBalance = account.getBalance() + amount;
                newBalance = Math.max(0, newBalance);

                db.accountDao().updateBalance(accountId, newBalance, Instant.now().toEpochMilli());
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
