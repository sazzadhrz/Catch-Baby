package com.sazzadhrz.childapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MapActivity";

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;

    //vars
    private static boolean mLocationPermissionGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private UserLocation userLoc;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Map is Ready", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: Map is Ready");
        mMap = googleMap;

        if (mLocationPermissionGranted) {
            getDeviceLocation();
//            getLastKnownLocation();

            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override  // --- 6
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: Create the map");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        getLocationPermission();
    }

    //initialize the map --- 5
    private void initMap() {
        Log.d(TAG, "initMap: initializing the map");
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapActivity.this);

//        saveUserLocation();
    }

    // move the map camera according to latlng
    private void moveCamera(LatLng latlng, float zoom) {
        Log.d(TAG, "moveCamera: moving the cameta according to latlng");
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, zoom));

        UserLocation userLocation = new UserLocation(latlng.latitude, latlng.longitude);
//        UserLocation userLocation = new UserLocation(20.369, 81.65);
        userLoc = userLocation;

        saveUserLocation();

    }

    //save user location
    private void saveUserLocation() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("message");

//        myRef.setValue(temp);
//        myRef.setValue("Working");
        myRef.setValue(userLoc);
    }


    // get current location of the device --- 7
    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: getting devices current location");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            final Task location = mFusedLocationProviderClient.getLastLocation();
            location.addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Log.d(TAG, "onComplete: Found Location");
                        Location currentLocation = (Location) task.getResult();

                        // starts
                        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
                        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        double longitude = location.getLongitude();
                        double latitude = location.getLatitude();
                        moveCamera(new LatLng(latitude, longitude), DEFAULT_ZOOM);
                        // end

//                        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, LOCATION_PERMISSION_REQUEST_CODE);

//                        moveCamera(new LatLng(currentLocation.getLatitude(),
//                                currentLocation.getLongitude()), DEFAULT_ZOOM);

//                        LatLng saveLL = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
//                        LatLng saveLL = new LatLng(latitude, longitude);
//                        LatLng saveLL = new LatLng(50.33, 169.256);
//                        temp = saveLL;
//                        saveUserLocation();

//                        moveCamera(new LatLng(23.810331, 90.412521), DEFAULT_ZOOM);

                    }
                    else {
                        Log.d(TAG, "onComplete: Couldn't found Location");
                        Toast.makeText(MapActivity.this,
                                "Unable to get current Location", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException " + e.getMessage());
        }
    }

    // CHECK FOR THE PERMISISONS, IF THERE IS NO PERMISSION THEN ASK FOR THE PERMISSIONS --- 3
    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: Getting location permission");
        String[] permissions = {FINE_LOCATION, COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
                initMap();
            } else {
                ActivityCompat.requestPermissions(this, permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        }
        else {
            ActivityCompat.requestPermissions(this, permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
    } }

    @Override  // --- 4
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: Called");
        mLocationPermissionGranted = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0)
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: Permission Failed");
                            return;
                        }
                    }
                mLocationPermissionGranted = true;
                Log.d(TAG, "onRequestPermissionsResult: Permission Granted");
                // initialize the map
                initMap();
            }
        }
    }
}
