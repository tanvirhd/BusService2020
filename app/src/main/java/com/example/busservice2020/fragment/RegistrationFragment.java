package com.example.busservice2020.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.LongDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.example.busservice2020.R;
import com.example.busservice2020.activity.HomeActivity;
import com.example.busservice2020.databinding.FragmentRegistrationBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import static android.app.Activity.RESULT_OK;
import static com.example.busservice2020.activity.LoginActivity.user;
import static com.example.busservice2020.fragment.LoginFragment.mAuth;

public class RegistrationFragment extends Fragment {
    private static String TAG="RegistrationFragment";
    private FragmentRegistrationBinding fragmentRegistrationBinding;

    static int PIC_IMAGE=1;
    Uri pickedPhotoUri=null;
    StorageReference storageRef;

    public RegistrationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        storageRef= FirebaseStorage.getInstance().getReference().child("UserProfilePicture");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentRegistrationBinding=FragmentRegistrationBinding.inflate(inflater,container,false);
        View view=fragmentRegistrationBinding.getRoot();

        ((AppCompatActivity)getActivity()).getSupportActionBar().show();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Registration");
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeAsUpIndicator(R.drawable.back_arrow);

        fragmentRegistrationBinding.userPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        fragmentRegistrationBinding.btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user.setName(fragmentRegistrationBinding.etUserName.getText().toString());
                user.setEmail(fragmentRegistrationBinding.etUseremail.getText().toString());

                if(fragmentRegistrationBinding.etUserName.getText().toString().equals("") ||
                    fragmentRegistrationBinding.etUseremail.getText().toString().equals("")){
                    fragmentRegistrationBinding.warningSms.setText("* Please fillup all data");
                    fragmentRegistrationBinding.warningSms.setVisibility(View.VISIBLE);
                }else{
                  if(pickedPhotoUri==null){
                      fragmentRegistrationBinding.warningSms.setVisibility(View.VISIBLE);
                      fragmentRegistrationBinding.warningSms.setText("* Select a picture");
                  }else {
                      hideKeyboard(getActivity());
                      fragmentRegistrationBinding.btnContinue.setText("Please wait...");
                      fragmentRegistrationBinding.btnContinue.setClickable(false);

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
                }
            }
        });

        return view;
    }

    private void getDownloadUri(StorageReference mStorage){
        Log.d(TAG,"getDownloadUrl Called");
        mStorage.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                setDownloadUri(uri);
                uploadUserInformation(FirebaseDatabase.getInstance().getReference("userlist"));
            }
        });
    }

    private void setDownloadUri(Uri uri){
        Log.d(TAG,"setDownloadUri Called");
        user.setImageURL(uri.toString());
        //photoDownloadUriString=uri.toString();
        //userNew.setUserProfiePictureUri(photoDownloadUriString);
    }

    private void uploadUserInformation(DatabaseReference mRef){
        Log.d(TAG,"uploadUserInformation Called");
        Log.d(TAG,"=> "+ user.getImageURL());
        mRef.child(mAuth.getUid()).setValue(user);

        startActivity(new Intent(getActivity(), HomeActivity.class));getActivity().finish();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        Log.d(TAG, "onAttach: called");
        super.onAttach(context);
    }

    private void openGallery(){
        Intent galleryIntent=new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,PIC_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==PIC_IMAGE && resultCode== RESULT_OK&& data!= null){
            pickedPhotoUri=data.getData();
            fragmentRegistrationBinding.userPhoto.setImageURI(pickedPhotoUri);
            user.setImageURL(pickedPhotoUri.toString());

            Log.d(TAG, "uri:"+pickedPhotoUri+"\n"+" uri string:"+pickedPhotoUri.toString());
        }
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}
