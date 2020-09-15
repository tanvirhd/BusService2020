package com.example.busservice2020.SentNotification;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface NotificationAPI {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAzxVAsss:APA91bHjfPknSUXwwwaAiVexH-5XxrKKNgTxJ-24_OGh7yP3QdqFjj1-IUEtmP4HojwbU6xYu54LIM3ynUr48F9nQfNwx0a5gmCyLjR9LpRJH1O0Z8A5meaXDyK3ZbEmLSmoWiVDewQW"
    })
    @POST("fcm/send")
    Call<Response>sendNotification(@Body NotificationSender body);
}
