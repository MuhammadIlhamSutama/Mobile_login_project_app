package network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object SupabaseClient {
    const val API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im90ZWVidmd0c3Zncmtmb29pbnJ2Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDMwNDU1NzQsImV4cCI6MjA1ODYyMTU3NH0.IE1UReAZZk-9fbqi8SV3EF86Py703eoJVvpEBbzCBAo"
    private const val BASE_URL = "https://oteebvgtsvgrkfooinrv.supabase.co/rest/v1/"

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
