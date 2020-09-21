package com.example.busservice2020.retrofit;

import com.example.busservice2020.model.DirectionResponse;
import com.example.busservice2020.model_distancematrix.DistanceResponse;

import java.util.Map;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;

public interface ApiInterface {
    @POST("directions/json")
    Observable<DirectionResponse> getDirectionResponse(@QueryMap Map<String, String> parameters);
    @POST("distancematrix/json")
    Observable<DistanceResponse> getDistanceInfo(@QueryMap Map<String, String> parameters);
}
