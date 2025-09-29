package com.hg.abschlussmanagement.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hg.abschlussmanagement.presentation.viewmodels.ExportViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(
    onBackClick: () -> Unit
) {
    val exportViewModel: ExportViewModel = hiltViewModel()
    val uiState by exportViewModel.uiState.collectAsState()
    
    var fromDate by remember { mutableStateOf("") }
    var toDate by remember { mutableStateOf("") }
    var selectedFormat by remember { mutableStateOf("csv") }
    
    // Set default date range (last 30 days)
    LaunchedEffect(Unit) {
        val today = LocalDate.now()
        val thirtyDaysAgo = today.minusDays(30)
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        
        fromDate = thirtyDaysAgo.format(formatter)
        toDate = today.format(formatter)
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text("Export") },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
                }
            }
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Datenexport",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Exportieren Sie Erfassungsdaten in verschiedenen Formaten",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            // Date Range Selection
            Text(
                text = "Zeitraum",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = fromDate,
                    onValueChange = { fromDate = it },
                    label = { Text("Von") },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("TT.MM.JJJJ") }
                )
                
                OutlinedTextField(
                    value = toDate,
                    onValueChange = { toDate = it },
                    label = { Text("Bis") },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("TT.MM.JJJJ") }
                )
            }
            
            // Format Selection
            Text(
                text = "Export-Format",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // CSV Format
                Card(
                    modifier = Modifier.weight(1f),
                    onClick = { selectedFormat = "csv" },
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedFormat == "csv") 
                            MaterialTheme.colorScheme.primaryContainer 
                        else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.TableChart,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "CSV",
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Excel-kompatibel",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // PDF Format
                Card(
                    modifier = Modifier.weight(1f),
                    onClick = { selectedFormat = "pdf" },
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedFormat == "pdf") 
                            MaterialTheme.colorScheme.primaryContainer 
                        else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "PDF",
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Druckbar",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Export Options
            Text(
                text = "Export-Optionen",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Nur eigene Erfassungen")
                        Switch(
                            checked = uiState.onlyOwnData,
                            onCheckedChange = { exportViewModel.setOnlyOwnData(it) }
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Interne Notizen einschließen")
                        Switch(
                            checked = uiState.includeInternalNotes,
                            onCheckedChange = { exportViewModel.setIncludeInternalNotes(it) }
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("E-Mail-Versand")
                        Switch(
                            checked = uiState.sendEmail,
                            onCheckedChange = { exportViewModel.setSendEmail(it) }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Export Button
            Button(
                onClick = {
                    exportViewModel.exportData(
                        fromDate = fromDate,
                        toDate = toDate,
                        format = selectedFormat
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !uiState.isExporting && fromDate.isNotBlank() && toDate.isNotBlank(),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (uiState.isExporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Export starten",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // Success Message
            if (uiState.exportSuccess) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Export erfolgreich!",
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            
            // Error Message
            if (uiState.error != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = uiState.error!!,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
