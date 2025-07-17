package com.example.active

import com.example.active.models.StreamMetadata
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object StreamRepository {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())  // Enables Kotlin data class support
        .build()

    private val api = Retrofit.Builder()
        .baseUrl("https://www.radio-active.net/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))  // 🔑 Use created Moshi
        .build()
        .create(StreamApi::class.java)

    suspend fun getMetadata(): StreamMetadata = api.getMetadata()
}
