package network;

import java.util.List;
import java.util.Map;

import data.model.Employee;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface SupabaseService {
    @GET("employees")
    Call<List<Employee>> getEmployeeByName(
            @Query("name") String nameFilter,
            @Header("apikey") String apiKey,
            @Header("Authorization") String auth,
            @Header("Accept") String accept
    );

    @POST("employees")
    Call<Void> createEmployee(
            @Header("apikey") String apiKey,
            @Header("Authorization") String auth,
            @Header("Content-Type") String contentType,
            @Body Employee employee
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
