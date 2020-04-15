package com.example.busservice2020.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.busservice2020.databinding.FragmentLoginBinding;

public class LoginFragment extends Fragment {
    private static final String TAG="LoginFragment";

    private FragmentLoginBinding fragmentLoginBinding;
    Fragment_Communication fragment_communication;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                    fragment_communication.sharePhnNumber("VerificationFragment", fragmentLoginBinding.enterPhnNumber.getText().toString());
                }
        });

        return  view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        fragment_communication=(Fragment_Communication)getActivity();
    }
}
