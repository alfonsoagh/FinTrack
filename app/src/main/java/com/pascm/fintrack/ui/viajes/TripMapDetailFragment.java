package com.pascm.fintrack.ui.viajes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.pascm.fintrack.R;
import com.pascm.fintrack.data.local.entity.Transaction;
import com.pascm.fintrack.data.local.entity.Trip;
import com.pascm.fintrack.data.repository.TransactionRepository;
import com.pascm.fintrack.data.repository.TripRepository;
import com.pascm.fintrack.databinding.FragmentTripMapDetailBinding;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TripMapDetailFragment extends Fragment implements OnMapReadyCallback {

    private FragmentTripMapDetailBinding binding;
    private TripRepository tripRepository;
    private TransactionRepository transactionRepository;
    private GoogleMap mMap;
    private long tripId;
    private Trip currentTrip;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTripMapDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tripRepository = new TripRepository(requireContext());
        transactionRepository = new TransactionRepository(requireContext());

        if (getArguments() != null) {
            tripId = getArguments().getLong("tripId", -1);
            if (tripId != -1) {
                setupUI();
                setupMap();
                loadTripData();
            } else {
                Toast.makeText(requireContext(), "Viaje no encontrado", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(view).navigateUp();
            }
        }
    }

    private void setupUI() {
        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
    }

    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void loadTripData() {
        tripRepository.getTripById(tripId).observe(getViewLifecycleOwner(), trip -> {
            if (trip != null) {
                currentTrip = trip;
                loadExpensesOnMap();
            }
        });
    }

    private void loadExpensesOnMap() {
        if (mMap == null || currentTrip == null) return;

        long userId = com.pascm.fintrack.util.SessionManager.getUserId(requireContext());
        transactionRepository.getTransactionsByTrip(userId, tripId).observe(getViewLifecycleOwner(), transactions -> {
            if (transactions != null) {
                mMap.clear();
                List<LatLng> allLocations = new ArrayList<>();

                // Agregar marcador de inicio
                if (currentTrip.getOriginLatitude() != null && currentTrip.getOriginLongitude() != null) {
                    LatLng origin = new LatLng(currentTrip.getOriginLatitude(), currentTrip.getOriginLongitude());
                    mMap.addMarker(new MarkerOptions()
                            .position(origin)
                            .title("Inicio: " + (currentTrip.getOrigin() != null ? currentTrip.getOrigin() : "Origen"))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                    allLocations.add(origin);
                }

                // Agregar marcador de destino
                if (currentTrip.getDestinationLatitude() != null && currentTrip.getDestinationLongitude() != null) {
                    LatLng destination = new LatLng(currentTrip.getDestinationLatitude(), currentTrip.getDestinationLongitude());
                    mMap.addMarker(new MarkerOptions()
                            .position(destination)
                            .title("Destino: " + currentTrip.getDestination())
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                    allLocations.add(destination);
                }

                // Agregar marcadores de gastos
                for (Transaction transaction : transactions) {
                    if (transaction.getLatitude() != null && transaction.getLongitude() != null) {
                        LatLng location = new LatLng(transaction.getLatitude(), transaction.getLongitude());

                        String title = getTransactionTypeString(transaction.getType());
                        String snippet = currencyFormat.format(transaction.getAmount());

                        mMap.addMarker(new MarkerOptions()
                                .position(location)
                                .title(title)
                                .snippet(snippet)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

                        allLocations.add(location);
                    }
                }

                // Dibujar línea de ruta si hay inicio y destino
                if (currentTrip.getOriginLatitude() != null && currentTrip.getOriginLongitude() != null &&
                    currentTrip.getDestinationLatitude() != null && currentTrip.getDestinationLongitude() != null) {

                    LatLng origin = new LatLng(currentTrip.getOriginLatitude(), currentTrip.getOriginLongitude());
                    LatLng destination = new LatLng(currentTrip.getDestinationLatitude(), currentTrip.getDestinationLongitude());

                    mMap.addPolyline(new PolylineOptions()
                            .add(origin, destination)
                            .width(5)
                            .color(getResources().getColor(R.color.primary, null))
                            .geodesic(true));
                }

                // Ajustar la cámara para mostrar todos los marcadores
                if (!allLocations.isEmpty()) {
                    LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
                    for (LatLng location : allLocations) {
                        boundsBuilder.include(location);
                    }
                    LatLngBounds bounds = boundsBuilder.build();
                    int padding = 100; // padding en píxeles
                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
                }
            }
        });
    }

    private String getTransactionTypeString(Transaction.TransactionType type) {
        switch (type) {
            case INCOME: return "Ingreso";
            case EXPENSE: return "Gasto";
            case TRANSFER: return "Transferencia";
            default: return "Transacción";
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        loadExpensesOnMap();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
