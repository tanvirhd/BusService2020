package com.example.busservice2020.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.busservice2020.databinding.ActivityProfileBinding;
import com.example.busservice2020.model.UserModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import static com.example.busservice2020.activity.LoginActivity.user;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";
    private ActivityProfileBinding binding;
    private DatabaseReference userRef;

    private static int PIC_IMAGE=1;
    private static int STORAGE_PERMISSION_CODE =11;
    private Uri pickedPhotoUri=null;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle("Profile");
        storageRef= FirebaseStorage.getInstance().getReference().child("UserProfilePicture");
        userRef= FirebaseDatabase.getInstance().getReference("userlist");
        downloadinfo(userRef);

        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();finish();
            }
        });

        binding.changePicUserProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
                   openGallery();
                }else{
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
                }
            }
        });

        binding.btnUpdateUserProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.btnUpdateUserProfile.setEnabled(false);
                final StorageReference imagePath=storageRef.child(pickedPhotoUri.getLastPathSegment());
                try{
                    imagePath.putFile(pickedPhotoUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Log.d(TAG,"Upload Complete");
                            getDownloadUri(imagePath);
                        }
                    });
                }catch (Exception e){
                    Log.d(TAG, "onClick: error:"+e.getMessage());
                }
            }
        });
    }//end of onCreate

    private void getDownloadUri(StorageReference mStorage){
        Log.d(TAG,"getDownloadUrl Called");
        mStorage.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                setDownloadUri(uri);
            }
        });
    }

    private void setDownloadUri(Uri uri){
        Log.d(TAG,"setDownloadUri Called");
        userRef.child(FirebaseAuth.getInstance().getUid()).child("imageURL").setValue(uri.toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(ProfileActivity.this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                binding.btnUpdateUserProfile.setEnabled(false);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                binding.btnUpdateUserProfile.setEnabled(true);
                Toast.makeText(ProfileActivity.this, "Profile Update Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==STORAGE_PERMISSION_CODE){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                openGallery();
            }else {
                Toast.makeText(this, "Storage Permission is required.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==PIC_IMAGE && resultCode== RESULT_OK&& data!= null){
            pickedPhotoUri=data.getData();
            binding.picUserProfile.setImageURI(pickedPhotoUri);
            binding.btnUpdateUserProfile.setEnabled(true);
        }else {
            Toast.makeText(ProfileActivity.this, "Something Went Wrong", Toast.LENGTH_SHORT).show();
        }
    }

    private void downloadinfo(DatabaseReference ref){
        ref.child(FirebaseAuth.getInstance().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    UserModel user=snapshot.getValue(UserModel.class);
                    binding.useremailUserProfile.setText(user.getEmail());
                    binding.usernumberUserProfile.setText(user.getPhnNumber());
                    binding.usernameUserProfile.setText(user.getName());
                    try {
                        Glide.with(getApplicationContext()).load(user.getImageURL()).into(binding.picUserProfile);
                        Log.d(TAG, "updateUI: downloading pro picyure");
                    }catch (Exception e){
                        Log.d(TAG, "image load error: "+e.getMessage());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void openGallery(){
        Intent galleryIntent=new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,PIC_IMAGE);
    }
}