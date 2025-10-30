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
import com.pascm.fintrack.data.local.entity.DebitCardEntity;
import com.pascm.fintrack.data.repository.CardRepository;
import com.pascm.fintrack.model.CreditCard;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EditDebitCardFragment extends Fragment {

    private CardRepository cardRepository;
    private long cardId;
    private DebitCardEntity existingCard;

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_debit_card, container, false);
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
        cardRepository.getDebitCardById(cardId).observe(getViewLifecycleOwner(), card -> {
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

                // Balance will be entered manually by user
                edtCurrentBalance.setText("");

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
        edtCurrentBalance.addTextChangedListener(previewUpdater);
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

        // Extraer valores del formulario
        String bankName = edtBankName.getText().toString().trim();
        String alias = edtCardAlias.getText() != null ? edtCardAlias.getText().toString().trim() : "";

        // Parsear balance
        double currentBalance = 0;
        try {
            if (edtCurrentBalance.getText() != null && !edtCurrentBalance.getText().toString().trim().isEmpty()) {
                currentBalance = Double.parseDouble(edtCurrentBalance.getText().toString().replaceAll("[^0-9.]", ""));
            }
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Verifica el saldo ingresado", Toast.LENGTH_SHORT).show();
            return;
        }

        // Actualizar la tarjeta existente
        existingCard.setIssuer(bankName);
        existingCard.setLabel(alias.isEmpty() ? "Débito" : alias);
        existingCard.setGradient(selectedGradient.name());

        final double finalBalance = currentBalance;

        // Actualizar en la base de datos
        cardRepository.updateDebitCard(existingCard);

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

        cardRepository.deleteDebitCard(existingCard);
        Toast.makeText(requireContext(),
            "Tarjeta eliminada: " + existingCard.getLabel(),
            Toast.LENGTH_SHORT).show();
        Navigation.findNavController(requireView()).navigateUp();
    }
}
