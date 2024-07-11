package net.ezra.ui.home

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Label
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.VerticalAlignmentLine
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role.Companion.Button
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberImagePainter
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import net.ezra.R
import net.ezra.navigation.ROUTE_ADD_PRODUCT
import net.ezra.navigation.ROUTE_HOME
import net.ezra.navigation.ROUTE_LOGIN
import net.ezra.navigation.ROUTE_SHOPPING_CART
import net.ezra.navigation.ROUTE_USER_DASHBOARD
import net.ezra.navigation.ROUTE_VIEW_PROD
import net.ezra.navigation.ROUTE_VIEW_SPECIALOFFER
import net.ezra.ui.products.Product
import net.ezra.ui.products.ProductListItem
import net.ezra.ui.products.fetchProducts

data class Screen(val title: String, val icon: Int)

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "ResourceAsColor", "UnusedMaterialScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController) {
    var isDrawerOpen by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var locationEnabled by remember { mutableStateOf(true) }
    var productList by remember { mutableStateOf(emptyList<Product>()) }
    var specialOfferList by remember { mutableStateOf(emptyList<Product>()) }
    var isLoading by remember { mutableStateOf(true) }
    var displayedProductCount by remember { mutableStateOf(1) }
    var progress by remember { mutableStateOf(0) }
    var userEmail by remember { mutableStateOf("No Email") }

    val callLauncher: ManagedActivityResultLauncher<Intent, ActivityResult> =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) { _ -> }

    LaunchedEffect(Unit) {
        fetchSpecialOffer { specialOffer ->
            specialOfferList = specialOffer
        }
        fetchProducts { fetchedProducts ->
            productList = fetchedProducts
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            HomeTopBar(
                searchQuery,
                onSearchQueryChange = { searchQuery = it },
                locationEnabled,
                onLocationToggle = { locationEnabled = !locationEnabled })
        },
        content = {
            HomeContent(
                navController,
                isDrawerOpen,
                onDrawerClose = { isDrawerOpen = false },
                isLoading,
                productList,
                progress,
                specialOfferList
            )
        },
        bottomBar = { BottomBar(navController = navController) },
//        isFloatingActionButtonDocked = true,
        floatingActionButtonPosition = FabPosition.Center
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(searchQuery: String, onSearchQueryChange: (String) -> Unit, locationEnabled: Boolean, onLocationToggle: () -> Unit) {
    var userName by remember { mutableStateOf("Guest") }
    var profilePictureUrl by remember { mutableStateOf("https://via.placeholder.com/150") } // Default profile picture URL

    // Fetch user data from Firebase Authentication
    LaunchedEffect(Unit) {
        val user = Firebase.auth.currentUser
        if (user != null) {
            userName = user.displayName ?: "No Name"
            profilePictureUrl = user.photoUrl?.toString() ?: profilePictureUrl
        }
    }

    Column(
        modifier = Modifier
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFFF009E), Color(0xFFC60084))
                )
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Image(painter = painterResource(id = R.drawable.air_bnb_), contentDescription ="logo" )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search Icon",
                tint = Color.White,
                modifier = Modifier.padding(end = 8.dp)
            )
            BasicTextField(
                value = searchQuery,
                onValueChange = { searchQuery },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, shape = CircleShape)
                    .height(50.dp)
                    .padding(horizontal = 16.dp),
            ) { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = "Looking For Shoes",
                            color = Color.Magenta,
                            fontSize = 16.sp,
                        )
                    } else {
                        innerTextField()
                    }
                }
            }
        }
    }
}
@Composable
fun HomeContent(
    navController: NavHostController,
    isDrawerOpen: Boolean,
    onDrawerClose: () -> Unit,
    isLoading: Boolean,
    productList: List<Product>,
    progress: Int,
    specialOfferList: List<Product>
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { if (isDrawerOpen) onDrawerClose() }
    ) {
        Spacer(modifier = Modifier.height(80.dp))
        Column(

            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFFF0068), Color(0xFF6200EE))
                    )
                ),
        ) {
            Spacer(modifier = Modifier.height(250.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = {
                    navController.navigate(ROUTE_VIEW_PROD) {
                        popUpTo(ROUTE_HOME) { inclusive = true }
                    }
                },
                    colors = ButtonDefaults.buttonColors(Color.White),

                    )
                {
                    Image(
                        painter = painterResource(id = R.drawable.home), // Replace with your image resource
                        contentDescription = "shoes icon",
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("houses",
                        color = Color.Black,)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    navController.navigate(ROUTE_VIEW_SPECIALOFFER) {
                        popUpTo(ROUTE_HOME) { inclusive = true }
                    }
                },
                    colors = ButtonDefaults.buttonColors(Color.White)
                )
                {
                    Image(
                        painter = painterResource(id = R.drawable.special_offer), // Replace with your image resource
                        contentDescription = "shoes icon",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Affordable",
                        color = Color.Black)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(
                horizontalAlignment = CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
            ) {

                items(specialOfferList.take(5)) { specialOffer ->
                    SpecialOfferItem(specialOffer)
                }
            }
        }
    }
}

