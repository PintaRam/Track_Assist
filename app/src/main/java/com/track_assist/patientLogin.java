package com.track_assist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class patientLogin extends AppCompatActivity {
    Button loginButton;
    FirebaseDatabase myFire;
    DatabaseReference myDb;
    TextInputLayout username, password;
    String userId, guideName, guideId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_login);

        // Initialize views
        loginButton = findViewById(R.id.buttonLogin);
        username = findViewById(R.id.editTextId);
        password = findViewById(R.id.editTextPassword);

        // Initialize Firebase
        myFire = FirebaseDatabase.getInstance();

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String patientReg = username.getEditText().getText().toString().trim();
                String logPassword = password.getEditText().getText().toString().trim();

                if (patientReg.isEmpty() || logPassword.isEmpty()) {
                    Toast.makeText(patientLogin.this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                myDb = myFire.getReference("Patient").child("info");
                myDb.orderByChild("patientReg").equalTo(patientReg).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            boolean passwordMatched = false;
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                String passwordFromDb = snapshot.child("passReg").getValue(String.class);
                                if (passwordFromDb != null && passwordFromDb.equals(logPassword)) {
                                    passwordMatched = true;
                                    userId = snapshot.getKey();
                                    guideName = snapshot.child("guideName").getValue(String.class);
                                    guideId = snapshot.child("guideId").getValue(String.class);
                                    break;
                                }
                            }
                            if (passwordMatched) {
                                Toast.makeText(patientLogin.this, "Credentials Matched !!", Toast.LENGTH_SHORT).show();
                                Intent i = new Intent(patientLogin.this, patientsDashBoard.class);
                                i.putExtra("UserId", userId);
                                i.putExtra("patientReg", patientReg);
                                i.putExtra("guideName", guideName);
                                i.putExtra("guideId", guideId);
                                startActivity(i);
                            } else {
                                Toast.makeText(patientLogin.this, "Password Not matched", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(patientLogin.this, "Invalid Patient Registration Number. Does not Exist", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(patientLogin.this, "Error checking registration number existence. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
