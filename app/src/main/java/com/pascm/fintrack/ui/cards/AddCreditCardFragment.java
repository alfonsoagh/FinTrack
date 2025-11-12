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
import com.pascm.fintrack.util.SessionManager;

import java.text.NumberFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AddCreditCardFragment extends Fragment {

    private CardRepository cardRepository;

    private TextInputEditText edtBankName, edtCardAlias, edtCardNumber;
    private TextInputEditText edtCreditLimit, edtCurrentBalance;
    private TextInputEditText edtStatementDay, edtDueDay;
    private RadioGroup rgBrand;

    // Vista previa
    private View previewCardMaterial;
    private ConstraintLayout previewCardContainer;
    private TextView previewBankName, previewCardAlias, previewBrandText;
    private TextView previewCardNumber, previewAvailable;
    private TextView previewStatementDate, previewDueDate;

    private CreditCard.CardGradient selectedGradient = CreditCard.CardGradient.VIOLET;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private boolean isFormatting = false;
    private TextWatcher cardNumberWatcher;

    private LocalDate nextStatementDate;
    private LocalDate nextPaymentDate;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_credit_card, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize repository
        cardRepository = new CardRepository(requireContext());

        initViews(view);
        setupListeners();
        setupGradientSelector(view);
        updatePreview();
        updatePreviewGradient(); // Inicializar gradiente por defecto
    }

    private void initViews(View view) {
        // Form fields
        edtBankName = view.findViewById(R.id.edtBankName);
        edtCardAlias = view.findViewById(R.id.edtCardAlias);
        edtCardNumber = view.findViewById(R.id.edtCardNumber);
        edtCreditLimit = view.findViewById(R.id.edtCreditLimit);
        edtCurrentBalance = view.findViewById(R.id.edtCurrentBalance);
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
        previewStatementDate = previewCard.findViewById(R.id.previewStatementDate);
        previewDueDate = previewCard.findViewById(R.id.previewDueDate);

        // Buttons
        view.findViewById(R.id.btnBack).setOnClickListener(v ->
                Navigation.findNavController(view).navigateUp());

        view.findViewById(R.id.btnCancel).setOnClickListener(v ->
                Navigation.findNavController(view).navigateUp());

        view.findViewById(R.id.btnSave).setOnClickListener(v -> saveCard());
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

        cardNumberWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isFormatting) {
                    formatCardNumber();
                    detectBrand();
                    updatePreview();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };
        edtCardNumber.addTextChangedListener(cardNumberWatcher);

        edtCreditLimit.addTextChangedListener(previewUpdater);
        edtCurrentBalance.addTextChangedListener(previewUpdater);

        // Date pickers for statement and payment dates
        edtStatementDay.setOnClickListener(v -> showStatementDatePicker());
        edtDueDay.setOnClickListener(v -> showPaymentDatePicker());

        rgBrand.setOnCheckedChangeListener((group, checkedId) -> updatePreview());
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

    private void formatCardNumber() {
        if (edtCardNumber == null || isFormatting) return;

        isFormatting = true;
        String text = edtCardNumber.getText().toString().replaceAll("\\s", "");
        StringBuilder formatted = new StringBuilder();

        for (int i = 0; i < Math.min(text.length(), 16); i++) {
            if (i > 0 && i % 4 == 0) {
                formatted.append(" ");
            }
            formatted.append(text.charAt(i));
        }

        int selectionStart = edtCardNumber.getSelectionStart();
        int spacesBeforeCursor = 0;
        String currentText = edtCardNumber.getText().toString();
        for (int i = 0; i < Math.min(selectionStart, currentText.length()); i++) {
            if (currentText.charAt(i) == ' ') spacesBeforeCursor++;
        }

        edtCardNumber.setText(formatted.toString());

        int newPosition = selectionStart;
        if (formatted.length() > selectionStart && selectionStart > 0) {
            newPosition = Math.min(selectionStart + (formatted.toString().substring(0, Math.min(formatted.length(), selectionStart + 1)).replaceAll("[^\\s]", "").length() - spacesBeforeCursor), formatted.length());
        }
        edtCardNumber.setSelection(Math.min(Math.max(0, newPosition), formatted.length()));

        isFormatting = false;
    }

    private void detectBrand() {
        if (edtCardNumber == null) return;
        String number = edtCardNumber.getText().toString().replaceAll("\\s", "");

        if (number.startsWith("4")) {
            rgBrand.check(R.id.rbVisa);
        } else if (number.startsWith("5") || number.startsWith("2")) {
            rgBrand.check(R.id.rbMastercard);
        } else if (number.startsWith("3")) {
            rgBrand.check(R.id.rbAmex);
        }
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

            // Validar que la fecha de pago no sea anterior a la fecha de corte
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
        // Bank name
        String bankName = edtBankName != null && edtBankName.getText() != null
                ? edtBankName.getText().toString() : "";
        previewBankName.setText(bankName.isEmpty() ? "Banco" : bankName);

        // Alias
        String alias = edtCardAlias != null && edtCardAlias.getText() != null
                ? edtCardAlias.getText().toString() : "";
        previewCardAlias.setText(alias.isEmpty() ? "Alias" : alias);

        // Brand
        int checkedId = rgBrand.getCheckedRadioButtonId();
        String brand = "VISA";
        if (checkedId == R.id.rbMastercard) brand = "MASTERCARD";
        else if (checkedId == R.id.rbAmex) brand = "AMEX";
        else if (checkedId == R.id.rbOther) brand = "OTRA";
        previewBrandText.setText(brand);

        // Card number
        String cardNumber = edtCardNumber != null && edtCardNumber.getText() != null
                ? edtCardNumber.getText().toString() : "";
        String last4 = cardNumber.replaceAll("\\s", "");
        last4 = last4.length() >= 4 ? last4.substring(last4.length() - 4) : "0000";
        previewCardNumber.setText("•••• •••• •••• " + last4);

        // Available amount
        try {
            double limit = edtCreditLimit != null && edtCreditLimit.getText() != null
                    ? Double.parseDouble(edtCreditLimit.getText().toString().replaceAll("[^0-9.]", "")) : 0;
            double balance = edtCurrentBalance != null && edtCurrentBalance.getText() != null
                    ? Double.parseDouble(edtCurrentBalance.getText().toString().replaceAll("[^0-9.]", "")) : 0;
            double available = Math.max(0, limit - balance);
            previewAvailable.setText(currencyFormat.format(available));
        } catch (NumberFormatException e) {
            previewAvailable.setText("—");
        }

        // Dates
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

        // Actualizar colores de texto
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

    private void saveCard() {
        // Validar campos requeridos
        if (edtBankName.getText() == null || edtBankName.getText().toString().trim().isEmpty()) {
            Toast.makeText(requireContext(), "Ingresa el nombre del banco", Toast.LENGTH_SHORT).show();
            return;
        }

        if (edtCardAlias.getText() == null || edtCardAlias.getText().toString().trim().isEmpty()) {
            Toast.makeText(requireContext(), "Ingresa un alias para la tarjeta", Toast.LENGTH_SHORT).show();
            return;
        }

        if (edtCardNumber.getText() == null || edtCardNumber.getText().toString().replaceAll("\\s", "").length() < 4) {
            Toast.makeText(requireContext(), "Ingresa un número de tarjeta válido", Toast.LENGTH_SHORT).show();
            return;
        }

        // Extraer valores del formulario
        String bankName = edtBankName.getText().toString().trim();
        String alias = edtCardAlias.getText().toString().trim();
        String cardNumber = edtCardNumber.getText().toString().replaceAll("\\s", "");
        String last4 = cardNumber.length() >= 4 ? cardNumber.substring(cardNumber.length() - 4) : "0000";

        // Determinar brand
        int checkedId = rgBrand.getCheckedRadioButtonId();
        String brand = "visa"; // Default
        if (checkedId == R.id.rbMastercard) brand = "mastercard";
        else if (checkedId == R.id.rbAmex) brand = "amex";
        else if (checkedId == R.id.rbOther) brand = "other";

        // Parsear límite y balance
        double creditLimit = 0;
        double currentBalance = 0;
        try {
            if (edtCreditLimit.getText() != null && !edtCreditLimit.getText().toString().trim().isEmpty()) {
                creditLimit = Double.parseDouble(edtCreditLimit.getText().toString().replaceAll("[^0-9.]", ""));
            }
            if (edtCurrentBalance.getText() != null && !edtCurrentBalance.getText().toString().trim().isEmpty()) {
                currentBalance = Double.parseDouble(edtCurrentBalance.getText().toString().replaceAll("[^0-9.]", ""));
            }
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Verifica los montos ingresados", Toast.LENGTH_SHORT).show();
            return;
        }

        // Extraer días de las fechas seleccionadas
        Integer statementDay = nextStatementDate != null ? nextStatementDate.getDayOfMonth() : null;
        Integer dueDay = nextPaymentDate != null ? nextPaymentDate.getDayOfMonth() : null;

        // Crear entidad de tarjeta
        CreditCardEntity card = new CreditCardEntity();
        card.setUserId(SessionManager.getUserId(requireContext()));
        card.setIssuer(bankName);
        card.setLabel(alias);
        card.setBrand(brand);
        card.setPanLast4(last4);
        card.setCreditLimit(creditLimit);
        card.setCurrentBalance(currentBalance);
        card.setStatementDay(statementDay);
        card.setPaymentDueDay(dueDay);
        card.setGradient(selectedGradient.name());

        // Guardar en la base de datos
        cardRepository.insertCreditCard(card);

        Toast.makeText(requireContext(), "Tarjeta registrada exitosamente", Toast.LENGTH_SHORT).show();
        Navigation.findNavController(requireView()).navigateUp();
    }
}
