// Import necessary libraries and classes.
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.example.fetchrewardscodingexercise.ApiService
import com.example.fetchrewardscodingexercise.Item
import com.example.fetchrewardscodingexercise.MainViewModel
import okhttp3.ResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.MockitoAnnotations
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.CountDownLatch

// Define a MainViewModelTest class to hold all testing scenarios.
class MainViewModelTest {

    // Define a list of items that will be used in the tests.
    private val items = listOf(
        Item(4, 1, "Item 4"),
        Item(6, 2, "Item 6"),
        Item(2, 1, "Item 2"),
        Item(5, 1, "Item 5"),
        Item(10, 2, null),
        Item(3, 2, "Item 3")
    )

    // Rule that swaps the background executor used by the Architecture Components with a different one which executes each task synchronously.
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    // Mock objects using Mockito, these objects mimic the actual objects' behavior.
    @Mock
    private lateinit var apiService: ApiService
    @Mock
    private lateinit var itemsObserver: Observer<in List<Item>?>
    @Mock
    private lateinit var errorObserver: Observer<String>
    @Mock
    private lateinit var isLoadingObserver: Observer<Boolean>

    // The main view model which we'll be testing.
    private lateinit var mainViewModel: MainViewModel

    // This method runs before each test. It sets up the necessary objects and behavior.
    @Before
    fun setup() {
        // Initialize Mockito and the mock objects.
        MockitoAnnotations.openMocks(this)

        // Mock the call to the API service.
        val call = mockCall(Response.success(items))

        // Define the behavior of the API service when the fetchItems() function is called.
        `when`(apiService.fetchItems()).thenReturn(call)

        // Initialize the MainViewModel with the mocked API service.
        mainViewModel = MainViewModel(apiService)
    }

    // This method runs after each test, cleaning up any observers that were added to the LiveData objects during the test.
    @After
    fun cleanUp() {
        // Remove observers from LiveData objects to prevent memory leaks.
        mainViewModel.itemsLiveData.removeObserver(itemsObserver)
        mainViewModel.errorLiveData.removeObserver(errorObserver)
    }

    // This helper function mocks a Call<T> to mimic actual API behavior.
    @Suppress("UNCHECKED_CAST")
    private fun <T> mockCall(response: Response<T>): Call<T> {
        // Mock a call to the API.
        val call = mock(Call::class.java) as Call<T>

        // Define the behavior of the call when enqueue() function is called.
        `when`(call.enqueue(any())).thenAnswer { invocation ->
            val callback = invocation.getArgument<Callback<T>>(0)

            // Depending on whether the response is successful or contains an error body, call the appropriate callback function.
            if (response.isSuccessful || response.errorBody() != null) {
                callback.onResponse(call, response)
            } else {
                callback.onFailure(call, RuntimeException("API request failed"))
            }
            null
        }
        return call
    }

    // This test method checks the behavior of the MainViewModel when the API response is successful.
    @Test
    fun `test successful API response`() {
        // Observe the items and error LiveData.
        mainViewModel.itemsLiveData.observeForever(itemsObserver)
        mainViewModel.errorLiveData.observeForever(errorObserver)

        // Create a CountDownLatch to wait for the isLoading LiveData to change.
        val latch = CountDownLatch(1)

        // Observe the isLoading LiveData.
        mainViewModel.isLoading.observeForever { isLoading ->
            if (isLoading == true) {
                // isLoading is true, assert and continue with the test.
                assertEquals(true, isLoading)
                latch.countDown()
            }
        }

        // Call the function that will trigger the API call.
        mainViewModel.fetchItems()

        // Assert that isLoading is false and the error message is empty after the API call.
        assertEquals(false, mainViewModel.isLoading.value)
        assertEquals("", mainViewModel.errorLiveData.value)
    }

