package net.ezra.ui

import android.content.res.Configuration
import android.view.animation.OvershootInterpolator
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import net.ezra.R
import net.ezra.navigation.ROUTE_HOME
import net.ezra.navigation.ROUTE_LOGIN

@Composable
fun SplashScreen(navController: NavHostController) {
    var scale by remember { mutableStateOf(0f) }
    var alpha by remember { mutableStateOf(0f) }
    var rotation by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        val animatableScale = Animatable(0f)
        val animatableAlpha = Animatable(0f)
        val animatableRotation = Animatable(0f)

        animatableScale.animateTo(
            targetValue = 0.7f,
            animationSpec = tween(
                durationMillis = 800,
                easing = { OvershootInterpolator(4f).getInterpolation(it) }
            )
        )

        animatableAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = LinearEasing)
        )

        animatableRotation.animateTo(
            targetValue = 720f,
            animationSpec = tween(durationMillis = 800, easing = LinearEasing)
        )

        scale = animatableScale.value
        alpha = animatableAlpha.value
        rotation = animatableRotation.value

        delay(3000L)
        navController.navigate(ROUTE_LOGIN) {
            popUpTo(navController.graph.startDestinationId) {
                inclusive = true
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Image(
                painter = painterResource(id = R.drawable.air_bnb_),
                contentDescription = "Logo",
                modifier = Modifier
                    .scale(2f)
                    .rotate(720f)
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
fun HomeScreenPreviewLight() {
    SplashScreen(rememberNavController())
}
