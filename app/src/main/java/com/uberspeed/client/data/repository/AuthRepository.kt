package com.uberspeed.client.data.repository

import com.uberspeed.client.data.RetrofitClient
import com.uberspeed.client.data.model.AuthResponse
import com.uberspeed.client.data.model.LoginRequest
import com.uberspeed.client.data.model.RegisterRequest
import com.uberspeed.client.utils.Resource
import retrofit2.Response

class AuthRepository {

    suspend fun login(email: String, password: String): Resource<AuthResponse> {
        return try {
            val response = RetrofitClient.apiService.login(LoginRequest(email, password))
            handleResponse(response)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred")
        }
    }

    suspend fun register(name: String, email: String, phone: String, password: String): Resource<AuthResponse> {
        return try {
            val request = RegisterRequest(name, email, phone, password)
            val response = RetrofitClient.apiService.register(request)
            handleResponse(response)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred")
        }
    }

    private fun <T> handleResponse(response: Response<T>): Resource<T> {
        if (response.isSuccessful && response.body() != null) {
            return Resource.Success(response.body()!!)
        }
        val errorMsg = try {
            val errorBody = response.errorBody()?.string()
            if (errorBody != null) {
                val json = org.json.JSONObject(errorBody)
                json.optString("message", response.message())
            } else {
                response.message()
            }
        } catch (e: Exception) {
            response.message()
        }
        return Resource.Error(errorMsg ?: "Unknown error")
    }
}
