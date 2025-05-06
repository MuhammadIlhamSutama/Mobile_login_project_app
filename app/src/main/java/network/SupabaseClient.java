package network;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SupabaseClient {
    public static final SupabaseClient INSTANCE = new SupabaseClient();
    public static final String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im90ZWVidmd0c3Zncmtmb29pbnJ2Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDMwNDU1NzQsImV4cCI6MjA1ODYyMTU3NH0.IE1UReAZZk-9fbqi8SV3EF86Py703eoJVvpEBbzCBAo";
    private static final String BASE_URL = "https://oteebvgtsvgrkfooinrv.supabase.co/rest/v1/";

    private final SupabaseService retrofitService;

    private SupabaseClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client) // menyertakan interceptor logging
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        retrofitService = retrofit.create(SupabaseService.class);
    }

    public SupabaseService getRetrofitService() {
        return retrofitService;
    }
}
