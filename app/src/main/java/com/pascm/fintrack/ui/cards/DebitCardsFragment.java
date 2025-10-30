package com.pascm.fintrack.ui.cards;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pascm.fintrack.R;
import com.pascm.fintrack.data.local.FinTrackDatabase;
import com.pascm.fintrack.data.local.dao.AccountDao;
import com.pascm.fintrack.data.local.entity.Account;
import com.pascm.fintrack.data.local.entity.DebitCardEntity;
import com.pascm.fintrack.data.repository.CardRepository;
import com.pascm.fintrack.model.CreditCard;
import com.pascm.fintrack.model.DebitCard;
import com.pascm.fintrack.util.SessionManager;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class DebitCardsFragment extends Fragment {

    private RecyclerView rvDebitCards;
    private View emptyStateContainer;
    private DebitCardAdapter adapter;
    private CardRepository cardRepository;
    private AccountDao accountDao;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_debit_cards, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Repositories
        cardRepository = new CardRepository(requireContext());
        FinTrackDatabase database = FinTrackDatabase.getDatabase(requireContext());
        accountDao = database.accountDao();

        // User ID
        long userId = SessionManager.getUserId(requireContext());

        // Configurar botón de cerrar
        View btnClose = view.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(v -> Navigation.findNavController(view).navigateUp());

        // Configurar FAB de agregar
        view.findViewById(R.id.fabAddCard).setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_debitCards_to_addDebitCard)
        );

        // Empty state container
        emptyStateContainer = view.findViewById(R.id.emptyStateContainer);

        // Configurar RecyclerView
        rvDebitCards = view.findViewById(R.id.rvDebitCards);
        rvDebitCards.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new DebitCardAdapter();
        adapter.setOnCardClickListener(card -> {
            Bundle args = new Bundle();
            args.putLong("cardId", card.getCardId());
            Navigation.findNavController(requireView()).navigate(
                R.id.action_debitCards_to_editDebitCard,
                args
            );
        });

        rvDebitCards.setAdapter(adapter);

        // Cargar tarjetas desde BD
        observeCards(userId);
    }

    private void observeCards(long userId) {
        cardRepository.getAllDebitCards(userId).observe(getViewLifecycleOwner(), entities -> {
            if (entities != null && !entities.isEmpty()) {
                adapter.setCards(entities);
                showContent();
            } else {
                adapter.setCards(new ArrayList<>());
                showEmptyState();
            }
        });
    }

    private void showEmptyState() {
        if (emptyStateContainer != null) {
            emptyStateContainer.setVisibility(View.VISIBLE);
        }
        if (rvDebitCards != null) {
            rvDebitCards.setVisibility(View.GONE);
        }
    }

    private void showContent() {
        if (emptyStateContainer != null) {
            emptyStateContainer.setVisibility(View.GONE);
        }
        if (rvDebitCards != null) {
            rvDebitCards.setVisibility(View.VISIBLE);
        }
    }
}
