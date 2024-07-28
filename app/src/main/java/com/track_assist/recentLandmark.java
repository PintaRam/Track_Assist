package com.track_assist;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class recentLandmark extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private PlacesClient placesClient;
    private RecyclerView recyclerView;
    private LandmarkAdapter landmarkAdapter;
    private List<Landmark> landmarkList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_landmark);

        // Initialize Places API
        Places.initialize(getApplicationContext(), "@string/MAPS_API_KEY");
        placesClient = Places.createClient(this);

        // Setup RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        landmarkAdapter = new LandmarkAdapter(landmarkList);
        recyclerView.setAdapter(landmarkAdapter);

        checkLocationPermission();
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getCurrentPlace();
        }
    }

    private void getCurrentPlace() {
        List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);
        FindCurrentPlaceRequest request = FindCurrentPlaceRequest.newInstance(placeFields);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Task<FindCurrentPlaceResponse> placeResponse = placesClient.findCurrentPlace(request);
            placeResponse.addOnCompleteListener(new OnCompleteListener<FindCurrentPlaceResponse>() {
                @Override
                public void onComplete(@NonNull Task<FindCurrentPlaceResponse> task) {
                    if (task.isSuccessful() && task.getResult() != null) {
                        FindCurrentPlaceResponse response = task.getResult();
                        if (!response.getPlaceLikelihoods().isEmpty()) {
                            for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {
                                Place place = placeLikelihood.getPlace();
                                Landmark landmark = new Landmark(
                                        place.getName(),
                                        place.getAddress(),
                                        place.getLatLng().latitude,
                                        place.getLatLng().longitude,
                                        System.currentTimeMillis()
                                );
                                saveLandmarkToDatabase(landmark);
                                landmarkList.add(landmark);
                            }
                            landmarkAdapter.notifyDataSetChanged();
                        }
                    } else {
                        if (task.getException() != null) {
                            Log.e("recentLandmark", "Exception: " + task.getException().getMessage());
                        }
                    }
                }
            });
        } else {
            Log.e("recentLandmark", "Location permission not granted");
        }
    }

    private void saveLandmarkToDatabase(Landmark landmark) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Patient").child("Guides")
                .child("GuideId").child("PatientId").child("RecentVisited");

        String key = databaseReference.push().getKey();
        if (key != null) {
            databaseReference.child(key).setValue(landmark);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentPlace();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
