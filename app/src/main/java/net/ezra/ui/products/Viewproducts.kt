package net.ezra.ui.products


import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.contentColorFor
import androidx.compose.material.*
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarDefaults.backgroundColor
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import net.ezra.navigation.ROUTE_HOME

data class Product(
    var id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    var imageUrl: String = ""
)

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(navController: NavController, products: List<Product>) {
    var isLoading by remember { mutableStateOf(true) }
    var productList by remember { mutableStateOf(emptyList<Product>()) }
    var displayedProductCount by remember { mutableStateOf(1) }
    var progress by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        fetchProducts { fetchedProducts ->
            productList = fetchedProducts
            isLoading = false
            displayedProductCount = 10
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = "Houses", fontSize = 30.sp, color = Color.White)
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate(ROUTE_HOME)
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFFF0068),
                    )
            )
        },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFFFF0068), Color(0xFF6200EE))
                        )
                    )
                    .padding(it)
            ) {
                if (isLoading) {
                    // Progress indicator
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(progress = progress / 100f)
                        Text(text = "Loading... $progress%", fontSize = 20.sp)
                    }
                } else {
                    if (productList.isEmpty()) {
                        // No products found
                        Text(text = "No Houses Found")
                    } else {
                        // Products list
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.Center,
                            contentPadding = PaddingValues(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            items(productList.take(displayedProductCount)) { product ->
                                ProductListItem(product) {
                                    navController.navigate("productDetail/${product.id}")
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        // Load More Button
                        if (displayedProductCount < productList.size) {
                            Button(
                                onClick = { displayedProductCount += 8 },
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Text(text = "Load More")
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun ProductListItem(product: Product, onItemClick: (String) -> Unit) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                .padding(8.dp)
                .clickable { onItemClick(product.id) }
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White) // Adjust background color as needed
        ) {
            Row(
                modifier = Modifier.padding(16.dp)
            ) {
                // Product Image
                Image(
                    painter = rememberImagePainter(product.imageUrl),
                    contentDescription = null,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(16.dp))
                // Product Details Column
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically)
                ) {
                    // Product Name
                    Text(
                        text = "Name: ${product.name}",
                        style = MaterialTheme.typography.subtitle1,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "Description: ${product.description}",
                        color = Color.Black,
                        fontSize = 12.sp
                    )
                    // Product Price
                    Text(
                        text = "Price: $${product.price}",
                        style = MaterialTheme.typography.body2,
                        color = Color.Gray
                    )
                }
            }
        }
    }

suspend fun fetchProducts(onSuccess: (List<Product>) -> Unit) {
    val firestore = Firebase.firestore
    val snapshot = firestore.collection("products").get().await()
    val productList = snapshot.documents.mapNotNull { doc ->
        val product = doc.toObject<Product>()
        product?.id = doc.id
        product
    }
    onSuccess(productList)
}

suspend fun fetchProduct(productId: String, onSuccess: (Product?) -> Unit) {
    val firestore = Firebase.firestore
    val docRef = firestore.collection("products").document(productId)
    val snapshot = docRef.get().await()
    val product = snapshot.toObject<Product>()
    onSuccess(product)
}


