package com.uberspeed.client.data.repository

import com.uberspeed.client.data.RetrofitClient
import com.uberspeed.client.data.model.ServiceRequest
import com.uberspeed.client.data.model.Trip
import com.uberspeed.client.utils.Resource
import retrofit2.Response

class TripRepository {

    suspend fun requestService(token: String, request: ServiceRequest): Resource<Trip> {
        return try {
            // Add "Bearer " prefix if backend requires it, but simple implementation here
            val authToken = if (token.startsWith("Bearer ")) token else "Bearer $token"
            val response = RetrofitClient.apiService.requestService(authToken, request)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.message())
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun getTripHistory(token: String): Resource<List<Trip>> {
        return try {
            val authToken = if (token.startsWith("Bearer ")) token else "Bearer $token"
            val response = RetrofitClient.apiService.getTripHistory(authToken)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.message())
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }
}
