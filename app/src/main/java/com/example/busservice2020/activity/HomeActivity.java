package com.example.busservice2020.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.busservice2020.R;
import com.example.busservice2020.adapter.AdapterAvailableBus;
import com.example.busservice2020.databinding.ActivityHomeBinding;

import com.example.busservice2020.interfaces.AdapterCallback;
import com.example.busservice2020.model.AvailableBus;
import com.example.busservice2020.model.DirectionResponse;
import com.example.busservice2020.model.ModelParcel;
import com.example.busservice2020.model.ModelPickupRequest;
import com.example.busservice2020.model.OverviewPolyline;
import com.example.busservice2020.model.UserModel;
import com.example.busservice2020.model_distancematrix.DistanceResponse;
import com.example.busservice2020.model_distancematrix.Element;
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
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
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
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//todo drow polyline from start to destination location has some issues.must recheck.
public class HomeActivity extends AppCompatActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener,
        AdapterCallback {

    private static final String TAG = "HomeActivity";
    private static final int STARTLOCATION_REQUEST_CODE = 0;
    private static final int DESTINATIONLOCATION_REQUEST_CODE = 1;
    private static boolean callfornearbybus = false;
    private ActivityHomeBinding binding;
    private String username;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference registeredBuses;

    private Toolbar toolbar;
    private TextView toolbarTitle;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    private TextView headerName;
    private ImageView headerPic;

    private GoogleMap mMap;
    private static final float DEFAULT_ZOOM = 17f;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private Location mLastLocation;
    private LocationCallback locationCallback;

    private PlacesClient placesClient;
    private Place place;
    private Marker startMarker, destinationMarker;
    private String destinationPlaceName;
    private ViewmodelDirectionApi viewmodelDirectionApi;

    HashMap<String, GeoLocation> busList = new HashMap<>();
    HashMap<String, Marker> markerList = new HashMap<>();
    List<String> nearbyBusIdList = new ArrayList<>();
    List<AvailableBus> nearbyBusList;
    boolean isMapAlreadyCalled;

    private AdapterAvailableBus adapterAvailableBus;
    private Dialog dialog_pickuprequest;
    private ImageView qrcode;
    private ValueEventListener pickuprequestvalueEventListener;

    private Handler mainHandler = new Handler();
    private boolean isMapListIterating;

    private BottomSheetBehavior bottomSheetBehavior;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Log.d(TAG, "onCreate: called");
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        viewmodelDirectionApi = new ViewModelProvider.AndroidViewModelFactory(getApplication()).create(ViewmodelDirectionApi.class);


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

        bottomSheetBehavior = BottomSheetBehavior.from(binding.homeAppbar.bottomsheetContainer);
        binding.homeAppbar.bottomsheetContainer.setVisibility(View.INVISIBLE); //todo temporary INVISIBLE.remove when testing done
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

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        Thread GetLastLocationThread = new Thread(new GetLocationRunable(fusedLocationClient));
        GetLastLocationThread.start();

        binding.homeAppbar.mapcontainer.iconCentermap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (markerList.size() == 0) {
                    moveCamera(mLastLocation, "iconCentermap");
                } else {
                    fitMapForAllMArkers(markerList);
                }
            }
        });

        binding.homeAppbar.mapcontainer.iconDestination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDestinationAdjustNote();
            }
        });

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    //Log.d(TAG, "Location Updated.line 197 ");
                    mLastLocation = location;
                    if (callfornearbybus) {
                        getNearbyBuses(startMarker == null ? new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()) : startMarker.getPosition());
                    }
                }
            }
        };

        registeredBuses = FirebaseDatabase.getInstance().getReference("registeredbuses");
        nearbyBusList = new ArrayList<>();
        adapterAvailableBus = new AdapterAvailableBus(getApplicationContext(), nearbyBusList, this);
        binding.homeAppbar.recBuslist.setLayoutManager(new LinearLayoutManager(this));
        binding.homeAppbar.recBuslist.setAdapter(adapterAvailableBus);


    }//end of onCreate

    @Override
    public void onMapReady(GoogleMap googleMap) {
        //Log.d(TAG, "onMapReady: called");
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        //LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)){}

        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM));
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);


        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                //Log.d(TAG, "onMarkerDragStart: dragging parker");
            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                destinationMarker.setPosition(marker.getPosition());
                //Log.d(TAG, "onMarkerDragEnd: " + destinationMArker.getId());
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
                                    .icon(getBitmapDescriptor(getResources().getDrawable(R.drawable.ic_startmarker, null)))//BitmapDescriptorFactory.fromResource(R.drawable.ic_startmarker)
                                    .title(place.getName()));
                        } else {
                            startMarker.setPosition(place.getLatLng());
                        }
                        moveCamera(place.getLatLng(), "onActivityResult");

                        Log.i(TAG, "Place: " + place.getName() + ", " + place.getId() + " markerid=" + startMarker.getId());
                        break;
                    case AutocompleteActivity.RESULT_ERROR:
                        // TODO: Handle the error.
                        Status status = Autocomplete.getStatusFromIntent(data);
                        //Log.d(TAG, "onActivityResult:RESULT_ERROR " + status.getStatusMessage());
                        break;
                    case RESULT_CANCELED:
                        //Log.d(TAG, "onActivityResult: user cancled.");
                        break;
                }
                break;
            case DESTINATIONLOCATION_REQUEST_CODE:
                switch (resultCode) {
                    case RESULT_OK:
                        place = Autocomplete.getPlaceFromIntent(data);
                        destinationPlaceName = place.getName();
                        binding.homeAppbar.searchDestinationLocation.setText(place.getName());

                        if (destinationMarker == null) {
                            destinationMarker = mMap.addMarker(new MarkerOptions()
                                    .position(place.getLatLng())
                                    .draggable(true)
                                    .icon(getBitmapDescriptor(getResources().getDrawable(R.drawable.ic_destinationmarker, null)))
                                    .title("Destination:" + place.getName()));
                        } else {
                            destinationMarker.setPosition(place.getLatLng());
                        }
                        binding.homeAppbar.mapcontainer.iconDestination.setVisibility(View.VISIBLE);
                        showDestinationAdjustNote();
                        //moveCamera(place.getLatLng(), "onActivityResult");
                        getNearbyBuses(startMarker == null ? new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()) : startMarker.getPosition());
                        callfornearbybus = true;
                        setMargins(binding.homeAppbar.mapcontainer.mapcontainer, 0, 8, 0, 150);
                        binding.homeAppbar.bottomsheetContainer.setVisibility(View.VISIBLE);

                        Log.i(TAG, "Place: " + place.getId() + ", " + place.getLatLng());
                        break;
                    case AutocompleteActivity.RESULT_ERROR:
                        Status status = Autocomplete.getStatusFromIntent(data);
                        //Log.d(TAG, "onActivityResult:RESULT_ERROR: " + status.getStatusMessage());
                        break;
                    case RESULT_CANCELED:
                        //Log.d(TAG, "onActivityResult:RESULT_CANCELED user cancled.");
                        break;
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void getNearbyBuses(LatLng latLng) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("availableBuses");
        GeoFire geoFire = new GeoFire(reference);

        //todo remove geoQuery listener when done.
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(latLng.latitude, latLng.longitude), 2);

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                //Log.d(TAG, "onKeyEntered: called");
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
                //Log.d(TAG, "onGeoQueryReady: called. And busList size = " + busList.size());
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        updateMarkerList(busList);
                        nearbyBusIdList.clear();
                        nearbyBusIdList.addAll(busList.keySet());

                        busList.clear();
                    }
                });

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                //Log.d(TAG, "onGeoQueryError: " + error.getMessage());
            }
        });

    }

    private void updateMarkerList(HashMap<String, GeoLocation> buslist) {
        //Log.d(TAG, "updateMarkerList: Caller Thread: " + Thread.currentThread().getName());
        //Log.d(TAG, "updateMarkerList: called with buslist of size:" + buslist.size() + " And markerlist of size:" + markerList.size());

        List<String> removeMarker = new ArrayList<>();
        if (!markerList.isEmpty()) {  // if there is no marker then no need to iterate otherwise update marker list
            for (Map.Entry me : markerList.entrySet()) {
                if (buslist.isEmpty()) {
                    removeMarker.add((String) me.getKey());
                } else {
                    if (!buslist.containsKey(me.getKey())) {
                        removeMarker.add((String) me.getKey());
                    }
                }
            }

            for (String key : removeMarker) {
                markerList.get(key).remove();    //remove marker from map
                markerList.remove(key);          //remove marker from markerlist
            }

        }

        if (!buslist.isEmpty()) {
            for (Map.Entry me : buslist.entrySet()) {
                updateMarkerLocation(me.getKey().toString(), (GeoLocation) me.getValue());
            }
        } else {
            nearbyBusList.clear();
            adapterAvailableBus.notifyDataSetChanged();
        }
    }

    private void updateMarkerLocation(String key, GeoLocation location) {
        //Log.d(TAG, "updateMarkerLocation:called for marker:" + key);
        //Log.d(TAG, "updateMarkerLocation: marketlist =>" + markerList);

        if (!markerList.containsKey(key)) {
            //Log.d(TAG, "updateMarkerLocation: adding marker:" + key);
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(location.latitude, location.longitude))
                    .draggable(true)
                    .icon(getBitmapDescriptor(getResources().getDrawable(R.drawable.ic_marker_bus, null)))
                    .title(key));
            markerList.put(key, marker);

        } else {
            //Log.d(TAG, "updateMarkerLocation: repositioning marker:" + key);
            markerList.get(key).setPosition(new LatLng(location.latitude, location.longitude));
        }

        updateNearbybusList();
    }

    private void updateNearbybusList() {
        Log.d(TAG, "onDataChange: nearbyBusList size:" + nearbyBusList.size() + "nearbyBusIDList size:" + nearbyBusIdList.size());

        registeredBuses.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                nearbyBusList.clear();
                int position = 0;
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    AvailableBus availableBus = ds.getValue(AvailableBus.class); //basically i am gettting all registered buses and filtering out the nearby ones
                    if (nearbyBusIdList.contains(availableBus.getBusid())) {
                        nearbyBusList.add(availableBus);
                        markerList.get(availableBus.getBusid()).setTitle(availableBus.getCompanyname());
                        getDistanceInformation(markerList.get(availableBus.getBusid()).getPosition(), mLastLocation, position);
                        position++;
                    }
                    adapterAvailableBus.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void getDistanceInformation(LatLng destinationLatLang, Location OriginLocation, final int position) {
        try {
            String lat2 = String.valueOf(destinationLatLang.latitude);
            String long2 = String.valueOf(destinationLatLang.longitude);

            String lat1 = String.valueOf(OriginLocation.getLatitude());
            String long1 = String.valueOf(OriginLocation.getLongitude());

            Map<String, String> mapQuery = new HashMap<>();
            mapQuery.put("units", "imperial");
            mapQuery.put("origins", lat1 + "," + long1);
            mapQuery.put("destinations", lat2 + "," + long2);
            mapQuery.put("key", "AIzaSyCdP8QSuapjIn5DZEfWXG5EH6EIiYb6uuY");

            viewmodelDirectionApi.getDistanceResponse(mapQuery).observe(this, new Observer<DistanceResponse>() {
                @Override
                public void onChanged(DistanceResponse distanceResponse) {
                    if (distanceResponse != null) {
                        final Element element = distanceResponse.getRows().get(0).getElements().get(0);
                        nearbyBusList.get(position).setRem_time_distance(element.getDuration().getText() + " . " + element.getDistance().getText());
                        adapterAvailableBus.notifyDataSetChanged();

                        Log.d(TAG, "getDistanceResponse: " + element.getDuration().getText() + "   &   " + element.getDistance().getValue() + "");
                    } else {
                        Log.d(TAG, "getDistanceResponse: null");
                    }
                }
            });
        } catch (Exception e) {
            Log.d(TAG, "getDistanceInformation: exception:" + e.getMessage());
        }
    }

    /*private void getLastLocation(FusedLocationProviderClient flpc) {
        Log.d(TAG, "getLastLocation: called");
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        flpc.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location == null) {
                            //todo handle location object when it is null
                            Log.d(TAG, "onSuccess: location is null");
                        } else {
                            mLastLocation = location;



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
    }*/

    private LocationRequest createLocationRequest() {
        //Log.d(TAG, "createLocationRequest: called");
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    private void startLocationUpdates(LocationRequest locationRequest) {
        //Log.d(TAG, "startLocationUpdates: called");
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());
    }

    //todo research on this
    private void stopLocationUpdate() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private void removeGeoQueryListener(GeoQuery geoQuery) {
        geoQuery.removeAllListeners();
    }

    private void initPlacesAPI() {
        try {
            if (!Places.isInitialized()) {
                Places.initialize(getApplicationContext(), "AIzaSyCdP8QSuapjIn5DZEfWXG5EH6EIiYb6uuY");
            }
            placesClient = Places.createClient(this);
        } catch (Exception e) {
            //Log.d(TAG, "initPlacesAPI: error" + e.getMessage());
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


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.admin:
                Toast.makeText(this, "navigation admin item slelected", Toast.LENGTH_SHORT).show();
                break;
            case R.id.logout:
                firebaseAuth.signOut();
                updateCurrentUserName();
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

    private void updateNavHeader(final String userid) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("userlist").child(userid);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserModel user = dataSnapshot.getValue(UserModel.class);
                if (user != null) {
                    username = user.getName();
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

    private void fitMapForAllMArkers(HashMap<String, Marker> markers) {


        LatLngBounds.Builder b = new LatLngBounds.Builder();
        b.include(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
        for (Map.Entry me : markers.entrySet()) {
            Marker marker = (Marker) me.getValue();
            b.include(marker.getPosition());
        }
        LatLngBounds bounds = b.build();
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 200);
        mMap.animateCamera(cu);
    }

    public void setMargins(View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(l, t, r, b);
            v.requestLayout();
        }
    }

    /*private void updateToken(){
        FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        String refreshToken= FirebaseInstanceId.getInstance().getToken();
        Token token= new Token(refreshToken);
        FirebaseDatabase.getInstance().getReference("Tokens").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(token);
    }

    public void sendNotifications(String usertoken, String title, String message) {
        NewNotificationData data = new NewNotificationData(title, message);
        NotificationSender sender = new NotificationSender(data, usertoken);
       try{
           apiService.sendNotification(sender).enqueue(new Callback<Response>() {
               @Override
               public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                   if (response.code() == 200) {
                       if (response.body().success == 1) {
                           Log.d(TAG, "onResponse: Notification Sent ");

                       }else{
                           Log.d(TAG, "onResponse: Notification Sending failed");
                       }
                   }
               }

               @Override
               public void onFailure(Call<Response> call, Throwable t) {
                   Log.d(TAG, "onFailure: sendNotifications error:"+t.getMessage());
               }
           });
       }catch (Exception e){
           Log.d(TAG, "sendNotifications: errorrrrrrrrrrrrrrrrrr:"+e.getMessage());
       }
    }
*/
    /*private void putMarker(HashMap<String, GeoLocation> list) {

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


    }*/

    @Override
    public void onItemClickCallBack(String busid) {
         moveCamera(markerList.get(busid).getPosition(),"onItemClickCallBAck");
         markerList.get(busid).showInfoWindow();
    }

    @Override
    public void onPickUpRequestCallBack(final String busid) {
        final String userid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        dialog_pickuprequest = initPickupRequestDialog(HomeActivity.this);
        qrcode = dialog_pickuprequest.findViewById(R.id.qrcode);

        dialog_pickuprequest.findViewById(R.id.dialog_cancel_pickuprewuest).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("pickuprequest").child(busid).child(userid);
                reference.removeEventListener(pickuprequestvalueEventListener);

                reference.child("pickupstatus").setValue("pickupcanceled").addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        callfornearbybus = true;
                        dialog_pickuprequest.dismiss();
                        Log.d(TAG, "onSuccess: status updated");
                    }
                });
            }
        });

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("pickuprequest").child(busid).child(userid);
        ModelPickupRequest pickupRequest = new ModelPickupRequest(userid, username,
                mLastLocation.getLatitude() + "", mLastLocation.getLongitude() + "", "pickmeup",
                false, false);
        reference.setValue(pickupRequest).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                callfornearbybus = false;
                pickuprequestvalueEventListener = startListeningForAcceptance(busid, userid);
                dialog_pickuprequest.show();
            }
        });
    }

    private ValueEventListener startListeningForAcceptance(final String busid, final String userid) {
        final DatabaseReference pickuprequestRef = FirebaseDatabase.getInstance().getReference("pickuprequest").child(busid).child(userid);
        ValueEventListener requestvalueEventListener = pickuprequestRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ModelPickupRequest pickupRequest = snapshot.getValue(ModelPickupRequest.class);
                if (pickupRequest.isIspickuprequestRejected()) {
                    stopListeningToPickupRequest(pickuprequestRef, busid, userid);
                } else if (pickupRequest.isIsrequestAccepted()) {

                    stopListeningToPickupRequest(pickuprequestRef, busid, userid, 00);
                    ModelParcel percel = new ModelParcel(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()),
                            destinationMarker.getPosition(), userid, busid, destinationPlaceName);
                    startActivity(new Intent(HomeActivity.this, StartRideActivity.class).putExtra(getString(R.string.parcel), percel));
                    finish();

                    /*dialog_pickuprequest.findViewById(R.id.dialogphase1).setVisibility(View.GONE);
                    dialog_pickuprequest.findViewById(R.id.dialogphase2).setVisibility(View.VISIBLE);
                    generateBarCode(userid);*/
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return requestvalueEventListener;
    }

    private void stopListeningToPickupRequest(DatabaseReference ref, String busid, String userid) {
        ref.removeEventListener(pickuprequestvalueEventListener);
        callfornearbybus = true;
        dialog_pickuprequest.dismiss();

        /*DatabaseReference reference=FirebaseDatabase.getInstance().getReference("pickuprequest").child(busid).child(userid);
        reference.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "onSuccess: remove pickup request successfull0");
            }
        });*/
        Toast.makeText(this, "Pickup Request Cancled", Toast.LENGTH_SHORT).show();
    }

    private void stopListeningToPickupRequest(DatabaseReference ref, String busid, String userid, int nothing) {
        ref.removeEventListener(pickuprequestvalueEventListener);
        /*DatabaseReference reference=FirebaseDatabase.getInstance().getReference("pickuprequest").child(busid).child(userid);
        reference.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "onSuccess: pickup request accepted successfull1");
            }
        });*/
        Toast.makeText(this, "Pickup Request Accepted", Toast.LENGTH_SHORT).show();
    }

    private Dialog initPickupRequestDialog(Activity activity) {
        Dialog dialog = new Dialog(activity);
        dialog.setContentView(R.layout.dialog_pickuprequest);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return dialog;
    }

    private BitmapDescriptor getBitmapDescriptor(Drawable vectorDrawable) {

        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bm = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bm);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bm);
    }

    private void showDestinationAdjustNote() {
        //todo note not showing.
        moveCamera(destinationMarker.getPosition(), "iconDestination");
        binding.homeAppbar.mapcontainer.destnationAdjustNote.setVisibility(View.VISIBLE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                binding.homeAppbar.mapcontainer.destnationAdjustNote.setVisibility(View.GONE);
            }
        }, 4000);

    }

    private void updateCurrentUserName() {
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(getString(R.string.sharedPref_key), MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPref.edit();
        edit.putString("name", "");
        edit.commit();
    }

    private String getCurrentUserName() {
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(getString(R.string.sharedPref_key), MODE_PRIVATE);
        return sharedPref.getString("name", "");
    }

    // Runable Classes
    class GetLocationRunable implements Runnable {
        FusedLocationProviderClient fusedLocationProviderClient;

        public GetLocationRunable(FusedLocationProviderClient fusedLocationProviderClient) {
            this.fusedLocationProviderClient = fusedLocationProviderClient;
        }

        @Override
        public void run() {
            getLastLocation(fusedLocationProviderClient);
        }

        private void getLastLocation(FusedLocationProviderClient flpc) {
            Log.d(TAG, "getLastLocation: called");
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            flpc.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(final Location location) {
                            if (location == null) {
                                //todo handle location object when it is null
                                Log.d(TAG, "onSuccess: location is null");
                                getLastLocation(fusedLocationClient);
                            } else {
                                mainHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mLastLocation = location;
                                        moveCamera(location, "getLastLocation");
                                        binding.homeAppbar.marker2.setImageResource(R.drawable.dot_selected_blue);
                                        binding.homeAppbar.searchStartLocation.setText("Your Location");

                                        startLocationUpdates(createLocationRequest());
                                    }
                                });
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "onFailure: error=" + e.getMessage());
                        }
                    });
        }
    }


}//the End
