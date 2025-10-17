package com.pascm.fintrack.ui.lugar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pascm.fintrack.R;
import com.pascm.fintrack.data.local.entity.Merchant;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder> {

    private List<Merchant> places;
    private OnPlaceActionListener listener;

    public interface OnPlaceActionListener {
        void onPlaceClick(Merchant place);
        void onEditClick(Merchant place);
        void onDeleteClick(Merchant place);
    }

    public PlaceAdapter() {
        this.places = new ArrayList<>();
    }

    public void setPlaces(List<Merchant> places) {
        this.places = places;
        notifyDataSetChanged();
    }

    public void setOnPlaceActionListener(OnPlaceActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_place, parent, false);
        return new PlaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceViewHolder holder, int position) {
        Merchant place = places.get(position);
        holder.bind(place, listener);
    }

    @Override
    public int getItemCount() {
        return places.size();
    }

    static class PlaceViewHolder extends RecyclerView.ViewHolder {
        private final TextView txtPlaceName;
        private final TextView txtPlaceAddress;
        private final TextView txtPlaceCoordinates;
        private final TextView txtUsageCount;
        private final ImageView imgFrequentBadge;
        private final Button btnEdit;
        private final Button btnDelete;

        public PlaceViewHolder(@NonNull View itemView) {
            super(itemView);
            txtPlaceName = itemView.findViewById(R.id.txt_place_name);
            txtPlaceAddress = itemView.findViewById(R.id.txt_place_address);
            txtPlaceCoordinates = itemView.findViewById(R.id.txt_place_coordinates);
            txtUsageCount = itemView.findViewById(R.id.txt_usage_count);
            imgFrequentBadge = itemView.findViewById(R.id.img_frequent_badge);
            btnEdit = itemView.findViewById(R.id.btn_edit_place);
            btnDelete = itemView.findViewById(R.id.btn_delete_place);
        }

        public void bind(Merchant place, OnPlaceActionListener listener) {
            // Nombre del lugar
            txtPlaceName.setText(place.getName());

            // Dirección (opcional)
            if (place.getAddress() != null && !place.getAddress().isEmpty()) {
                txtPlaceAddress.setVisibility(View.VISIBLE);
                txtPlaceAddress.setText(place.getAddress());
            } else {
                txtPlaceAddress.setVisibility(View.GONE);
            }

            // Coordenadas (opcional)
            if (place.hasLocation()) {
                txtPlaceCoordinates.setVisibility(View.VISIBLE);
                String coordinates = String.format(Locale.US, "%.6f, %.6f",
                        place.getLatitude(), place.getLongitude());
                txtPlaceCoordinates.setText(coordinates);
            } else {
                txtPlaceCoordinates.setVisibility(View.GONE);
            }

            // Contador de uso
            String usageText = place.getUsageCount() == 1 ?
                    "Usado 1 vez" :
                    String.format(Locale.getDefault(), "Usado %d veces", place.getUsageCount());
            txtUsageCount.setText(usageText);

            // Badge de favorito
            imgFrequentBadge.setVisibility(place.isFrequent() ? View.VISIBLE : View.GONE);

            // Click en el item completo
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPlaceClick(place);
                }
            });

            // Botón editar
            btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClick(place);
                }
            });

            // Botón eliminar
            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(place);
                }
            });
        }
    }
}
