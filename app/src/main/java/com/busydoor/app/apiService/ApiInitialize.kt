package com.busydoor.app.apiService

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
/**
 * Created by Admin on 30-11-2023.
 */

object ApiInitialize {
   private const val BASE_URL ="https://bzboss.app/busydoor_api/public/"
//   private const val BASE_URL ="https://demo.emeetify.com:5100/"
    const val LOCAL_URL = BASE_URL +"api/"
    const val IMAGE_URL = BASE_URL +"uploads/"
    const val DUMMY_IMAGE_URL = IMAGE_URL +"dummyuser.png"
    const val TERM_AND_CONDITION = BASE_URL +"term/condition"
    const val PRIVACY_POLIVY = BASE_URL +"privecy/policy"
    const val ABOUTS_US = BASE_URL +"about/us"
    private var retrofit: Retrofit? = null
    private lateinit var apiInIt: ApiInterface

    @Synchronized
    fun getApiInIt(): ApiInterface {
        return apiInIt
    }

    fun initialize(): Retrofit {
        val gson = GsonBuilder()
            .setLenient()
            .create()

        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .baseUrl(LOCAL_URL)
                .client(requestHeader)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
        }
        return retrofit!!
    }

    fun initialize(baseUrl: String): ApiInterface {

        val gson = GsonBuilder()
            .setLenient()
            .create()

        retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(requestHeader)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        apiInIt = retrofit!!.create(ApiInterface::class.java)

        return apiInIt
    }

    private val requestHeader: OkHttpClient
        get() {
            return OkHttpClient.Builder()
                .readTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS)
                .cache(null)
                .build()
        }
}
