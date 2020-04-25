package com.example.busservice2020.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.busservice2020.R;
import com.example.busservice2020.activity.HomeActivity;
import com.example.busservice2020.databinding.FragmentLoginBinding;
import com.example.busservice2020.interfaces.F2F_Commuication;
import com.example.busservice2020.interfaces.Fragment_Communication;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

import io.andref.rx.network.RxNetwork;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

import static android.content.Context.CONNECTIVITY_SERVICE;

public class LoginFragment extends Fragment  {
    private static final String TAG="LoginFragment";
    private FragmentLoginBinding fragmentLoginBinding;

    Fragment_Communication fragmentCommunication;
    F2F_Commuication f2fCommuication;

    private CompositeSubscription mCompositeSubscription;
    private ConnectivityManager mConnectivityManager;

    public static FirebaseAuth mAuth;
    public PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    public static String mVerificationId;
    public PhoneAuthProvider.ForceResendingToken resendToken;
    //public String code=null;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mConnectivityManager = (ConnectivityManager) getActivity().getSystemService(CONNECTIVITY_SERVICE);

        mAuth=FirebaseAuth.getInstance();
        initFireBaseCallbacks();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        fragmentLoginBinding =FragmentLoginBinding.inflate(inflater,container,false);
        View view= fragmentLoginBinding.getRoot();
        fragmentLoginBinding.enterPhnNumber.requestFocus();
        ((AppCompatActivity)getActivity()).getSupportActionBar().hide();

        fragmentLoginBinding.nextButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(fragmentLoginBinding.enterPhnNumber.equals("") || fragmentLoginBinding.enterPhnNumber.length()!=11) {
                        Toast.makeText(getActivity(), "Invalid Number!!", Toast.LENGTH_SHORT).show();
                    }else {
                        sendVerificationCode(fragmentLoginBinding.enterPhnNumber.getText().toString());
                        resetIn30SEC(15,fragmentLoginBinding.nextButton);
                        fragmentLoginBinding.nextButton.setClickable(false);
                        mCompositeSubscription.unsubscribe();
                    }
                }
        });

        return  view;
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        fragmentCommunication =(Fragment_Communication)getActivity();
        f2fCommuication =(F2F_Commuication)getActivity();
    }

    @Override
    public void onPause() {
        super.onPause();
        mCompositeSubscription.unsubscribe();
    }

    @Override
    public void onResume() {
        super.onResume();

        fragmentLoginBinding.nextButton.setText("Next");
        fragmentLoginBinding.nextButton.setClickable(true);

        //checking network connection state
        mCompositeSubscription = new CompositeSubscription();
        mCompositeSubscription.add(
                RxNetwork.connectivityChanges(getContext(), mConnectivityManager)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<Boolean>()
                        {
                            @Override
                            public void call(Boolean connected)
                            {
                                if(connected){
                                    fragmentLoginBinding.nextButton.setBackgroundResource(R.drawable.button_layout);
                                    fragmentLoginBinding.nextButton.setText("NEXT");
                                    Log.d(TAG, "call: internet available");
                                }else {
                                    fragmentLoginBinding.nextButton.setBackgroundResource(R.drawable.button_layout_red);
                                    fragmentLoginBinding.nextButton.setText("No Internet!");
                                    Log.d(TAG, "call: internet not available");
                                }
                                fragmentLoginBinding.nextButton.setClickable(connected);
                            }
                        })
        );
    }


    private void initFireBaseCallbacks(){
        Log.d(TAG, "initFireBaseCallbacks: Called");
        mAuth= FirebaseAuth.getInstance();
        mCallbacks=new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                Log.d(TAG, "onVerificationCompleted: Called");

                if(phoneAuthCredential.getSmsCode() != null){
                    //callingf verification fragment
                    f2fCommuication.onAutoRetriveSMS(phoneAuthCredential.getSmsCode());
                    Toast.makeText(getActivity(), "Auto-Retrival", Toast.LENGTH_SHORT).show();
                }else {
                    verifyCode(phoneAuthCredential);
                    Toast.makeText(getActivity(), "Instant Verification", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Log.d(TAG, "onVerificationFailed: "+e.getMessage());
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                Log.d(TAG, "onCodeSent: Done.");
                super.onCodeSent(s, forceResendingToken);

                mVerificationId=s;
                resendToken=forceResendingToken;
                Toast.makeText(getActivity(), "onCodeSent: Done.", Toast.LENGTH_SHORT).show();

                fragmentCommunication.onCodeSendResponse(TAG,fragmentLoginBinding.enterPhnNumber.getText().toString());

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
    }

    private void verifyCode(PhoneAuthCredential credential) {
        Log.d(TAG, "verifyCode: Done");
        mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Toast.makeText(getActivity(), "Verification Complete.", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getActivity(), HomeActivity.class));getActivity().finish(); //jump to Home Activity
                }
            }
        });
    }

    public void resetIn30SEC(int Seconds, final Button btn){

        btn.setText("Please wait...");
        new CountDownTimer(Seconds* 1000+1000, 1000) {

            public void onTick(long millisUntilFinished) {
                //int seconds = (int) (millisUntilFinished / 1000);
                //btn.setText(" "+ String.format("%02d", seconds)+" ");
            }

            public void onFinish() {
                Toast.makeText(getActivity(), "Something went wrong!! Try again.", Toast.LENGTH_SHORT).show();
                btn.setText("Next");
                btn.setClickable(true);
            }
        }.start();
    }
}
