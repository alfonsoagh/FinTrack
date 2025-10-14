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
import com.pascm.fintrack.model.CreditCard;

import java.util.ArrayList;
import java.util.List;

public class CreditCardsFragment extends Fragment {

    private RecyclerView rvCreditCards;
    private CreditCardAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_credit_cards, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Configurar botón de cerrar
        View btnClose = view.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(v -> Navigation.findNavController(view).navigateUp());

        // Configurar FAB de agregar
        view.findViewById(R.id.fabAddCard).setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_creditCards_to_addCreditCard)
        );

        // Configurar RecyclerView
        rvCreditCards = view.findViewById(R.id.rvCreditCards);
        rvCreditCards.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new CreditCardAdapter();
        adapter.setOnCardClickListener(card -> {
            Toast.makeText(requireContext(),
                "Tarjeta: " + card.getBank() + " - " + card.getLabel(),
                Toast.LENGTH_SHORT).show();
            // TODO: Navegar a detalles de la tarjeta cuando esté implementado
        });

        rvCreditCards.setAdapter(adapter);

        // Cargar datos de ejemplo
        loadSampleData();
    }

    private void loadSampleData() {
        List<CreditCard> cards = new ArrayList<>();

        cards.add(new CreditCard(
                "Banco Santander",
                "Tarjeta de Viajes",
                "mastercard",
                "1234",
                20000,
                5250,
                CreditCard.CardGradient.VIOLET
        ));

        cards.add(new CreditCard(
                "BBVA",
                "Tarjeta de Compras",
                "visa",
                "5678",
                15000,
                14500,
                CreditCard.CardGradient.CRIMSON
        ));

        adapter.setCards(cards);
    }
}
