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
import com.pascm.fintrack.util.SessionManager;
import com.pascm.fintrack.data.local.FinTrackDatabase;
import com.pascm.fintrack.data.local.dao.AccountDao;
import com.pascm.fintrack.data.local.entity.Account;
import com.pascm.fintrack.data.local.entity.DebitCardEntity;
import com.pascm.fintrack.data.repository.CardRepository;

import java.text.NumberFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AddDebitCardFragment extends Fragment {

    private TextInputEditText edtBankName, edtCardAlias, edtCardNumber;
    private TextInputEditText edtCurrentBalance;
    private RadioGroup rgBrand;

    // Vista previa
    private View previewCardMaterial;
    private ConstraintLayout previewCardContainer;
    private TextView previewBankName, previewCardAlias, previewBrandText;
    private TextView previewCardNumber, previewBalance;

    private CreditCard.CardGradient selectedGradient = CreditCard.CardGradient.VIOLET;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));

    private boolean isFormatting = false;
    private TextWatcher cardNumberWatcher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_debit_card, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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
        edtCurrentBalance = view.findViewById(R.id.edtCurrentBalance);
        rgBrand = view.findViewById(R.id.rgBrand);

        // Preview views
        View previewCard = view.findViewById(R.id.cardPreview);
        previewCardMaterial = previewCard.findViewById(R.id.previewCardMaterial);
        previewCardContainer = previewCard.findViewById(R.id.previewCardContainer);
        previewBankName = previewCard.findViewById(R.id.previewBankName);
        previewCardAlias = previewCard.findViewById(R.id.previewCardAlias);
        previewBrandText = previewCard.findViewById(R.id.previewBrandText);
        previewCardNumber = previewCard.findViewById(R.id.previewCardNumber);
        previewBalance = previewCard.findViewById(R.id.previewBalance);

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
        edtCurrentBalance.addTextChangedListener(previewUpdater);

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

        // Balance
        try {
            double balance = edtCurrentBalance != null && edtCurrentBalance.getText() != null
                    ? Double.parseDouble(edtCurrentBalance.getText().toString().replaceAll("[^0-9.]", "")) : 0;
            previewBalance.setText(currencyFormat.format(balance));
        } catch (NumberFormatException e) {
            previewBalance.setText("—");
        }
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

        if (edtCardNumber.getText() == null || edtCardNumber.getText().toString().replaceAll("\\s", "").length() < 4) {
            Toast.makeText(requireContext(), "Ingresa un número de tarjeta válido", Toast.LENGTH_SHORT).show();
            return;
        }

        String bankName = edtBankName.getText().toString().trim();
        String alias = edtCardAlias.getText() != null ? edtCardAlias.getText().toString().trim() : "";
        String cardNumber = edtCardNumber.getText().toString().replaceAll("\\s", "");
        String last4 = cardNumber.length() >= 4 ? cardNumber.substring(cardNumber.length() - 4) : "0000";

        int checkedId = rgBrand.getCheckedRadioButtonId();
        String brand = "visa";
        if (checkedId == R.id.rbMastercard) brand = "mastercard";
        else if (checkedId == R.id.rbAmex) brand = "amex";
        else if (checkedId == R.id.rbOther) brand = "other";

        long userId = SessionManager.getUserId(requireContext());
        if (userId <= 0) {
            Toast.makeText(requireContext(), "Sesión no válida", Toast.LENGTH_SHORT).show();
            return;
        }

        // Obtener el saldo inicial ingresado
        double initialBalance = 0;
        try {
            if (edtCurrentBalance.getText() != null && !edtCurrentBalance.getText().toString().trim().isEmpty()) {
                initialBalance = Double.parseDouble(edtCurrentBalance.getText().toString().replaceAll("[^0-9.]", ""));
            }
        } catch (NumberFormatException e) {
            initialBalance = 0;
        }

        // Variables finales para usar dentro de la lambda
        final String finalBankName = bankName;
        final String finalAlias = alias;
        final String finalLast4 = last4;
        final String finalBrand = brand;
        final CreditCard.CardGradient finalGradient = selectedGradient;
        final long finalUserId = userId;
        final double finalInitialBalance = initialBalance;

        // Ejecutar guardado en hilo de fondo
        FinTrackDatabase.databaseWriteExecutor.execute(() -> {
            try {
                FinTrackDatabase db = FinTrackDatabase.getDatabase(requireContext());
                AccountDao accountDao = db.accountDao();

                // Crear una nueva cuenta para esta tarjeta de débito con el saldo inicial
                Account acc = new Account();
                acc.setUserId(finalUserId);
                acc.setName(finalAlias.isEmpty() ? finalBankName + " - Débito" : finalAlias);
                acc.setType(Account.AccountType.CHECKING);
                acc.setCurrencyCode("MXN");
                acc.setBalance(finalInitialBalance);
                acc.setAvailable(finalInitialBalance);
                acc.setCreatedAt(Instant.now());
                acc.setUpdatedAt(Instant.now());
                long accountId = accountDao.insert(acc);

                // Crear entidad de tarjeta de débito
                DebitCardEntity card = new DebitCardEntity();
                card.setUserId(finalUserId);
                card.setAccountId(accountId);
                card.setIssuer(finalBankName);
                card.setLabel(finalAlias.isEmpty() ? "Débito" : finalAlias);
                card.setBrand(finalBrand);
                card.setPanLast4(finalLast4);
                card.setGradient(finalGradient.name());
                card.setPrimary(false);
                card.setActive(true);
                card.setArchived(false);
                card.setCreatedAt(Instant.now());
                card.setUpdatedAt(Instant.now());

                // Insertar via repositorio
                new CardRepository(requireContext()).insertDebitCard(card);

                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Tarjeta de débito registrada", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).navigateUp();
                });
            } catch (Exception e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Error al guardar: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        });
    }
}
