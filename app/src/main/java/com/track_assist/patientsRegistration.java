package com.track_assist;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.regex.Pattern;

public class patientsRegistration extends AppCompatActivity {
    String guideNameInput, guideIdInput, patNameInput, patAddressInput, patientRegInput, passRegInput, confirmPassRegInput;

    private FirebaseDatabase fireDb;
    private DatabaseReference Dbrefer;
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

        fireDb = FirebaseDatabase.getInstance();
        Dbrefer = fireDb.getReference("Patient").child("info");

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

        if(validateAndRegister())
                { Database();
                    Intent i=new Intent(patientsRegistration.this,patientLogin.class);
                    i.putExtra("Pass",passReg.getText().toString().trim());
                    startActivity(i);
                } else {
                    Toast.makeText(patientsRegistration.this, "Registration Failed!!!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(patientsRegistration.this, "Taking you to Login Screen", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(patientsRegistration.this, patientLogin.class);
                startActivity(i);
            }
        });
    }

    private void Database() {
        Users user = new Users(guideNameInput, guideIdInput, patNameInput, patAddressInput, patientRegInput, passRegInput);

        Dbrefer.orderByChild("patientReg").equalTo(patientRegInput).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Toast.makeText(patientsRegistration.this, "Patient already registered.", Toast.LENGTH_SHORT).show();
                } else {
                    storeUserData(user);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(patientsRegistration.this, "Error checking email existence. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateAndRegister() {
        guideNameInput = guideName.getText().toString().trim();
        guideIdInput = guideId.getText().toString().trim();
        patNameInput = patName.getText().toString().trim();
        patAddressInput = patAddress.getText().toString().trim();
        patientRegInput = patientReg.getText().toString().trim();
        passRegInput = passReg.getText().toString().trim();
        confirmPassRegInput = confirmPassReg.getText().toString().trim();

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

    private void storeUserData(Users user) {
        Dbrefer.child(user.getPatientReg()).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    new Handler().postDelayed(() -> {
                        Toast.makeText(patientsRegistration.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(patientsRegistration.this, patientsDashBoard.class);
                        startActivity(i);
                        finish();
                    }, 3000); // Reduced delay to 3 seconds for better user experience
                } else {
                    Toast.makeText(patientsRegistration.this, "Failed to store user data. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
