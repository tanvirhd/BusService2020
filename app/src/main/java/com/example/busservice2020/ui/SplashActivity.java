package com.example.busservice2020.ui;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.busservice2020.activity.HomeActivity;
import com.example.busservice2020.activity.LoginActivity;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;;

//todo ask background location permission
//todo current user issue needs to be fixed

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "SplashActivity";
    private static final int FINE_LOCATION_REQUEST_CODE=11;
    private static final int LOCATION_SETTINGS_REQUEST_CODE=22;
    FirebaseUser firebaseUser;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();


        if(requestLocationPermission()){
            locationSettingOption();
        }

        /*Dexter.withActivity(this)
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

                }).check();*/

    }

    private void checkUserStatus() {
        /*DatabaseReference reference = FirebaseDatabase.getInstance().getReference("userlist");
        UserModel user = new UserModel("", "", "", "");
        reference.child("root_user_id").setValue(user);*/
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

    private boolean  requestLocationPermission(){
        if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
            return true;
        }else{
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},FINE_LOCATION_REQUEST_CODE);
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==FINE_LOCATION_REQUEST_CODE){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Log.d(TAG, "onRequestPermissionsResult: permission granted.");
                locationSettingOption();
            }else {
                Log.d(TAG, "onRequestPermissionsResult: permission denied.");
                finish();
            }
        }else{
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void locationSettingOption() {
        final LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    try {
                        Log.d(TAG, "try to loction on ");
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(SplashActivity.this,
                                LOCATION_SETTINGS_REQUEST_CODE);
                    } catch (IntentSender.SendIntentException sendEx) {
                        Log.d(TAG, "try to loction onnnn ");
                    }
                }
            }
        });

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                Log.d(TAG, "onSuccess: location is on.");

                if (firebaseUser != null) {
                    checkUserStatus();
                } else {
                    Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(requestCode==LOCATION_SETTINGS_REQUEST_CODE){
            switch (resultCode){
                case RESULT_OK:
                    Log.d(TAG, "onActivityResult: RESULT_OK");

                    if (firebaseUser != null) {
                        checkUserStatus();
                    } else {
                        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }

                    break;
                case RESULT_CANCELED:
                    new AlertDialog.Builder(this)
                            .setTitle("Enable Location Services")
                            .setMessage( "You need to enable Location Services for better user user Experience." )
                            .setPositiveButton( "Enable" , new
                                    DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick (DialogInterface paramDialogInterface , int paramInt) {
                                            locationSettingOption();
                                        }
                                    })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            })
                            .show() ;
                    Log.d(TAG, "onActivityResult: RESULT_CANCELED");
                    break;
                default:
                    super.onActivityResult(requestCode, resultCode, data);
            }
        }
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