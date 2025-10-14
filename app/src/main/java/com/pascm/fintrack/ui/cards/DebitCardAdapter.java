package com.pascm.fintrack.ui.cards;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.pascm.fintrack.R;
import com.pascm.fintrack.model.CreditCard;
import com.pascm.fintrack.model.DebitCard;

import java.util.ArrayList;
import java.util.List;

public class DebitCardAdapter extends RecyclerView.Adapter<DebitCardAdapter.CardViewHolder> {

    private List<DebitCard> cards;
    private OnCardClickListener listener;

    public interface OnCardClickListener {
        void onCardClick(DebitCard card);
    }

    public DebitCardAdapter() {
        this.cards = new ArrayList<>();
    }

    public void setCards(List<DebitCard> cards) {
        this.cards = cards;
        notifyDataSetChanged();
    }

    public void setOnCardClickListener(OnCardClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_debit_card, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        DebitCard card = cards.get(position);
        holder.bind(card, listener);
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    static class CardViewHolder extends RecyclerView.ViewHolder {
        private final ConstraintLayout cardContainer;
        private final TextView txtBankName;
        private final TextView txtCardAlias;
        private final TextView txtCardNumber;
        private final TextView txtCardBalance;
        private final ImageView imgBrandLogo;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            cardContainer = itemView.findViewById(R.id.cardContainer);
            txtBankName = itemView.findViewById(R.id.txtBankName);
            txtCardAlias = itemView.findViewById(R.id.txtCardAlias);
            txtCardNumber = itemView.findViewById(R.id.txtCardNumber);
            txtCardBalance = itemView.findViewById(R.id.txtCardBalance);
            imgBrandLogo = itemView.findViewById(R.id.imgBrandLogo);
        }

        public void bind(DebitCard card, OnCardClickListener listener) {
            txtBankName.setText(card.getBank());
            txtCardAlias.setText(card.getAlias());
            txtCardNumber.setText("•••• •••• •••• " + card.getPanLast4());
            txtCardBalance.setText(card.getBalance());

            // Configurar el gradiente de fondo
            CreditCard.CardGradient gradient = card.getGradient();
            GradientDrawable gradientDrawable = new GradientDrawable(
                    GradientDrawable.Orientation.TL_BR,
                    new int[]{
                            gradient.getStartColor(),
                            gradient.getCenterColor(),
                            gradient.getEndColor()
                    }
            );
            gradientDrawable.setCornerRadius(24 * itemView.getContext().getResources().getDisplayMetrics().density);
            cardContainer.setBackground(gradientDrawable);

            // Configurar el logo de la marca
            int logoRes = "visa".equalsIgnoreCase(card.getBrand())
                    ? R.drawable.ic_visa_logo
                    : R.drawable.ic_mastercard_logo;
            imgBrandLogo.setImageResource(logoRes);

            // Click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCardClick(card);
                }
            });
        }
    }
}
