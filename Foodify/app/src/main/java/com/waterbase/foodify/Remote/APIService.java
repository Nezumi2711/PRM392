package com.waterbase.foodify.Remote;

import com.waterbase.foodify.Model.DataMessage;
import com.waterbase.foodify.Model.MyResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers(

            {
                    "Content-Type:application/json",
                    "Authorization:key=[YOUR_AUTHORIZATION_KEY]"
            }
    )


    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body DataMessage body);
}
