package com.example.ocr_test

import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val NAVER_URL = BuildConfig.naver_uri

    private var naverRetrofit: Retrofit? = null

    private fun initRetrofit(base_url: String, factory: Converter.Factory): Retrofit? {
        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)

        val client: OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()

        return if (base_url === NAVER_URL) {
            if (naverRetrofit == null) {
                naverRetrofit = Retrofit.Builder()
                    .baseUrl(base_url)
                    .addConverterFactory(factory)
                    .client(client)
                    .build()
            }
            naverRetrofit
        } else {
//            if (weatherRetrofit == null) {
//                weatherRetrofit = Builder()
//                    .baseUrl(base_url)
//                    .addConverterFactory(factory)
//                    .client(client)
//                    .build()
//            }
            naverRetrofit
        }
    }

    //GSON
    fun getNaverRetrofit(): MyApi {
        return initRetrofit(NAVER_URL, GsonConverterFactory.create())!!.create(MyApi::class.java)
    }

}