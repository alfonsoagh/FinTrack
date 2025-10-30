package com.pascm.fintrack.ui.lugar;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.pascm.fintrack.R;
import com.pascm.fintrack.databinding.ActivityMapPickerBinding;

import java.util.Arrays;
import java.util.Locale;

public class MapPickerActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String EXTRA_LATITUDE = "extra_latitude";
    public static final String EXTRA_LONGITUDE = "extra_longitude";
    public static final String EXTRA_PLACE_NAME = "extra_place_name";

    private ActivityMapPickerBinding binding;
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private ActivityResultLauncher<Intent> placesAutocompleteLauncher;

    private LatLng selectedLocation;
    private String selectedPlaceName = null;
    private static final float DEFAULT_ZOOM = 15f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapPickerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Places API
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        }

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize Places Autocomplete launcher
        placesAutocompleteLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Place place = Autocomplete.getPlaceFromIntent(result.getData());
                        LatLng latLng = place.getLatLng();
                        if (latLng != null && googleMap != null) {
                            // Guardar el nombre del lugar
                            selectedPlaceName = place.getName();
                            selectedLocation = latLng;
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
                        }
                    } else if (result.getResultCode() == AutocompleteActivity.RESULT_ERROR && result.getData() != null) {
                        Status status = Autocomplete.getStatusFromIntent(result.getData());
                        Toast.makeText(this, "Error: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );

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

        // Search button - Open Places Autocomplete
        binding.btnSearch.setOnClickListener(v -> openPlacesSearch());

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
        if (selectedPlaceName != null) {
            resultIntent.putExtra(EXTRA_PLACE_NAME, selectedPlaceName);
        }
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void openPlacesSearch() {
        try {
            // Define place fields to return
            java.util.List<Place.Field> fields = Arrays.asList(
                    Place.Field.ID,
                    Place.Field.NAME,
                    Place.Field.ADDRESS,
                    Place.Field.LAT_LNG
            );

            // Create autocomplete intent
            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                    .build(this);

            placesAutocompleteLauncher.launch(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Error al abrir búsqueda: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
