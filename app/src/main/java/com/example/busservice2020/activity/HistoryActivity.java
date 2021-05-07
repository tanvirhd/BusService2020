package com.example.busservice2020.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.busservice2020.R;
import com.example.busservice2020.adapter.AdapterHistory;
import com.example.busservice2020.databinding.ActivityHistoryBinding;
import com.example.busservice2020.model.ModelHistory;
import com.example.busservice2020.model.ModelHistoryRyc;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {
    private static final String TAG = "HistoryActivity";
    private ActivityHistoryBinding binding;

    private List<ModelHistoryRyc> historyRycList;
    private AdapterHistory adapterHistory;
    DatabaseReference historyref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        historyref= FirebaseDatabase.getInstance().getReference("history");

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle("Ride History");

        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();finish();
            }
        });


        historyRycList=new ArrayList<>();
        adapterHistory=new AdapterHistory(getApplicationContext(),historyRycList);
        binding.recycridehistory.setAdapter(adapterHistory);
        binding.recycridehistory.setLayoutManager(new LinearLayoutManager(this));

        loadhistory();

        binding.swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadhistory();
            }
        });
    }

    private void loadhistory(){
        binding.swipe.setRefreshing(true);
        historyref.child(FirebaseAuth.getInstance().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    historyRycList.clear();
                    for(DataSnapshot ds:snapshot.getChildren()){
                        ModelHistory history=ds.getValue(ModelHistory.class);
                        ModelHistoryRyc historyRyc=convertHistory(history);
                        historyRycList.add(historyRyc);
                    }
                    adapterHistory.notifyDataSetChanged();
                }
                binding.swipe.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private ModelHistoryRyc convertHistory(ModelHistory history){
        ModelHistoryRyc historyRyc=new ModelHistoryRyc();

        historyRyc.setBusname(history.getBusname());
        historyRyc.setBuslicense(history.getBuslicense());
        historyRyc.setDate(history.getDate());
        historyRyc.setDestinationPlaceName(history.getDestinationPlaceName());

        List<String> pickupdaaress=getAddress(new LatLng(history.getStartLat(),history.getStartlang()));
        Log.d(TAG, "convertHistory: address:"+pickupdaaress.get(0)+pickupdaaress.get(1)+pickupdaaress.get(2)+pickupdaaress.get(3)+pickupdaaress.get(4)+pickupdaaress.get(5));

        historyRyc.setPickuplocationname(pickupdaaress.get(0));

        return historyRyc;
    }

    private List<String> getAddress(LatLng latLng){
        Geocoder geocoder;

        List<Address> addresses = null;
        geocoder = new Geocoder(this, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(latLng.latitude,latLng.longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String address = null;
        String city = null;
        String state = null;
        String country = null;
        String postalCode = null;
        String knownName = null;

        if (addresses != null) {

            address = addresses.get(0).getAddressLine(0);
            city = addresses.get(0).getLocality();
            state = addresses.get(0).getSubAdminArea();
            country = addresses.get(0).getCountryName();
            postalCode = addresses.get(0).getPostalCode();
            knownName = addresses.get(0).getFeatureName();

        }


        List<String> list =new ArrayList<>();
        list.add(0,address);
        list.add(1,city);
        list.add(2,state);
        list.add(3,country);
        list.add(4,postalCode);
        list.add(5,knownName);

        return list;
    }
}