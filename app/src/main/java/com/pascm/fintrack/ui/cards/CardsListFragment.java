package com.pascm.fintrack.ui.cards;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.pascm.fintrack.R;
import com.pascm.fintrack.util.CardsManager;

import java.util.List;

public class CardsListFragment extends Fragment {

    private String type; // "credit" o "debit"
    private View root;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_cards_list, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle args = getArguments();
        if (args != null) type = args.getString("type", CardsManager.TYPE_CREDIT);
        else type = CardsManager.TYPE_CREDIT;

        TextView title = view.findViewById(R.id.tvTitle);
        if (title != null) title.setText(CardsManager.TYPE_CREDIT.equals(type) ? R.string.tarjetas_credito : R.string.tarjetas_debito);

        View btnBack = view.findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> Navigation.findNavController(view).navigateUp());

        MaterialButton btnAdd = view.findViewById(R.id.btnAddCard);
        if (btnAdd != null) btnAdd.setOnClickListener(v -> {
            Bundle b = new Bundle();
            b.putString("type", type);
            Navigation.findNavController(view).navigate(R.id.action_cardsList_to_addCard, b);
        });

        renderCards(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (root != null) renderCards(root);
    }

    private void renderCards(@NonNull View view) {
        LinearLayout listContainer = view.findViewById(R.id.cardsContainer);
        if (listContainer == null) return;
        listContainer.removeAllViews();

        List<String> cards = CardsManager.getCards(requireContext(), type);
        if (cards.isEmpty()) {
            TextView empty = new TextView(requireContext());
            empty.setText(R.string.no_hay_tarjetas);
            listContainer.addView(empty);
            return;
        }
        int pad = (int) (8 * getResources().getDisplayMetrics().density);
        for (String label : cards) {
            MaterialCardView card = new MaterialCardView(requireContext());
            card.setUseCompatPadding(true);
            card.setCardElevation(2f);
            LinearLayout inner = new LinearLayout(requireContext());
            inner.setOrientation(LinearLayout.VERTICAL);
            inner.setPadding(pad, pad, pad, pad);
            TextView tv = new TextView(requireContext());
            tv.setText(label);
            inner.addView(tv);
            card.addView(inner);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.topMargin = pad;
            listContainer.addView(card, lp);
        }
    }
}
