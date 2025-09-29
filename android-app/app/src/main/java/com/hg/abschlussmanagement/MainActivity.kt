package com.hg.abschlussmanagement

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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hg.abschlussmanagement.presentation.screens.DashboardScreen
import com.hg.abschlussmanagement.presentation.screens.ErfassungScreen
import com.hg.abschlussmanagement.presentation.screens.ExportScreen
import com.hg.abschlussmanagement.presentation.screens.GastformularScreen
import com.hg.abschlussmanagement.presentation.screens.LoginScreen
import com.hg.abschlussmanagement.presentation.screens.ScannerScreen
import com.hg.abschlussmanagement.presentation.screens.UebersichtScreen
import com.hg.abschlussmanagement.presentation.screens.WebViewScreen
import com.hg.abschlussmanagement.presentation.screens.WelcomeScreen
import com.hg.abschlussmanagement.presentation.viewmodels.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    AppNavHost(navController)
                }
            }
        }
    }
}

@Composable
private fun AppNavHost(navController: NavHostController) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.authState.collectAsState()
    
    NavHost(navController = navController, startDestination = "welcome") {
        composable("welcome") {
            WelcomeScreen(
                onLoginClick = { navController.navigate("login") },
                onGuestClick = { navController.navigate("webview") },
                onGastformularClick = { navController.navigate("gastformular") }
            )
        }
        
        composable("login") {
            LoginScreen(
                onLoginSuccess = { navController.navigate("dashboard") { 
                    popUpTo("welcome") { inclusive = true } 
                }},
                onBackClick = { navController.popBackStack() }
            )
        }
        
        composable("webview") {
            WebViewScreen(
                onLoginClick = { navController.navigate("login") },
                onGastformularClick = { navController.navigate("gastformular") }
            )
        }
        
        composable("dashboard") {
            DashboardScreen(
                onLogout = { 
                    authViewModel.logout()
                    navController.navigate("welcome") { 
                        popUpTo(0) { inclusive = true } 
                    }
                },
                onNewErfassungClick = { navController.navigate("erfassung") },
                onUebersichtClick = { navController.navigate("uebersicht") },
                onExportClick = { navController.navigate("export") },
                onScannerClick = { navController.navigate("scanner") }
            )
        }
        
        composable("erfassung") {
            ErfassungScreen(
                onBackClick = { navController.popBackStack() },
                onSaveSuccess = { navController.popBackStack() }
            )
        }
        
        composable("uebersicht") {
            UebersichtScreen(
                onBackClick = { navController.popBackStack() },
                onNewErfassungClick = { navController.navigate("erfassung") },
                onErfassungClick = { id -> 
                    // TODO: Navigate to detail screen
                }
            )
        }
        
        composable("gastformular") {
            GastformularScreen(
                onBackClick = { navController.popBackStack() },
                onScanClick = { navController.navigate("scanner") },
                onSubmitSuccess = { navController.popBackStack() }
            )
        }
        
        composable("scanner") {
            ScannerScreen(
                onBackClick = { navController.popBackStack() },
                onScanComplete = { result ->
                    // Navigate back to the calling screen with OCR result
                    navController.popBackStack()
                }
            )
        }
        
        composable("export") {
            ExportScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
