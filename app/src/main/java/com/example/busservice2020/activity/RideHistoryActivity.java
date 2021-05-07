package com.example.busservice2020.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.example.busservice2020.R;
import com.example.busservice2020.databinding.ActivityRideHistoryBinding;

public class RideHistoryActivity extends AppCompatActivity {
    private static final String TAG = "RideHistoryActivity";
    private ActivityRideHistoryBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityRideHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


    }
}