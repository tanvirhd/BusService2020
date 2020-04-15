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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.dpizarro.pinview.library.PinView;
import com.example.busservice2020.R;
import com.example.busservice2020.activity.HomeActivity;
import com.example.busservice2020.databinding.FragmentVerificationBinding;


public class VerificationFragment extends Fragment {
    private static final String TAG="VerificationFragment";
    private String phnNumber;
    private FragmentVerificationBinding fragmentVerificationBinding;


    public VerificationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle=this.getArguments();
        if(bundle!=null){
            phnNumber=bundle.getString(getString(R.string.intent_message));
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


        fragmentVerificationBinding.phnNumber.setText("Verification was sent to "+phnNumber);
        fragmentVerificationBinding.pinView.requestFocus();

        fragmentVerificationBinding.pinView.setOnCompleteListener(new PinView.OnCompleteListener() {
            @Override
            public void onComplete(boolean completed, String pinResults) {
                if(fragmentVerificationBinding.pinView.getPinResults().equals("123456")){
                    Log.d(TAG, "pin complete");
                    hideKeyboard(getActivity());
                    startActivity(new Intent(getActivity(), HomeActivity.class));
                    getActivity().finish();

                }else {
                    Toast.makeText(getActivity(), "Incorrect PIN", Toast.LENGTH_SHORT).show();
                    fragmentVerificationBinding.pinView.clear();
                }
            }
        });
        return view;

    }//end of onCreateView

    /*@Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected: itemID"+item.getItemId());
        if(item.getItemId()==android.R.id.home){
            Log.d(TAG, "onOptionsItemSelected: pressed");
            getActivity().onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }*/

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
