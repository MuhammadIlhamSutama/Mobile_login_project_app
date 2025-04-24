package network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object SupabaseClient {
    const val API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImlnbHNhYmlscHl2YXJ6ZGFkb2NqIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDQzODMyMjUsImV4cCI6MjA1OTk1OTIyNX0.jB7ual9xU_xixJW-8I0Ft98RBTl1CZo_cCzlFcVu0Ks"
    private const val BASE_URL = "https://iglsabilpyvarzdadocj.supabase.co/rest/v1/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client) // <- tambahkan client yang ada logging-nya
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val retrofitService: SupabaseService by lazy {
        retrofit.create(SupabaseService::class.java)
    }
}
