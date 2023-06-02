package com.example.fetchrewardscodingexercise

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// MainViewModel class that extends ViewModel
// It provides data for the UI and survives configuration changes
// service is an instance of ApiService that is used to fetch data from the network
class MainViewModel(
    private val service: ApiService
) : ViewModel() {

    // MutableLiveData for tracking whether data is loading
    // LiveData is used because it respects the lifecycle of app components
    private val _isLoading = MutableLiveData(false)
    // Public LiveData that's exposed to other classes. This is read-only to the other classes to prevent unwanted changes
    val isLoading: LiveData<Boolean> = _isLoading

    // MutableLiveData to hold the list of Items
    private val _itemsLiveData = MutableLiveData<List<Item>>()
    // Public LiveData for items
    val itemsLiveData: LiveData<List<Item>> = _itemsLiveData

    // MutableLiveData to hold any errors that occur while fetching the data
    private val _errorLiveData = MutableLiveData<String>()
    // Public LiveData for error messages
    val errorLiveData: LiveData<String> = _errorLiveData

    // The init block is executed when an instance of the class is created
    // Here, fetchItems() is called as soon as an instance of MainViewModel is created
    init {
        fetchItems()
    }

    // fetchItems() fetches data from the network
    internal fun fetchItems() {
        _isLoading.value = true

        val call = service.fetchItems()
        call.enqueue(object : Callback<List<Item>> {
            // onResponse is called when a network response is received
            // it processes the response and updates _itemsLiveData or _errorLiveData
            override fun onResponse(call: Call<List<Item>>, response: Response<List<Item>>) {
                if (response.isSuccessful) {
                    _errorLiveData.value = ""

                    val items = response.body()
                    val processedItems = items?.filter { !it.name.isNullOrBlank() }
                        ?.sortedWith(compareBy<Item> { it.listId }
                            .thenBy { it.name?.substringAfterLast("Item ")?.toIntOrNull() ?: Int.MAX_VALUE })
                    _itemsLiveData.value = processedItems
                    _isLoading.value = false
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Unknown error"
                    _itemsLiveData.value = null
                    _isLoading.value = false
                    _errorLiveData.value = errorMessage
                }
            }

            // onFailure is called when a network request fails
            // It updates _errorLiveData with the error message
            override fun onFailure(call: Call<List<Item>>, t: Throwable) {
                _isLoading.value = false
                _itemsLiveData.value = null
                _errorLiveData.value = t.message ?: "Unknown error"
            }
        })
    }
}










