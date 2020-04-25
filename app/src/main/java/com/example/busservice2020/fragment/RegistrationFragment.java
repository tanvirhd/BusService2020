package com.example.busservice2020.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.busservice2020.R;
import com.example.busservice2020.databinding.FragmentRegistrationBinding;

public class RegistrationFragment extends Fragment {
    private static String TAG="RegistrationFragment";
    private FragmentRegistrationBinding fragmentRegistrationBinding;

    public RegistrationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentRegistrationBinding=FragmentRegistrationBinding.inflate(inflater,container,false);
        View view=fragmentRegistrationBinding.getRoot();

        return view;
    }
}
