package net.ezra.ui.products

import android.annotation.SuppressLint
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.AlertDialog
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import net.ezra.navigation.ROUTE_VIEW_USER_PRODUCTS
import java.util.UUID

@SuppressLint("UnusedMaterialScaffoldPaddingParameter", "UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(navController: NavController, onProductAdded: () -> Unit) {
    var productName by remember { mutableStateOf("") }
    var productDescription by remember { mutableStateOf("") }
    var productPrice by remember { mutableStateOf("") }
    var productImageUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // Track if fields are empty
    var productNameError by remember { mutableStateOf(false) }
    var productDescriptionError by remember { mutableStateOf(false) }
    var productPriceError by remember { mutableStateOf(false) }
    var productImageError by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            productImageUri = it
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = "Add Products", fontSize = 24.sp, color = Color.White)
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate(ROUTE_VIEW_USER_PRODUCTS)
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            "backIcon",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Gray,
                    titleContentColor = Color.Black,
                )
            )
        },
        content = {
            if (isLoading) {
                LoadingDialog()
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .padding(16.dp) ,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                item {
                    Spacer(modifier = Modifier.height(70.dp))
                }
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(Color.Gray)
                            .padding(16.dp)
                            .clickable { launcher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (productImageUri != null) {
                            Image(
                                painter = rememberImagePainter(productImageUri),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text("Tap to select an image", color = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = productName,
                        onValueChange = { productName = it },
                        label = { Text("Product Name") },
                        isError = productNameError,
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color(0xff0FB06A),
                            unfocusedBorderColor = Color.Gray,
                            unfocusedLabelColor = Color.Gray,
                            focusedLabelColor = Color.White,
                            cursorColor = Color(0xff0FB06A),
                            textColor = Color.White
                        )

                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = productDescription,
                        onValueChange = { productDescription = it },
                        label = { Text("Product Description") },
                        isError = productDescriptionError,
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color(0xff0FB06A),
                            unfocusedBorderColor = Color.Gray,
                            unfocusedLabelColor = Color.Gray,
                            focusedLabelColor = Color.White,
                            cursorColor = Color(0xff0FB06A),
                            textColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = productPrice,
                        onValueChange = { productPrice = it },
                        label = { Text("Product Price") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        keyboardActions = KeyboardActions(onDone = { /* Handle Done action */ }),
                        isError = productPriceError,
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color(0xff0FB06A),
                            unfocusedBorderColor = Color.Gray,
                            unfocusedLabelColor = Color.Gray,
                            focusedLabelColor = Color.White,
                            cursorColor = Color(0xff0FB06A),
                            textColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (productNameError) {
                        Text("Product Name is required", color = Color.Red)
                    }
                    if (productDescriptionError) {
                        Text("Product Description is required", color = Color.Red)
                    }
                    if (productPriceError) {
                        Text("Product Price is required", color = Color.Red)
                    }
                    if (productImageError) {
                        Text("Product Image is required", color = Color.Red)
                    }

                    Button(
                        onClick = {
                            // Reset error flags
                            productNameError = productName.isBlank()
                            productDescriptionError = productDescription.isBlank()
                            productPriceError = productPrice.isBlank()
                            productImageError = productImageUri == null

                            // Add product if all fields are filled
                            if (!productNameError && !productDescriptionError && !productPriceError && !productImageError) {
                                isLoading = true
                                addProductToFirestore(
                                    navController,
                                    onProductAdded,
                                    productName,
                                    productDescription,
                                    productPrice.toDouble(),
                                    productImageUri,
                                    onLoadingChange = { isLoading = it }
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(Color.Gray),
                        modifier = Modifier
                            .clickable(indication = rememberRipple(bounded = true), interactionSource = remember { MutableInteractionSource() }) { /* Handle click */ }
                            .padding(16.dp),
                        shape = MaterialTheme.shapes.small

                    ) {
                        Text("Add Product", color = Color.White, fontSize = 16.sp)
                    }
                }
            }
        }
    )
}

@Composable
fun LoadingDialog() {
    AlertDialog(
        onDismissRequest = {},
        title = {
            Text(text = "Loading")
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Please wait while we add your product")
            }
        },
        buttons = {}
    )
}

private fun addProductToFirestore(
    navController: NavController,
    onProductAdded: () -> Unit,
    productName: String,
    productDescription: String,
    productPrice: Double,
    productImageUri: Uri?,
    onLoadingChange: (Boolean) -> Unit
) {
    if (productName.isEmpty() || productDescription.isEmpty() || productPrice.isNaN() || productImageUri == null) {
        // Validate input fields
        return
    }

    val productId = UUID.randomUUID().toString()
    val currentUser = FirebaseAuth.getInstance().currentUser

    if (currentUser == null) {
        // Handle user not logged in
        onLoadingChange(false)
        return
    }

    val firestore = Firebase.firestore
    val productData = hashMapOf(
        "name" to productName,
        "description" to productDescription,
        "price" to productPrice,
        "imageUrl" to "",
        "userId" to currentUser.uid  // Associate product with the current user
    )

    firestore.collection("products").document(productId)
        .set(productData)
        .addOnSuccessListener {
            uploadImageToStorage(productId, productImageUri) { imageUrl ->
                firestore.collection("products").document(productId)
                    .update("imageUrl", imageUrl)
                    .addOnSuccessListener {
                        // Display toast message
                        Toast.makeText(
                            navController.context,
                            "Product added successfully!",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Navigate to the user's products screen
                        navController.navigate(ROUTE_VIEW_USER_PRODUCTS)

                        // Invoke the onProductAdded callback
                        onProductAdded()

                        // Hide the loading dialog
                        onLoadingChange(false)
                    }
                    .addOnFailureListener { e ->
                        // Handle error updating product document
                        // Hide the loading dialog
                        onLoadingChange(false)
                    }
            }
        }
        .addOnFailureListener { e ->
            // Handle error adding product to Firestore
            // Hide the loading dialog
            onLoadingChange(false)
        }
}

private fun uploadImageToStorage(productId: String, imageUri: Uri?, onSuccess: (String) -> Unit) {
    if (imageUri == null) {
        onSuccess("")
        return
    }

    val storageRef = Firebase.storage.reference
    val imagesRef = storageRef.child("products/$productId.jpg")

    imagesRef.putFile(imageUri)
        .addOnSuccessListener { taskSnapshot ->
            imagesRef.downloadUrl
                .addOnSuccessListener { uri ->
                    onSuccess(uri.toString())
                }
                .addOnFailureListener {
                    // Handle failure to get download URL
                }
        }
        .addOnFailureListener {
            // Handle failure to upload image
        }
}
