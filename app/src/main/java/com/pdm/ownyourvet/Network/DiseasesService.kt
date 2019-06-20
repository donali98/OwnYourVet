package com.pdm.ownyourvet.Network

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.pdm.ownyourvet.Room.Entities.Diseases
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET

const val BASE_URL = ""

interface DiseasesService {

    @GET
    fun getDiseases():Deferred<Response<Diseases>>

    companion object {
        fun getDiseasesService():DiseasesService= Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create())
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build().create(DiseasesService::class.java)
    }

}