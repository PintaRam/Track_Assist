package com.track_assist;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.regex.Pattern;

public class patientsRegistration extends AppCompatActivity {

    private EditText guideName;
    private EditText guideId;
    private EditText patName;
    private EditText patAddress;
    private EditText patientReg;
    private EditText passReg;
    private EditText confirmPassReg;
    private Button registerButton;
    private Button loginButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_patients_registration);
        guideName = findViewById(R.id.Guide_name);
        guideId = findViewById(R.id.guide_id);
        patName = findViewById(R.id.patname);
        patAddress = findViewById(R.id.pataddress);
        patientReg = findViewById(R.id.Patientreg);
        passReg = findViewById(R.id.passreg);
        confirmPassReg = findViewById(R.id.confrpassreg);
        registerButton = findViewById(R.id.regibtn);
        loginButton = findViewById(R.id.loginreg);

        // Set click listener for the register button
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validateAndRegister())
                {
                    Intent i=new Intent(patientsRegistration.this,patientLogin.class);
                    i.putExtra("Pass",passReg.getText().toString().trim());
                    i.putExtra("Id",patientReg.getText().toString().trim());
                    startActivity(i);
                }
                else {
                    Toast.makeText(patientsRegistration.this, "Registartion Failed!!!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(patientsRegistration.this, "Taking you to Login Sreen", Toast.LENGTH_SHORT).show();
                Intent i=new Intent(patientsRegistration.this,patientLogin.class);
                startActivity(i);
            }
        });
    }
    private boolean validateAndRegister() {
        String guideNameInput = guideName.getText().toString().trim();
        String guideIdInput = guideId.getText().toString().trim();
        String patNameInput = patName.getText().toString().trim();
        String patAddressInput = patAddress.getText().toString().trim();
        String patientRegInput = patientReg.getText().toString().trim();
        String passRegInput = passReg.getText().toString().trim();
        String confirmPassRegInput = confirmPassReg.getText().toString().trim();

        if (!isValidInput(patName, patNameInput, "Patient Name is required")) return false;
        if (!isValidInput(patientReg, patientRegInput, "Patient Registration Number is required")) return false;
        if (!isValidPassword(passRegInput)) {
            passReg.setError("Password must be at least 13 characters long and include letters, numbers, and special characters");
            passReg.requestFocus();
            return false;
        }
        if (!passRegInput.equals(confirmPassRegInput)) {
            confirmPassReg.setError("Passwords do not match");
            confirmPassReg.requestFocus();
            return false;
        }

        if (!isValidInput(patAddress, patAddressInput, "Patient Address is required")) return false;
        if (!isValidInput(guideName, guideNameInput, "Guide Name is required")) return false;
        if (!isValidInput(guideId, guideIdInput, "Guide ID is required")) return false;




        if (!isValidInput(confirmPassReg, confirmPassRegInput, "Confirm Password is required")) return false;


        Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show();
        // Proceed with registration logic (e.g., save to database, move to next activity, etc.)
        return true;
    }

    private boolean isValidInput(EditText editText, String input, String errorMsg) {
        if (TextUtils.isEmpty(input)) {
            editText.setError(errorMsg);
            editText.requestFocus();
            return false;
        }
        return true;
    }

    private boolean isValidPassword(String password) {
        if (password.length() < 8) {
            return false;
        }

        Pattern letter = Pattern.compile("[a-zA-Z]");
        Pattern digit = Pattern.compile("[0-9]");
        Pattern special = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]");

        return letter.matcher(password).find() &&
                digit.matcher(password).find() &&
                special.matcher(password).find();
    }
}