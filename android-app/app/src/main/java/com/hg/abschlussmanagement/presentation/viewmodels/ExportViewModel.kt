package com.hg.abschlussmanagement.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hg.abschlussmanagement.data.repository.ExportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExportViewModel @Inject constructor(
    private val exportRepository: ExportRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ExportUiState())
    val uiState: StateFlow<ExportUiState> = _uiState.asStateFlow()
    
    fun setOnlyOwnData(onlyOwn: Boolean) {
        _uiState.value = _uiState.value.copy(onlyOwnData = onlyOwn)
    }
    
    fun setIncludeInternalNotes(include: Boolean) {
        _uiState.value = _uiState.value.copy(includeInternalNotes = include)
    }
    
    fun setSendEmail(sendEmail: Boolean) {
        _uiState.value = _uiState.value.copy(sendEmail = sendEmail)
    }
    
    fun exportData(fromDate: String, toDate: String, format: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isExporting = true,
                error = null,
                exportSuccess = false
            )
            
            try {
                exportRepository.exportData(
                    fromDate = fromDate,
                    toDate = toDate,
                    format = format,
                    onlyOwnData = _uiState.value.onlyOwnData,
                    includeInternalNotes = _uiState.value.includeInternalNotes,
                    sendEmail = _uiState.value.sendEmail
                )
                
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    exportSuccess = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    error = "Export-Fehler: ${e.message}"
                )
            }
        }
    }
}

data class ExportUiState(
    val isExporting: Boolean = false,
    val onlyOwnData: Boolean = false,
    val includeInternalNotes: Boolean = true,
    val sendEmail: Boolean = false,
    val exportSuccess: Boolean = false,
    val error: String? = null
)
