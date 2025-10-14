package com.pascm.fintrack.ui.cards;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.pascm.fintrack.R;
import com.pascm.fintrack.model.CreditCard;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CreditCardAdapter extends RecyclerView.Adapter<CreditCardAdapter.CardViewHolder> {

    private List<CreditCard> cards;
    private OnCardClickListener listener;
    private final NumberFormat currencyFormat;

    public interface OnCardClickListener {
        void onCardClick(CreditCard card);
    }

    public CreditCardAdapter() {
        this.cards = new ArrayList<>();
        this.currencyFormat = NumberFormat.getNumberInstance(Locale.US);
        this.currencyFormat.setMaximumFractionDigits(0);
    }

    public void setCards(List<CreditCard> cards) {
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
                .inflate(R.layout.item_credit_card, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        CreditCard card = cards.get(position);
        holder.bind(card, listener, currencyFormat);
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    static class CardViewHolder extends RecyclerView.ViewHolder {
        private final ConstraintLayout cardContainer;
        private final TextView txtBankName;
        private final TextView txtCardLabel;
        private final TextView txtCardNumber;
        private final TextView txtBalance;
        private final TextView txtLimit;
        private final TextView txtUsageLabel;
        private final TextView txtUsagePercentage;
        private final ImageView imgBrandLogo;
        private final ProgressBar progressBar;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            cardContainer = itemView.findViewById(R.id.cardContainer);
            txtBankName = itemView.findViewById(R.id.txtBankName);
            txtCardLabel = itemView.findViewById(R.id.txtCardLabel);
            txtCardNumber = itemView.findViewById(R.id.txtCardNumber);
            txtBalance = itemView.findViewById(R.id.txtBalance);
            txtLimit = itemView.findViewById(R.id.txtLimit);
            txtUsageLabel = itemView.findViewById(R.id.txtUsageLabel);
            txtUsagePercentage = itemView.findViewById(R.id.txtUsagePercentage);
            imgBrandLogo = itemView.findViewById(R.id.imgBrandLogo);
            progressBar = itemView.findViewById(R.id.progressBar);
        }

        public void bind(CreditCard card, OnCardClickListener listener, NumberFormat currencyFormat) {
            txtBankName.setText(card.getBank());
            txtCardLabel.setText(card.getLabel());
            txtCardNumber.setText("•••• •••• •••• " + card.getPanLast4());
            txtBalance.setText(currencyFormat.format(card.getBalance()));
            txtLimit.setText(currencyFormat.format(card.getLimit()));

            // Configurar el nivel de uso
            CreditCard.UsageLevel usageLevel = card.getUsageLevel();
            txtUsageLabel.setText(usageLevel.getLabel());

            // Configurar la barra de progreso
            float usagePercentage = card.getUsagePercentage();
            progressBar.setProgress((int) usagePercentage);
            txtUsagePercentage.setText(String.format(Locale.US, "%.0f%% de uso", usagePercentage));

            // Cambiar el color de la barra de progreso según el nivel de uso
            GradientDrawable progressDrawable = new GradientDrawable();
            progressDrawable.setCornerRadius(5 * itemView.getContext().getResources().getDisplayMetrics().density);
            progressDrawable.setColor(usageLevel.getColor());

            android.graphics.drawable.ClipDrawable clipDrawable = new android.graphics.drawable.ClipDrawable(
                    progressDrawable,
                    android.view.Gravity.START,
                    android.graphics.drawable.ClipDrawable.HORIZONTAL
            );

            android.graphics.drawable.LayerDrawable layerDrawable = new android.graphics.drawable.LayerDrawable(
                    new android.graphics.drawable.Drawable[]{
                            itemView.getContext().getDrawable(R.drawable.progress_bg),
                            clipDrawable
                    }
            );
            layerDrawable.setId(0, android.R.id.background);
            layerDrawable.setId(1, android.R.id.progress);
            progressBar.setProgressDrawable(layerDrawable);

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
