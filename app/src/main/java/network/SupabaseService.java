package network;

import java.util.List;
import java.util.Map;

import data.model.Employee;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.Query;
import retrofit2.http.Body;

public interface SupabaseService {
    @GET("employees")
    Call<List<Employee>> getEmployeeByName(
            @Query("name") String nameFilter,
            @Header("apikey") String apiKey,
            @Header("Authorization") String auth,
            @Header("Accept") String accept
    );

    @PATCH("employees")
    Call<Void> updatePassword(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authorization,
            @Header("Content-Type") String contentType,
            @Query("id") String idFilter,
            @Body Map<String, Object> body
    );
}
