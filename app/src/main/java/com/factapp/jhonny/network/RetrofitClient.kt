package com.factapp.jhonny.network

import com.factapp.jhonny.network.dto.model.Address
import com.factapp.jhonny.network.dto.model.Company
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Paso 2: instancia única de Retrofit + OkHttp + Gson.
 * Uso: `RetrofitClient.api` cuando [ApiService] tenga métodos.
 */
object RetrofitClient {

    private val gson = GsonBuilder()
        .setLenient()
        .registerTypeAdapter(Address::class.java, AddressTypeAdapter())
        .registerTypeAdapter(Company::class.java, CompanyTypeAdapter())
        .create()

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(ApiConfig.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val api: ApiService = retrofit.create(ApiService::class.java)
}
