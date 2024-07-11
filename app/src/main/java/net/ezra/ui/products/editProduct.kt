package net.ezra.ui.products

import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import net.ezra.R

@SuppressLint("UnusedMaterialScaffoldPaddingParameter", "UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateProductScreen(navController: NavController, productId: String) {
    var updatedProductName by remember { mutableStateOf("") }
    var updatedProductDescription by remember { mutableStateOf("") }
    var updatedProductPrice by remember { mutableStateOf("") }
    var updatedProductImageUri by remember { mutableStateOf<Uri?>(null) }
    var product by remember { mutableStateOf<Product?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    val firestore = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()

    LaunchedEffect(productId) {
        val document = firestore.collection("products").document(productId).get().await()
        val fetchedProduct = document.toObject(Product::class.java) ?: Product()
        product = fetchedProduct
        updatedProductName = fetchedProduct.name
        updatedProductDescription = fetchedProduct.description
        updatedProductPrice = fetchedProduct.price.toString()
        updatedProductImageUri = Uri.parse(fetchedProduct.imageUrl)
        isLoading = false
    }

    fun uploadImageToStorage(productId: String, imageUri: Uri?, onSuccess: (String) -> Unit) {
        if (imageUri != null) {
            val storageRef = storage.reference.child("product_images").child("$productId.jpg")
            val uploadTask = storageRef.putFile(imageUri)
            uploadTask.addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    onSuccess(uri.toString())
                }
            }
        }
    }

    val getContent = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        updatedProductImageUri = uri
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Edit Product",
                        fontSize = 24.sp,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigateUp()
                    }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            "backIcon",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Gray,
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
                            .background(Brush.verticalGradient(listOf(Color.White, Color.LightGray)))
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Spacer(modifier = Modifier.height(50.dp))

                        updatedProductImageUri?.let { uri ->
                            Image(
                                painter = rememberImagePainter(data = uri),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(200.dp)
                                    .padding(8.dp)
                                    .clip(MaterialTheme.shapes.medium)
                                    .background(Color.White, shape = MaterialTheme.shapes.medium),
                                contentScale = ContentScale.Crop
                            )
                        } ?: Image(
                            painter = painterResource(id = R.drawable.profile), // Placeholder image
                            contentDescription = null,
                            modifier = Modifier.size(120.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        TextField(
                            value = updatedProductName,
                            onValueChange = { updatedProductName = it },
                            label = { Text("Product Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(
                            value = updatedProductDescription,
                            onValueChange = { updatedProductDescription = it },
                            label = { Text("Product Description") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(
                            value = updatedProductPrice,
                            onValueChange = { updatedProductPrice = it },
                            label = { Text("Product Price") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = {
                            getContent.launch("image/*")
                        }) {
                            Text("Select Image")
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                val updatedProduct = Product(
                                    id = productId,
                                    name = updatedProductName,
                                    description = updatedProductDescription,
                                    price = updatedProductPrice.toDouble(),
                                    imageUrl = product.imageUrl // Retain existing image if no new image selected
                                )

                                if (updatedProductImageUri != null) {
                                    uploadImageToStorage(productId, updatedProductImageUri) { imageUrl ->
                                        updatedProduct.imageUrl = imageUrl
                                        firestore.collection("products").document(productId).set(updatedProduct)
                                        navController.popBackStack()
                                    }
                                } else {
                                    firestore.collection("products").document(productId).set(updatedProduct)
                                    navController.popBackStack()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color.Gray,
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .shadow(2.dp, RoundedCornerShape(15.dp))
                        ) {
                            Text("Save", fontSize = 18.sp)
                        }
                    }
                }
            }
        }
    )
}
