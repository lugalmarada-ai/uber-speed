package com.uberspeed.client.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uberspeed.client.data.model.ServiceRequest
import com.uberspeed.client.data.model.Trip
import com.uberspeed.client.data.repository.TripRepository
import com.uberspeed.client.utils.Resource
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val repository = TripRepository()

    private val _requestState = MutableLiveData<Resource<Trip>>()
    val requestState: LiveData<Resource<Trip>> = _requestState

    fun requestService(token: String, request: ServiceRequest) {
        _requestState.value = Resource.Loading()
        viewModelScope.launch {
            val result = repository.requestService(token, request)
            _requestState.value = result
        }
    }
}
