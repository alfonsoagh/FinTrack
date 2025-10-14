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

import com.google.android.material.textfield.TextInputEditText;
import com.pascm.fintrack.R;
import com.pascm.fintrack.model.CreditCard;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AddCreditCardFragment extends Fragment {

    private TextInputEditText edtBankName, edtCardAlias, edtCardNumber;
    private TextInputEditText edtCreditLimit, edtCurrentBalance;
    private TextInputEditText edtStatementDay, edtDueDay;
    private RadioGroup rgBrand;

    // Vista previa
    private ConstraintLayout previewCardContainer;
    private TextView previewBankName, previewCardAlias, previewBrandText;
    private TextView previewCardNumber, previewAvailable;
    private TextView previewStatementDate, previewDueDate;

    private CreditCard.CardGradient selectedGradient = CreditCard.CardGradient.VIOLET;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_credit_card, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupListeners();
        setupGradientSelector(view);
        updatePreview();
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
        edtCardNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                formatCardNumber();
                detectBrand();
                updatePreview();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        edtCreditLimit.addTextChangedListener(previewUpdater);
        edtCurrentBalance.addTextChangedListener(previewUpdater);
        edtStatementDay.addTextChangedListener(previewUpdater);
        edtDueDay.addTextChangedListener(previewUpdater);

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
        if (edtCardNumber == null) return;
        String text = edtCardNumber.getText().toString().replaceAll("\\s", "");
        StringBuilder formatted = new StringBuilder();

        for (int i = 0; i < Math.min(text.length(), 16); i++) {
            if (i > 0 && i % 4 == 0) {
                formatted.append(" ");
            }
            formatted.append(text.charAt(i));
        }

        int selectionStart = edtCardNumber.getSelectionStart();
        edtCardNumber.removeTextChangedListener(null);
        edtCardNumber.setText(formatted.toString());
        edtCardNumber.setSelection(Math.min(selectionStart, formatted.length()));
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
        String statementDay = edtStatementDay != null && edtStatementDay.getText() != null
                ? edtStatementDay.getText().toString() : "—";
        String dueDay = edtDueDay != null && edtDueDay.getText() != null
                ? edtDueDay.getText().toString() : "—";
        previewStatementDate.setText("Corte: " + statementDay);
        previewDueDate.setText("Pago: " + dueDay);
    }

    private void updatePreviewGradient() {
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
    }

    private void saveCard() {
        // Validar campos requeridos
        if (edtBankName.getText() == null || edtBankName.getText().toString().trim().isEmpty()) {
            Toast.makeText(requireContext(), "Ingresa el nombre del banco", Toast.LENGTH_SHORT).show();
            return;
        }

        if (edtCardNumber.getText() == null || edtCardNumber.getText().toString().replaceAll("\\s", "").length() < 4) {
            Toast.makeText(requireContext(), "Ingresa un número de tarjeta válido", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Guardar la tarjeta en preferencias o base de datos
        Toast.makeText(requireContext(), "Tarjeta registrada exitosamente", Toast.LENGTH_SHORT).show();
        Navigation.findNavController(requireView()).navigateUp();
    }
}
