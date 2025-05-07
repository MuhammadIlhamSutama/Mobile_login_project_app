package network;

import java.util.List;
import java.util.Map;

import data.model.Client;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Body;

public interface SupabaseService {
    @GET("clients")
    Call<List<Client>> getClientByUsername(
            @Query("username") String usernameFilter,
            @Header("apikey") String apiKey,
            @Header("Authorization") String auth,
            @Header("Accept") String accept
    );

    @PATCH("clients")
    Call<Void> updatePassword(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authorization,
            @Header("Content-Type") String contentType,
            @Query("user_id") String userIdFilter, // Correct column name
            @Body Map<String, Object> body
    );


}
