package com.intellidev.app.googlemapslynda;

import android.*;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.Task;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    boolean mLocationPermissionGranted = false;
    final int LOCATION_PERMISION_REQUIST_CODE = 103;
    final String TAG = MapActivity.class.getSimpleName();
    GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private float DEFAULT_ZOOM = 15f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map2);
        getLocationPermisions();
    }

    // TODO 4
    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapActivity.this);
    }


    //TODO 1
    private void getLocationPermisions() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), permissions[i]) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
            } else {
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISION_REQUIST_CODE);
            }
        }
        if (mLocationPermissionGranted)
            initMap();
    }

    // TODO 2
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case LOCATION_PERMISION_REQUIST_CODE:
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionGranted = false;
                            return;
                        }
                    }
                    mLocationPermissionGranted = true;
                    initMap();
                }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    // TODO 3 implement OnMapReadyCallback on this activity
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "map ready", Toast.LENGTH_SHORT).show();
        mMap = googleMap;
        getDeviceLocation();

        // TODO 6 if you want to mark current location with blue marker
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);

        // TODO 7 to hide the button that make the map at the center of current location if you need a search interface
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
    }

    // TODO 5 get device location
    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: getting the current device location");
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            if (mLocationPermissionGranted)
            {
                com.google.android.gms.tasks.Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: location found");
                            Location currentLocation = (Location) task.getResult();
                            moveCamera(new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude()), DEFAULT_ZOOM);
                        }
                        else {
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(MapActivity.this, "unable to find current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.d(TAG, "getDeviceLocation: SecurityException" + e);
        }

    }

    private void moveCamera (LatLng latLng, float zoom)
    {
        Log.d(TAG, "moveCamera: to lat : " + latLng.latitude + ", long : " + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoom));
    }
}
