package com.example.orion.utils.api;

import android.content.Context;

import com.example.orion.utils.PreferenceManager;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;

public class ApiClient {

    private static Retrofit retrofit = null;
//    public static String base_url = "http://36.94.61.28:1106/";
//    public static String base_url = "http://192.168.137.1:8000/";
    public static String base_url = "http://192.168.8.101:8000/";
    public static String base_api = base_url + "api/";

    public static Retrofit getClient(Context context) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        clientBuilder.addInterceptor(logging);

        clientBuilder.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();
                Request.Builder requestBuilder = original.newBuilder()
                        .header("Accept", "application/json");

                PreferenceManager pref = new PreferenceManager(context);
                String token = pref.getToken();

                if (token != null && !token.isEmpty()) {
                    requestBuilder.header("Authorization", "Bearer " + token);
                }

                Request request = requestBuilder.build();
                return chain.proceed(request);
            }
        });

        OkHttpClient client = clientBuilder.build();

        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(base_api)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }

        return retrofit;
    }
}
