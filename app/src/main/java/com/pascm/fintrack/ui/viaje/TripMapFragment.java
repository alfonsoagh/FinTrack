package com.pascm.fintrack.ui.viaje;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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
import java.util.Locale;

public class TripMapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap map;
    private TripRepository tripRepository;
    private TransactionRepository transactionRepository;
    private Trip currentTrip;
    private List<Transaction> tripTransactions = new ArrayList<>();

    // Ubicación del usuario
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng userLatLng;

    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fine = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean coarse = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
                if (Boolean.TRUE.equals(fine) || Boolean.TRUE.equals(coarse)) {
                    enableMyLocationAndFetch();
                } else {
                    Toast.makeText(requireContext(), "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
                }
            });

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

        // Init fused location
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

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
                                    updateCamera();
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
        map.getUiSettings().setMyLocationButtonEnabled(true);

        // Intentar habilitar mi ubicación y obtener última ubicación
        checkAndRequestLocationPermissions();

        // Setup markers if data is loaded
        if (currentTrip != null) {
            setupMapMarkers();
            updateCamera();
        }
    }

    private void checkAndRequestLocationPermissions() {
        boolean fineGranted = androidx.core.content.ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED;
        boolean coarseGranted = androidx.core.content.ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED;
        if (fineGranted || coarseGranted) {
            enableMyLocationAndFetch();
        } else {
            locationPermissionLauncher.launch(new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private void enableMyLocationAndFetch() {
        if (map == null) return;
        try {
            if (androidx.core.content.ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED ||
                    androidx.core.content.ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                map.setMyLocationEnabled(true);

                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(location -> {
                            if (location != null) {
                                userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                                updateCamera();
                            }
                        })
                        .addOnFailureListener(e -> {
                            // ignore; fallback a markers
                        });
            }
        } catch (SecurityException se) {
            // permisos pueden haber cambiado
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

                String snippet = String.format(Locale.getDefault(), "$%.2f %s",
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

        // Guardar puntos para updateCamera (se retoman de las capas actuales)
        // No centramos aquí; updateCamera() lo hará considerando userLatLng
    }

    private void updateCamera() {
        if (map == null) return;

        List<LatLng> points = new ArrayList<>();

        // Recoger puntos visibles desde los datos actuales
        if (currentTrip != null) {
            if (currentTrip.getOriginLatitude() != null && currentTrip.getOriginLongitude() != null) {
                points.add(new LatLng(currentTrip.getOriginLatitude(), currentTrip.getOriginLongitude()));
            }
            if (currentTrip.getDestinationLatitude() != null && currentTrip.getDestinationLongitude() != null) {
                points.add(new LatLng(currentTrip.getDestinationLatitude(), currentTrip.getDestinationLongitude()));
            }
        }
        for (Transaction t : tripTransactions) {
            if (t.getLatitude() != null && t.getLongitude() != null) {
                points.add(new LatLng(t.getLatitude(), t.getLongitude()));
            }
        }
        if (userLatLng != null) {
            points.add(userLatLng);
        }

        if (!points.isEmpty()) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (LatLng p : points) builder.include(p);
            try {
                map.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));
            } catch (Exception e) {
                // fallback a centrar en mi posición si existe
                if (userLatLng != null) {
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 12f));
                } else if (!points.isEmpty()) {
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(points.get(0), 10f));
                }
            }
        } else {
            // Fallback global
            LatLng mexico = new LatLng(19.4326, -99.1332);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(mexico, 5f));
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
