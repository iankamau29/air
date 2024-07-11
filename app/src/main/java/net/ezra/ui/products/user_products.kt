package net.ezra.ui.products

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import net.ezra.navigation.ROUTE_USER_DASHBOARD

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "RememberReturnType", "SuspiciousIndentation")
@Composable
fun UserProductsScreen(navController: NavHostController) {
    var isLoading by remember { mutableStateOf(true) }
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userProducts = remember { mutableStateListOf<Product>() }
    val specialOffers = remember { mutableStateListOf<Product>() }

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            val db = Firebase.firestore

            // Fetch regular products
            db.collection("products")
                .whereEqualTo("userId", currentUser.uid)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val product = document.toObject(Product::class.java)
                        userProducts.add(product)
                    }
                    isLoading = false
                }
                .addOnFailureListener {
                    isLoading = false
                }

            // Fetch special offers
            db.collection("special offers")
                .whereEqualTo("userId", currentUser.uid)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val offer = document.toObject(Product::class.java)
                        specialOffers.add(offer)
                    }
                    isLoading = false
                }
                .addOnFailureListener {
                    isLoading = false
                }
        } else {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Your Products",
                        fontSize = 20.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate(ROUTE_USER_DASHBOARD) }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Gray)
            )
        },
        content = { paddingValues ->
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                val combinedProducts = userProducts + specialOffers
                if (combinedProducts.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "You have no products",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White)
                            .padding(paddingValues)  // Apply padding values provided by Scaffold
                    ) {
                        items(combinedProducts.size) { index ->
                            val product = combinedProducts[index]
                            if (userProducts.contains(product)) {
                                ProductItem(product) {
                                    navController.navigate("editProduct/${product.id}")
                                }
                            } else {
                                SpecialOfferItem(product) {
                                    navController.navigate("editProduct/${product.id}")
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun ProductItem(product: Product, onItemClick: (String) -> Unit) {
    Column(
        modifier = Modifier
            .width(180.dp)
            .padding(vertical = 8.dp, horizontal = 8.dp)
            .background(Color.White, shape = MaterialTheme.shapes.medium)
            .shadow(elevation = 4.dp, shape = MaterialTheme.shapes.medium)
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(Color.LightGray)
                .clickable { onItemClick(product.id) }
        ) {
            Image(
                painter = rememberImagePainter(data = product.imageUrl),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = product.name,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = product.description,
            fontSize = 14.sp,
            color = Color.Gray,
            maxLines = 2,
            modifier = Modifier.padding(end = 4.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "\$${product.price}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF43A047)
        )
    }
}

@Composable
fun SpecialOfferItem(product: Product, onItemClick: (String) -> Unit) {
    Column(
        modifier = Modifier
            .width(180.dp)
            .padding(vertical = 8.dp, horizontal = 8.dp)
            .background(Color.White, shape = MaterialTheme.shapes.medium)
            .shadow(elevation = 4.dp, shape = MaterialTheme.shapes.medium)
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(Color.LightGray)
                .clickable { onItemClick(product.id) }
        ) {
            Image(
                painter = rememberImagePainter(data = product.imageUrl),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = product.name,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = product.description,
            fontSize = 14.sp,
            color = Color.Gray,
            maxLines = 2,
            modifier = Modifier.padding(end = 4.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "\$${product.price}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF43A047)
        )
    }
}
