package network;

import android.util.Log;
import com.example.myapplication.BuildConfig;
import java.util.HashMap;
import java.util.Map;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SupabaseClient {
    public static final SupabaseClient INSTANCE = new SupabaseClient();
    public static final String API_KEY = BuildConfig.SUPABASE_KEY;
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
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        retrofitService = retrofit.create(SupabaseService.class);
    }

    public SupabaseService getRetrofitService() {
        return retrofitService;
    }

    public static void updateEmployeePassword(String id, String newPassword) {
        SupabaseService service = INSTANCE.getRetrofitService();

        Map<String, Object> body = new HashMap<>();
        body.put("password", newPassword);

        Call<Void> call = service.updatePassword(
                API_KEY,
                "Bearer " + API_KEY,
                "application/json",
                "eq." + id,
                body
        );

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("Supabase", "Password updated successfully");
                } else {
                    Log.e("Supabase", "Update failed: " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("Supabase", "Network error: " + t.getMessage());
            }
        });
    }
}
