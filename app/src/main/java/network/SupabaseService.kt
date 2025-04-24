package network

import data.model.Client
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface SupabaseService {
    @GET("clients")
    suspend fun getClientById(
        @Query("id") idFilter: String,
        @Header("apikey") apiKey: String = SupabaseClient.API_KEY,
        @Header("Authorization") auth: String = "Bearer ${SupabaseClient.API_KEY}",
        @Header("Accept") accept: String = "application/json"
    ): List<Client>
}
