package com.pascm.fintrack.ui.viaje;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.pascm.fintrack.R;
import com.pascm.fintrack.data.local.entity.Transaction;
import com.pascm.fintrack.data.local.entity.Trip;
import com.pascm.fintrack.data.repository.TransactionRepository;
import com.pascm.fintrack.data.repository.TripRepository;
import com.pascm.fintrack.util.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class TripMapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap map;
    private TripRepository tripRepository;
    private TransactionRepository transactionRepository;
    private Trip currentTrip;
    private List<Transaction> tripTransactions = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_trip_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize repositories
        tripRepository = new TripRepository(requireContext());
        transactionRepository = new TransactionRepository(requireContext());

        // Back button
        view.findViewById(R.id.btn_back).setOnClickListener(v ->
                Navigation.findNavController(v).navigateUp()
        );

        // Initialize map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Load trip data
        loadTripData();
    }

    private void loadTripData() {
        long userId = SessionManager.getUserId(requireContext());
        tripRepository.getActiveTrip(userId).observe(getViewLifecycleOwner(), trip -> {
            if (trip != null) {
                currentTrip = trip;
                // Load transactions for this trip
                transactionRepository.getTransactionsByTrip(userId, trip.getTripId())
                        .observe(getViewLifecycleOwner(), transactions -> {
                            if (transactions != null) {
                                tripTransactions = transactions;
                                // Update map if ready
                                if (map != null) {
                                    setupMapMarkers();
                                }
                            }
                        });
            } else {
                Toast.makeText(requireContext(), "No hay viaje activo", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(false);

        // Setup markers if data is loaded
        if (currentTrip != null) {
            setupMapMarkers();
        }
    }

    private void setupMapMarkers() {
        if (map == null || currentTrip == null) return;

        map.clear();
        List<LatLng> allPoints = new ArrayList<>();

        // 1. Agregar marcador verde para el punto de inicio
        if (currentTrip.getOriginLatitude() != null && currentTrip.getOriginLongitude() != null) {
            LatLng originLatLng = new LatLng(
                    currentTrip.getOriginLatitude(),
                    currentTrip.getOriginLongitude()
            );

            map.addMarker(new MarkerOptions()
                    .position(originLatLng)
                    .title("Inicio")
                    .snippet(currentTrip.getOrigin() != null ? currentTrip.getOrigin() : "Punto de inicio")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            );

            allPoints.add(originLatLng);
        }

        // 2. Agregar marcador rojo (bandera) para el punto final
        if (currentTrip.getDestinationLatitude() != null && currentTrip.getDestinationLongitude() != null) {
            LatLng destinationLatLng = new LatLng(
                    currentTrip.getDestinationLatitude(),
                    currentTrip.getDestinationLongitude()
            );

            map.addMarker(new MarkerOptions()
                    .position(destinationLatLng)
                    .title("Meta")
                    .snippet(currentTrip.getDestination() != null ? currentTrip.getDestination() : "Punto final")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            );

            allPoints.add(destinationLatLng);
        }

        // 3. Agregar marcadores azules para cada gasto con ubicación
        for (Transaction transaction : tripTransactions) {
            if (transaction.getLatitude() != null && transaction.getLongitude() != null) {
                LatLng transactionLatLng = new LatLng(
                        transaction.getLatitude(),
                        transaction.getLongitude()
                );

                String title = transaction.getNotes() != null && !transaction.getNotes().isEmpty()
                        ? transaction.getNotes()
                        : "Gasto";

                String snippet = String.format("$%.2f %s",
                        transaction.getAmount(),
                        transaction.getCurrencyCode());

                map.addMarker(new MarkerOptions()
                        .position(transactionLatLng)
                        .title(title)
                        .snippet(snippet)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                );

                allPoints.add(transactionLatLng);
            }
        }

        // Ajustar la cámara para mostrar todos los marcadores
        if (!allPoints.isEmpty()) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (LatLng point : allPoints) {
                builder.include(point);
            }
            LatLngBounds bounds = builder.build();

            // Añadir padding (100px) para que los marcadores no estén en el borde
            try {
                map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
            } catch (Exception e) {
                // Si falla, centrar en el primer punto
                if (!allPoints.isEmpty()) {
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(allPoints.get(0), 10f));
                }
            }
        } else {
            // Si no hay puntos, centrar en México
            LatLng mexico = new LatLng(19.4326, -99.1332);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(mexico, 5f));
            Toast.makeText(requireContext(),
                    "No hay ubicaciones registradas para este viaje",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Convierte un Drawable a BitmapDescriptor (útil para iconos personalizados)
     */
    private BitmapDescriptor bitmapDescriptorFromVector(int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(requireContext(), vectorResId);
        if (vectorDrawable == null) {
            return BitmapDescriptorFactory.defaultMarker();
        }

        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}
