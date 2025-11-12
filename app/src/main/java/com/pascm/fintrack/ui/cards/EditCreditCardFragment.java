package com.pascm.fintrack.ui.cards;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.pascm.fintrack.R;
import com.pascm.fintrack.data.local.entity.CreditCardEntity;
import com.pascm.fintrack.data.repository.CardRepository;
import com.pascm.fintrack.model.CreditCard;

import java.text.NumberFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EditCreditCardFragment extends Fragment {

    private CardRepository cardRepository;
    private long cardId;
    private CreditCardEntity existingCard;

    private TextInputEditText edtBankName, edtCardAlias, edtCardNumber;
    private TextInputEditText edtCreditLimit, edtStatementDay, edtDueDay;
    private RadioGroup rgBrand;

    // Vista previa
    private View previewCardMaterial;
    private ConstraintLayout previewCardContainer;
    private TextView previewBankName, previewCardAlias, previewBrandText;
    private TextView previewCardNumber, previewAvailable, previewAvailableLabel;
    private TextView previewStatementDate, previewDueDate;

    private CreditCard.CardGradient selectedGradient = CreditCard.CardGradient.VIOLET;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private LocalDate nextStatementDate;
    private LocalDate nextPaymentDate;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_credit_card, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get card ID from arguments
        if (getArguments() != null) {
            cardId = getArguments().getLong("cardId", -1);
        }

        if (cardId == -1) {
            Toast.makeText(requireContext(), "Error: ID de tarjeta inválido", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(view).navigateUp();
            return;
        }

        // Initialize repository
        cardRepository = new CardRepository(requireContext());

        initViews(view);
        loadCardData();
        setupListeners();
        setupGradientSelector(view);
    }

    private void loadCardData() {
        cardRepository.getCreditCardById(cardId).observe(getViewLifecycleOwner(), card -> {
            if (card == null) {
                Toast.makeText(requireContext(), "Tarjeta no encontrada", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(requireView()).navigateUp();
                return;
            }

            existingCard = card;

            {
                // Set form values
                edtBankName.setText(card.getIssuer());
                edtCardAlias.setText(card.getLabel());
                edtCardNumber.setText("**** **** **** " + card.getPanLast4());

                if (card.getCreditLimit() > 0) {
                    edtCreditLimit.setText(String.valueOf(card.getCreditLimit()));
                }
                // Eliminado el setText para edtCurrentBalance ya que no es editable

                // Set brand radio button
                String brand = card.getBrand() != null ? card.getBrand().toLowerCase() : "visa";
                switch (brand) {
                    case "mastercard":
                        rgBrand.check(R.id.rbMastercard);
                        break;
                    case "amex":
                        rgBrand.check(R.id.rbAmex);
                        break;
                    case "other":
                        rgBrand.check(R.id.rbOther);
                        break;
                    default:
                        rgBrand.check(R.id.rbVisa);
                        break;
                }

                // Set dates
                if (card.getStatementDay() != null) {
                    LocalDate now = LocalDate.now();
                    // Validar que el día sea válido para el mes actual
                    int maxDayInMonth = now.lengthOfMonth();
                    int statementDay = Math.min(card.getStatementDay(), maxDayInMonth);
                    nextStatementDate = LocalDate.of(now.getYear(), now.getMonth(), statementDay);
                    edtStatementDay.setText(nextStatementDate.format(dateFormatter));
                }
                if (card.getPaymentDueDay() != null) {
                    LocalDate now = LocalDate.now();
                    // Validar que el día sea válido para el mes actual
                    int maxDayInMonth = now.lengthOfMonth();
                    int paymentDay = Math.min(card.getPaymentDueDay(), maxDayInMonth);
                    nextPaymentDate = LocalDate.of(now.getYear(), now.getMonth(), paymentDay);
                    edtDueDay.setText(nextPaymentDate.format(dateFormatter));
                }

                // Set gradient
                if (card.getGradient() != null) {
                    try {
                        selectedGradient = CreditCard.CardGradient.valueOf(card.getGradient());
                    } catch (IllegalArgumentException e) {
                        selectedGradient = CreditCard.CardGradient.VIOLET;
                    }
                }

                updatePreview();
                updatePreviewGradient();
            }
        });
    }

    private void initViews(View view) {
        // Form fields
        edtBankName = view.findViewById(R.id.edtBankName);
        edtCardAlias = view.findViewById(R.id.edtCardAlias);
        edtCardNumber = view.findViewById(R.id.edtCardNumber);
        edtCreditLimit = view.findViewById(R.id.edtCreditLimit);
        edtStatementDay = view.findViewById(R.id.edtStatementDay);
        edtDueDay = view.findViewById(R.id.edtDueDay);
        rgBrand = view.findViewById(R.id.rgBrand);

        // Preview views
        View previewCard = view.findViewById(R.id.cardPreview);
        previewCardMaterial = previewCard.findViewById(R.id.previewCardMaterial);
        previewCardContainer = previewCard.findViewById(R.id.previewCardContainer);
        previewBankName = previewCard.findViewById(R.id.previewBankName);
        previewCardAlias = previewCard.findViewById(R.id.previewCardAlias);
        previewBrandText = previewCard.findViewById(R.id.previewBrandText);
        previewCardNumber = previewCard.findViewById(R.id.previewCardNumber);
        previewAvailable = previewCard.findViewById(R.id.previewAvailable);
        previewAvailableLabel = previewCard.findViewById(R.id.previewAvailableLabel);
        previewStatementDate = previewCard.findViewById(R.id.previewStatementDate);
        previewDueDate = previewCard.findViewById(R.id.previewDueDate);

        // Buttons
        view.findViewById(R.id.btnBack).setOnClickListener(v ->
                Navigation.findNavController(view).navigateUp());

        view.findViewById(R.id.btnDelete).setOnClickListener(v -> showDeleteConfirmation());

        view.findViewById(R.id.btnSave).setOnClickListener(v -> updateCard());
    }

    private void setupListeners() {
        TextWatcher previewUpdater = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePreview();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        edtBankName.addTextChangedListener(previewUpdater);
        edtCardAlias.addTextChangedListener(previewUpdater);
        edtCreditLimit.addTextChangedListener(previewUpdater);
        // Eliminado edtCurrentBalance de los listeners ya que no es editable

        // Date pickers for statement and payment dates
        edtStatementDay.setOnClickListener(v -> showStatementDatePicker());
        edtDueDay.setOnClickListener(v -> showPaymentDatePicker());
    }

    private void setupGradientSelector(View view) {
        RecyclerView rvGradients = view.findViewById(R.id.rvGradients);
        rvGradients.setLayoutManager(new LinearLayoutManager(requireContext(),
                LinearLayoutManager.HORIZONTAL, false));

        List<CreditCard.CardGradient> gradients = new ArrayList<>();
        gradients.add(CreditCard.CardGradient.VIOLET);
        gradients.add(CreditCard.CardGradient.SKY_BLUE);
        gradients.add(CreditCard.CardGradient.SUNSET);
        gradients.add(CreditCard.CardGradient.EMERALD);
        gradients.add(CreditCard.CardGradient.ROYAL);
        gradients.add(CreditCard.CardGradient.SILVER);
        gradients.add(CreditCard.CardGradient.ONYX);
        gradients.add(CreditCard.CardGradient.CRIMSON);
        gradients.add(CreditCard.CardGradient.GOLD);

        GradientSelectorAdapter adapter = new GradientSelectorAdapter(gradients, gradient -> {
            selectedGradient = gradient;
            updatePreviewGradient();
        });
        rvGradients.setAdapter(adapter);
    }

    private void showStatementDatePicker() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Próxima fecha de corte")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            nextStatementDate = Instant.ofEpochMilli(selection)
                    .atZone(ZoneId.of("UTC"))
                    .toLocalDate();
            edtStatementDay.setText(nextStatementDate.format(dateFormatter));
            updatePreview();
        });

        datePicker.show(getParentFragmentManager(), "STATEMENT_DATE_PICKER");
    }

    private void showPaymentDatePicker() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Próxima fecha de pago")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            LocalDate selectedPaymentDate = Instant.ofEpochMilli(selection)
                    .atZone(ZoneId.of("UTC"))
                    .toLocalDate();

            if (nextStatementDate != null && selectedPaymentDate.isBefore(nextStatementDate)) {
                Toast.makeText(requireContext(),
                    "La fecha límite de pago no puede ser anterior a la fecha de corte",
                    Toast.LENGTH_LONG).show();
                return;
            }

            nextPaymentDate = selectedPaymentDate;
            edtDueDay.setText(nextPaymentDate.format(dateFormatter));
            updatePreview();
        });

        datePicker.show(getParentFragmentManager(), "PAYMENT_DATE_PICKER");
    }

    private void updatePreview() {
        if (previewBankName == null) return;

        String bankName = edtBankName != null && edtBankName.getText() != null
                ? edtBankName.getText().toString() : "";
        previewBankName.setText(bankName.isEmpty() ? "Banco" : bankName);

        String alias = edtCardAlias != null && edtCardAlias.getText() != null
                ? edtCardAlias.getText().toString() : "";
        previewCardAlias.setText(alias.isEmpty() ? "Alias" : alias);

        int checkedId = rgBrand.getCheckedRadioButtonId();
        String brand = "VISA";
        if (checkedId == R.id.rbMastercard) brand = "MASTERCARD";
        else if (checkedId == R.id.rbAmex) brand = "AMEX";
        else if (checkedId == R.id.rbOther) brand = "OTRA";
        previewBrandText.setText(brand);

        if (existingCard != null) {
            previewCardNumber.setText("•••• •••• •••• " + existingCard.getPanLast4());
        }

        try {
            double limit = edtCreditLimit != null && edtCreditLimit.getText() != null
                    ? Double.parseDouble(edtCreditLimit.getText().toString().replaceAll("[^0-9.]", "")) : 0;
            double available = Math.max(0, limit);
            previewAvailable.setText(currencyFormat.format(available));
        } catch (NumberFormatException e) {
            previewAvailable.setText("—");
        }

        if (nextStatementDate != null) {
            previewStatementDate.setText("Corte: " + nextStatementDate.getDayOfMonth());
        } else {
            previewStatementDate.setText("Corte: —");
        }

        if (nextPaymentDate != null) {
            previewDueDate.setText("Pago: " + nextPaymentDate.getDayOfMonth());
        } else {
            previewDueDate.setText("Pago: —");
        }

        // Actualizar el color del texto según el gradiente seleccionado
        updateTextColors();
    }

    private void updateTextColors() {
        int textColor = (selectedGradient == CreditCard.CardGradient.SILVER ||
                         selectedGradient == CreditCard.CardGradient.GOLD)
                ? getResources().getColor(android.R.color.black)
                : getResources().getColor(android.R.color.white);

        previewBankName.setTextColor(textColor);
        previewCardAlias.setTextColor(textColor);
        previewBrandText.setTextColor(textColor);
        previewCardNumber.setTextColor(textColor);
        previewAvailable.setTextColor(textColor);
        if (previewAvailableLabel != null) {
            previewAvailableLabel.setTextColor(textColor);
        }
        previewStatementDate.setTextColor(textColor);
        previewDueDate.setTextColor(textColor);
    }

    private void updatePreviewGradient() {
        if (previewCardContainer == null || selectedGradient == null) return;

        try {
            GradientDrawable gradientDrawable = new GradientDrawable(
                    GradientDrawable.Orientation.TL_BR,
                    new int[]{
                            selectedGradient.getStartColor(),
                            selectedGradient.getCenterColor(),
                            selectedGradient.getEndColor()
                    }
            );
            gradientDrawable.setCornerRadius(16 * getResources().getDisplayMetrics().density);
            previewCardContainer.setBackground(gradientDrawable);

            // Actualizar colores de texto después de cambiar el gradiente
            updateTextColors();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateCard() {
        if (existingCard == null) {
            Toast.makeText(requireContext(), "Error al cargar la tarjeta", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar campos requeridos
        if (edtBankName.getText() == null || edtBankName.getText().toString().trim().isEmpty()) {
            Toast.makeText(requireContext(), "Ingresa el nombre del banco", Toast.LENGTH_SHORT).show();
            return;
        }

        if (edtCardAlias.getText() == null || edtCardAlias.getText().toString().trim().isEmpty()) {
            Toast.makeText(requireContext(), "Ingresa un alias para la tarjeta", Toast.LENGTH_SHORT).show();
            return;
        }

        // Extraer valores del formulario
        String bankName = edtBankName.getText().toString().trim();
        String alias = edtCardAlias.getText().toString().trim();

        // Parsear límite de crédito
        double creditLimit = 0;
        try {
            if (edtCreditLimit.getText() != null && !edtCreditLimit.getText().toString().trim().isEmpty()) {
                creditLimit = Double.parseDouble(edtCreditLimit.getText().toString().replaceAll("[^0-9.]", ""));
            }
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Verifica el límite de crédito ingresado", Toast.LENGTH_SHORT).show();
            return;
        }

        // Extraer días de las fechas seleccionadas
        Integer statementDay = nextStatementDate != null ? nextStatementDate.getDayOfMonth() : null;
        Integer dueDay = nextPaymentDate != null ? nextPaymentDate.getDayOfMonth() : null;

        // Actualizar la tarjeta existente (manteniendo el saldo currentBalance sin cambios)
        existingCard.setIssuer(bankName);
        existingCard.setLabel(alias);
        existingCard.setCreditLimit(creditLimit);
        // No modificamos el currentBalance ya que se calcula automáticamente
        existingCard.setStatementDay(statementDay);
        existingCard.setPaymentDueDay(dueDay);
        existingCard.setGradient(selectedGradient.name());

        // Actualizar en la base de datos
        cardRepository.updateCreditCard(existingCard);

        Toast.makeText(requireContext(), "Tarjeta actualizada exitosamente", Toast.LENGTH_SHORT).show();
        Navigation.findNavController(requireView()).navigateUp();
    }

    private void showDeleteConfirmation() {
        if (existingCard == null) {
            Toast.makeText(requireContext(), "Error: No se puede eliminar la tarjeta", Toast.LENGTH_SHORT).show();
            return;
        }

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("¿Eliminar tarjeta?")
            .setMessage("¿Estás seguro de que deseas eliminar la tarjeta \"" + existingCard.getLabel() + "\"?\n\nEsta acción no se puede deshacer.")
            .setPositiveButton("Eliminar", (dialog, which) -> deleteCard())
            .setNegativeButton("Cancelar", null)
            .show();
    }

    private void deleteCard() {
        if (existingCard == null) return;

        cardRepository.deleteCreditCard(existingCard);
        Toast.makeText(requireContext(),
            "Tarjeta eliminada: " + existingCard.getLabel(),
            Toast.LENGTH_SHORT).show();
        Navigation.findNavController(requireView()).navigateUp();
    }
}
