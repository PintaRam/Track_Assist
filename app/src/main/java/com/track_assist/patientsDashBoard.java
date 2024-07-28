package com.track_assist;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class patientsDashBoard extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final int PICK_IMAGE_REQUEST = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int REQUEST_CHECK_SETTINGS = 2;

    private ImageView patientPhoto;
    private TextView patientName;
    private TextView patientAge;
    private ImageView imageView;
    Uri imageUri;

    private String regist;
    private TextView pname, gname, pid, gid, city;

    private LocationCallback locationCallback;
    private LocationRequest locationRequest;

    private Marker currentLocationMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_dashdoard);

        // Initialize views
        patientPhoto = findViewById(R.id.patientPhoto);
        patientName = findViewById(R.id.patientName);
        imageView = findViewById(R.id.patientPhoto);
        pname = findViewById(R.id.pname);
        gname = findViewById(R.id.gname);
        pid = findViewById(R.id.patid);
        gid = findViewById(R.id.guid);
        city = findViewById(R.id.p_city);

        // Retrieve patientReg from Intent
        regist = getIntent().getStringExtra("patientReg");
        Log.d("patientsDashBoard", "Retrieved patientReg: " + regist);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageChooser();
            }
        });

        // Initialize the map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Load patient details
        if (regist != null && !regist.isEmpty()) {
            loadPatientDetails(regist);
        } else {
            Log.d("patientsDashBoard", "No patient registration number provided");
        }

        // Setup location updates
        setupLocationUpdates();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Check location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            mMap.setMyLocationEnabled(true);
            getDeviceLocation();
            checkLocationSettings();
        }
    }

    private void loadPatientDetails(String patientRegNumber) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Patient").child("info");

        databaseReference.orderByChild("patientReg").equalTo(patientRegNumber).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("patientsDashBoard", "DataSnapshot: " + dataSnapshot);
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String pName = snapshot.child("patName").getValue(String.class);
                        String gName = snapshot.child("guideName").getValue(String.class);
                        String place = snapshot.child("patAddress").getValue(String.class);
                        String guideId = snapshot.child("guideId").getValue(String.class);
                        String patientId = snapshot.child("patientReg").getValue(String.class);

                        Log.d("patientsDashBoard", "Patient Name: " + pName);
                        Log.d("patientsDashBoard", "Guide Name: " + gName);
                        Log.d("patientsDashBoard", "Place: " + place);
                        Log.d("patientsDashBoard", "Guide ID: " + guideId);
                        Log.d("patientsDashBoard", "Patient ID: " + patientId);

                        pname.setText(pName != null ? "Name: " + pName : "N/A");
                        gname.setText(gName != null ? "Guide: " + gName : "N/A");
                        city.setText(place != null ? "City: " + place : "N/A");
                        gid.setText(guideId != null ? "Guide Id: " + guideId : "N/A");
                        pid.setText(patientId != null ? "Patient Id: " + patientId : "N/A");
                    }
                } else {
                    Log.d("patientsDashBoard", "Patient data not found");
                    Toast.makeText(patientsDashBoard.this, "Patient data not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("patientsDashBoard", "DatabaseError: " + databaseError.getMessage());
                Toast.makeText(patientsDashBoard.this, "Failed to load patient data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkLocationSettings() {
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10000)
                .setFastestInterval(5000);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize location requests here.
                if (ContextCompat.checkSelfPermission(patientsDashBoard.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                    startLocationUpdates();
                }
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(), and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(patientsDashBoard.this, REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });
    }

    private void getDeviceLocation() {
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            Location loc = task.getResult();
                            if (loc != null) {
                                LatLng currentLatLng = new LatLng(loc.getLatitude(), loc.getLongitude());

                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f));
                                updateMarker(currentLatLng, "Current Location");
                                getAddressFromLocation(loc);

                            }
                        } else {
                            Log.d("patientsDashBoard", "Current location is null. Using defaults.");
                            LatLng defaultLocation = new LatLng(-34, 151);
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 15f));
                            mMap.addMarker(new MarkerOptions().position(defaultLocation).title("Default Location"));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void startLocationUpdates() {
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void setupLocationUpdates() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    LatLng newLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    updateMarker(newLatLng, "Updated Location");
                    getAddressFromLocation(location);
                }
            }
        };
    }

    private void updateMarker(LatLng position, String title) {
        if (currentLocationMarker != null) {
            currentLocationMarker.remove();
        }
        currentLocationMarker = mMap.addMarker(new MarkerOptions()
                .position(position)
                .title(title)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
    }

    private void getAddressFromLocation(Location location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String addressString = address.getAddressLine(0);
                saveLocationToFirebase(location, addressString);
            } else {
                Log.d("patientsDashBoard", "Address not found for the location");
                saveLocationToFirebase(location, "Address not found");
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("patientsDashBoard", "Geocoder IOException: " + e.getMessage());
            saveLocationToFirebase(location, "Geocoder error");
        }
    }

    private void saveLocationToFirebase(Location location, String address) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Patient").child("Guides")
                .child(gid.getText().toString()).child(pid.getText().toString()).child("currentLocation");

        Map<String, Object> updates = new HashMap<>();
        updates.put("latitude", location.getLatitude());
        updates.put("longitude", location.getLongitude());
        updates.put("address", address); // Store the address here
        updates.put("patientId", pid.getText().toString());
        updates.put("patientName", pname.getText().toString());
        databaseReference.updateChildren(updates);
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            patientPhoto.setImageURI(imageUri);
            uploadImageToFirebase(imageUri);
        }
    }

    private void uploadImageToFirebase(Uri imageUri) {
        if (imageUri != null) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference("PatientPhotos/" + System.currentTimeMillis() + "." + getFileExtension(imageUri));
            storageReference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(patientsDashBoard.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(patientsDashBoard.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private String getFileExtension(Uri uri) {
        return getContentResolver().getType(uri).split("/")[1];
    }
}
