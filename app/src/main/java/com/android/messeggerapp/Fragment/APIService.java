package com.android.messeggerapp.Fragment;

import com.android.messeggerapp.Notifications.MyResponse;
import com.android.messeggerapp.Notifications.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService
{
    @Headers({
            "Content-Type:application/json",
            "AAAAwRVbP_s:APA91bEX0NPghX_1uSrIeoYQfcGPvYnVl7J7jpzk9y3o7RKL6OW93waX-78m7RrPOq3Fa6oDcIQIKBZ5x9oVQA6w3CMojT3RRBA-0wr3CY7IbQH09hS-MQsP019ZqwY_S1ScHYjyzavH"
    })

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
