package com.example.fetchrewardscodingexercise

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.livedata.observeAsState
import com.example.fetchrewardscodingexercise.ui.theme.FetchRewardsCodingExerciseTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Sealed classes are used for representing restricted class hierarchies
// where a value can have one of the types from a limited set. Here, it is used to classify a row as either a Header or an ItemEntry
sealed class ItemRow {
    // This class represents a header row with a list ID
    data class Header(val listId: Int) : ItemRow()
    // This class represents an item entry row with an item
    data class ItemEntry(val item: Item) : ItemRow()
}

// MainActivity is the starting point of the application. It extends ComponentActivity which is a base class for activities that want to use the new 'androidx.activity' APIs.
class MainActivity : ComponentActivity() {

    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Building the ApiService with Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("https://fetch-hiring.s3.amazonaws.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val service = retrofit.create(ApiService::class.java)

        // ViewModelFactory is used to create and return a new MainViewModel instance.
        val viewModelFactory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return MainViewModel(service) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }

        // Initialize MainViewModel with ViewModelFactory
        mainViewModel = ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)

        // Set up the UI
        setContent {
            FetchRewardsCodingExerciseTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
                    Row(modifier = Modifier.fillMaxSize(),horizontalArrangement = Arrangement.Center) {
                        Text(
                            text = "Fetch Rewards",
                            color = Color.Green,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.padding(top = 16.dp))
                    }
                    DisplayItems(mainViewModel)
                }
            }
        }
    }
}


// A composable function to display items.
// If the data is loading, it shows a CircularProgressIndicator.
// If there is an error, it shows the error message.
// Otherwise, it displays the items grouped by their list ID.
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DisplayItems(viewModel: MainViewModel) {
    val viewModel: MainViewModel = viewModel()

    val isLoading: Boolean by viewModel.isLoading.observeAsState(false)
    val items: List<Item> by viewModel.itemsLiveData.observeAsState(emptyList())
    val error: String by viewModel.errorLiveData.observeAsState("")

    // Display a CircularProgressIndicator if data is loading
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
        }
        // Display the error message if there's an error
    } else if (error.isNotEmpty()) {
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = error,
                color = Color.Green,
                fontSize = 20.sp,
                modifier = Modifier.padding(16.dp)
            )
        }
        // Display the items grouped by their list ID if the data has loaded successfully
    } else {
        val groupedItems: Map<Int, List<Item>> = items.groupBy { it.listId }

        LazyColumn(modifier = Modifier.padding(16.dp)) {
            groupedItems.forEach { (listId, itemList) ->
                stickyHeader(key = listId) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .padding(end = 16.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Box(
                            modifier = Modifier
                                .height(26.dp)
                                .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                        ) {
                            Text(
                                text = "List ID: $listId",
                                color = Color.Green,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp),
                                textAlign = TextAlign.End
                            )
                        }
                    }
                }
                itemList.sortedBy {
                    it.name?.substringAfterLast("Item ")?.toIntOrNull() ?: Int.MAX_VALUE
                }.forEach { item ->
                    item {
                        Column(
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Text(
                                color = Color.Green,
                                text = item.name ?: "Unnamed Item",
                                fontSize = 16.sp
                            )
                            Divider(
                                modifier = Modifier
                                    .padding(top = 1.dp)
                                    .width(215.dp),
                                color = Color.LightGray,
                                thickness = 1.dp
                            )
                        }
                    }
                }
            }
        }
    }
}





















