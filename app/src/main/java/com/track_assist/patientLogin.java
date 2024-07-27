package com.track_assist;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;


public class patientLogin extends AppCompatActivity {


    private EditText editTextId;
    private EditText editTextPassword;
    private Button buttonLogin;
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
                String id = editTextId.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();

                if (validatePassword(password)) {
                    // Successful validation, navigate to PatientDashboardActivity
                    Intent intent = new Intent(patientLogin.this, patientsDashBoard.class);
                    startActivity(intent);
                    finish();
                } else {
                    // Show error message
                    Toast.makeText(patientLogin.this, "Invalid Password", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean validatePassword(String password) {
        // Add your password validation logic here
        // For example, check if the password is not empty and matches a predefined password
        String validPassword = "your_valid_password"; // Replace with your valid password logic
        return password.equals(validPassword);
    }
}







