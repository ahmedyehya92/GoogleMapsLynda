package com.intellidev.app.googlemapslynda;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.gcm.Task;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, PlaceAutoCompleteAdapter.OnAutoLocationItemClickListner {

    Place place;
    TextView tvAddress;
    boolean mLocationPermissionGranted = false;
    final int LOCATION_PERMISION_REQUIST_CODE = 103;
    final String TAG = MapActivity.class.getSimpleName();
    GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private float DEFAULT_ZOOM = 15f;
    private PlaceAutoCompleteAdapter autoCompleteAdapter;
    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(new LatLng(-40, -168), new LatLng(71, 136));

    // Widgets
    // TODO 13 this was edittext but changed to make autocomplete
    AutoCompleteTextView etSearch;

    ImageView icGps;
    private GoogleApiClient mGoogleApiClient;
    private GeoDataClient mGeoDataClient;
    Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map2);
        mHandler = new Handler(Looper.getMainLooper());
        etSearch = findViewById(R.id.et_search);
        tvAddress = findViewById(R.id.tv_address);
        icGps = findViewById(R.id.ic_gps);
        getLocationPermisions();

    }

    // TODO 8
    private void setupSearchBox ()
    {
        // Todo 14 to activate autocomplete
        mGeoDataClient = Places.getGeoDataClient(this, null);
        autoCompleteAdapter = new PlaceAutoCompleteAdapter(this,mGeoDataClient, LAT_LNG_BOUNDS,null);
        autoCompleteAdapter.setOnAutoLocationItemClickListner(this);
        etSearch.setAdapter(autoCompleteAdapter);



        etSearch.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {

                if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                        actionId == EditorInfo.IME_ACTION_DONE ||
                        actionId == EditorInfo.IME_ACTION_NEXT ||
                        keyEvent.getAction() == KeyEvent.ACTION_DOWN ||
                        keyEvent.getAction() == KeyEvent.KEYCODE_ENTER)
                {
                    geoLocate();
                    return true;
                }


                return false;
            }
        });

        // TODO 12 gps icon to get device location again
        icGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDeviceLocation();
            }
        });

    }

    // TODO 9 you must enable "Google Maps Geocoding API" to make that work
    private void geoLocate() {
        Log.d(TAG, "geoLocate: ");
        String searchQuery = etSearch.getText().toString();
        Geocoder geocoder = new Geocoder(MapActivity.this);
        List<Address> list = new ArrayList<>();

        try {
            list = geocoder.getFromLocationName(searchQuery, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (list.size()>0)
        {
            Address address = list.get(0);
            Log.d(TAG, "geoLocate: " + address.toString());

            // TODO 10 Move camera to the location (lat & lang)
            moveCamera(new LatLng(address.getLatitude(),address.getLongitude()),DEFAULT_ZOOM, address.getAddressLine(0));

        }

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
        setupSearchBox();

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

        Point x_y_points = new Point(((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getView().getWidth()/2, ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getView().getHeight()/2);
        LatLng latLng =
                mMap.getProjection().fromScreenLocation(x_y_points);
        Log.d(TAG, "centerLocation lat: "+ latLng.latitude);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Point x_y_points = new Point(((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getView().getWidth()/2, ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getView().getHeight()/2);
                LatLng latLg =
                        mMap.getProjection().fromScreenLocation(x_y_points);
                Log.d(TAG, "centerLocation lat: "+ latLg.latitude);
            }
        });

        mMap.setOnCameraMoveCanceledListener(new GoogleMap.OnCameraMoveCanceledListener() {
            @Override
            public void onCameraMoveCanceled() {
                /*
                Point x_y_points = new Point(((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getView().getWidth()/2, ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getView().getHeight()/2);
                final LatLng latLg =
                        mMap.getProjection().fromScreenLocation(x_y_points);
                Log.d(TAG, "centerLocation lat: "+ latLg.latitude);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        List<Address> addresses;
                        Geocoder geocoder = new Geocoder(MapActivity.this);
                        try {
                            addresses = geocoder.getFromLocation(latLg.latitude, latLg.longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                            etSearch.setText(address);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                */
            }
        });

        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                Point x_y_points = new Point(((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getView().getWidth()/2, ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getView().getHeight()/2);
                final LatLng latLg =
                        mMap.getProjection().fromScreenLocation(x_y_points);
                Log.d(TAG, "centerLocation lat: "+ latLg.latitude);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        List<Address> addresses;
                        Geocoder geocoder = new Geocoder(MapActivity.this);
                        try {
                            addresses = geocoder.getFromLocation(latLg.latitude, latLg.longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                            tvAddress.setText(address);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                /*
                Point x_y_points = new Point(((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getView().getWidth()/2, ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getView().getHeight()/2);
                final LatLng latLg =
                        mMap.getProjection().fromScreenLocation(x_y_points);
                Log.d(TAG, "centerLocation lat: "+ latLg.latitude);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        List<Address> addresses;
                        Geocoder geocoder = new Geocoder(MapActivity.this);
                        try {
                            addresses = geocoder.getFromLocation(latLg.latitude, latLg.longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                            etSearch.setText(address);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
/*

                */

            }
        });
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
                            moveCamera(new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude()), DEFAULT_ZOOM, "My Location");
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


    private void moveCamera (LatLng latLng, float zoom, String title)
    {
        Log.d(TAG, "moveCamera: to lat : " + latLng.latitude + ", long : " + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoom));

        // TODO 11 to show address hint above marker
        /*MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title(title);
        mMap.addMarker(markerOptions); */

        // TODO 12 to hide keyboard when moving camera
        hideKeyboard();
    }


    public void hideKeyboard() {
        View view = findViewById(android.R.id.content);
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onAutoLocationItemClicked(String plceId) {
        getPlace(plceId);
        //Log.d(TAG, "onAutoLocationItemClicked: "+place.getName());
    }

    private void getPlace (String placeId) {
        mGeoDataClient.getPlaceById(placeId).addOnCompleteListener(new OnCompleteListener<PlaceBufferResponse>() {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<PlaceBufferResponse> task) {
                if (task.isSuccessful()) {
                    PlaceBufferResponse places = task.getResult();
                    place = places.get(0);
                    Log.i(TAG, "Place found: " + place.getLatLng().toString());
                    moveCamera(place.getLatLng(),DEFAULT_ZOOM,"My Location");
                    Log.d(TAG, "onAutoLocationItemClicked: "+place.getName());
                    tvAddress.setText(place.getAddress());
                    etSearch.dismissDropDown();
                    places.release();
                } else {
                    Log.e(TAG, "Place not found.");
                }
            }
        });
    }
}
