package com.sazzadhrz.childapp;

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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final int ERROR_DIALOG_REQUEST = 9001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(isServiceOk()) {
            init();
            initParent();
        }

        // Write a message to the database
//        FirebaseDatabase database = FirebaseDatabase.getInstance();
//        DatabaseReference myRef = database.getReference("message");
//
//        myRef.setValue("Hello, Bhai!");

    }

    // initialize the map --- 2
    public void init() {
        Button btnMap = findViewById(R.id.btnMap);

        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MapActivity.class);
                startActivity(intent);  // opens mapActivity
            }
        });
    }

    // initialize parent
    public void initParent() {
        Button btnParent = findViewById(R.id.btnParent);

        btnParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ParentActivity.class);
                startActivity(intent);  // opens mapActivity
            }
        });
    }

    // check if google play service is ok or not --- 1
    public boolean isServiceOk() {
        Log.d(TAG, "isServiceOk: checking goolge services version");

        int availble = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if(availble == ConnectionResult.SUCCESS) {
            //everything is working and user can use google play services
            Log.d(TAG, "isServiceOk: Google play services is working");
            return true;
        }
        else if (GoogleApiAvailability.getInstance().isUserResolvableError(availble)) {
            //an erro occured but we can solve them
            Log.d(TAG, "isServiceOk: an erro occured but we can solve them");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, availble, ERROR_DIALOG_REQUEST);
            ((Dialog) dialog).show();
        }
        else {
            Toast.makeText(this, "you cant make map request", Toast.LENGTH_SHORT).show();
        }

        return false;

    }
}
