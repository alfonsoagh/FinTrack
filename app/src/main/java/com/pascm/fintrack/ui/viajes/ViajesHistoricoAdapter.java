package com.pascm.fintrack.ui.viajes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pascm.fintrack.R;
import com.pascm.fintrack.data.local.entity.Trip;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ViajesHistoricoAdapter extends RecyclerView.Adapter<ViajesHistoricoAdapter.TripViewHolder> {

    private List<Trip> trips = new ArrayList<>();
    private final OnTripClickListener listener;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", new Locale("es", "MX"));
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));

    public interface OnTripClickListener {
        void onTripClick(Trip trip);
    }

    public ViajesHistoricoAdapter(OnTripClickListener listener) {
        this.listener = listener;
    }

    public void setTrips(List<Trip> trips) {
        this.trips = trips;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TripViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_trip_historico, parent, false);
        return new TripViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TripViewHolder holder, int position) {
        Trip trip = trips.get(position);
        holder.bind(trip, dateFormatter, currencyFormat, listener);
    }

    @Override
    public int getItemCount() {
        return trips.size();
    }

    static class TripViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvDestination;
        private final TextView tvDates;
        private final TextView tvTotalExpenses;
        private final TextView tvStatus;

        public TripViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDestination = itemView.findViewById(R.id.tv_trip_destination);
            tvDates = itemView.findViewById(R.id.tv_trip_dates);
            tvTotalExpenses = itemView.findViewById(R.id.tv_trip_total);
            tvStatus = itemView.findViewById(R.id.tv_trip_status);
        }

        public void bind(Trip trip, DateTimeFormatter dateFormatter, NumberFormat currencyFormat, OnTripClickListener listener) {
            tvDestination.setText(trip.getDestination());

            String dates = trip.getStartDate().format(dateFormatter) + " - " + trip.getEndDate().format(dateFormatter);
            tvDates.setText(dates);

            // Show budget amount or zero if not set
            double total = trip.getBudgetAmount() != null ? trip.getBudgetAmount() : 0.0;
            tvTotalExpenses.setText(currencyFormat.format(total));

            if (trip.isActive()) {
                tvStatus.setVisibility(View.VISIBLE);
                tvStatus.setText("Activo");
                tvStatus.setTextColor(itemView.getContext().getColor(R.color.success_green));
            } else {
                tvStatus.setVisibility(View.VISIBLE);
                tvStatus.setText("Finalizado");
                tvStatus.setTextColor(itemView.getContext().getColor(R.color.on_surface_variant));
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTripClick(trip);
                }
            });
        }
    }
}
