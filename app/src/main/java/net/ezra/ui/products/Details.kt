package net.ezra.ui.products

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await


@SuppressLint("UnusedMaterialScaffoldPaddingParameter", "UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(navController: NavController, productId: String) {
    var product by remember { mutableStateOf<Product?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(productId) {
        isLoading = true
        product = fetchProduct(productId)
        isLoading = false
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = product?.name ?: "Details",
                        fontSize = 24.sp,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "backIcon", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFFF0068),
                    titleContentColor = Color.White,
                )
            )
        },
        content = {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                product?.let { product ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color(0xFFFF0068), Color(0xFF6200EE))
                                )
                            )
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Spacer(modifier = Modifier.height(50.dp))

                        Image(
                            painter = rememberImagePainter(product.imageUrl),
                            contentDescription = null,
                            modifier = Modifier
                                .size(200.dp)
                                .padding(8.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .background(Color.White, shape = MaterialTheme.shapes.medium),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Name: ${product.name}",
                            style = MaterialTheme.typography.h5.copy(fontWeight = FontWeight.Bold),
                            color = Color.Black,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Price: $${product.price}",
                            style = MaterialTheme.typography.subtitle1.copy(color = Color.Black),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Description: ${product.description}",
                            style = MaterialTheme.typography.body1,
                            color = Color.Black,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Buy Button
                        Button(
                            onClick = { addToCartAndNavigate(navController, product) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .shadow(2.dp, RoundedCornerShape(15.dp))
                        ) {
                            Text("Buy", fontSize = 18.sp)
                        }
                    }
                }
            }
        }
    )
}

private fun addToCartAndNavigate(navController: NavController, product: Product) {
    CartState.addToCart(CartItem(product))
    navController.navigateUp()
}

suspend fun fetchProduct(productId: String): Product? {
    val db = FirebaseFirestore.getInstance()
    val productsCollection = db.collection("products")
    val productCollection1 = db.collection("special offers")

    return try {
        val documentSnapshot = productsCollection.document(productId).get().await()

        if (documentSnapshot.exists()) {
            val productData = documentSnapshot.data ?: return null
            Product(
                id = productId,
                name = productData["name"] as String,
                description = productData["description"] as String,
                price = (productData["price"] as? Double) ?: 0.0,
                imageUrl = productData["imageUrl"] as? String ?: ""
            )
        } else {
            val documentSnapshot1 = productCollection1.document(productId).get().await()
            val productData = documentSnapshot1.data ?: return null
            Product(
                id = productId,
                name = productData["name"] as String,
                description = productData["description"] as String,
                price = (productData["price"] as? Double) ?: 0.0,
                imageUrl = productData["imageUrl"] as? String ?: ""
            )
        }
    } catch (e: Exception) {
        null
    }
}

data class CartItem(val product: Product)
