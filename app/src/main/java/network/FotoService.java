package network;

import java.util.List;

import data.model.Foto;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface FotoService {

    @Headers({
            "apikey: " + SupabaseClient.API_KEY,
            "Authorization: Bearer " + SupabaseClient.API_KEY
    })
    @GET("fotos")
    Call<List<Foto>> getFotosByName(
            @Query("select") String select,
            @Query("order") String order,
            @Query("name") String nameFilter
    );
}
