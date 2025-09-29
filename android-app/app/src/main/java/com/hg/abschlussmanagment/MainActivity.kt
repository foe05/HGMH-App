package com.hg.abschussmanagment

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.hg.abschussmanagment.data.UserPreferencesRepository
import com.hg.abschussmanagment.network.WpApiClientProvider
import com.hg.abschussmanagment.ui.FormsScreensKt.FormSubmitScreen
import com.hg.abschussmanagment.ui.FormsScreensKt.FormsListScreen
import com.hg.abschussmanagment.ui.LoginScreen
import com.hg.abschussmanagment.ui.OnboardingScreen
import com.hg.abschussmanagment.util.ConnectivityObserver

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                val navController = rememberNavController()
                AppNavHost(navController)
            }
        }
    }
}

@Composable
private fun AppNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            val ctx = LocalContext.current
            val baseUrl by UserPreferencesRepository.baseUrlFlow(ctx).collectAsState(initial = null)
            val jwt by UserPreferencesRepository.jwtFlow(ctx).collectAsState(initial = null)
            androidx.compose.runtime.LaunchedEffect(baseUrl, jwt) {
                when {
                    baseUrl.isNullOrBlank() -> navController.navigate("onboarding") { popUpTo("splash") { inclusive = true } }
                    jwt.isNullOrBlank() -> navController.navigate("login") { popUpTo("splash") { inclusive = true } }
                    else -> navController.navigate("forms") { popUpTo("splash") { inclusive = true } }
                }
            }
        }
        composable("onboarding") {
            val ctx = LocalContext.current
            val baseUrl by UserPreferencesRepository.baseUrlFlow(ctx).collectAsState(initial = null)
            OnboardingScreen(
                initialUrl = baseUrl,
                validateAndSave = { url ->
                    val client = WpApiClientProvider.httpClient
                    val request = WpApiClientProvider.buildJsonGet(url, "/wp-json")
                    try {
                        client.newCall(request).execute().use { resp ->
                            val ok = resp.isSuccessful
                            if (ok) UserPreferencesRepository.setBaseUrl(ctx, url)
                            ok
                        }
                    } catch (e: Exception) {
                        false
                    }
                },
                onSuccess = { navController.navigate("login") { popUpTo("onboarding") { inclusive = true } } }
            )
        }
        composable("login") {
            val ctx = LocalContext.current
            val baseUrl by UserPreferencesRepository.baseUrlFlow(ctx).collectAsState(initial = null)
            if (!baseUrl.isNullOrBlank()) {
                LoginScreen(context = ctx, baseUrl = baseUrl!!, onSuccess = {
                    navController.navigate("forms") { popUpTo("login") { inclusive = true } }
                })
            }
        }
        composable("forms") {
            val ctx = LocalContext.current
            val baseUrl by UserPreferencesRepository.baseUrlFlow(ctx).collectAsState(initial = null)
            val jwt by UserPreferencesRepository.jwtFlow(ctx).collectAsState(initial = null)
            if (!baseUrl.isNullOrBlank() && !jwt.isNullOrBlank()) {
                FormsListScreen(context = ctx, baseUrl = baseUrl!!, jwt = jwt!!, nav = navController)
            }
        }
        composable("form/{id}") { backStack ->
            val ctx = LocalContext.current
            val baseUrl by UserPreferencesRepository.baseUrlFlow(ctx).collectAsState(initial = null)
            val jwt by UserPreferencesRepository.jwtFlow(ctx).collectAsState(initial = null)
            val id = backStack.arguments?.getString("id") ?: return@composable
            if (!baseUrl.isNullOrBlank() && !jwt.isNullOrBlank()) {
                FormSubmitScreen(context = ctx, baseUrl = baseUrl!!, jwt = jwt!!, formId = id) {
                    navController.popBackStack()
                }
            }
        }
    }
}
