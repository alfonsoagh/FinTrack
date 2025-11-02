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
import com.pascm.fintrack.data.local.entity.DebitCardEntity;
import com.pascm.fintrack.model.CreditCard;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DebitCardAdapter extends RecyclerView.Adapter<DebitCardAdapter.CardViewHolder> {

    private List<DebitCardEntity> cards;
    private java.util.Map<Long, Double> balances; // accountId -> balance
    private OnCardClickListener listener;
    private final DateTimeFormatter expiryFormatter = DateTimeFormatter.ofPattern("MM/yy");
    private final NumberFormat currencyFormat;

    public interface OnCardClickListener {
        void onCardClick(DebitCardEntity card);
    }

    public DebitCardAdapter() {
        this.cards = new ArrayList<>();
        this.balances = new java.util.HashMap<>();
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));
        this.currencyFormat.setMaximumFractionDigits(2);
    }

    public void setCards(List<DebitCardEntity> cards) {
        this.cards = cards;
        notifyDataSetChanged();
    }

    public void setBalances(java.util.Map<Long, Double> balances) {
        this.balances = balances;
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
        DebitCardEntity card = cards.get(position);
        Double balance = balances.get(card.getAccountId());
        holder.bind(card, balance, listener, expiryFormatter, currencyFormat);
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

        public void bind(DebitCardEntity card, Double balance, OnCardClickListener listener, DateTimeFormatter expiryFormatter,
                         NumberFormat currencyFormat) {
            txtBankName.setText(card.getIssuer());
            txtCardAlias.setText(card.getLabel());
            txtCardNumber.setText("•••• •••• •••• " + (card.getPanLast4() != null ? card.getPanLast4() : "0000"));

            // Balance comes from the linked account
            if (balance != null) {
                txtCardBalance.setText(currencyFormat.format(balance));
            } else {
                txtCardBalance.setText("$0.00");
            }

            // Configurar el gradiente de fondo
            CreditCard.CardGradient gradient;
            try {
                gradient = CreditCard.CardGradient.valueOf(card.getGradient());
            } catch (IllegalArgumentException e) {
                // Try with prefixed gradient names
                try {
                    gradient = CreditCard.CardGradient.valueOf(card.getGradient().replace("GRADIENT_", ""));
                } catch (IllegalArgumentException ex) {
                    gradient = CreditCard.CardGradient.SKY_BLUE; // default for debit
                }
            }

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
