package com.pascm.fintrack.ui.cards;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.pascm.fintrack.R;
import com.pascm.fintrack.data.local.entity.CreditCardEntity;
import com.pascm.fintrack.data.repository.CardRepository;
import com.pascm.fintrack.model.CreditCard;
import com.pascm.fintrack.util.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CreditCardsFragment extends Fragment {

    private RecyclerView rvCreditCards;
    private View emptyStateContainer;
    private MaterialCardView cardPaymentNotice;
    private CreditCardAdapter adapter;
    private CardRepository cardRepository;
    private List<CreditCardEntity> pendingPaymentCards = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_credit_cards, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Repository
        cardRepository = new CardRepository(requireContext());

        // User ID
        long userId = SessionManager.getUserId(requireContext());

        // Botón cerrar
        View btnClose = view.findViewById(R.id.btnClose);
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> Navigation.findNavController(view).navigateUp());
        }

        // FAB agregar
        View fabAdd = view.findViewById(R.id.fabAddCard);
        if (fabAdd != null) {
            fabAdd.setOnClickListener(v ->
                    Navigation.findNavController(view).navigate(R.id.action_creditCards_to_addCreditCard)
            );
        }

        // Empty state container
        emptyStateContainer = view.findViewById(R.id.emptyStateContainer);

        // Payment notice card
        cardPaymentNotice = view.findViewById(R.id.cardPaymentNotice);
        if (cardPaymentNotice != null) {
            cardPaymentNotice.setOnClickListener(v -> navigateToPaymentTransfer());
        }

        // RecyclerView
        rvCreditCards = view.findViewById(R.id.rvCreditCards);
        rvCreditCards.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new CreditCardAdapter();
        adapter.setOnCardClickListener(card -> editCard(card));
        rvCreditCards.setAdapter(adapter);

        // Cargar tarjetas
        observeCards(userId);
    }

    private void observeCards(long userId) {
        cardRepository.getAllCreditCards(userId).observe(getViewLifecycleOwner(), entities -> {
            if (entities != null && !entities.isEmpty()) {
                adapter.setCards(entities);

                // Detectar tarjetas pendientes de pago (en periodo de pago)
                pendingPaymentCards.clear();
                for (CreditCardEntity card : entities) {
                    if (card.isInPaymentPeriod() && card.getCurrentBalance() > 0) {
                        pendingPaymentCards.add(card);
                    }
                }

                // Mostrar u ocultar anuncio de pago pendiente
                if (!pendingPaymentCards.isEmpty() && cardPaymentNotice != null) {
                    cardPaymentNotice.setVisibility(View.VISIBLE);
                } else if (cardPaymentNotice != null) {
                    cardPaymentNotice.setVisibility(View.GONE);
                }

                showContent();
            } else {
                adapter.setCards(new ArrayList<>());
                pendingPaymentCards.clear();
                if (cardPaymentNotice != null) {
                    cardPaymentNotice.setVisibility(View.GONE);
                }
                showEmptyState();
            }
        });
    }

    private void navigateToPaymentTransfer() {
        if (pendingPaymentCards.isEmpty()) {
            return;
        }

        // Tomar la primera tarjeta pendiente de pago
        CreditCardEntity card = pendingPaymentCards.get(0);

        // Navegar a la pantalla de transferencia con los datos precargados
        Bundle args = new Bundle();
        args.putLong("creditCardId", card.getCardId());
        args.putDouble("amount", card.getCurrentBalance());
        args.putString("cardLabel", card.getIssuer() + " - " + card.getLabel());

        Navigation.findNavController(requireView()).navigate(
            R.id.action_creditCards_to_transferencia,
            args
        );
    }

    private void showEmptyState() {
        if (emptyStateContainer != null) {
            emptyStateContainer.setVisibility(View.VISIBLE);
        }
        if (rvCreditCards != null) {
            rvCreditCards.setVisibility(View.GONE);
        }
    }

    private void showContent() {
        if (emptyStateContainer != null) {
            emptyStateContainer.setVisibility(View.GONE);
        }
        if (rvCreditCards != null) {
            rvCreditCards.setVisibility(View.VISIBLE);
        }
    }

    private void editCard(CreditCardEntity card) {
        Bundle args = new Bundle();
        args.putLong("cardId", card.getCardId());
        Navigation.findNavController(requireView()).navigate(
            R.id.action_creditCards_to_editCreditCard,
            args
        );
    }
}
