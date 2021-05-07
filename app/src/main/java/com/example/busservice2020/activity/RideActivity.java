package com.example.busservice2020.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.busservice2020.R;
import com.example.busservice2020.databinding.ActivityRideBinding;
import com.example.busservice2020.model.DirectionResponse;
import com.example.busservice2020.model.ModelHistory;
import com.example.busservice2020.model.ModelParcel;
import com.example.busservice2020.model.OverviewPolyline;
import com.example.busservice2020.model_distancematrix.DistanceResponse;
import com.example.busservice2020.model_distancematrix.Element;
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
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.PolyUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RideActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "RideActivity";
    private ActivityRideBinding binding;
    private ModelParcel parcel;
    private GoogleMap mMap;
    private Marker startmarker, destinationmarker, busmarker;
    private ValueEventListener buslocationListener;
    private DatabaseReference busRef,businfoRef,pickuprequestRef;
    private ViewmodelDirectionApi viewmodelDirectionApi;
    private List<LatLng> polylineLatLngList;
    private int distanseMeasure=0;
    private Dialog dialog_finish_ride;
    private DatabaseReference historyRef;
    public static  String RIDETIMEDATE;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityRideBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        RIDETIMEDATE =getCurrentTime()+" "+getCurrentDate();

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle("Riding");


        parcel = getIntent().getParcelableExtra(getResources().getString(R.string.parcel));
        historyRef=FirebaseDatabase.getInstance().getReference("history");
        busRef = FirebaseDatabase.getInstance().getReference("availableBuses").child(parcel.getBusId()).child("l");
        businfoRef=FirebaseDatabase.getInstance().getReference("registeredbuses").child(parcel.getBusId());
        pickuprequestRef=FirebaseDatabase.getInstance().getReference("pickuprequest");
        viewmodelDirectionApi = new ViewModelProvider.AndroidViewModelFactory(getApplication()).create(ViewmodelDirectionApi.class);

        dialog_finish_ride=initDialog(RideActivity.this);
        dialog_finish_ride.findViewById(R.id.finishRide).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_finish_ride.dismiss();
                busRef.removeEventListener(buslocationListener);
                pickuprequestRef.child(parcel.getBusId()).child(parcel.getUserId()).child("pickupstatus").setValue("finished")
                        .addOnSuccessListener(RideActivity.this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        new Thread(new UpdateHistory(historyRef,parcel,StartRideActivity.BUSNAME,StartRideActivity.BUSLICENSE,RideActivity.RIDETIMEDATE)).run();
                    }
                });


                startActivity(new Intent(RideActivity.this,HomeActivity.class));
                finish();
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_startride_activity);
        mapFragment.getMapAsync(this);
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
                .title("Star Location"));
        destinationmarker = mMap.addMarker(new MarkerOptions()
                .position(parcel.getDestinationlication())
                .icon(getBitmapDescriptor(getResources().getDrawable(R.drawable.ic_destinationmarker, null)))
                .title(parcel.getDestinationPlaceName()));
        startmarker.showInfoWindow();

        getDirectionResponse(startmarker,destinationmarker);

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
                        getDistanceInformation(busmarker.getPosition(),destinationmarker.getPosition());
                    } else {
                        busmarker = mMap.addMarker(new MarkerOptions()
                                .position(buslatlng)
                                .icon(getBitmapDescriptor(getResources().getDrawable(R.drawable.ic_marker_bus, null)))
                                .title("bus"));
                        getDistanceInformation(busmarker.getPosition(),destinationmarker.getPosition());
                    }

                    if(distanseMeasure!=0 &&distanseMeasure<=500){
                        dialog_finish_ride.show();
                    }

                    fitMapForAllMArkers(startmarker,destinationmarker);startmarker.showInfoWindow();

                } else {
                    Toast.makeText(RideActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private BitmapDescriptor getBitmapDescriptor(Drawable vectorDrawable) {
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bm = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bm);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bm);
    }

    private void fitMapForAllMArkers(Marker s, Marker b) {
        LatLngBounds.Builder latlangBoundBuilder = new LatLngBounds.Builder();
        latlangBoundBuilder.include(s.getPosition());
        latlangBoundBuilder.include(b.getPosition());
        LatLngBounds bounds = latlangBoundBuilder.build();
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 200);
        mMap.animateCamera(cu);
    }

    public void getDistanceInformation(LatLng destinationLatLang, LatLng OriginLatLang){
        final Element[] element = new Element[1];
        Log.d(TAG, "getDistanceInformation: called from HomeActivity");
        try{
            String lat2= String.valueOf(destinationLatLang.latitude);
            String long2= String.valueOf(destinationLatLang.longitude);

            String lat1= String.valueOf(OriginLatLang.latitude);
            String long1= String.valueOf(OriginLatLang.longitude);

            Map<String, String> mapQuery = new HashMap<>();
            mapQuery.put("units", "imperial");
            mapQuery.put("origins", lat1+","+long1);
            mapQuery.put("destinations", lat2+","+long2);
            mapQuery.put("key","AIzaSyCdP8QSuapjIn5DZEfWXG5EH6EIiYb6uuY");

            viewmodelDirectionApi.getDistanceResponse(mapQuery).observe(this, new Observer<DistanceResponse>() {
                @Override
                public void onChanged(DistanceResponse distanceResponse) {
                    if(distanceResponse!=null){
                        element[0] =distanceResponse.getRows().get(0).getElements().get(0);
                        //binding.tvAwayMinutes.setText(element[0].getDuration().getText()+", ");
                        //binding.tvAwayKilo.setText(element[0].getDistance().getText()+"");
                        distanseMeasure=element[0].getDistance().getValue();

                        Log.d(TAG, "data paisi: "+ element[0].getDuration().getText()+"   &   "+ element[0].getDistance().getValue()+"");
                    }else {
                        Log.d(TAG, "data paisi:null");
                    }
                }
            });
        }catch (Exception e){
            Log.d(TAG, "getDistanceInformation: exception:"+e.getMessage());
        }
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

    private Dialog initDialog(Activity activity) {
        Dialog dialog = new Dialog(activity);
        dialog.setContentView(R.layout.dialog_reached_destination);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return dialog;
    }

    private String getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        return dateFormat.format(calendar.getTime());
    }

    private String getCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        DateFormat dateFormat = new SimpleDateFormat("hh:mm aa");
        return dateFormat.format(calendar.getTime());
    }


    class UpdateHistory implements Runnable{
        DatabaseReference ref;
        ModelParcel parcel;
        String busname,buslicense,timedate;

        public UpdateHistory(DatabaseReference ref, ModelParcel parcel, String busname, String buslicense, String timedate) {
            this.ref = ref;
            this.parcel = parcel;
            this.busname = busname;
            this.buslicense = buslicense;
            this.timedate = timedate;
        }

        @Override
        public void run() {
            String key=ref.push().getKey();
            ModelHistory history=new ModelHistory(parcel.getStartlocation().latitude,parcel.getStartlocation().longitude,
                    parcel.getUserId(),parcel.getDestinationPlaceName(),busname,timedate,buslicense);

            ref.child(parcel.getUserId()).child(key).setValue(history).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d("History Runable", "onSuccess: history added to db");
                }
            });

        }
    }

}