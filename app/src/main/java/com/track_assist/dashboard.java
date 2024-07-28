package com.track_assist;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class dashboard extends AppCompatActivity {
    private String regist;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        Handler handler = new Handler();
        regist = getIntent().getStringExtra("patientReg");
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(dashboard.this,patientsDashBoard.class);
                intent.putExtra("patientReg", regist);
                startActivity(intent);
                finish();
            }
        }, 2000);
    }
}