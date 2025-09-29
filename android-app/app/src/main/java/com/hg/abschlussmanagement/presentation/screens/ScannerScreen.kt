package com.hg.abschlussmanagement.presentation.screens

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.hg.abschlussmanagement.presentation.viewmodels.ScannerViewModel
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    onBackClick: () -> Unit,
    onScanComplete: (com.hg.abschlussmanagement.data.models.OCRResult) -> Unit
) {
    val scannerViewModel: ScannerViewModel = hiltViewModel()
    val uiState by scannerViewModel.uiState.collectAsState()
    
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )
    
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }
    
    // Handle scan completion
    LaunchedEffect(uiState.ocrResult) {
        uiState.ocrResult?.let { result ->
            onScanComplete(result)
        }
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Top App Bar
        TopAppBar(
            title = { Text("Wildursprungsschein scannen") },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
                }
            },
            actions = {
                IconButton(onClick = { scannerViewModel.toggleFlash() }) {
                    Icon(
                        if (uiState.flashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "Blitz"
                    )
                }
            }
        )
        
        if (!hasCameraPermission) {
            // Permission denied state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Kamera-Berechtigung erforderlich",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Bitte erlauben Sie den Zugriff auf die Kamera, um Wildursprungsscheine zu scannen.",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }) {
                        Text("Berechtigung erteilen")
                    }
                }
            }
        } else {
            // Camera view
            Box(modifier = Modifier.fillMaxSize()) {
                // Camera preview
                CameraPreview(
                    modifier = Modifier.fillMaxSize(),
                    onImageCaptured = { imageProxy ->
                        scannerViewModel.processImage(imageProxy)
                    },
                    flashEnabled = uiState.flashEnabled,
                    cameraExecutor = cameraExecutor
                )
                
                // Overlay with scan area
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                ) {
                    // Scan area indicator
                    Box(
                        modifier = Modifier
                            .size(300.dp)
                            .align(Alignment.Center)
                            .border(
                                width = 2.dp,
                                color = Color.White,
                                shape = RoundedCornerShape(8.dp)
                            )
                    )
                    
                    // Instructions
                    Text(
                        text = "Richten Sie den Wildursprungsschein in den Rahmen",
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(16.dp)
                            .background(
                                Color.Black.copy(alpha = 0.7f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp),
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
                
                // Bottom controls
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(
                            Color.Black.copy(alpha = 0.8f),
                            RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                        )
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Capture button
                    Button(
                        onClick = { scannerViewModel.captureImage() },
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(36.dp)),
                        enabled = !uiState.isProcessing
                    ) {
                        if (uiState.isProcessing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = "Foto aufnehmen",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    
                    Text(
                        text = if (uiState.isProcessing) "Verarbeitung..." else "Tippen zum Scannen",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }
        }
        
        // Processing overlay
        if (uiState.isProcessing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.padding(32.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Texterkennung läuft...",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Bitte warten Sie, während der Wildursprungsschein analysiert wird.",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CameraPreview(
    modifier: Modifier = Modifier,
    onImageCaptured: (ImageProxy) -> Unit,
    flashEnabled: Boolean,
    cameraExecutor: ExecutorService
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    val preview = Preview.Builder().build()
    val imageCapture = ImageCapture.Builder()
        .setFlashMode(if (flashEnabled) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF)
        .build()
    
    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    
    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                
                val camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
                
                preview.setSurfaceProvider(previewView.surfaceProvider)
            }, cameraExecutor)
            
            previewView
        },
        modifier = modifier
    )
}
