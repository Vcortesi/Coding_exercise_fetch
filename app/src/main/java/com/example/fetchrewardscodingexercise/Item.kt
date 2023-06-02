package com.example.fetchrewardscodingexercise

import com.google.gson.annotations.SerializedName

// Definition of the Item data class, representing an item in your application. This class will be used for parsing the API response.
data class Item(
    // Each property in the data class has a @SerializedName annotation to map the JSON keys in the API response to properties of the data class.
    @SerializedName("id")
    val id: Int,

    @SerializedName("listId")
    val listId: Int,

    @SerializedName("name")
    val name: String?  // 'name' is nullable because the JSON 'name' key might not be present in the API response.
)
