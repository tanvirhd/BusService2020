package com.example.busservice2020.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.busservice2020.R;
import com.example.busservice2020.databinding.ActivityHomeBinding;

import com.example.busservice2020.model.UserModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


public class HomeActivity extends AppCompatActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener
       {
    private static final String TAG = "HomeActivity";
    private ActivityHomeBinding binding;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    Toolbar toolbar;
    TextView toolbarTitle;
    DrawerLayout drawerLayout;
    NavigationView navigationView;

    TextView headerName;
    ImageView headerPic;

    private GoogleMap mMap;
    private static final float DEFAULT_ZOOM =17f;
    private FusedLocationProviderClient fusedLocationClient;
    private Location mLastLocation;
    private LocationCallback locationCallback;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: called");
        super.onCreate(savedInstanceState);
        binding=ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        toolbar = findViewById(R.id.home_toolbar);
        setSupportActionBar(toolbar);
        toolbarTitle=findViewById(R.id.toolbarTitle);
        toolbarTitle.setText("Bus Service 2020");

        drawerLayout=findViewById(R.id.home_drwerlayout);
        navigationView=findViewById(R.id.home_navigation);
        ActionBarDrawerToggle actionBarDrawerToggle=new ActionBarDrawerToggle(this,
                drawerLayout,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);

        actionBarDrawerToggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.black,null));
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
        View header=navigationView.getHeaderView(0);
        headerName=header.findViewById(R.id.header_name);
        headerPic=header.findViewById(R.id.header_photo);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        updateNavHeader(firebaseUser.getUid());

        //todo=================================================== maps =======================================================
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fusedLocationClient= LocationServices.getFusedLocationProviderClient(this);
        getLastLocation(fusedLocationClient);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    moveCamera(location,"locationcallback onLocationResult");
                }
            }
        };


    }//end of onCreate

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: called");
        mMap = googleMap;

        mMap.setMyLocationEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM));
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
    }

    private void getLastLocation(FusedLocationProviderClient flpc){
        Log.d(TAG, "getLastLocation: called");
        flpc.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        mLastLocation=location;
                        moveCamera(mLastLocation,"getLastLocation");
                        startLocationUpdates(createLocationRequest());
                        if (location != null) {
                            //todo handle location object when it is null
                        }
                    }
                });
    }

    protected LocationRequest createLocationRequest() {
        Log.d(TAG, "createLocationRequest: called");
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return  locationRequest;
    }

    private void startLocationUpdates(LocationRequest locationRequest) {
        Log.d(TAG, "startLocationUpdates: called");
        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());
    }

    private void updateNavHeader(final String userid){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("userlist").child(userid);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserModel user = dataSnapshot.getValue(UserModel.class);
                if(user!=null){
                    headerName.setText(user.getName());
                    Picasso.get().load(user.getImageURL()).into(headerPic);
                    //Glide.with(HomeActivity.this).load(user.getImageURL()).into(headerPic);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: database error" + databaseError.getMessage());
            }
        });
    }

     @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.admin:
                Toast.makeText(this, "navigation admin item slelected", Toast.LENGTH_SHORT).show();
                break;
            case R.id.logout:
                Toast.makeText(this, "navigation  logout item slelected", Toast.LENGTH_SHORT).show();
                break;
            case R.id.ex_1:
                Toast.makeText(this, "navigation Extra  item slelected", Toast.LENGTH_SHORT).show();
                break;
            case R.id.ex_2:
                Toast.makeText(this, "navigation second Extra item slelected", Toast.LENGTH_SHORT).show();
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    public void moveCamera(Location location,String caller){
        Log.d(TAG, "moveCamera: called by "+caller);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),location.getLongitude()),DEFAULT_ZOOM));
    }

    public void moveCamera(LatLng latLng,String caller ){
        Log.d(TAG, "moveCamera: called by "+caller);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,DEFAULT_ZOOM));
    }


}
