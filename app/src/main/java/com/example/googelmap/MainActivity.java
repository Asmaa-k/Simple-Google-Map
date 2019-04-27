package com.example.googelmap;

import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int ERROR_DIALOG_REQUEST = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (isServicesOk()) {
            init();
        }
    }

    private void init() {
        Button map = findViewById(R.id.map_btn);
        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MapsActivity.class));
            }
        });
    }

    private boolean isServicesOk() {
        Log.d(TAG, "isServicesOk: Check if google service is working");
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if (available == ConnectionResult.SUCCESS) {
            Log.d(TAG, "isServicesOk: google service is available");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            Log.d(TAG, "isServicesOk: an Error ocurred but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else Toast.makeText(this, "You can't make map request", Toast.LENGTH_LONG).show();

        return false;
    }
}
