package com.example.busservice2020.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.busservice2020.activity.LoginActivity;
import com.example.busservice2020.model.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG="SplashActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //addUser();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    //ToDo check current user status
    private void addUser(){
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("userlist");
        UserModel user=new UserModel("","","","");
        reference.child("root_user_id").setValue(user);
    }
}