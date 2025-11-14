package com.pascm.fintrack.ui.movimiento;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pascm.fintrack.R;
import com.pascm.fintrack.data.local.entity.Merchant;
import com.pascm.fintrack.data.repository.PlaceRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Dialog para seleccionar un lugar frecuente de la lista guardada
 */
public class SelectFrequentPlaceDialogFragment extends DialogFragment {

    public interface PlaceSelectionListener {
        void onPlaceSelected(Merchant place);
    }

    private PlaceSelectionListener listener;
    private PlaceRepository placeRepository;
    private RecyclerView recyclerView;
    private FrequentPlaceAdapter adapter;
    private List<Merchant> places = new ArrayList<>();

    public static SelectFrequentPlaceDialogFragment newInstance(PlaceSelectionListener listener) {
        SelectFrequentPlaceDialogFragment fragment = new SelectFrequentPlaceDialogFragment();
        fragment.listener = listener;
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        placeRepository = new PlaceRepository(requireContext());

        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_select_place, null);
        recyclerView = view.findViewById(R.id.recyclerPlaces);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new FrequentPlaceAdapter(places, place -> {
            if (listener != null) {
                listener.onPlaceSelected(place);
            }
            dismiss();
        });
        recyclerView.setAdapter(adapter);

        loadFrequentPlaces();

        return new AlertDialog.Builder(requireContext())
                .setTitle("Seleccionar lugar frecuente")
                .setView(view)
                .setNegativeButton("Cancelar", null)
                .create();
    }

    private void loadFrequentPlaces() {
        placeRepository.getAllPlaces().observe(this, merchantList -> {
            if (merchantList != null && !merchantList.isEmpty()) {
                places.clear();
                places.addAll(merchantList);
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(requireContext(), "No hay lugares guardados", Toast.LENGTH_SHORT).show();
                dismiss();
            }
        });
    }

    /**
     * Adapter para mostrar lugares frecuentes
     */
    private static class FrequentPlaceAdapter extends RecyclerView.Adapter<FrequentPlaceAdapter.ViewHolder> {

        private final List<Merchant> places;
        private final OnPlaceClickListener clickListener;

        interface OnPlaceClickListener {
            void onClick(Merchant place);
        }

        FrequentPlaceAdapter(List<Merchant> places, OnPlaceClickListener clickListener) {
            this.places = places;
            this.clickListener = clickListener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_place_selection, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Merchant place = places.get(position);
            holder.bind(place, clickListener);
        }

        @Override
        public int getItemCount() {
            return places.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            private final android.widget.TextView tvPlaceName;
            private final android.widget.TextView tvPlaceAddress;

            ViewHolder(View itemView) {
                super(itemView);
                tvPlaceName = itemView.findViewById(R.id.tvPlaceName);
                tvPlaceAddress = itemView.findViewById(R.id.tvPlaceAddress);
            }

            void bind(Merchant place, OnPlaceClickListener listener) {
                tvPlaceName.setText(place.getName());

                String subtitle = "";
                if (place.getAddress() != null && !place.getAddress().isEmpty()) {
                    subtitle = place.getAddress();
                } else if (place.hasLocation()) {
                    subtitle = String.format(java.util.Locale.US, "%.6f, %.6f", place.getLatitude(), place.getLongitude());
                }
                tvPlaceAddress.setText(subtitle);

                itemView.setOnClickListener(v -> listener.onClick(place));
            }
        }
    }
}
