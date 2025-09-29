package com.hg.abschlussmanagement.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hg.abschlussmanagement.data.models.*
import com.hg.abschlussmanagement.data.repository.ErfassungRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ErfassungViewModel @Inject constructor(
    private val erfassungRepository: ErfassungRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ErfassungUiState())
    val uiState: StateFlow<ErfassungUiState> = _uiState.asStateFlow()
    
    init {
        loadStammdaten()
    }
    
    private fun loadStammdaten() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val wildarten = erfassungRepository.getWildarten()
                val kategorien = erfassungRepository.getKategorien()
                val jagdgebiete = erfassungRepository.getJagdgebiete()
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    wildarten = wildarten,
                    kategorien = kategorien,
                    jagdgebiete = jagdgebiete
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Fehler beim Laden der Stammdaten: ${e.message}"
                )
            }
        }
    }
    
    fun saveErfassung(
        wusNummer: String,
        wildartId: Int,
        kategorieId: Int,
        jagdgebietId: Int,
        bemerkungen: String,
        interneNotiz: String
    ) {
        // Validation
        val wusError = validateWusNummer(wusNummer)
        val wildartError = if (wildartId == 0) "Wildart muss ausgewählt werden" else null
        val kategorieError = if (kategorieId == 0) "Kategorie muss ausgewählt werden" else null
        val jagdgebietError = if (jagdgebietId == 0) "Jagdgebiet muss ausgewählt werden" else null
        
        if (wusError != null || wildartError != null || kategorieError != null || jagdgebietError != null) {
            _uiState.value = _uiState.value.copy(
                wusError = wusError,
                wildartError = wildartError,
                kategorieError = kategorieError,
                jagdgebietError = jagdgebietError
            )
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                wusError = null,
                wildartError = null,
                kategorieError = null,
                jagdgebietError = null
            )
            
            try {
                val erfassungRequest = ErfassungRequest(
                    wusNummer = wusNummer,
                    wildartId = wildartId,
                    kategorieId = kategorieId,
                    jagdgebietId = jagdgebietId,
                    bemerkungen = bemerkungen,
                    interneNotiz = interneNotiz
                )
                
                erfassungRepository.createErfassung(erfassungRequest)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSaved = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Fehler beim Speichern: ${e.message}"
                )
            }
        }
    }
    
    private fun validateWusNummer(wusNummer: String): String? {
        return when {
            wusNummer.isBlank() -> "WUS-Nummer ist erforderlich"
            wusNummer.length != 7 -> "WUS-Nummer muss 7-stellig sein"
            !wusNummer.all { it.isDigit() } -> "WUS-Nummer darf nur Zahlen enthalten"
            else -> null
        }
    }
}

data class ErfassungUiState(
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val wildarten: List<Wildart> = emptyList(),
    val kategorien: List<Kategorie> = emptyList(),
    val jagdgebiete: List<Jagdgebiet> = emptyList(),
    val error: String? = null,
    val wusError: String? = null,
    val wildartError: String? = null,
    val kategorieError: String? = null,
    val jagdgebietError: String? = null
)
