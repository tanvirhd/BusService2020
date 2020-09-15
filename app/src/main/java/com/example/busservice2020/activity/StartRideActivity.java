package com.example.busservice2020.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.busservice2020.R;
import com.example.busservice2020.databinding.ActivityStartRideBinding;
import com.example.busservice2020.model.AvailableBus;
import com.example.busservice2020.model.DirectionResponse;
import com.example.busservice2020.model.ModelBus;
import com.example.busservice2020.model.ModelParcel;
import com.example.busservice2020.model.OverviewPolyline;
import com.example.busservice2020.viewmodel.ViewmodelDirectionApi;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.PolyUtil;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StartRideActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "StartRideActivity";
    private ActivityStartRideBinding binding;
    private static final float DEFAULT_ZOOM = 17f;
    private ModelParcel parcel;
    private GoogleMap mMap;
    private Marker startmarker, destinationmarker, busmarker;
    private ValueEventListener buslocationListener;
    private DatabaseReference busRef,businfoRef;

    private ViewmodelDirectionApi viewmodelDirectionApi;
    private List<LatLng> polylineLatLngList;
    private Polyline polyline;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStartRideBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewmodelDirectionApi = new ViewModelProvider.AndroidViewModelFactory(getApplication()).create(ViewmodelDirectionApi.class);
        parcel = getIntent().getParcelableExtra(getResources().getString(R.string.parcel));
        busRef = FirebaseDatabase.getInstance().getReference("availableBuses").child(parcel.getBusId()).child("l");
        businfoRef=FirebaseDatabase.getInstance().getReference("registeredbuses").child(parcel.getBusId());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_startride_activity);
        mapFragment.getMapAsync(this);
        binding.iconCentermap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMap != null && destinationmarker!=null && startmarker!=null && busmarker!=null) {
                    fitMapForAllMArkers(startmarker,busmarker);
                }
            }
        });

        binding.iconDestination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMap != null && destinationmarker!=null && startmarker!=null && busmarker!=null) {
                    fitMapForAllMArkers(startmarker,destinationmarker,busmarker);
                }
            }
        });

        binding.tvCancelpickup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                busRef.removeEventListener(buslocationListener);
                startActivity(new Intent(StartRideActivity.this,HomeActivity.class));
                finish();
            }
        });

        businfoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    ModelBus bus=snapshot.getValue(ModelBus.class);
                    binding.busname.setText(bus.getCompanyname());
                    binding.busLicence.setText(bus.getLicense());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        generateBarCode(parcel.getUserId());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }


        mMap = googleMap;
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        startmarker = mMap.addMarker(new MarkerOptions()
                .position(parcel.getStartlocation())
                .icon(getBitmapDescriptor(getResources().getDrawable(R.drawable.ic_startmarker, null)))
                .title("Pickup Location"));
        destinationmarker = mMap.addMarker(new MarkerOptions()
                .position(parcel.getDestinationlication())
                .icon(getBitmapDescriptor(getResources().getDrawable(R.drawable.ic_destinationmarker, null)))
                .title(parcel.getDestinationPlaceName()));
        startmarker.showInfoWindow();

        moveCamera(startmarker.getPosition(), "onMapReady");

        buslocationListener = busRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    List<Object> map = (List<Object>) snapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    if (map.get(0) != null) {
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if (map.get(1) != null) {
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    LatLng buslatlng = new LatLng(locationLat, locationLng);

                    if (busmarker != null) {
                        busmarker.setPosition(buslatlng);
                        getDirectionResponse(busmarker,startmarker);
                    } else {
                        busmarker = mMap.addMarker(new MarkerOptions()
                                .position(buslatlng)
                                .icon(getBitmapDescriptor(getResources().getDrawable(R.drawable.ic_marker_bus, null)))
                                .title("bus"));
                        getDirectionResponse(busmarker,startmarker);
                    }

                    fitMapForAllMArkers(startmarker,busmarker);startmarker.showInfoWindow();

                } else {
                    Toast.makeText(StartRideActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

      /*  Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                fitMapForAllMArkers(startmarker, destinationmarker, null, mMap);
            }
        }, 4000);*/

    }


    private void fitMapForAllMArkers(Marker s, Marker b) {
        LatLngBounds.Builder latlangBoundBuilder = new LatLngBounds.Builder();
        latlangBoundBuilder.include(s.getPosition());
        latlangBoundBuilder.include(b.getPosition());
        LatLngBounds bounds = latlangBoundBuilder.build();
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 150);
        mMap.animateCamera(cu);
    }

    private void fitMapForAllMArkers(Marker s, Marker d, Marker b) {
        LatLngBounds.Builder latlangBoundBuilder = new LatLngBounds.Builder();
        latlangBoundBuilder.include(s.getPosition());
        latlangBoundBuilder.include(d.getPosition());
        latlangBoundBuilder.include(b.getPosition());
        LatLngBounds bounds = latlangBoundBuilder.build();
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 150);
        mMap.animateCamera(cu);
    }

    private BitmapDescriptor getBitmapDescriptor(Drawable vectorDrawable) {
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bm = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bm);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bm);
    }

    public void moveCamera(Location location, String caller) {
        Log.d(TAG, "moveCamera: called by " + caller);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), DEFAULT_ZOOM));
    }

    public void moveCamera(LatLng latLng, String caller) {
        Log.d(TAG, "moveCamera: called by " + caller);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
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

                polyline=mMap.addPolyline(new PolylineOptions().color(R.color.pilyline).geodesic(true).add(
                        new LatLng(origin.latitude, origin.longitude),
                        new LatLng(destination.latitude, destination.longitude)).width(12));
            }

        } else {
            Log.d(TAG, "drawPolyLine: overviewPolyline=null");
        }
    }

    private void generateBarCode(String userid){
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(userid, BarcodeFormat.QR_CODE,200,200);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap qr_bitmap = barcodeEncoder.createBitmap(bitMatrix);
            binding.ticketQr.setImageBitmap(qr_bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }
}