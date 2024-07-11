package net.ezra.ui.auth

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import net.ezra.navigation.ROUTE_LOGIN
import net.ezra.navigation.ROUTE_REGISTER

@Composable
fun SignUpScreen(navController: NavController, onSignUpSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var userImageUri by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            userImageUri = it
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Sign Up",
            style = MaterialTheme.typography.h4.copy(
                fontSize = 32.sp,
                color = Color.Magenta
            ),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color.Magenta)
                .clickable { launcher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            userImageUri?.let {
                Image(
                    painter = rememberImagePainter(it),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            } ?: Text("Tap to add photo", color = Color.White, textAlign = TextAlign.Center)
        }

        OutlinedTextField(
            value = userName,
            onValueChange = { userName = it },
            label = { Text("Username") },
            modifier = Modifier
                .width(350.dp)
                .padding(bottom = 8.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color.Magenta,
                cursorColor = Color.Magenta,
                textColor = Color.Black
            )
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
                cursorColor = Color.Magenta,
                textColor = Color.Black
            )
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .width(350.dp)
                .padding(bottom = 8.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color.Magenta,
                cursorColor = Color.Magenta,
                textColor = Color.Black
            )
        )

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .width(350.dp)
                .padding(bottom = 16.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color.Magenta,
                cursorColor = Color.Magenta,
                textColor = Color.Black
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
                    when {
                        email.isBlank() -> error = "Email is required"
                        password.isBlank() -> error = "Password is required"
                        confirmPassword.isBlank() -> error = "Password Confirmation required"
                        password != confirmPassword -> error = "Passwords do not match"
                        userName.isBlank() -> error = "Username is required"
                        userImageUri == null -> error = "Profile photo is required"
                        else -> {
                            isLoading = true
                            signUp(email, password, userName, userImageUri, {
                                isLoading = false
                                Toast.makeText(context, "Sign-up successful!", Toast.LENGTH_SHORT).show()
                                onSignUpSuccess()
                            }) { errorMessage ->
                                isLoading = false
                                error = errorMessage
                            }
                        }
                    }
                },
                modifier = Modifier
                    .width(100.dp)
                    .padding(bottom = 8.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Magenta),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Sign Up", color = Color.White)
            }

            Text(
                text = "Already have an account? Login",
                modifier = Modifier
                    .clickable {
                        navController.navigate(ROUTE_LOGIN) {
                            popUpTo(ROUTE_REGISTER) { inclusive = true }
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
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

private fun signUp(
    email: String,
    password: String,
    userName: String,
    userImageUri: Uri?,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    FirebaseAuth.getInstance().fetchSignInMethodsForEmail(email)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val signInMethods = task.result?.signInMethods ?: emptyList()
                if (signInMethods.isNotEmpty()) {
                    onError("Email is already registered")
                } else {
                    // Email is not registered, proceed with sign-up
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { signUpTask ->
                            if (signUpTask.isSuccessful) {
                                val user = FirebaseAuth.getInstance().currentUser
                                user?.let {
                                    val storageRef = FirebaseStorage.getInstance().reference
                                    val profileImagesRef = storageRef.child("profileImages/${user.uid}.jpg")

                                    userImageUri?.let { uri ->
                                        profileImagesRef.putFile(uri)
                                            .addOnSuccessListener { taskSnapshot ->
                                                profileImagesRef.downloadUrl.addOnSuccessListener { uri ->
                                                    val profileUpdates = userProfileChangeRequest {
                                                        displayName = userName
                                                        photoUri = uri
                                                    }
                                                    user.updateProfile(profileUpdates)
                                                        .addOnCompleteListener { profileUpdateTask ->
                                                            if (profileUpdateTask.isSuccessful) {
                                                                onSuccess()
                                                            } else {
                                                                onError(profileUpdateTask.exception?.message ?: "Sign-up failed")
                                                            }
                                                        }
                                                }
                                            }
                                            .addOnFailureListener { exception ->
                                                onError(exception.message ?: "Image upload failed")
                                            }
                                    }
                                } ?: run {
                                    onError("User not found after sign-up")
                                }
                            } else {
                                onError(signUpTask.exception?.message ?: "Sign-up failed")
                            }
                        }
                }
            } else {
                onError(task.exception?.message ?: "Failed to check email availability")
            }
        }
}
