package net.ezra.ui.auth

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import net.ezra.navigation.ROUTE_DASHBOARD
import net.ezra.navigation.ROUTE_HOME
import net.ezra.navigation.ROUTE_LOGIN
import net.ezra.navigation.ROUTE_REGISTER

@Composable
fun LoginScreen(navController: NavController, onLoginSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)

    fun saveLoginStatus(isLoggedIn: Boolean) {
        sharedPreferences.edit().putBoolean("isLoggedIn", isLoggedIn).apply()
    }

    BackHandler {
        navController.popBackStack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AuthHead()

        Text(
            text = "Login",
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 32.sp,
                color = Color.Magenta,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier
                .width(350.dp)
                .padding(bottom = 8.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color.Magenta,
                unfocusedBorderColor = Color.Magenta,
                unfocusedLabelColor = Color.Magenta,
                focusedLabelColor = Color.Magenta,
                cursorColor = Color.Magenta,
                textColor = Color.Magenta
            )
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .width(350.dp)
                .padding(bottom = 16.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color.Magenta,
                unfocusedBorderColor = Color.Magenta,
                unfocusedLabelColor = Color.Magenta,
                focusedLabelColor = Color.Magenta,
                cursorColor = Color.Magenta,
                textColor = Color.Magenta
            )
        )

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(48.dp)
                    .padding(bottom = 16.dp),
                color = Color.Magenta
            )
        } else {
            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        error = "Please fill in all fields"
                    } else {
                        isLoading = true
                        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                isLoading = false
                                if (task.isSuccessful) {
                                    saveLoginStatus(true)
                                    onLoginSuccess()
                                    navController.navigate(ROUTE_HOME) {
                                        popUpTo(navController.graph.startDestinationId) {
                                            inclusive = true
                                        }
                                    }
                                } else {
                                    error = task.exception?.message ?: "Login failed"
                                }
                            }
                    }
                },
                modifier = Modifier
                    .width(100.dp)
                    .padding(bottom = 8.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Magenta)
            ) {
                Text("Login", color = Color.White)
            }

            Text(
                text = "Don't have an account? Register",
                modifier = Modifier
                    .clickable {
                        navController.navigate(ROUTE_REGISTER) {
                            popUpTo(ROUTE_LOGIN) { inclusive = true }
                        }
                    }
                    .padding(8.dp),
                textAlign = TextAlign.Center,
                color = Color.Magenta
            )
            Text(
                text = "SKIP AND GO HOME ?",
                modifier = Modifier
                    .clickable {
                        navController.navigate(ROUTE_HOME) {
                            popUpTo(ROUTE_LOGIN) { inclusive = true }
                        }
                    }
                    .padding(8.dp),
                textAlign = TextAlign.Center,
                color = Color.Magenta
            )
        }

        error?.let {
            Text(
                text = it,
                color = Color.Red,
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun AuthHead() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = "Welcome Back!",
            style = MaterialTheme.typography.headlineLarge.copy(
                color = Color.Magenta,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Please login to continue",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = Color.Magenta
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(navController = rememberNavController(), onLoginSuccess = {})
}
