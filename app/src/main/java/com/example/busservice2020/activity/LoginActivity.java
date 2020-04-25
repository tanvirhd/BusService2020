package com.example.busservice2020.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.example.busservice2020.R;
import com.example.busservice2020.databinding.ActivityLoginBinding;
import com.example.busservice2020.fragment.VerificationFragment;
import com.example.busservice2020.interfaces.F2F_Commuication;
import com.example.busservice2020.interfaces.Fragment_Communication;
import com.example.busservice2020.fragment.LoginFragment;

public class LoginActivity extends AppCompatActivity implements Fragment_Communication , F2F_Commuication {
    private static final String TAG="LoginActivity";

    ActivityLoginBinding binding;
    Toolbar toolbar;

    VerificationFragment verificationFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: called");
        super.onCreate(savedInstanceState);
        binding=ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        toolbar=findViewById(R.id.toolbar);
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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            Log.d(TAG, "onOptionsItemSelected: called");
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void doFragmentTransaction(Fragment fragment, String message){
        FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
        if(!message.equals("")){
            Bundle bundle=new Bundle();
            bundle.putString(getString(R.string.intent_message),message);
            fragment.setArguments(bundle);
        }
        transaction.replace(R.id.frmagelayout,fragment);
        transaction.addToBackStack(fragment.getTag());
        transaction.commit();
    }

    private void init_fragment(){
        LoginFragment loginFragment=new LoginFragment();
        FragmentTransaction fragmentTransaction=getSupportFragmentManager().beginTransaction();
        //fragmentTransaction.addToBackStack("LoginFragment");
        fragmentTransaction.add(R.id.frmagelayout,loginFragment,null).commit();
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

    //================================================================



}
