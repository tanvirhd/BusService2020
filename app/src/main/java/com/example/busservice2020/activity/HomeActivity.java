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

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
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
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
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
    private LocationRequest locationRequest;
    private Location mLastLocation;
    private LocationCallback locationCallback;

    private PlacesClient placesClient;
    private Place place;
    private Marker startMarker, destinationMArker;
    private List<LatLng> polylineLatLngList;

    private Marker bus01, bus02;//temporary marked used in putMarked method
    HashMap<String, GeoLocation> busList = new HashMap<>();
    HashMap<String, Marker> markerList = new HashMap<>();

    private Handler mainHandler = new Handler();


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

        //======================================================= maps =======================================================
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    Log.d(TAG, "Location Updated. ");
                    mLastLocation = location;
                }
            }
        };


    }//end of onCreate

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: called");

        //LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)){ }

        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM));
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getLastLocation(fusedLocationClient);

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case STARTLOCATION_REQUEST_CODE:
                switch (resultCode) {
                    case RESULT_OK:
                        place = Autocomplete.getPlaceFromIntent(data);

                        binding.homeAppbar.marker2.setImageResource(R.drawable.dot_selected_black);
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
                break;
            case DESTINATIONLOCATION_REQUEST_CODE:
                switch (resultCode) {
                    case RESULT_OK:
                        place = Autocomplete.getPlaceFromIntent(data);
                        binding.homeAppbar.searchDestinationLocation.setText(place.getName());

                        if (destinationMArker == null) {
                            destinationMArker = mMap.addMarker(new MarkerOptions()
                                    .position(place.getLatLng())
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_location))
                                    .title("Destination"));
                        } else {
                            destinationMArker.setPosition(place.getLatLng());
                        }

                        moveCamera(place.getLatLng(), "onActivityResult");
                        getNearbyBuses(startMarker==null?new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()):startMarker.getPosition());

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
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.admin:
                Toast.makeText(this, "navigation admin item slelected", Toast.LENGTH_SHORT).show();
                break;
            case R.id.logout:
                firebaseAuth.signOut();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
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

    private void getNearbyBuses(LatLng latLng) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("availableBuses");
        GeoFire geoFire = new GeoFire(reference);

        //todo remove geoQuery listener when done.
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(latLng.latitude, latLng.longitude), 2);

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                Log.d(TAG, "onKeyEntered: called");
                busList.put(key, location);
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                Log.d(TAG, "onGeoQueryReady: called. And busList size = " + busList.size());
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        //putMarker(busList);
                        updateMarkerList(busList);
                        busList.clear();
                    }
                });
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                Log.d(TAG, "onGeoQueryError: " + error.getMessage());
            }
        });

    }

    private void updateMarkerList(HashMap<String, GeoLocation> buslist) {
        Log.d(TAG, "updateMarkerList: called with buslist of size:" + buslist.size() + " And markerlist size:" + markerList.size());

        if (!markerList.isEmpty()) { // if there is no marker then no need to iterate otherwise update marker list
            for (Map.Entry me : markerList.entrySet()) {
                if (buslist.isEmpty()) {
                    Log.d(TAG, "updateMarkerList: (if) marker removed for key=" + me.getKey().toString());
                    markerList.get(me.getKey()).remove();
                    markerList.remove(me.getKey());
                } else {
                    if (!buslist.containsKey(me.getKey())) {
                        Log.d(TAG, "updateMarkerList: (else) marker removed for key=" + me.getKey().toString());
                        markerList.get(me.getKey()).remove();
                        markerList.remove(me.getKey());
                    }
                }
            }
        }

        if (!buslist.isEmpty()) {
            for (Map.Entry me : buslist.entrySet()) {
                Log.d(TAG, "updateMarkerList: inside loop. key=" + me.getKey().toString());
                updateMarkerLocation(me.getKey().toString(), (GeoLocation) me.getValue());
            }
            Log.d(TAG, "updateMarkerList: loop finished.");
        }
    }

    private void updateMarkerLocation(String key, GeoLocation location) {
        Log.d(TAG, "updateMarkerLocation:called for marker:" + key);
        Log.d(TAG, "updateMarkerLocation: marketlist =>" + markerList);

        if (!markerList.containsKey(key)) {
            Log.d(TAG, "updateMarkerLocation: adding marker:" + key);
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(location.latitude, location.longitude))
                    .draggable(true)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_location))
                    .title(key));
            markerList.put(key, marker);
        } else {
            Log.d(TAG, "updateMarkerLocation: repositioning marker:" + key);
            markerList.get(key).setPosition(new LatLng(location.latitude, location.longitude));
        }


    }

    private void getLastLocation(FusedLocationProviderClient flpc) {
        Log.d(TAG, "getLastLocation: called");
        flpc.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location == null) {
                            //todo handle location object when it is null
                        } else {
                            mLastLocation = location;
                            moveCamera(location, "getLastLocation");

                            binding.homeAppbar.marker2.setImageResource(R.drawable.dot_selected_blue);
                            binding.homeAppbar.searchStartLocation.setText("Your Location");

                            startLocationUpdates(createLocationRequest());
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: error=" + e.getMessage());
                    }
                });
    }

    private LocationRequest createLocationRequest() {
        Log.d(TAG, "createLocationRequest: called");
        locationRequest = LocationRequest.create();
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
            Toast.makeText(this, "something went wrong", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "getDirectionResponse: something went wrong");
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
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), DEFAULT_ZOOM));
    }

    public void moveCamera(LatLng latLng, String caller) {
        Log.d(TAG, "moveCamera: called by " + caller);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
    }

    private void removeGeoQueryListener(GeoQuery geoQuery) {
        geoQuery.removeAllListeners();
    }

    private void putMarker(HashMap<String, GeoLocation> list) {

        for (Map.Entry mp : list.entrySet()) {
            switch (mp.getKey().toString()) {
                case "bus01":
                    GeoLocation location = list.get("bus01");
                    if (bus01 == null) {
                        Log.d(TAG, "putMarker: bus01 added");
                        bus01 = mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(location.latitude, location.longitude))
                                .draggable(true)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_location))
                                .title("buo01"));
                    } else {
                        Log.d(TAG, "putMarker: bus01 location updated");
                        bus01.setPosition(new LatLng(location.latitude, location.longitude));
                    }
                    break;
                case "bus02":
                    GeoLocation location1 = list.get("bus02");
                    if (bus02 == null) {
                        Log.d(TAG, "putMarker: bus01 added");
                        bus02 = mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(location1.latitude, location1.longitude))
                                .draggable(true)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_location))
                                .title("buo01"));
                    } else {
                        Log.d(TAG, "putMarker: bus01 location updated");
                        bus02.setPosition(new LatLng(location1.latitude, location1.longitude));
                    }
                    break;
            }
        }


    }

}//the End
