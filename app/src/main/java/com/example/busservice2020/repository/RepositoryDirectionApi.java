package com.example.busservice2020.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.busservice2020.model.DirectionResponse;
import com.example.busservice2020.retrofit.ApiClient;
import com.example.busservice2020.retrofit.ApiInterface;

import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.http.QueryMap;


public class RepositoryDirectionApi {
    private static String TAG="RepositoryDirectionApi";
    private ApiInterface apiRequest;

    public RepositoryDirectionApi() {
        apiRequest= ApiClient.getApiInterface();
    }

    public LiveData<DirectionResponse> getDirectionResponse(@QueryMap Map<String, String> parameters){
        final MutableLiveData<DirectionResponse> response=new MutableLiveData<>();
        apiRequest.getDirectionResponse(parameters).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<DirectionResponse>() {
                    @Override
                    public void accept(DirectionResponse directionResponse) throws Exception {
                        response.postValue(directionResponse);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.d(TAG, "accept: error"+throwable.getMessage());
                    }
                });
        return  response;
    }

}
