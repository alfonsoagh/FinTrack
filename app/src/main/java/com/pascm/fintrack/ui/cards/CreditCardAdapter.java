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
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.pascm.fintrack.R;
import com.pascm.fintrack.data.local.entity.CreditCardEntity;
import com.pascm.fintrack.model.CreditCard;

import java.text.NumberFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CreditCardAdapter extends RecyclerView.Adapter<CreditCardAdapter.CardViewHolder> {

    private List<CreditCardEntity> cards;
    private OnCardClickListener listener;
    private final NumberFormat currencyFormat;
    private final DateTimeFormatter expiryFormatter = DateTimeFormatter.ofPattern("MM/yy");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("d MMM", new Locale("es", "MX"));

    public interface OnCardClickListener {
        void onCardClick(CreditCardEntity card);
    }

    public CreditCardAdapter() {
        this.cards = new ArrayList<>();
        this.currencyFormat = NumberFormat.getNumberInstance(Locale.US);
        this.currencyFormat.setMaximumFractionDigits(0);
    }

    public void setCards(List<CreditCardEntity> cards) {
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
        CreditCardEntity card = cards.get(position);
        holder.bind(card, listener, currencyFormat, expiryFormatter, dateFormatter);
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
        private final ImageView imgCardIcon;
        private final ProgressBar progressBar;
        private final TextView txtStatementDate;
        private final TextView txtPaymentDate;
        private final TextView txtBalanceLabel;
        private final TextView txtLimitLabel;
        private final TextView txtStatementLabel;
        private final TextView txtPaymentLabel;

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
            imgCardIcon = itemView.findViewById(R.id.imgCardIcon);
            progressBar = itemView.findViewById(R.id.progressBar);
            txtStatementDate = itemView.findViewById(R.id.txtStatementDate);
            txtPaymentDate = itemView.findViewById(R.id.txtPaymentDate);
            txtBalanceLabel = itemView.findViewById(R.id.txtBalanceLabel);
            txtLimitLabel = itemView.findViewById(R.id.txtLimitLabel);
            txtStatementLabel = itemView.findViewById(R.id.txtStatementLabel);
            txtPaymentLabel = itemView.findViewById(R.id.txtPaymentLabel);
        }

        public void bind(CreditCardEntity card, OnCardClickListener listener, NumberFormat currencyFormat,
                         DateTimeFormatter expiryFormatter, DateTimeFormatter dateFormatter) {
            txtBankName.setText(card.getIssuer());
            txtCardLabel.setText(card.getLabel());
            txtCardNumber.setText("•••• •••• •••• " + card.getPanLast4());
            txtBalance.setText(currencyFormat.format(card.getCurrentBalance()));
            txtLimit.setText(currencyFormat.format(card.getCreditLimit()));

            // Configurar el nivel de uso
            CreditCardEntity.UsageLevel usageLevel = card.getUsageLevel();
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
            CreditCard.CardGradient gradient;
            try {
                gradient = CreditCard.CardGradient.valueOf(card.getGradient());
            } catch (IllegalArgumentException e) {
                gradient = CreditCard.CardGradient.VIOLET; // default
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

            // Actualizar el color del texto según el gradiente seleccionado
            int textColor = (gradient == CreditCard.CardGradient.SILVER ||
                             gradient == CreditCard.CardGradient.GOLD)
                    ? itemView.getContext().getResources().getColor(android.R.color.black)
                    : itemView.getContext().getResources().getColor(android.R.color.white);

            txtBankName.setTextColor(textColor);
            txtCardLabel.setTextColor(textColor);
            txtCardNumber.setTextColor(textColor);
            txtBalance.setTextColor(textColor);
            txtLimit.setTextColor(textColor);
            txtUsageLabel.setTextColor(textColor);
            txtUsagePercentage.setTextColor(textColor);
            txtStatementDate.setTextColor(textColor);
            txtBalanceLabel.setTextColor(textColor);
            txtLimitLabel.setTextColor(textColor);
            txtStatementLabel.setTextColor(textColor);
            txtPaymentLabel.setTextColor(textColor);

            // Aplicar color al ícono de la tarjeta
            imgCardIcon.setColorFilter(textColor);

            // Configurar el logo de la marca
            int logoRes = "visa".equalsIgnoreCase(card.getBrand())
                    ? R.drawable.ic_visa_logo
                    : R.drawable.ic_mastercard_logo;
            imgBrandLogo.setImageResource(logoRes);

            // Mostrar fecha de corte próxima
            LocalDate nextStatement = card.getNextStatementDate();
            if (nextStatement != null) {
                txtStatementDate.setText(nextStatement.format(dateFormatter));
            } else {
                txtStatementDate.setText("--");
            }

            // Mostrar fecha de pago próxima
            LocalDate nextPayment = card.getNextPaymentDueDate();
            if (nextPayment != null) {
                txtPaymentDate.setText(nextPayment.format(dateFormatter));

                // Si ya pasó la fecha de corte, mostrar fecha de pago en rojo
                if (card.isInPaymentPeriod()) {
                    txtPaymentDate.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.holo_red_light));
                } else {
                    txtPaymentDate.setTextColor(textColor);
                }
            } else {
                txtPaymentDate.setText("--");
                txtPaymentDate.setTextColor(textColor);
            }

            // Click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCardClick(card);
                }
            });
        }
    }
}
