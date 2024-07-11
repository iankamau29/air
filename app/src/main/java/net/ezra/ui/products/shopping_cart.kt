package net.ezra.ui.products

import android.annotation.SuppressLint
import android.util.Base64
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import net.ezra.navigation.ROUTE_HOME
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.tasks.await
import net.ezra.ui.products.LipanaMpesaPassword.businessShortCode
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Header
import retrofit2.http.GET
import retrofit2.http.Query
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.*

object LipanaMpesaPassword {
    val lipaTime: String = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
    val businessShortCode = "174379"
    val passkey = "bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919"

    val dataToEncode = businessShortCode + passkey + lipaTime

    val onlinePassword: String = Base64.encodeToString(
        dataToEncode.toByteArray(),
        Base64.NO_WRAP
    ).toString()
}

// Define data classes for M-Pesa API requests and responses
data class MpesaOAuthResponse(val access_token: String, val expires_in: String)

data class MpesaRequest(
    val BusinessShortCode: String,
    val Password: String,
    val Timestamp: String,
    val TransactionType: String,
    val Amount: String,
    val PartyA: String,
    val PartyB: String,
    val PhoneNumber: String,
    val CallBackURL: String,
    val AccountReference: String,
    val TransactionDesc: String
)

data class MpesaResponse(
    val MerchantRequestID: String,
    val CheckoutRequestID: String,
    val ResponseCode: String,
    val ResponseDescription: String,
    val CustomerMessage: String
)

// Define Retrofit interfaces for M-Pesa API
interface MpesaAuthService {
    @GET("oauth/v1/generate")
    fun getOAuthToken(@Query("grant_type") grantType: String = "client_credentials"): Call<MpesaOAuthResponse>
}

interface MpesaApiService {
    @Headers("Content-Type: application/json")
    @POST("mpesa/stkpush/v1/processrequest")
    fun performStkPush(@Header("Authorization") authToken: String, @Body mpesaRequest: MpesaRequest): Call<MpesaResponse>
}

object RetrofitInstance {
    private const val BASE_URL = "https://sandbox.safaricom.co.ke/mpesa/stkpush/v1/processrequest"
    private const val CONSUMER_KEY = "A5HEnPnnXb2h19Amvuj758JVzuWgtxmGW5AmhXfSbJsAPOCZ"
    private const val CONSUMER_SECRET = "zMIe7IpwPHTUQw1uh0vpYmjQ1m2roTZr0WRfxhoJRhew7flFBWRFr22BfxoQkS6O"

    val authService: MpesaAuthService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MpesaAuthService::class.java)
    }

    val apiService: MpesaApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MpesaApiService::class.java)
    }

    private fun getBasicAuthHeader(): String {
        val auth = "$CONSUMER_KEY:$CONSUMER_SECRET"
        return "Basic " + Base64.encodeToString(auth.toByteArray(), Base64.NO_WRAP)
    }

    suspend fun getOAuthToken(): String {
        val authHeader = getBasicAuthHeader()
        val response = authService.getOAuthToken("client_credentials").execute()
        if (response.isSuccessful) {
            return response.body()?.access_token ?: throw Exception("Failed to obtain access token")
        } else {
            throw Exception("Failed to obtain access token: ${response.errorBody()?.string()}")
        }
    }
}

// Checkout function with M-Pesa integration


// Composable function for Shopping Cart screen
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun ShoppingCartScreen(navController: NavController) {
    val cartItems by remember { derivedStateOf { CartState.cartItems } }
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var phoneNumber by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Booked Houses",
                        fontSize = 24.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate(ROUTE_HOME)
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // Optional: Add other actions if needed
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFFF0068),
                    titleContentColor = Color.White
                )
            )
        },
        content = {
            Column(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFFFF0068), Color(0xFF6200EE))
                        )
                    )
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                if (cartItems.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Your cart is empty", style = MaterialTheme.typography.h6)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        items(cartItems) { item ->
                            CartItemRow(item, onRemoveItem = { removedItem ->
                                CartState.cartItems.remove(removedItem)
                            })
                            Divider()
                        }
                    }
                }

                // Phone Number Input
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

                // Checkout Button at the bottom
                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            try {
                                try {
                                    Log.d("Mpesa", "Performing M-Pesa checkout...")
                                    val timestamp = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
                                    val password = LipanaMpesaPassword.onlinePassword
                                    val authToken = RetrofitInstance.getOAuthToken()
                                    val mpesaRequest = MpesaRequest(
                                        BusinessShortCode = businessShortCode,
                                        Password = password,
                                        Timestamp = timestamp,
                                        TransactionType = "CustomerPayBillOnline",
                                        Amount = "1", // Example amount
                                        PartyA = phoneNumber,
                                        PartyB = businessShortCode,
                                        PhoneNumber = phoneNumber,
                                        CallBackURL = "https://mydomain.com/pat", // Replace with your actual callback URL
                                        AccountReference = "account",
                                        TransactionDesc = "payment"
                                    )
                                    val response = RetrofitInstance.apiService.performStkPush("Bearer $authToken", mpesaRequest).execute()
                                    if (response.isSuccessful) {
                                        // Handle successful response
                                        val mpesaResponse = response.body()
                                        Log.d("Mpesa", "Success: ${mpesaResponse?.CustomerMessage}")
                                        // Store order in Firestore (optional)
                                        storeOrderInFirestore(cartItems)
                                    } else {
                                        // Handle error
                                        Log.e("Mpesa", "Error: ${response.errorBody()?.string()}")
                                    }
                                } catch (e: Exception) {
                                    Log.e("Mpesa", "Exception: ${e.message}")
                                }
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .height(48.dp)
                ) {
                    Text("Checkout", fontSize = 18.sp)
                }
            }

            // Loading dialog
            // Loading dialog
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .padding(16.dp)
                            .background(Color.White)
                            .width(IntrinsicSize.Max)
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Checking out...", style = MaterialTheme.typography.h6)
                    }
                }
            }
        }
    )
}


// Function to store order in Firestore (if needed)
private suspend fun storeOrderInFirestore(cartItems: List<CartItem>) {
    val db = FirebaseFirestore.getInstance()
    val ordersCollection = db.collection("orders")

    val batch = db.batch()
    for (cartItem in cartItems) {
        val orderData = mapOf(
            "productId" to cartItem.product.id,
            "productName" to cartItem.product.name,
            "quantity" to 1, // For simplicity, assuming quantity is 1
            "timestamp" to System.currentTimeMillis()
        )
        batch.set(ordersCollection.document(), orderData)
    }

    // Commit the batch
    batch.commit().await()
}

// Function to perform M-Pesa checkout
@Composable
fun CartItemRow(cartItem: CartItem, onRemoveItem: (CartItem) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(Color.White)
    ) {
        Image(
            painter = rememberImagePainter(cartItem.product.imageUrl),
            contentDescription = null,
            modifier = Modifier
                .size(80.dp)
                .padding(8.dp)
                .background(Color.Gray)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically)
        ) {
            Text(
                text = cartItem.product.name,
                style = MaterialTheme.typography.h6,
                color = Color.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Price: $${cartItem.product.price}",
                style = MaterialTheme.typography.body1,
                color = Color.Black
            )
        }
        IconButton(
            onClick = { onRemoveItem(cartItem) },
            modifier = Modifier.align(Alignment.CenterVertically)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Remove",
                tint = Color.Red
            )
        }
    }
}