    // This test method checks the behavior of the MainViewModel when the API response has an error.
    @Test
    fun `test API response with error`() {
        val errorMessage = "Error 500: Internal Server Error"

        // Mock the API call to return an error.
        val call: Call<List<Item>> = mockCall(Response.error(500, mockResponseBody(errorMessage)))

        // Define the behavior of the API service when the fetchItems() function is called.
        `when`(apiService.fetchItems()).thenReturn(call)

        // Observe the items and error LiveData.
        mainViewModel.itemsLiveData.observeForever(itemsObserver)
        mainViewModel.errorLiveData.observeForever(errorObserver)

        // Create a CountDownLatch to wait for the isLoading LiveData to change.
        val latch = CountDownLatch(1)

        // Observe the isLoading LiveData.
        mainViewModel.isLoading.observeForever { isLoading ->
            if (isLoading == true) {
                // isLoading is true, assert and continue with the test.
                assertEquals(true, isLoading)
                latch.countDown()
            }
        }

        // Call the function that will trigger the API call.
        mainViewModel.fetchItems()

        // Assert that isLoading is false, items are null and the error message is as expected after the API call.
        assertEquals(false, mainViewModel.isLoading.value)
        assertNull(mainViewModel.itemsLiveData.value)
        assertEquals(errorMessage, mainViewModel.errorLiveData.value)
    }

    // This test method checks the behavior of the MainViewModel when the API call fails due to a network error.
    @Test
    fun `test API network error`() {
        val errorMessage = "Network connection failed"

        // Mock the API call to return an error.
        val call: Call<List<Item>> = mockCall(Response.error(400, mockResponseBody(errorMessage)))

        // Define the behavior of the API service when the fetchItems() function is called.
        `when`(apiService.fetchItems()).thenReturn(call)

        // Observe the items and error LiveData.
        mainViewModel.itemsLiveData.observeForever(itemsObserver)
        mainViewModel.errorLiveData.observeForever(errorObserver)

        // Create a CountDownLatch to wait for the isLoading LiveData to change.
        val latch = CountDownLatch(1)

        // Observe the isLoading LiveData.
        mainViewModel.isLoading.observeForever { isLoading ->
            if (isLoading == true) {
                // isLoading is true, assert and continue with the test.
                assertEquals(true, isLoading)
                latch.countDown()
            }
        }

        // Call the function that will trigger the API call.
        mainViewModel.fetchItems()

        // Wait for the latch to countdown, indicating that the response handling is complete.
        latch.await()

        // Assert that isLoading is false, items are null and the error message is as expected after the API call.
        assertEquals(false, mainViewModel.isLoading.value)
        assertNull(mainViewModel.itemsLiveData.value)
        assertEquals(errorMessage, mainViewModel.errorLiveData.value)
    }

    // This test method checks that items are correctly grouped and sorted by the MainViewModel.
    @Test
    fun `test displaying items grouped by listId`() {
        // Call the function that will trigger the API call.
        mainViewModel.fetchItems()

        // Define the expected items after grouping and sorting.
        val expectedItems = listOf(
            Item(2, 1, "Item 2"),
            Item(4, 1, "Item 4"),
            Item(5, 1, "Item 5"),
            Item(3, 2, "Item 3"),
            Item(6, 2, "Item 6")
        )

        // Observe the items LiveData.
        mainViewModel.itemsLiveData.observeForever(itemsObserver)

        // Create a CountDownLatch to wait for the items LiveData to change.
        val latch = CountDownLatch(1)

        // Observe the items LiveData.
        mainViewModel.itemsLiveData.observeForever { actualItems ->
            if (actualItems != null) {
                // Verify the ordering by listId and name.
                assertEquals(expectedItems, actualItems)
                latch.countDown()
            }
        }

        // Wait for the latch to countdown, indicating that the items LiveData has changed.
        latch.await()
    }

    // This helper function creates a ResponseBody from a string.
    @Suppress("UNCHECKED_CAST")
    private fun <T> mockResponseBody(data: T): ResponseBody {
        // Convert the data to a string and create a ResponseBody.
        val responseBody = ResponseBody.create(null, data.toString())
        return responseBody
    }
}