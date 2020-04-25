package com.example.busservice2020.trash;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.busservice2020.databinding.ActivityTempBinding;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.HintRequest;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class TempActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, OtpReceivedInterface {

    private static String TAG="TempActivity";

    SmsBroadcastReceiver mSmsBroadcastReceiver;
    GoogleApiClient googleApiClient;
    private ActivityTempBinding binding;

    private int RESOLVE_HINT=111;

    private FirebaseAuth mAuth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken resendToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityTempBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mSmsBroadcastReceiver=new SmsBroadcastReceiver();
        googleApiClient=new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .enableAutoManage(this,this)
                .addApi(Auth.CREDENTIALS_API)
                .build();

        mSmsBroadcastReceiver.setOnOtpListeners(this);
        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction(SmsRetriever.SMS_RETRIEVED_ACTION);
        getApplicationContext().registerReceiver(mSmsBroadcastReceiver,intentFilter);

        mAuth=FirebaseAuth.getInstance();
        initFireBaseCallbacks();

        binding.btnTemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    sendVerificationCode("01786914359");  //"01786914359"
                    startSMSListener();
                }catch (Exception e){
                    Log.d(TAG, "onClick: exception:"+e.getMessage());
                }
            }
        });


    }//end of oncreate

    public void startSMSListener() {
        try{
            Log.d(TAG, "startSMSListener: called");
            SmsRetrieverClient mClient = SmsRetriever.getClient(this);
            Task<Void> mTask = mClient.startSmsRetriever();
            mTask.addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override public void onSuccess(Void aVoid) {
                    Log.d(TAG, "onSuccess: SMS Retriever starts");
                }
            });
            mTask.addOnFailureListener(new OnFailureListener() {
                @Override public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onFailure: error:"+e.getMessage());
                }
            });
        }catch (Exception e){
            Log.d(TAG, "startSMSListener:exception:"+e.getMessage() );
        }
    }

    private void initFireBaseCallbacks(){
        Log.d(TAG, "initFireBaseCallbacks: Done");
        mAuth= FirebaseAuth.getInstance();
        mCallbacks=new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                Log.d(TAG, "onVerificationCompleted: Auto Complete");
                String code=phoneAuthCredential.getSmsCode();
                if(code!=null){
                    Log.d(TAG, "onVerificationCompleted: menual");
                    binding.tvTemp.setText(code);
                    //fragmentVerificationBinding.pinView.setValue(code);
                    //verifyVerificationCode(code);
                }
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Log.d(TAG, "onVerificationFailed: "+e.getMessage());
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                Log.d(TAG, "onCodeSent: done.");
                mVerificationId=s;
                resendToken=forceResendingToken;

            }
        };
    }

    private void sendVerificationCode(String mobile) {
        try{
            Log.d(TAG, "sendVerificationCode: Done.");
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    "+88" + mobile,
                    60,
                    TimeUnit.SECONDS,
                    TaskExecutors.MAIN_THREAD,
                    mCallbacks);
        }catch (Exception e){
            Log.d(TAG, "sendVerificationCode: exception"+e.getMessage());
        }
    }

    private void verifyVerificationCode(String otp) {
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
                    Toast.makeText(TempActivity.this, "Verification Complete.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Construct a request for phone numbers and show the picker
    private void requestHint() {
        HintRequest hintRequest = new HintRequest.Builder()
                .setPhoneNumberIdentifierSupported(true)
                .build();

        PendingIntent pendingIntent=Auth.CredentialsApi.getHintPickerIntent(googleApiClient,hintRequest);

        try {
            startIntentSenderForResult(pendingIntent.getIntentSender(),
                    RESOLVE_HINT, null, 0, 0, 0);

        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }

    // Obtain the phone number from the result
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESOLVE_HINT) {
            if (resultCode == RESULT_OK) {
                Credential credential = data.getParcelableExtra(Credential.EXTRA_KEY);
                // credential.getId();  <-- will need to process phone number string
            }
        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }
    @Override
    public void onConnectionSuspended(int i) {

    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    @Override
    public void onOtpReceived(String otp) {
         binding.tvTemp.setText(otp);
    }
    @Override
    public void onOtpTimeout() {
        Log.d(TAG, "OtpTimeout");
    }
}


