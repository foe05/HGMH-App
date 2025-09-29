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
class UebersichtViewModel @Inject constructor(
    private val erfassungRepository: ErfassungRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(UebersichtUiState())
    val uiState: StateFlow<UebersichtUiState> = _uiState.asStateFlow()
    
    fun loadErfassungen() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val erfassungen = erfassungRepository.getErfassungen()
                val wildarten = erfassungRepository.getWildarten()
                val jagdgebiete = erfassungRepository.getJagdgebiete()
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    erfassungen = erfassungen,
                    wildarten = wildarten,
                    jagdgebiete = jagdgebiete,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Fehler beim Laden der Erfassungen: ${e.message}"
                )
            }
        }
    }
    
    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        applyFilters()
    }
    
    fun setWildartFilter(wildartId: Int?) {
        _uiState.value = _uiState.value.copy(wildartFilter = wildartId)
        applyFilters()
    }
    
    fun setJagdgebietFilter(jagdgebietId: Int?) {
        _uiState.value = _uiState.value.copy(jagdgebietFilter = jagdgebietId)
        applyFilters()
    }
    
    private fun applyFilters() {
        val currentState = _uiState.value
        var filteredErfassungen = currentState.erfassungen
        
        // Apply search filter
        if (currentState.searchQuery.isNotBlank()) {
            val query = currentState.searchQuery.lowercase()
            filteredErfassungen = filteredErfassungen.filter { erfassung ->
                erfassung.wusNummer.contains(query, ignoreCase = true) ||
                erfassung.wildart.name.lowercase().contains(query) ||
                erfassung.kategorie.name.lowercase().contains(query) ||
                erfassung.jagdgebiet.name.lowercase().contains(query) ||
                erfassung.bemerkungen.lowercase().contains(query)
            }
        }
        
        // Apply wildart filter
        currentState.wildartFilter?.let { wildartId ->
            filteredErfassungen = filteredErfassungen.filter { it.wildart.id == wildartId }
        }
        
        // Apply jagdgebiet filter
        currentState.jagdgebietFilter?.let { jagdgebietId ->
            filteredErfassungen = filteredErfassungen.filter { it.jagdgebiet.id == jagdgebietId }
        }
        
        _uiState.value = currentState.copy(filteredErfassungen = filteredErfassungen)
    }
}

data class UebersichtUiState(
    val isLoading: Boolean = false,
    val erfassungen: List<Erfassung> = emptyList(),
    val filteredErfassungen: List<Erfassung> = emptyList(),
    val wildarten: List<Wildart> = emptyList(),
    val jagdgebiete: List<Jagdgebiet> = emptyList(),
    val searchQuery: String = "",
    val wildartFilter: Int? = null,
    val jagdgebietFilter: Int? = null,
    val error: String? = null
)
