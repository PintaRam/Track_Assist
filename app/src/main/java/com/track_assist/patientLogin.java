package com.track_assist;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class patientLogin extends AppCompatActivity {


    private EditText editTextId;
    private EditText editTextPassword;
    private Button buttonLogin;
    private String  p_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_login);

        editTextId = findViewById(R.id.editTextId);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);


        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                p_id = editTextId.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();

                validateCredentials(id,password);
            }
        });
    }

    private void validateCredentials(String id, String password) {
        DatabaseReference myDb=FirebaseDatabase.getInstance().getReference("Patient").child("");
        myDb.orderByChild().equalTo(logname).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Email exists in the database
                    // Now, iterate through the dataSnapshot to find the user with the given email
                    boolean passwordMatched = false; // Flag to indicate if password matched
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        // Retrieve the user's password from the database
                        String passwordFromDb = snapshot.child("paswd").getValue(String.class);
                        // Check if the retrieved password matches the one provided by the user
                        if (passwordFromDb != null && passwordFromDb.equals(logpaswd)) {
                            // Password matches, authentication successful
                            passwordMatched = true;
                            break; // Exit the loop as authentication is successful
                        }
                    }
                    if (passwordMatched) {
                        Toast.makeText(Login.this, "Credentials Matched !!", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(Login.this, OrganizeMapper.class);
                        startActivity(i);
                    } else {
                        Toast.makeText(Login.this, "Password Not matched", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Email does not exist in the database
                    Toast.makeText(Login.this, "Invalid email.Email does not Exists", Toast.LENGTH_SHORT).show();
                }
            }
    }
}







