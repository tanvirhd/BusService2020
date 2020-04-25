package com.example.busservice2020.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.example.busservice2020.R;
import com.example.busservice2020.activity.HomeActivity;
import com.example.busservice2020.databinding.FragmentVerificationBinding;
import com.goodiebag.pinview.Pinview;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import static com.example.busservice2020.fragment.LoginFragment.mAuth;
import static com.example.busservice2020.fragment.LoginFragment.mVerificationId;


public class VerificationFragment extends Fragment{
    private static final String TAG="VerificationFragment";
    private FragmentVerificationBinding fragmentVerificationBinding;

    private String phnNumber;

    public VerificationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle=this.getArguments();
        if(bundle!=null){
            phnNumber=bundle.getString(getString(R.string.intent_message));
            Toast.makeText(getActivity(), "phn="+phnNumber, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentVerificationBinding =FragmentVerificationBinding.inflate(inflater,container,false);
        View view= fragmentVerificationBinding.getRoot();

        ((AppCompatActivity)getActivity()).getSupportActionBar().show();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Verification");
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeAsUpIndicator(R.drawable.back_arrow);


        fragmentVerificationBinding.phnNumber.setText("Enter verification code sent to +88"+phnNumber);
        fragmentVerificationBinding.pinView.requestFocus();


        fragmentVerificationBinding.btnResendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: cliked");
            }
        });
        
        fragmentVerificationBinding.pinView.setPinViewEventListener(new Pinview.PinViewEventListener() {
            @Override
            public void onDataEntered(Pinview pinview, boolean fromUser) {
                verifyVerificationCode(pinview.getValue());
            }
        });

        return view;
    }//end of onCreateView

    public void setOTP(String otp){
        fragmentVerificationBinding.pinView.setValue(otp);
    }

    private void verifyVerificationCode(String otp) {
        Log.d(TAG, "verifyVerificationCode: Done");
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, otp);
        mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    hideKeyboard(getActivity());
                    startActivity(new Intent(getActivity(), HomeActivity.class));getActivity().finish();
                    Toast.makeText(getActivity(), "Verification Complete.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /*@Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected: itemID"+item.getItemId());
        if(item.getItemId()==android.R.id.home){
            Log.d(TAG, "onOptionsItemSelected: pressed");
            getActivity().onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }*/

    /*    private void verifyVerificationCode(String otp) {
        Log.d(TAG, "verifyVerificationCode: Done");
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, otp);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        Log.d(TAG, "signInWithPhoneAuthCredential: Done.");
          mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
              @Override
              public void onComplete(@NonNull Task<AuthResult> task) {
                   if(task.isSuccessful()){
                       Toast.makeText(getActivity(), "Verification Complete.", Toast.LENGTH_SHORT).show();
                   }
              }
          });
    }

    private void initFireBaseCallbacks(){
        Log.d(TAG, "initFireBaseCallbacks: Done");
        mAuth= FirebaseAuth.getInstance();
        mCallbacks=new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                Log.d(TAG, "onVerificationCompleted: Auto Complete");
                Toast.makeText(getActivity(), "Verification auto-completed", Toast.LENGTH_SHORT).show();
                  String code=phoneAuthCredential.getSmsCode();
                  if(code!=null){
                      fragmentVerificationBinding.pinView.setValue(code);
                      verifyVerificationCode(code);
                  }
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Log.d(TAG, "onVerificationFailed: "+e.getMessage());
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                fragmentVerificationBinding.phnNumber.setText("Enter Verification code sent to +88"+phnNumber);
                mVerificationId=s;
                resendToken=forceResendingToken;

            }
        };
    }

    private void sendVerificationCode(String mobile) {
        Log.d(TAG, "sendVerificationCode: Done.");
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+88" + mobile,
                60,
                TimeUnit.SECONDS,
                TaskExecutors.MAIN_THREAD,
                mCallbacks);
    }*/

}
