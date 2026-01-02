package com.uberspeed.client.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uberspeed.client.data.model.AuthResponse
import com.uberspeed.client.data.repository.AuthRepository
import com.uberspeed.client.utils.Resource
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _loginState = MutableLiveData<Resource<AuthResponse>>()
    val loginState: LiveData<Resource<AuthResponse>> = _loginState

    private val _registerState = MutableLiveData<Resource<AuthResponse>>()
    val registerState: LiveData<Resource<AuthResponse>> = _registerState

    fun login(email: String, password: String) {
        _loginState.value = Resource.Loading()
        viewModelScope.launch {
            val result = repository.login(email, password)
            _loginState.value = result
        }
    }

    fun register(name: String, email: String, phone: String, password: String) {
        _registerState.value = Resource.Loading()
        viewModelScope.launch {
            val result = repository.register(name, email, phone, password)
            _registerState.value = result
        }
    }
}
