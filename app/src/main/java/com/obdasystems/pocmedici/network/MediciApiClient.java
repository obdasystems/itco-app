package com.obdasystems.pocmedici.network;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;

import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MediciApiClient {
    public static final String BASE_URL = "http://10.0.0.195:9000/api/";
    //public static final String BASE_URL = "http://obdatest.dis.uniroma1.it:3000/api/";
    private static Retrofit retrofit = null;

    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

    private static Interceptor generalErrorInterceptor;

    private static Retrofit.Builder builder;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static <S> S createService(
            Class<S> serviceClass, String username, String password) {
        if (!TextUtils.isEmpty(username)
                && !TextUtils.isEmpty(password)) {
            String authToken = Credentials.basic(username, password);
            return createService(serviceClass, authToken);
        }

        return createService(serviceClass, null);
    }

    public static <S> S createService(
            Class<S> serviceClass, final String authToken) {
        if (!TextUtils.isEmpty(authToken)) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            AuthenticationInterceptor interceptor = new AuthenticationInterceptor(authToken);

            if (!httpClient.interceptors().contains(interceptor)) {
                Gson gson = new GsonBuilder()
                        .setLenient()
                        .create();

                httpClient.addInterceptor(interceptor);
                httpClient.addInterceptor(loggingInterceptor);
                httpClient.addInterceptor(getGeneralErrorInterceptor());
                builder = new Retrofit.Builder();
                builder.client(httpClient.build());
                retrofit = builder
                        .baseUrl(BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .build();
            }
        }

        return retrofit.create(serviceClass);
    }

    private static Interceptor getGeneralErrorInterceptor() {
        if(generalErrorInterceptor == null) {
            generalErrorInterceptor = new Interceptor() {
                @Override
                public okhttp3.Response intercept(Chain chain) throws IOException {
                    Request request = chain.request();
                    okhttp3.Response response = chain.proceed(request);

                    // todo deal with the issues the way you need to
                    if (response.code() == 500) {
                        //startActivity(new Intent( ErrorHandlingActivity.this,ServerIsBrokenActivity.class));
                        return response;
                    }

                    return response;
                }
            };
        }
        return generalErrorInterceptor;
    }
}
