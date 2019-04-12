package com.sazzadhrz.childapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

public class ParentActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "ParentActivity";

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;

    //vars
    private static boolean mLocationPermissionGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LatLng temp;
    private Marker marker;


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

        Circle circle = mMap.addCircle(new CircleOptions()
                .center(new LatLng(23.816225,  90.426069))
                .radius(75)
                .strokeColor(Color.RED)
                .fillColor(Color.argb(45, 255, 0, 0)));

        mMap.setOnCircleClickListener(new GoogleMap.OnCircleClickListener() {
            @Override
            public void onCircleClick(Circle circle) {
                int strokeColor = circle.getStrokeColor() ^ 0x00ffffff;
                circle.setStrokeColor(strokeColor);
            }
        });

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
        mapFragment.getMapAsync(ParentActivity.this);

//        saveUserLocation();
    }

    // move the map camera according to latlng
    private void moveCamera(LatLng latlng, float zoom) {
        Log.d(TAG, "moveCamera: moving the cameta according to latlng");
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, zoom));

    }

    //save user location
    private void saveUserLocation() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("message");

//        myRef.setValue("Hello, Duniya!");
        myRef.setValue(temp);
    }


    // recieve data from DB and change camera
    private void getAndMoveCamera() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("message");

        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                UserLocation uLoc = dataSnapshot.getValue(UserLocation.class);

                Toast.makeText(ParentActivity.this,
                        "Lat: " + uLoc.latitude, Toast.LENGTH_SHORT).show();

                LatLng dblatlng = new LatLng(uLoc.latitude, uLoc.longitude);
                moveCamera(dblatlng, DEFAULT_ZOOM);
//                mMap.addMarker(new MarkerOptions().position(dblatlng).title("My Child"));
//                Marker marker = null;
                //                    marker.remove();
                if(marker != null)
                    marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                marker = mMap.addMarker(new MarkerOptions().position(dblatlng));

                // get notification if child is is red zone
//                getNotified(dblatlng);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };
        myRef.addValueEventListener(postListener);
    }

    // get notification
//    private void getNotified(LatLng currentLatLng) {
//        //NSU
//        LatLng point1 = new LatLng(23.816414, 90.425575);
//        LatLng point2 = new LatLng(23.815943, 90.426611);
//        //Home
////        LatLng point1 = new LatLng(23.765421, 90.356623);
////        LatLng point2 = new LatLng(23.762233, 90.363436);
//
//        LatLngBounds LLBounds = new LatLngBounds(point1, point2);
//        if(LLBounds.contains(currentLatLng)) {
//            Toast.makeText(this, "VITOREI ASE", Toast.LENGTH_SHORT).show();
//        }
//        else
//            Toast.makeText(this, "Nai", Toast.LENGTH_SHORT).show();
//    }

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

                        // added new starts
                        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
                        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        double longitude = location.getLongitude();
                        double latitude = location.getLatitude();
//                        moveCamera(new LatLng(latitude, longitude), DEFAULT_ZOOM);
                        getAndMoveCamera();
                        // end

                    }
                    else {
                        Log.d(TAG, "onComplete: Couldn't found Location");
                        Toast.makeText(ParentActivity.this,
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
