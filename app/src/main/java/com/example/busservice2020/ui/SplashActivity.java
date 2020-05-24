package com.example.busservice2020.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.busservice2020.activity.HomeActivity;
import com.example.busservice2020.activity.LoginActivity;
import com.example.busservice2020.interfaces.Fragment_Communication;
import com.google.android.gms.location.LocationRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.List;


//todo ask location permission
//todo ask background location permission


public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "SplashActivity";
    FirebaseUser firebaseUser;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();


        if (firebaseUser != null) {
            checkUserStatus();
        } else {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }


        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {
                            Log.d(TAG, "All Permission Granted");
                        }
                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            Log.d(TAG, "Any Permission permanent denial");
                            // show alert dialog navigating to Settings
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }

                }).check();

    }

    private void checkUserStatus() {
//        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("userlist");
//        UserModel user = new UserModel("", "", "", "");
//        reference.child("root_user_id").setValue(user);
        String userid = firebaseUser.getUid();
        Log.d(TAG, "userid:" + userid);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("userlist").child(userid);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "@ " + dataSnapshot.getValue());
                if (dataSnapshot.getValue() == null) {
                    Log.d(TAG, "onDataChange: New User");
                    //test
                    Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();

                } else {
                    startActivity(new Intent(SplashActivity.this, HomeActivity.class));
                    finish();
                    Log.d(TAG, "onDataChange: Registered User");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: database error" + databaseError.getMessage());
            }
        });
    }

    //do no follow these methods. they ar buggy
    /*
    private static boolean LocationPermission = false;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 999;

    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String permissions[] = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {

            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), COARSE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED) {
                LocationPermission = true;
            } else {
                ActivityCompat.requestPermissions(this, permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }

        } else {
            ActivityCompat.requestPermissions(this, permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called");
        LocationPermission = false;
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            LocationPermission = true;
                            Log.d(TAG, "onRequestPermissionsResult: permission granted.");
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission failde");
                    LocationPermission = false;
                }
            }
        }
    }*/
}