package com.example.fetchrewardscodingexercise

import retrofit2.Call
import retrofit2.http.GET

// ApiService interface represents your API, and defines the endpoint to fetch items.
interface ApiService {
    // @GET annotation is used to mark this method as a GET request.
    // The 'hiring.json' argument to the @GET annotation is the relative path of the API endpoint.
    @GET("hiring.json")
    // fetchItems method returns a Call object, which can be used to send the API request asynchronously.
    // The List<Item> type argument represents the type of the API response.
    fun fetchItems(): Call<List<Item>>
}