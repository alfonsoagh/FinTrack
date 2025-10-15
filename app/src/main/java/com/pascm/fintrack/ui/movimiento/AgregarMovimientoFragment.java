package com.pascm.fintrack.ui.movimiento;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.pascm.fintrack.R;
import com.pascm.fintrack.data.local.FinTrackDatabase;
import com.pascm.fintrack.data.local.entity.Transaction;
import com.pascm.fintrack.data.repository.TransactionRepository;
import com.pascm.fintrack.data.repository.TripRepository;
import com.pascm.fintrack.databinding.FragmentAgregarMovimientoBinding;
import com.pascm.fintrack.util.SessionManager;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class AgregarMovimientoFragment extends Fragment {

    private FragmentAgregarMovimientoBinding binding;
    private TransactionRepository transactionRepository;
    private TripRepository tripRepository;

    private Transaction.TransactionType selectedType = Transaction.TransactionType.EXPENSE;
    private LocalDate selectedDate = LocalDate.now();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("d 'de' MMMM, yyyy", new Locale("es", "MX"));

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

        setupTypeButtons();
        updateDateDisplay();

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
                break;
            case EXPENSE:
                binding.btnGasto.setBackgroundColor(selectedColor);
                break;
            case TRANSFER:
                binding.btnTransferencia.setBackgroundColor(selectedColor);
                break;
        }
    }

    private void updateDateDisplay() {
        binding.tvDate.setText(selectedDate.format(dateFormatter));
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

        // TODO: Asociar con categoría seleccionada en spinner
        // String selectedCategory = binding.spinnerCategory.getSelectedItem().toString();
        // transaction.setCategoryId(getCategoryIdByName(selectedCategory));

        // TODO: Asociar con cuenta/tarjeta seleccionada en spinner
        // String selectedAccount = binding.spinnerAccount.getSelectedItem().toString();
        // transaction.setAccountId(getAccountIdByName(selectedAccount));

        // Verificar si hay un viaje activo y asociar automáticamente
        FinTrackDatabase.databaseWriteExecutor.execute(() -> {
            var activeTrip = tripRepository.getActiveTripSync(userId);
            if (activeTrip != null) {
                transaction.setTripId(activeTrip.getTripId());
            }

            // Guardar la transacción
            transactionRepository.insertTransaction(transaction);

            // Mostrar mensaje en main thread
            requireActivity().runOnUiThread(() -> {
                String typeText = selectedType == Transaction.TransactionType.INCOME ? "Ingreso" :
                                selectedType == Transaction.TransactionType.EXPENSE ? "Gasto" : "Transferencia";
                Toast.makeText(requireContext(), typeText + " guardado exitosamente", Toast.LENGTH_SHORT).show();
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