@SuppressLint("SuspiciousIndentation")
@Composable
fun BottomBar(navController: NavHostController) {
    val selectedIndex = remember { mutableStateOf(0) }
    val context = LocalContext.current
    val sharedPreferences: SharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
    val isLoggedIn: Boolean = sharedPreferences.getBoolean("isLoggedIn", false)

    BottomAppBar(containerColor = Color.Transparent)
    {
        BottomNavigation(
            backgroundColor = Color(0xFF6200EE),
            modifier = Modifier
                .width(400.dp)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
        ) {
            BottomNavigationItem(
                icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Home", tint = Color.White) },
                selected = (selectedIndex.value == 0),
                onClick = {
                    selectedIndex.value = 0
                    navController.navigate("home") { popUpTo("home") { inclusive = true } }
                }
            )
            BottomNavigationItem(
                icon = { Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = "Profile", tint = Color.White) },
                selected = (selectedIndex.value == 3),
                onClick = {
                    selectedIndex.value = 3
                    navController.navigate(ROUTE_SHOPPING_CART) { popUpTo("home") { inclusive = true } }
                }
            )
            BottomNavigationItem(
                icon = { Icon(imageVector = Icons.Default.AccountCircle, contentDescription = "Profile", tint = Color.White) },
                label = { Text(text = "Profile", color = Color.White) },
                selected = (selectedIndex.value == 3),
                onClick = {
                    selectedIndex.value = 3
                    if (isLoggedIn) {
                        navController.navigate(ROUTE_USER_DASHBOARD) { popUpTo("home") { inclusive = true } }
                    } else {
                        navController.navigate(ROUTE_LOGIN) { popUpTo(ROUTE_HOME) { inclusive = true } }
                    }
                }
            )
            BottomNavigationItem(
                icon = { Icon(imageVector = Icons.Default.Favorite, contentDescription = "Favorite", tint = Color.White) },
                selected = (selectedIndex.value == 2),
                onClick = {
                    selectedIndex.value = 2
                }
            )
            BottomNavigationItem(
                icon = { Icon(imageVector = Icons.Default.Notifications, contentDescription = "Profile", tint = Color.White) },
                selected = (selectedIndex.value == 3),
                onClick = {
                    selectedIndex.value = 3
                }
            )
        }
    }
}

private suspend fun fetchSpecialOffer(onSuccess: (List<Product>) -> Unit) {
    val firestore = Firebase.firestore
    val snapshot = firestore.collection("special offers").get().await()
    val productList = snapshot.documents.mapNotNull { doc ->
        val product = doc.toObject<Product>()
        product?.id = doc.id
        product
    }
    onSuccess(productList)
}

data class SpecialOffer(val discount: String, val productName: String, val description: String, val price: String, val imageUrl: String)

@Composable
fun SpecialOfferItem(offer: Product) {
    Column (
        modifier = Modifier,
        horizontalAlignment = CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ){
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(Color.White),
            modifier = Modifier
                .width(350.dp)
                .clickable { /* Handle item click */ },
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Image(
                    painter = rememberImagePainter(data = offer.imageUrl),
                    contentDescription = null,
                    modifier = Modifier
                        .size(130.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop,
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Text(
                        text = offer.name,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = offer.description,
                        color = Color.Black,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${offer.price}",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}
private suspend fun fetchSingleSpecialOffer(onComplete: (SpecialOffer?) -> Unit) {
    val db = Firebase.firestore
    val collectionRef = db.collection("special offers")

    // Assuming "offer1" is the document ID you want to fetch
    val documentId = "4da8be58-a2d2-42e3-82e3-cfd29bcce340"

    collectionRef.document(documentId)
        .get()
        .addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val data = snapshot.data
                val discount = data?.get("discount") as? String ?: "N/A"
                val productName = data?.get("name") as? String ?: "N/A"
                val description = data?.get("description") as? String ?: "N/A"
                val price = data?.get("price") as? Double ?: "N/A"
                val imageUrl = data?.get("imageUrl") as? String ?: ""

                onComplete(
                    SpecialOffer(
                        discount, productName, description,
                        price.toString(), imageUrl
                    )
                )
            } else {
                onComplete(null)
            }
        }
        .addOnFailureListener { exception ->
            // Handle error
            onComplete(null)
        }
}
