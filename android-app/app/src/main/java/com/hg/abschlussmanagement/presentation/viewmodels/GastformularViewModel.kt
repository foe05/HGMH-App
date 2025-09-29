package com.hg.abschlussmanagement.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hg.abschlussmanagement.data.models.OCRResult
import com.hg.abschlussmanagement.data.repository.GastformularRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GastformularViewModel @Inject constructor(
    private val gastformularRepository: GastformularRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(GastformularUiState())
    val uiState: StateFlow<GastformularUiState> = _uiState.asStateFlow()
    
    fun submitGastmeldung(
        melderName: String,
        melderEmail: String,
        melderTelefon: String,
        wusNummer: String,
        wildart: String,
        fundort: String,
        datum: String,
        bemerkungen: String
    ) {
        // Validation
        val nameError = if (melderName.isBlank()) "Name ist erforderlich" else null
        val emailError = if (melderEmail.isBlank()) "E-Mail ist erforderlich" 
                        else if (!isValidEmail(melderEmail)) "Ungültige E-Mail-Adresse" else null
        val wusError = if (wusNummer.isBlank()) "WUS-Nummer ist erforderlich"
                      else if (!wusNummer.matches(Regex("\\d{7}"))) "WUS-Nummer muss 7-stellig sein" else null
        val wildartError = if (wildart.isBlank()) "Wildart ist erforderlich" else null
        val fundortError = if (fundort.isBlank()) "Fundort ist erforderlich" else null
        val datumError = if (datum.isBlank()) "Datum ist erforderlich"
                        else if (!isValidDate(datum)) "Ungültiges Datum (TT.MM.JJJJ)" else null
        
        if (nameError != null || emailError != null || wusError != null || 
            wildartError != null || fundortError != null || datumError != null) {
            _uiState.value = _uiState.value.copy(
                nameError = nameError,
                emailError = emailError,
                wusError = wusError,
                wildartError = wildartError,
                fundortError = fundortError,
                datumError = datumError
            )
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                nameError = null,
                emailError = null,
                wusError = null,
                wildartError = null,
                fundortError = null,
                datumError = null
            )
            
            try {
                gastformularRepository.submitGastmeldung(
                    melderName = melderName,
                    melderEmail = melderEmail,
                    melderTelefon = melderTelefon,
                    wusNummer = wusNummer,
                    wildart = wildart,
                    fundort = fundort,
                    datum = datum,
                    bemerkungen = bemerkungen
                )
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSubmitted = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Fehler beim Absenden: ${e.message}"
                )
            }
        }
    }
    
    fun setOCRResult(result: OCRResult) {
        _uiState.value = _uiState.value.copy(ocrResult = result)
    }
    
    fun clearOCRResult() {
        _uiState.value = _uiState.value.copy(ocrResult = null)
    }
    
    private fun isValidEmail(email: String): Boolean {
        return email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))
    }
    
    private fun isValidDate(date: String): Boolean {
        return date.matches(Regex("\\d{1,2}\\.\\d{1,2}\\.\\d{4}"))
    }
}

data class GastformularUiState(
    val isLoading: Boolean = false,
    val isSubmitted: Boolean = false,
    val ocrResult: OCRResult? = null,
    val error: String? = null,
    val nameError: String? = null,
    val emailError: String? = null,
    val wusError: String? = null,
    val wildartError: String? = null,
    val fundortError: String? = null,
    val datumError: String? = null
)
