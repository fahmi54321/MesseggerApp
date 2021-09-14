package com.android.messeggerapp.Notifications

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

//todo 4 push notification (next Data)
class Client
{
    object Client
    {
        private var retrofit: Retrofit? = null

        fun getClient(url: String?) : Retrofit?
        {
            if (retrofit == null)
            {
                retrofit = Retrofit.Builder()
                    .baseUrl(url)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            }
            return retrofit
        }
    }
}