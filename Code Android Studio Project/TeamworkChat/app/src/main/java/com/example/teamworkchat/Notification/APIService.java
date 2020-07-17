package com.example.teamworkchat.Notification;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAA5-HS2e4:APA91bFoLKNz-keRagm-IDePoEQ9WluEHPXb54JMIeJHBdIj7TrN_mhlu622jdNHIBTi4eZlv0IRB7FStOsoHW3MUqfebaqKWFzFHOnm41ypH6rgdlTVv55iH-hZfprDDp0Rm5X2_deS"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
