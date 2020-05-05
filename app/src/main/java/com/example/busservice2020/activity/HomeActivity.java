package com.example.busservice2020.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.busservice2020.R;
import com.example.busservice2020.databinding.ActivityHomeBinding;
import com.example.busservice2020.model.UserModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {
    private static final String TAG = "HomeActivity";

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private ActivityHomeBinding binding;
    private GoogleMap mMap;

    Toolbar toolbar;
    DrawerLayout drawer;
    NavigationView navigationView;
    TextView headerName;
    ImageView headerPic;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: called");
        super.onCreate(savedInstanceState);
        binding=ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        toolbar = findViewById(R.id.home_toolbar);
        drawer = findViewById(R.id.home_drwerlayout);
        navigationView = findViewById(R.id.home_nav);

        View header=navigationView.getHeaderView(0);
        headerPic=header.findViewById(R.id.header_photo);
        headerName=header.findViewById(R.id.header_name);

        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.black,null));
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //todo etar kaj ki??
        /*if (savedInstanceState == null) {
            navigationView.setCheckedItem(R.id.admin);

        }*/

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        updateNavHeader(firebaseUser.getUid());

        navigationView.setNavigationItemSelectedListener(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }//end of onCreate

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

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

    }


}
