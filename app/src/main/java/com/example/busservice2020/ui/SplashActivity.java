package com.example.busservice2020.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.busservice2020.activity.HomeActivity;
import com.example.busservice2020.activity.LoginActivity;
import com.example.busservice2020.interfaces.Fragment_Communication;
import com.example.busservice2020.model.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "SplashActivity";
    FirebaseUser firebaseUser;
    FirebaseAuth firebaseAuth;
    Fragment_Communication fragment_communication;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        Log.d(TAG, "ddd" + firebaseUser);

        if (firebaseUser != null) {
            addUser();
        } else {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    //ToDo check current user status
    private void addUser() {
//        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("userlist");
//        UserModel user = new UserModel("", "", "", "");
//        reference.child("root_user_id").setValue(user);
        String userid = firebaseUser.getUid();
        Log.d(TAG, "userid:" + userid);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("userlist").child(userid);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "@ " + dataSnapshot.getValue());
                if (dataSnapshot.getValue() == null) {
                    Log.d(TAG, "onDataChange: New User");
                    //test
                    Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();

                } else {
                    startActivity(new Intent(SplashActivity.this, HomeActivity.class));
                    finish();
                    Log.d(TAG, "onDataChange: Registered User");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: database error" + databaseError.getMessage());
            }
        });
    }
}