package com.pascm.fintrack.ui.lugar;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.pascm.fintrack.R;
import com.pascm.fintrack.databinding.ActivityMapPickerBinding;

import java.util.Locale;

public class MapPickerActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String EXTRA_LATITUDE = "extra_latitude";
    public static final String EXTRA_LONGITUDE = "extra_longitude";

    private ActivityMapPickerBinding binding;
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;

    private LatLng selectedLocation;
    private static final float DEFAULT_ZOOM = 15f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapPickerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Setup map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Setup listeners
        setupListeners();
    }

    private void setupListeners() {
        // Back button
        binding.btnBack.setOnClickListener(v -> finish());

        // Confirm button
        binding.btnConfirm.setOnClickListener(v -> confirmLocation());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;

        // Configure map
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);

        // Enable my location if permission granted
        enableMyLocation();

        // Setup camera move listener
        googleMap.setOnCameraIdleListener(() -> {
            // Get center of screen as selected location
            LatLng center = googleMap.getCameraPosition().target;
            selectedLocation = center;
            updateCoordinatesDisplay(center);
        });

        // Try to get current location and move camera
        moveToCurrentLocation();
    }

    @SuppressLint("MissingPermission")
    private void enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        }
    }

    @SuppressLint("MissingPermission")
    private void moveToCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Move to default location (Mexico City)
            LatLng defaultLocation = new LatLng(19.4326, -99.1332);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        LatLng currentLocation = new LatLng(
                                location.getLatitude(),
                                location.getLongitude()
                        );
                        googleMap.animateCamera(
                                CameraUpdateFactory.newLatLngZoom(currentLocation, DEFAULT_ZOOM)
                        );
                    } else {
                        // Fallback to default location
                        LatLng defaultLocation = new LatLng(19.4326, -99.1332);
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                    }
                })
                .addOnFailureListener(e -> {
                    // Fallback to default location
                    LatLng defaultLocation = new LatLng(19.4326, -99.1332);
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                });
    }

    private void updateCoordinatesDisplay(LatLng location) {
        String coordinates = String.format(Locale.US, "%.6f, %.6f",
                location.latitude, location.longitude);
        binding.tvCoordinates.setText(coordinates);
    }

    private void confirmLocation() {
        if (selectedLocation == null) {
            Toast.makeText(this, "Selecciona una ubicación en el mapa", Toast.LENGTH_SHORT).show();
            return;
        }

        // Return selected location
        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_LATITUDE, selectedLocation.latitude);
        resultIntent.putExtra(EXTRA_LONGITUDE, selectedLocation.longitude);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
