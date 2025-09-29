package com.hg.abschlussmanagement.presentation.viewmodels

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.hg.abschlussmanagement.data.models.OCRResult
import com.hg.abschlussmanagement.data.repository.OCRRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import javax.inject.Inject

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val ocrRepository: OCRRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ScannerUiState())
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()
    
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    
    fun toggleFlash() {
        _uiState.value = _uiState.value.copy(flashEnabled = !_uiState.value.flashEnabled)
    }
    
    fun captureImage() {
        _uiState.value = _uiState.value.copy(isProcessing = true)
    }
    
    fun processImage(imageProxy: ImageProxy) {
        viewModelScope.launch {
            try {
                val bitmap = imageProxyToBitmap(imageProxy)
                val inputImage = InputImage.fromBitmap(bitmap, imageProxy.imageInfo.rotationDegrees)
                
                textRecognizer.process(inputImage)
                    .addOnSuccessListener { visionText ->
                        val ocrResult = parseOCRText(visionText.text)
                        _uiState.value = _uiState.value.copy(
                            isProcessing = false,
                            ocrResult = ocrResult
                        )
                    }
                    .addOnFailureListener { exception ->
                        _uiState.value = _uiState.value.copy(
                            isProcessing = false,
                            error = "OCR-Fehler: ${exception.message}"
                        )
                    }
                
                imageProxy.close()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    error = "Bildverarbeitungsfehler: ${e.message}"
                )
            }
        }
    }
    
    private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap {
        val buffer: ByteBuffer = imageProxy.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
    
    private fun parseOCRText(text: String): OCRResult {
        val lines = text.split("\n").map { it.trim() }
        
        var wusNummer: String? = null
        var wildart: String? = null
        var datum: String? = null
        var jagdgebiet: String? = null
        var erleger: String? = null
        var geschlecht: String? = null
        var altersklasse: String? = null
        
        for (line in lines) {
            when {
                // WUS-Nummer patterns
                line.contains(Regex("\\d{7}")) -> {
                    val match = Regex("\\d{7}").find(line)
                    wusNummer = match?.value
                }
                line.contains("Wildmarkennummer", ignoreCase = true) -> {
                    val match = Regex("\\d{7}").find(line)
                    wusNummer = match?.value
                }
                line.contains("WUS", ignoreCase = true) -> {
                    val match = Regex("\\d{7}").find(line)
                    wusNummer = match?.value
                }
                
                // Wildart patterns
                line.contains("Rotwild", ignoreCase = true) -> wildart = "Rotwild"
                line.contains("Damwild", ignoreCase = true) -> wildart = "Damwild"
                line.contains("Rehwild", ignoreCase = true) -> wildart = "Rehwild"
                line.contains("Schwarzwild", ignoreCase = true) -> wildart = "Schwarzwild"
                
                // Date patterns
                line.contains(Regex("\\d{1,2}\\.\\d{1,2}\\.\\d{2,4}")) -> {
                    val match = Regex("\\d{1,2}\\.\\d{1,2}\\.\\d{2,4}").find(line)
                    datum = match?.value
                }
                line.contains("Erlegungsdatum", ignoreCase = true) -> {
                    val match = Regex("\\d{1,2}\\.\\d{1,2}\\.\\d{2,4}").find(line)
                    datum = match?.value
                }
                
                // Jagdgebiet patterns
                line.contains("Jagdgebiet", ignoreCase = true) -> {
                    jagdgebiet = line.replace("Jagdgebiet", "", ignoreCase = true).trim()
                }
                line.contains("Revier", ignoreCase = true) -> {
                    jagdgebiet = line.replace("Revier", "", ignoreCase = true).trim()
                }
                
                // Erleger patterns
                line.contains("Erleger", ignoreCase = true) -> {
                    erleger = line.replace("Erleger", "", ignoreCase = true).trim()
                }
                
                // Geschlecht patterns
                line.contains("männlich", ignoreCase = true) -> geschlecht = "männlich"
                line.contains("weiblich", ignoreCase = true) -> geschlecht = "weiblich"
                
                // Altersklasse patterns
                line.contains(Regex("\\b[0-4]\\b")) -> {
                    val match = Regex("\\b[0-4]\\b").find(line)
                    altersklasse = match?.value
                }
            }
        }
        
        return OCRResult(
            wusNummer = wusNummer,
            wildart = wildart,
            datum = datum,
            jagdgebiet = jagdgebiet,
            erleger = erleger,
            geschlecht = geschlecht,
            altersklasse = altersklasse,
            rawText = text,
            confidence = 0.8f // Mock confidence
        )
    }
    
    fun clearResult() {
        _uiState.value = _uiState.value.copy(ocrResult = null)
    }
}

data class ScannerUiState(
    val isProcessing: Boolean = false,
    val flashEnabled: Boolean = false,
    val ocrResult: OCRResult? = null,
    val error: String? = null
)
