package com.track_assist;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.EncodedPolyline;
import com.track_assist.databinding.ActivityJourneyBinding;

import java.util.List;

public class Journey extends AppCompatActivity implements OnMapReadyCallback {

    private EditText sourceEditText;
    private EditText destinationEditText;
    private Button saveJourneyButton;
    private GoogleMap mMap;

    private DatabaseReference databaseReference;
    private GeoApiContext geoApiContext;
    private ActivityJourneyBinding binding;
    private String regnum;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityJourneyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sourceEditText = binding.sourceEditText;
        destinationEditText = binding.destinationEditText;
        saveJourneyButton = binding.saveJourneyButton;

        regnum = getIntent().getStringExtra("PatNum");
        Log.d(regnum,"RegNum");

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        requestLocationPermission();
        // Initialize databaseReference regardless of regnum
        databaseReference = FirebaseDatabase.getInstance().getReference("Patient").child("Journey");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        geoApiContext = new GeoApiContext.Builder()
                .apiKey("AIzaSyBPIUMX05Di24qGOo9jBjcXO7sZqTL52qA")
                .build();

        saveJourneyButton.setOnClickListener(v -> saveJourney());
    }
    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getDeviceLocation();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getDeviceLocation();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void getDeviceLocation() {
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (location != null) {
                                    LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                                    mMap.addMarker(new MarkerOptions().position(currentLatLng).title("Current Location"));
                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f));
                                }
                            }
                        });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        LatLng myloc = new LatLng(12.9166, 77.6101);
        mMap.addMarker(new MarkerOptions().position(myloc).title("Marker in BTM"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(myloc));
    }

    private void saveJourney() {
        String source = sourceEditText.getText().toString().trim();
        String destination = destinationEditText.getText().toString().trim();

        if (TextUtils.isEmpty(source) || TextUtils.isEmpty(destination)) {
            Toast.makeText(this, "Please enter both source and destination", Toast.LENGTH_SHORT).show();
            return;
        }

        // Ensure regnum is not null before saving the journey
        if (regnum != null) {
            String journeyId = databaseReference.push().getKey();
            JourneyData journeyData = new JourneyData(source, destination);

            if (journeyId != null) {
                databaseReference.child(regnum).child(journeyId).setValue(journeyData)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(Journey.this, "Journey saved successfully", Toast.LENGTH_SHORT).show();
                                showRouteOnMap(source, destination);
                            } else {
                                Toast.makeText(Journey.this, "Failed to save journey", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(this, "Failed to generate journey ID", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Patient number is null", Toast.LENGTH_SHORT).show();
        }
    }

    private void showRouteOnMap(String source, String destination) {
        new Thread(() -> {
            try {
                DirectionsResult result = DirectionsApi.getDirections(geoApiContext, source, destination).await();

                runOnUiThread(() -> {
                    if (result.routes != null && result.routes.length > 0) {
                        DirectionsRoute route = result.routes[0];
                        List<com.google.maps.model.LatLng> decodedPath = new EncodedPolyline(route.overviewPolyline.getEncodedPath()).decodePath();

                        PolylineOptions polylineOptions = new PolylineOptions();
                        polylineOptions.color(ContextCompat.getColor(this, R.color._light_green)); // Set your desired color here
                        polylineOptions.width(10); // Set your desired width here
                        for (com.google.maps.model.LatLng latLng : decodedPath) {
                            polylineOptions.add(new LatLng(latLng.lat, latLng.lng));
                        }

                        mMap.addPolyline(polylineOptions);
                        LatLng startLocation = new LatLng(route.legs[0].startLocation.lat, route.legs[0].startLocation.lng);
                        LatLng endLocation = new LatLng(route.legs[0].endLocation.lat, route.legs[0].endLocation.lng);

                        mMap.addMarker(new MarkerOptions().position(startLocation).title("Source"));
                        mMap.addMarker(new MarkerOptions().position(endLocation).title("Destination"));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startLocation, 10));
                    } else {
                        Toast.makeText(Journey.this, "No route found", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(Journey.this, "Failed to get directions", Toast.LENGTH_SHORT).show());
                e.printStackTrace();
            }
        }).start();
    }
    }

     class JourneyData {
        public String source;
        public String destination;

        public JourneyData() {
        }

        public JourneyData(String source, String destination) {
            this.source = source;
            this.destination = destination;
        }
}
