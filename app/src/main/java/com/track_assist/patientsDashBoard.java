package com.track_assist;

import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
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

import java.util.HashMap;
import java.util.Map;

//public class patientsDashBoard extends AppCompatActivity {
public class patientsDashBoard   extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final int PICK_IMAGE_REQUEST = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int REQUEST_CHECK_SETTINGS = 2;

    private ImageView patientPhoto;
    private TextView patientName;
    private TextView patientAge;
    private ImageView imageView;
    private Button buttonDial;
    Uri imageUri;

    private String regist;
    private TextView  pname,gname,pid,gid,city;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_dashdoard);

        // Initialize views
        patientPhoto = findViewById(R.id.patientPhoto);
        patientName = findViewById(R.id.patientName);
        imageView=findViewById(R.id.patientPhoto);
        pname=findViewById(R.id.pname);
        gname=findViewById(R.id.gname);
        pid=findViewById(R.id.patid);
        gid=findViewById(R.id.guid);
        city=findViewById(R.id.p_city);
        buttonDial = findViewById(R.id.button3);

        // Retrieve patientReg from Intent
        regist = getIntent().getStringExtra("patientReg");
        Log.d("patientsDashBoard", "Retrieved patientReg: " + regist);
        imageView.setOnClickListener(v -> openImageChooser());
        // Set patient details
        // For example, these values can be fetched from a database or passed via Intent
        buttonDial.setOnClickListener(v -> {
            openDialer("1234567890");  // Replace with the phone number you want to pre-fill
        });


        // Initialize the map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if(pid.getText().toString().trim()!=null)
          loadPatientDetails(pid.getText().toString().trim());
        else
            Log.d("hi","Else Executed");
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

        databaseReference.orderByChild("patientReg").equalTo(regist).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(String.valueOf(dataSnapshot),"data");
                if (dataSnapshot.exists()) {
                    String pName = dataSnapshot.child("patName").getValue(String.class);
                    String gName = dataSnapshot.child("guideName").getValue(String.class);
                    String place = dataSnapshot.child("patAddress").getValue(String.class);
                    String guideId = dataSnapshot.child("guideId").getValue(String.class);
                    String patientId = dataSnapshot.child("patientReg").getValue(String.class);

                    pname.setText(pName != null ? "Name :"+pName : "N/A");
                    gname.setText(gName != null ?"Guide :"+gName : "N/A");
                    city.setText(place != null ? "City :"+place : "N/A");
                    gid.setText(guideId != null ? "Guide Id :"+ guideId : "N/A");
                    pid.setText(patientId != null ? "Patient Id :"+patientId : "N/A");
                    Toast.makeText(patientsDashBoard.this, "Patient Data Saved Successfully!!!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(patientsDashBoard.this, "Patient data not found", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(patientsDashBoard.this, "Failed to load patient data", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void checkLocationSettings() {
        LocationRequest locationRequest = LocationRequest.create()
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
                    getDeviceLocation();
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
                Task<Location> locationResult = fusedLocationClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            Location loc = task.getResult();
                            if (loc != null) {
                                LatLng currentLocation = new LatLng(loc.getLatitude(), loc.getLongitude());

                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                                mMap.addMarker(new MarkerOptions()
                                        .position(currentLocation)
                                        .title("You are here"+loc.getLongitude()+" "+loc.getLatitude())
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.man_marker_mini))); // Ensure you have a custom_marker.png in res/drawable

                            }
                        } else {
                            Toast.makeText(patientsDashBoard.this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            patientPhoto.setImageURI(imageUri);
            uploadImageToFirebase();
        }
    }

    private void uploadImageToFirebase() {
        if (imageUri != null) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference("patient_images/" +regist+ ".jpg");
            storageReference.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String imageUrl = uri.toString();
                                    saveImageInfoToDatabase(imageUrl);
                                    Toast.makeText(patientsDashBoard.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(patientsDashBoard.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void saveImageInfoToDatabase(String imageUrl) {
        String patientId = pid.getText().toString().trim();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Patient").child("info").child(patientId);
        Map<String, Object> updates = new HashMap<>();
        updates.put("imageUrl", imageUrl);
        databaseReference.updateChildren(updates);
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                    getDeviceLocation();
                }
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void openDialer(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        startActivity(intent);
    }
}
