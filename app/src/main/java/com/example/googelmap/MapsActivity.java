package com.example.googelmap;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends AppCompatActivity {
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMITSSION_CODE = 1234;
    private static final String TAG = "MapsActivity";
    private static final float DEFAULT_ZOOM = 15F;

    //ver
    private Boolean mLocationPermissionGranted = false;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private GoogleMap mMap;
    SearchView searchView;
    ImageView mGps;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mGps = findViewById(R.id.ic_gps);
        searchView = findViewById(R.id.search_view);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        getLocationPermission();
    }

    private void init() {
        initSearchView();
        myLocation();
    }

    private void initSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                Geocoder geocoder = new Geocoder(MapsActivity.this);
                List<Address> list = new ArrayList<>();
                try {
                    list = geocoder.getFromLocationName(s, 1);
                } catch (IOException e) {
                    Log.d(TAG, "onQueryTextSubmit: " + e.getMessage());
                }
                if (list.size() > 0) {
                    Address address = list.get(0);
                    Log.d(TAG, "onQueryTextSubmit: found at" + address.toString());
                    moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM, address.getAddressLine(0));
                }


                return false;//onQueryTextSubmit :giving search result after submeiting
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
                //onQueryTextChange :giving search result while typing
            }
        });
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                Toast.makeText(MapsActivity.this, "Map Ready", Toast.LENGTH_SHORT).show();
                if (mLocationPermissionGranted) {
                    getDeviceLocation();


                    if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    mMap.setMyLocationEnabled(true);
                    mMap.getUiSettings().setMyLocationButtonEnabled(false);//to get red of location button so we can use our own setting
                    init();
                }
            }
        });
    }

    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: getting the devices current location");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            if (mLocationPermissionGranted) {
                Task location = mFusedLocationProviderClient.getLastLocation();//cal gsm library
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isComplete()) {
                            Log.d(TAG, "onComplete: location is found");
                            Location currentLocation = (Location) task.getResult();
                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude())
                                    , DEFAULT_ZOOM, "My Location");
                        } else {
                            Log.d(TAG, "onComplete: current location null");
                            Toast.makeText(MapsActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.d(TAG, "getDeviceLocation: SecurityException " + e.getMessage());
        }
    }

    private void moveCamera(LatLng latLng, float zoom, String title) {
        Log.d(TAG, "moveCamera: lat" + latLng.latitude + ", log" + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        if (!title.equals("My Location")) {
            MarkerOptions options = new MarkerOptions()
                    .position(latLng)
                    .title(title);
            mMap.addMarker(options);
        }
    }

    private void getLocationPermission() {
        String[] permitssins = {FINE_LOCATION, COARSE_LOCATION};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;//this mean all permission are set
                initMap();
            } else//ask for those permission
                ActivityCompat.requestPermissions(this, permitssins, LOCATION_PERMITSSION_CODE);
        else//ask for those permission
            ActivityCompat.requestPermissions(this, permitssins, LOCATION_PERMITSSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;

        switch (requestCode) {
            case LOCATION_PERMITSSION_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++)
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: Permission Faild");
                            return;
                        }
                    mLocationPermissionGranted = true;
                    Log.d(TAG, "onRequestPermissionsResult: Permission successfully granted");
                    initMap();//initial map
                }
            }
        }
    }

    private void myLocation() {
        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDeviceLocation();
            }
        });
    }
}
