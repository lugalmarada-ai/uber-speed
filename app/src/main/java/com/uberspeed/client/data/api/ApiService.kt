package com.uberspeed.client.data.api

import com.uberspeed.client.data.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Header

interface ApiService {

    // Auth
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/logout")
    suspend fun logout(@Header("Authorization") token: String): Response<Unit>

    // Trip / Service
    @POST("services/request")
    suspend fun requestService(
        @Header("Authorization") token: String,
        @Body request: ServiceRequest
    ): Response<Trip>

    @GET("trips/history")
    suspend fun getTripHistory(@Header("Authorization") token: String): Response<List<Trip>>
    
    @GET("trips/{id}")
    suspend fun getTripDetails(
        @Header("Authorization") token: String,
        @Path("id") tripId: String
    ): Response<Trip>
}
