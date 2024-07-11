package net.ezra.ui.dashboard

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberImagePainter
import com.google.firebase.auth.FirebaseAuth
import net.ezra.navigation.ROUTE_ADD_PRODUCT
import net.ezra.navigation.ROUTE_ADD_SPECIALOFFER
import net.ezra.navigation.ROUTE_ADD_STUDENTS
import net.ezra.navigation.ROUTE_HOME
import net.ezra.navigation.ROUTE_LOGIN
import net.ezra.navigation.ROUTE_VIEW_USER_PRODUCTS

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDashboardScreen(navController: NavHostController) {
    val context = LocalContext.current
    val sharedPreferences: SharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
    val isLoggedIn: Boolean = sharedPreferences.getBoolean("isLoggedIn", false)
    var userEmail by remember { mutableStateOf("No Email") }
    var userName by remember { mutableStateOf("No Name") }
    var profilePictureUrl by remember { mutableStateOf("https://via.placeholder.com/150") }

    if (!isLoggedIn) {
        LaunchedEffect(Unit) {
            navController.navigate(ROUTE_LOGIN) {
                popUpTo(navController.graph.startDestinationId) {
                    inclusive = true
                }
            }
        }
    } else {
        LaunchedEffect(Unit) {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                userEmail = currentUser.email ?: "No Email"
                userName = currentUser.displayName ?: "No Name"
                profilePictureUrl = currentUser.photoUrl?.toString() ?: profilePictureUrl
            }
        }

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Dashboard",
                            fontSize = 20.sp,
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            navController.navigate(ROUTE_HOME) {
                                popUpTo(navController.graph.startDestinationId) {
                                    inclusive = true
                                }
                            }
                        }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White)
                        }
                        IconButton(onClick = {
                            val editor = sharedPreferences.edit()
                            editor.putBoolean("isLoggedIn", false)
                            editor.apply()
                            navController.navigate(ROUTE_LOGIN) {
                                popUpTo(navController.graph.startDestinationId) {
                                    inclusive = true
                                }
                            }
                        }) {
                            Icon(Icons.Default.Settings, contentDescription = "Logout", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color(0xFFFF0068)
                    )
                )
            },
            content = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFFFF0068), Color(0xFF6200EE))
                            )
                        )
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White, shape = RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Welcome: $userName",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 32.sp,
                                color = Color(0xFFFF0068),
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(bottom = 16.dp),
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Email: $userEmail",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 20.sp,
                                color = Color(0xFFFF0068)
                            ),
                            modifier = Modifier.padding(bottom = 16.dp),
                            textAlign = TextAlign.Center
                        )
                        Row() {
                            Column {
                                Button(
                                    onClick = {
                                        navController.navigate(ROUTE_HOME) {
                                            popUpTo(navController.graph.startDestinationId) {
                                                inclusive = true
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .padding(bottom = 16.dp),
                                    colors = ButtonDefaults.buttonColors(Color(0xFFFF0068))
                                ) {
                                    Text("Go to Home", color = Color.White)
                                }
                                Button(
                                    onClick = {
                                        navController.navigate(ROUTE_ADD_SPECIALOFFER)
                                    },
                                    modifier = Modifier
                                        .padding(bottom = 16.dp),
                                    colors = ButtonDefaults.buttonColors(Color(0xFFFF0068))
                                ) {
                                    Text("Add Affordable")
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Button(
                                        onClick = {
                                            navController.navigate(ROUTE_ADD_PRODUCT)
                                        },
                                        modifier = Modifier
                                            .padding(bottom = 16.dp),
                                        colors = ButtonDefaults.buttonColors(Color(0xFFFF0068))
                                    ) {
                                        Text("Add Product")
                                    }
                                Button(
                                    onClick = {
                                        navController.navigate(ROUTE_VIEW_USER_PRODUCTS)
                                    },
                                    modifier = Modifier
                                        .padding(bottom = 16.dp),
                                    colors = ButtonDefaults.buttonColors(Color(0xFFFF0068))
                                ) {
                                    Text("View My Products")
                                }
                            }
                            }
                        Row() {
                            Button(
                                onClick = {
                                    FirebaseAuth.getInstance().signOut()
                                    val editor = sharedPreferences.edit()
                                    editor.putBoolean("isLoggedIn", false)
                                    editor.apply()
                                    navController.navigate(ROUTE_LOGIN) {
                                        popUpTo(navController.graph.startDestinationId) {
                                            inclusive = true
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .padding(bottom = 16.dp),
                                colors = ButtonDefaults.buttonColors(Color.Red)
                            ) {
                                Text("Logout", color = Color.White)
                            }
                        }
                    }
                }
            }
        )
    }
}
