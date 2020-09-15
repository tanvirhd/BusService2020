package com.example.busservice2020.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.busservice2020.R;
import com.example.busservice2020.databinding.ActivityLoginBinding;
import com.example.busservice2020.fragment.RegistrationFragment;
import com.example.busservice2020.fragment.VerificationFragment;
import com.example.busservice2020.interfaces.F2F_Commuication;
import com.example.busservice2020.interfaces.Fragment_Communication;
import com.example.busservice2020.fragment.LoginFragment;
import com.example.busservice2020.model.UserModel;

public class LoginActivity extends AppCompatActivity implements Fragment_Communication , F2F_Commuication {
    private static final String TAG="LoginActivity";

    private ActivityLoginBinding binding;
    Toolbar toolbar;

    VerificationFragment verificationFragment;
    static int REQUESTCODE=2;
    public static UserModel user=new UserModel("","","","");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: called");
        super.onCreate(savedInstanceState);
        binding=ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        toolbar=findViewById(R.id.home_toolbar);
        setSupportActionBar(toolbar);

        init_fragment();
        verificationFragment=new VerificationFragment();
    }//end of on Create

    @Override
    public void onCodeSendResponse(String fragmentTag, String phnNumber) {
        if(fragmentTag.equals("LoginFragment"))
            doFragmentTransaction(verificationFragment,phnNumber);
    }

    @Override
    public void callRegFragment(String tag) {
        if(tag.equals("CallRegFrag"))
            requestStoragePermission();
            doFragmentTransaction(new RegistrationFragment(),"null");
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            Log.d(TAG, "onOptionsItemSelected: called");
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void doFragmentTransaction(Fragment fragment, String message){
        FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();

        if(!message.equals("") && message!=null){
            Bundle bundle=new Bundle();
            bundle.putString(getString(R.string.intent_message),message);
            fragment.setArguments(bundle);

            transaction.addToBackStack(fragment.getTag());
            transaction.replace(R.id.frmagelayout,fragment);
            transaction.commit();
        }else if(message.equals("null")) {
            //transaction.addToBackStack(fragment.getTag());
            transaction.replace(R.id.frmagelayout,fragment);
            transaction.commit();
        }

    }

    private void init_fragment(){
        LoginFragment loginFragment=new LoginFragment();
        FragmentTransaction fragmentTransaction=getSupportFragmentManager().beginTransaction();
        //fragmentTransaction.addToBackStack("LoginFragment");
        fragmentTransaction.add(R.id.frmagelayout,loginFragment,null).commit();

        /*try{
            requestStoragePermission();
        }catch (Exception e){
            Log.d(TAG, "init_fragment: error"+e.getMessage());
        }
        RegistrationFragment registrationFragment=new RegistrationFragment();
        FragmentTransaction fragmentTransaction=getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.frmagelayout,registrationFragment,null).commit();*/
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.d(TAG, "onBackPressed: called");
    }

    @Override
    public void onAutoRetriveSMS(String code) {
        verificationFragment.setOTP(code);
    }

    public void requestStoragePermission() {
        Log.d(TAG, "requestStoragePermission: called");
      try{
          if(ContextCompat.checkSelfPermission(LoginActivity.this
                  , Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ){

              if(ActivityCompat.shouldShowRequestPermissionRationale(LoginActivity.this
                      ,Manifest.permission.READ_EXTERNAL_STORAGE) ){
                  Toast.makeText(this, "Provide Required Permission", Toast.LENGTH_SHORT).show();
              }else{
                  ActivityCompat.requestPermissions(LoginActivity.this,
                          new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},REQUESTCODE);
              }
          }
      }catch (Exception e){
          Log.d(TAG, "requestStoragePermission: error"+e.getMessage());
      }
    }


    //checking user status


}
