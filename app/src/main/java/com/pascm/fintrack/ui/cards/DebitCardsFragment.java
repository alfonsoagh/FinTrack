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

import com.google.android.material.button.MaterialButton;
import com.pascm.fintrack.R;
import com.pascm.fintrack.model.CreditCard;
import com.pascm.fintrack.model.DebitCard;

import java.util.ArrayList;
import java.util.List;

public class DebitCardsFragment extends Fragment {

    private RecyclerView rvDebitCards;
    private DebitCardAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_debit_cards, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Configurar botón de cerrar
        View btnClose = view.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(v -> Navigation.findNavController(view).navigateUp());

        // Configurar FAB de agregar
        view.findViewById(R.id.fabAddCard).setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_debitCards_to_addDebitCard)
        );

        // Configurar RecyclerView
        rvDebitCards = view.findViewById(R.id.rvDebitCards);
        rvDebitCards.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new DebitCardAdapter();
        adapter.setOnCardClickListener(card -> {
            Toast.makeText(requireContext(),
                "Tarjeta: " + card.getBank() + " - " + card.getAlias(),
                Toast.LENGTH_SHORT).show();
            // TODO: Navegar a detalles de la tarjeta cuando esté implementado
        });

        rvDebitCards.setAdapter(adapter);

        // Cargar datos de ejemplo
        loadSampleData();
    }

    private void loadSampleData() {
        List<DebitCard> cards = new ArrayList<>();

        cards.add(new DebitCard(
                "Banco Santander",
                "Nómina",
                "mastercard",
                "8892",
                CreditCard.CardGradient.VIOLET
        ));

        cards.add(new DebitCard(
                "Banco Nación",
                "Ahorros",
                "visa",
                "1234",
                CreditCard.CardGradient.SKY_BLUE
        ));

        cards.add(new DebitCard(
                "NU",
                "Compras",
                "mastercard",
                "2312",
                CreditCard.CardGradient.SUNSET
        ));

        adapter.setCards(cards);
    }
}
