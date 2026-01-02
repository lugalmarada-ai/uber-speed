package com.uberspeed.client.ui.booking

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uberspeed.client.data.model.Trip
import com.uberspeed.client.data.repository.TripRepository
import com.uberspeed.client.utils.Resource
import kotlinx.coroutines.launch

class TripViewModel : ViewModel() {

    private val repository = TripRepository()

    private val _historyState = MutableLiveData<Resource<List<Trip>>>()
    val historyState: LiveData<Resource<List<Trip>>> = _historyState

    fun getHistory(token: String) {
        _historyState.value = Resource.Loading()
        viewModelScope.launch {
            val result = repository.getTripHistory(token)
            _historyState.value = result
        }
    }
}
