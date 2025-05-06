package network;

import java.util.List;

import data.model.Client;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface SupabaseService {
    @GET("clients")
    Call<List<Client>> getClientByUsername(
            @Query("username") String usernameFilter,
            @Header("apikey") String apiKey,
            @Header("Authorization") String auth,
            @Header("Accept") String accept
    );
}
