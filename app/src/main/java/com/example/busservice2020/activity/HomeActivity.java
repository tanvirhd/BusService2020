package com.example.busservice2020.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
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

import com.example.busservice2020.model.DirectionResponse;
import com.example.busservice2020.model.OverviewPolyline;
import com.example.busservice2020.model.UserModel;
import com.example.busservice2020.viewmodel.ViewmodelDirectionApi;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.PolyUtil;
import com.squareup.picasso.Picasso;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class HomeActivity extends AppCompatActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "HomeActivity";
    private static final int STARTLOCATION_REQUEST_CODE = 0;
    private static final int DESTINATIONLOCATION_REQUEST_CODE = 1;
    private static final int REQUEST_CHECK_SETTINGS = 666;

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
    private static final float DEFAULT_ZOOM = 17f;
    private FusedLocationProviderClient fusedLocationClient;
    private Location mLastLocation;
    private LocationCallback locationCallback;

    private PlacesClient placesClient;
    private Place place;
    private Marker startMarker, destinationMArker;
    private List<LatLng> polylineLatLngList;


    ViewmodelDirectionApi viewmodelDirectionApi;

    //todo drow polyline from start to destination location has some issues.must recheck.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: called");
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        toolbar = findViewById(R.id.home_toolbar);
        setSupportActionBar(toolbar);
        toolbarTitle = findViewById(R.id.toolbarTitle);
        toolbarTitle.setText("Bus Service 2020");

        drawerLayout = findViewById(R.id.home_drwerlayout);
        navigationView = findViewById(R.id.home_navigation);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this,
                drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        actionBarDrawerToggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.black, null));
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);
        headerName = header.findViewById(R.id.header_name);
        headerPic = header.findViewById(R.id.header_photo);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        viewmodelDirectionApi = new ViewModelProvider.AndroidViewModelFactory(getApplication()).create(ViewmodelDirectionApi.class);
        //============================================initialization ends here===============================================

        updateNavHeader(firebaseUser.getUid());

        binding.homeAppbar.searchStartLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initAutoComplete(STARTLOCATION_REQUEST_CODE);
            }
        });

        binding.homeAppbar.searchDestinationLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initAutoComplete(DESTINATIONLOCATION_REQUEST_CODE);
            }
        });


        locationsetting(HomeActivity.this);
        //======================================================= maps =======================================================
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getLastLocation(fusedLocationClient);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    //moveCamera(location,"locationcallback onLocationResult");
                }
            }
        };


    }//end of onCreate

    private void locationsetting(Context context) {
        final LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                Log.d(TAG, "loction on");
            }
        });
        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    try {
                        Log.d(TAG, "try to loction on ");
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(HomeActivity.this,
                                REQUEST_CHECK_SETTINGS);

                    } catch (IntentSender.SendIntentException sendEx) {
                        Log.d(TAG, "try to loction onnnn ");
                    }

                }
            }
        });

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: called");
        mMap = googleMap;

        mMap.setMyLocationEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM));
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                startMarker.setPosition(marker.getPosition());
                Log.d(TAG, "onMarkerDragEnd: " + startMarker.getId());
            }
        });
    }

    private void getLastLocation(FusedLocationProviderClient flpc) {
        Log.d(TAG, "getLastLocation: called");
        flpc.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        mLastLocation = location;
                        moveCamera(mLastLocation, "getLastLocation");
                        startLocationUpdates(createLocationRequest());
                        if (location != null) {
                            //todo handle location object when it is null
                        }
                    }
                });
    }

    private LocationRequest createLocationRequest() {
        Log.d(TAG, "createLocationRequest: called");
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    private void startLocationUpdates(LocationRequest locationRequest) {
        Log.d(TAG, "startLocationUpdates: called");
        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());
    }

    //todo research on this
    private void stopLocationUpdate() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private void initPlacesAPI() {
        try {
            if (!Places.isInitialized()) {
                Places.initialize(getApplicationContext(), "AIzaSyCdP8QSuapjIn5DZEfWXG5EH6EIiYb6uuY");
            }
            placesClient = Places.createClient(this);
        } catch (Exception e) {
            Log.d(TAG, "initPlacesAPI: error" + e.getMessage());
        }
    }

    private void initAutoComplete(int CALLER_REQUEST_CODE) {

        initPlacesAPI();

        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG,
                Place.Field.TYPES);

        Intent intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.OVERLAY, fields)
                .setCountry("bd")

                .build(this);
        startActivityForResult(intent, CALLER_REQUEST_CODE);
    }

    private void getDirectionResponse(Marker origin, Marker destination) {
        Log.d(TAG, "getDirectionResponse: called");
        if (origin != null && destination != null) {
            Log.d(TAG, "getDirectionResponse: inside if");
            try {
                Map<String, String> mapQuery = new HashMap<>();
                mapQuery.put("key", "AIzaSyCdP8QSuapjIn5DZEfWXG5EH6EIiYb6uuY");
                mapQuery.put("origin", origin.getPosition().latitude + "," + origin.getPosition().longitude);
                mapQuery.put("destination", destination.getPosition().latitude + "," + destination.getPosition().longitude);
                //mapQuery.put("waypoints","place_id:EjzgprjgpoLgprjgpqYg4Kat4Kas4KaoIOCmj-CmreCmv-CmqOCmv-CmiSwgRGhha2EsIEJhbmdsYWRlc2giLiosChQKEgl5t4sRqLhVNxFuzi8hz2Bs_RIUChIJgWsCh7C4VTcRwgRZ3btjpY8|place_id:EiBNYW5payBNaWEgQXZlLCBEaGFrYSwgQmFuZ2xhZGVzaCIuKiwKFAoSCQ0daOiruFU3EX3sX5-U4v_rEhQKEgmBawKHsLhVNxHCBFndu2Oljw");

                viewmodelDirectionApi.getDirectionResponse(mapQuery).observe(this, new Observer<DirectionResponse>() {
                    @Override
                    public void onChanged(DirectionResponse directionResponse) {
                        Log.d(TAG, "onChanged: direction response status: " + directionResponse.getStatus());
                        if (directionResponse.getStatus().equals("OK")) {
                            OverviewPolyline overviewPolyline = directionResponse.getRoutes().get(0).getOverviewPolyline();
                            drawPolyLine(overviewPolyline);
                        }
                    }
                });
            } catch (Exception E) {
                Log.d(TAG, "getDirectionResponse: error" + E.getMessage());
            }
        } else {
            Log.d(TAG, "getDirectionResponse: something wet wrong");
        }
    }

    private void drawPolyLine(OverviewPolyline overviewPolyline) {

        if (overviewPolyline != null) {
            polylineLatLngList = PolyUtil.decode(overviewPolyline.getPoints());

            for (int k = 0; k < polylineLatLngList.size() - 1; k++) {
                LatLng origin = polylineLatLngList.get(k);
                LatLng destination = polylineLatLngList.get(k + 1);
                mMap.addPolyline(new PolylineOptions().color(R.color.pilyline).geodesic(true).add(
                        new LatLng(origin.latitude, origin.longitude),
                        new LatLng(destination.latitude, destination.longitude)).width(12));
            }

        } else {
            Log.d(TAG, "drawPolyLine: overviewPolyline=null");
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == STARTLOCATION_REQUEST_CODE) {
            switch (resultCode) {
                case RESULT_OK:
                    place = Autocomplete.getPlaceFromIntent(data);
                    binding.homeAppbar.searchStartLocation.setText(place.getName());

                    if (startMarker == null) {
                        startMarker = mMap.addMarker(new MarkerOptions()
                                .position(place.getLatLng())
                                .draggable(true)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_location))
                                .title(place.getId()));
                    } else {
                        startMarker.setPosition(place.getLatLng());
                    }


                    moveCamera(place.getLatLng(), "onActivityResult");

                    Log.i(TAG, "Place: " + place.getName() + ", " + place.getId() + " markerid=" + startMarker.getId());
                    break;
                case AutocompleteActivity.RESULT_ERROR:
                    // TODO: Handle the error.
                    Status status = Autocomplete.getStatusFromIntent(data);
                    Log.d(TAG, "onActivityResult: " + status.getStatusMessage());
                    break;
                case RESULT_CANCELED:
                    Log.d(TAG, "onActivityResult: user cancled.");
                    break;
            }
        } else if (requestCode == DESTINATIONLOCATION_REQUEST_CODE) {
            switch (resultCode) {
                case RESULT_OK:
                    place = Autocomplete.getPlaceFromIntent(data);
                    binding.homeAppbar.searchDestinationLocation.setText(place.getName());

                    if (destinationMArker == null) {
                        destinationMArker = mMap.addMarker(new MarkerOptions()
                                .position(place.getLatLng())
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_location))
                                .title(place.getId()));
                    } else {
                        destinationMArker.setPosition(place.getLatLng());
                    }

                    moveCamera(place.getLatLng(), "onActivityResult");

                    getDirectionResponse(startMarker, destinationMArker);

                    Log.i(TAG, "Place: " + place.getId() + ", " + place.getLatLng());
                    break;
                case AutocompleteActivity.RESULT_ERROR:
                    // TODO: Handle the error.
                    Status status = Autocomplete.getStatusFromIntent(data);
                    Log.d(TAG, "onActivityResult: " + status.getStatusMessage());
                    break;
                case RESULT_CANCELED:
                    Log.d(TAG, "onActivityResult: user cancled.");
                    break;
            }

        } else if (requestCode == REQUEST_CHECK_SETTINGS) {
            Log.d(TAG, "bal");

        }
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

    private void updateNavHeader(final String userid) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("userlist").child(userid);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserModel user = dataSnapshot.getValue(UserModel.class);
                if (user != null) {
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

    public void moveCamera(Location location, String caller) {
        Log.d(TAG, "moveCamera: called by " + caller);
        if (location != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), DEFAULT_ZOOM));

        }
    }

    public void moveCamera(LatLng latLng, String caller) {
        Log.d(TAG, "moveCamera: called by " + caller);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
    }


}
