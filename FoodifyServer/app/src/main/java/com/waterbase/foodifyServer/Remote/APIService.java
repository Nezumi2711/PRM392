package com.waterbase.foodifyServer.Remote;

import com.waterbase.foodifyServer.Model.MyResponse;
import com.waterbase.foodifyServer.Model.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers(

            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAvBRs8TM:APA91bGQ-HvHMz4_UxiTsq4wY9H9j_ScuMRGOd03BYpQ4RKTuGpN_5CPhtQwycJaVCWMaS2T5S850QgUBhWZauOeM5aVeZsW3VGk1sXFu5GnlOgk2ev0VjzkK8fDsV4mO8D8k5uIknsC"
            }
    )


    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
