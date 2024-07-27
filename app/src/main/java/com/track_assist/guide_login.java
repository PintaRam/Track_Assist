package com.track_assist;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class guide_login extends AppCompatActivity {

    private EditText editTextGuideId;
    private Button buttonOk;
    private Spinner spinnerPatientIds;
    private Button buttonViewPatientDetails;
    private FirebaseFirestore db;

    private static final String TAG = "GuideLoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide_login);

        editTextGuideId = findViewById(R.id.editTextGuideId);
        buttonOk = findViewById(R.id.buttonOk);
        spinnerPatientIds = findViewById(R.id.spinnerPatientIds);
        buttonViewPatientDetails = findViewById(R.id.buttonViewPatientDetails);

        db = FirebaseFirestore.getInstance();

        buttonOk.setOnClickListener(new View.OnClickListener() {
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

        buttonViewPatientDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedPatientId = spinnerPatientIds.getSelectedItem().toString();
                if (!TextUtils.isEmpty(selectedPatientId)) {
                    Intent intent = new Intent(guide_login.this, guideDashboard.class);
                    intent.putExtra("patientId", selectedPatientId);
                    startActivity(intent);
                } else {
                    Toast.makeText(guide_login.this, "Please select a Patient ID", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void fetchPatientIds(String guideId) {
        Log.d(TAG, "Fetching patient IDs for guide: " + guideId);
        db.collection("patients")
                .whereEqualTo("guideId", guideId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Query successful, processing results");
                            List<String> patientIds = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String patientId = document.getString("patientId");
                                if (!patientIds.contains(patientId)) {
                                    patientIds.add(patientId);
                                }
                            }
                            if (!patientIds.isEmpty()) {
                                Log.d(TAG, "Patient IDs found: " + patientIds.toString());
                                ArrayAdapter<String> adapter = new ArrayAdapter<>(guide_login.this, android.R.layout.simple_spinner_item, patientIds);
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                spinnerPatientIds.setAdapter(adapter);
                                spinnerPatientIds.setVisibility(View.VISIBLE);
                                buttonViewPatientDetails.setVisibility(View.VISIBLE);
                            } else {
                                Log.d(TAG, "No patients found for this guide");
                                Toast.makeText(guide_login.this, "No patients found for this guide", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.d(TAG, "Query failed", task.getException());
                            Toast.makeText(guide_login.this, "Failed to fetch patient IDs", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
