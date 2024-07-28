package com.track_assist;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class guide_login extends AppCompatActivity {

    private EditText editTextGuideId;
    private Spinner spinnerPatientIds;
    private FirebaseDatabase fireDb;
    private DatabaseReference dbReference;

    private static final String TAG = "GuideLoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide_login);

        editTextGuideId = findViewById(R.id.editTextGuideId);
        spinnerPatientIds = findViewById(R.id.spinnerPatientIds);

        fireDb = FirebaseDatabase.getInstance();

        findViewById(R.id.buttonOk).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String guideId = editTextGuideId.getText().toString().trim();
                if (!TextUtils.isEmpty(guideId)) {
                    fetchPatientIds(guideId);
                } else {
                    Toast.makeText(guide_login.this, "Please enter Guide ID", Toast.LENGTH_SHORT).show();
                }
            }
        });

        spinnerPatientIds.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            boolean isFirstSelection = true;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isFirstSelection) {
                    isFirstSelection = false;
                } else {
                    String selectedPatientInfo = (String) parent.getItemAtPosition(position);
                    if (!TextUtils.isEmpty(selectedPatientInfo)) {
                        Intent intent = new Intent(guide_login.this, guideDashboard.class);
                        intent.putExtra("patientInfo", selectedPatientInfo);
                        startActivity(intent);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void fetchPatientIds(String guideId) {
        Log.d(TAG, "Fetching patient IDs for guide: " + guideId);
        dbReference = fireDb.getReference("Patient").child("Guides").child(guideId);
        dbReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> patientIds = new ArrayList<>();
                for (DataSnapshot patientSnapshot : snapshot.getChildren()) {
                    String patientId = patientSnapshot.child("patientReg").getValue(String.class);
                    String patientName = patientSnapshot.child("patName").getValue(String.class);
                    if (patientId != null && patientName != null) {
                        patientIds.add(patientId + " - " + patientName);
                    }
                }
                if (!patientIds.isEmpty()) {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(guide_login.this, android.R.layout.simple_spinner_item, patientIds);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerPatientIds.setAdapter(adapter);
                    spinnerPatientIds.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(guide_login.this, "No patients found for this guide", Toast.LENGTH_SHORT).show();
                    spinnerPatientIds.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "Query failed", error.toException());
                Toast.makeText(guide_login.this, "Failed to fetch patient IDs", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
