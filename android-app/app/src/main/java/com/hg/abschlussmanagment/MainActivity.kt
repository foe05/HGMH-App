package com.hg.abschussmanagment

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hg.abschussmanagment.data.UserPreferencesRepository
import com.hg.abschussmanagment.network.WpApiClientProvider
import com.hg.abschussmanagment.ui.OnboardingScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

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
    NavHost(navController = navController, startDestination = "onboarding") {
        composable("onboarding") {
            val activity = navController.context as ComponentActivity
            val baseUrl by UserPreferencesRepository.baseUrlFlow(activity).collectAsState(initial = null)
            OnboardingScreen(
                initialUrl = baseUrl,
                validateAndSave = { url ->
                    val client = WpApiClientProvider.httpClient
                    val request = WpApiClientProvider.buildJsonRequest(url, "/wp-json")
                    try {
                        client.newCall(request).execute().use { resp ->
                            val ok = resp.isSuccessful
                            if (ok) UserPreferencesRepository.setBaseUrl(activity, url)
                            ok
                        }
                    } catch (e: Exception) {
                        false
                    }
                },
                onSuccess = { navController.navigate("home") { popUpTo("onboarding") { inclusive = true } } }
            )
        }
        composable("home") {
            Column(modifier = Modifier.fillMaxSize()) {
                Text("Willkommen")
            }
        }
    }
}
