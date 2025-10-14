package com.pascm.fintrack.ui.cards;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pascm.fintrack.R;
import com.pascm.fintrack.model.CreditCard;

import java.util.List;

public class GradientSelectorAdapter extends RecyclerView.Adapter<GradientSelectorAdapter.GradientViewHolder> {

    private final List<CreditCard.CardGradient> gradients;
    private final OnGradientSelectedListener listener;
    private int selectedPosition = 0;

    public interface OnGradientSelectedListener {
        void onGradientSelected(CreditCard.CardGradient gradient);
    }

    public GradientSelectorAdapter(List<CreditCard.CardGradient> gradients, OnGradientSelectedListener listener) {
        this.gradients = gradients;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GradientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_gradient_swatch, parent, false);
        return new GradientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GradientViewHolder holder, int position) {
        CreditCard.CardGradient gradient = gradients.get(position);
        boolean isSelected = position == selectedPosition;
        holder.bind(gradient, isSelected, () -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition == RecyclerView.NO_POSITION) return;

            int previousSelected = selectedPosition;
            selectedPosition = currentPosition;
            notifyItemChanged(previousSelected);
            notifyItemChanged(selectedPosition);
            if (listener != null) {
                listener.onGradientSelected(gradients.get(currentPosition));
            }
        });
    }

    @Override
    public int getItemCount() {
        return gradients.size();
    }

    static class GradientViewHolder extends RecyclerView.ViewHolder {
        private final View swatchView;
        private final View selectionIndicator;

        public GradientViewHolder(@NonNull View itemView) {
            super(itemView);
            swatchView = itemView.findViewById(R.id.swatchView);
            selectionIndicator = itemView.findViewById(R.id.selectionIndicator);
        }

        public void bind(CreditCard.CardGradient gradient, boolean isSelected, Runnable onClick) {
            // Apply gradient
            GradientDrawable gradientDrawable = new GradientDrawable(
                    GradientDrawable.Orientation.TL_BR,
                    new int[]{
                            gradient.getStartColor(),
                            gradient.getCenterColor(),
                            gradient.getEndColor()
                    }
            );
            gradientDrawable.setCornerRadius(12 * itemView.getContext().getResources().getDisplayMetrics().density);
            swatchView.setBackground(gradientDrawable);

            // Show/hide selection indicator
            selectionIndicator.setVisibility(isSelected ? View.VISIBLE : View.GONE);

            // Click listener
            itemView.setOnClickListener(v -> onClick.run());
        }
    }
}
