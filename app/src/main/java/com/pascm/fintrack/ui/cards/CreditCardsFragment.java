package com.pascm.fintrack.ui.cards;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
    private CreditCardAdapter adapter;
    private CardRepository cardRepository;

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

        // RecyclerView
        rvCreditCards = view.findViewById(R.id.rvCreditCards);
        rvCreditCards.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new CreditCardAdapter();
        adapter.setOnCardClickListener(card -> showCardOptionsMenu(view, card));
        rvCreditCards.setAdapter(adapter);

        // Cargar tarjetas
        observeCards(userId);
    }

    private void observeCards(long userId) {
        cardRepository.getAllCreditCards(userId).observe(getViewLifecycleOwner(), entities -> {
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

    private void showCardOptionsMenu(View anchorView, CreditCardEntity card) {
        PopupMenu popupMenu = new PopupMenu(requireContext(), anchorView);
        popupMenu.getMenuInflater().inflate(R.menu.menu_card_options, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_edit) {
                editCard(card);
                return true;
            } else if (itemId == R.id.action_delete) {
                showDeleteConfirmationDialog(card);
                return true;
            }
            return false;
        });

        popupMenu.show();
    }

    private void editCard(CreditCardEntity card) {
        // TODO: Implementar navegación a pantalla de edición
        Toast.makeText(requireContext(),
            "Editar tarjeta: " + card.getLabel(),
            Toast.LENGTH_SHORT).show();
    }

    private void showDeleteConfirmationDialog(CreditCardEntity card) {
        new AlertDialog.Builder(requireContext())
            .setTitle("¿Eliminar tarjeta?")
            .setMessage("¿Estás seguro de que deseas eliminar la tarjeta \"" + card.getLabel() + "\"? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar", (dialog, which) -> deleteCard(card))
            .setNegativeButton("Cancelar", null)
            .show();
    }

    private void deleteCard(CreditCardEntity card) {
        cardRepository.deleteCreditCard(card);
        Toast.makeText(requireContext(),
            "Tarjeta eliminada: " + card.getLabel(),
            Toast.LENGTH_SHORT).show();
    }
}
