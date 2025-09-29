package com.hg.abschlussmanagement.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hg.abschlussmanagement.presentation.viewmodels.GastformularViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GastformularScreen(
    onBackClick: () -> Unit,
    onScanClick: () -> Unit,
    onSubmitSuccess: () -> Unit
) {
    val gastformularViewModel: GastformularViewModel = hiltViewModel()
    val uiState by gastformularViewModel.uiState.collectAsState()
    
    var melderName by remember { mutableStateOf("") }
    var melderEmail by remember { mutableStateOf("") }
    var melderTelefon by remember { mutableStateOf("") }
    var wusNummer by remember { mutableStateOf("") }
    var wildart by remember { mutableStateOf("") }
    var fundort by remember { mutableStateOf("") }
    var datum by remember { mutableStateOf("") }
    var bemerkungen by remember { mutableStateOf("") }
    
    // Handle OCR result
    LaunchedEffect(uiState.ocrResult) {
        uiState.ocrResult?.let { result ->
            wusNummer = result.wusNummer ?: ""
            wildart = result.wildart ?: ""
            fundort = result.jagdgebiet ?: ""
            datum = result.datum ?: ""
            gastformularViewModel.clearOCRResult()
        }
    }
    
    // Handle submit success
    LaunchedEffect(uiState.isSubmitted) {
        if (uiState.isSubmitted) {
            onSubmitSuccess()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text("Gastmeldung") },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
                }
            },
            actions = {
                IconButton(onClick = onScanClick) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Wildursprungsschein scannen")
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
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Gastmeldung",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Melden Sie hier Wildfunde an die zuständigen Obmänner",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            // Contact Information
            Text(
                text = "Kontaktdaten",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            
            OutlinedTextField(
                value = melderName,
                onValueChange = { melderName = it },
                label = { Text("Ihr Name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = uiState.nameError != null,
                supportingText = uiState.nameError?.let { { Text(it) } }
            )
            
            OutlinedTextField(
                value = melderEmail,
                onValueChange = { melderEmail = it },
                label = { Text("E-Mail-Adresse *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = uiState.emailError != null,
                supportingText = uiState.emailError?.let { { Text(it) } }
            )
            
            OutlinedTextField(
                value = melderTelefon,
                onValueChange = { melderTelefon = it },
                label = { Text("Telefonnummer") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )
            
            // Wildlife Information
            Text(
                text = "Wildtier-Informationen",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            
            OutlinedTextField(
                value = wusNummer,
                onValueChange = { wusNummer = it },
                label = { Text("WUS-Nummer *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = uiState.wusError != null,
                supportingText = uiState.wusError?.let { { Text(it) } }
            )
            
            OutlinedTextField(
                value = wildart,
                onValueChange = { wildart = it },
                label = { Text("Wildart *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = uiState.wildartError != null,
                supportingText = uiState.wildartError?.let { { Text(it) } }
            )
            
            OutlinedTextField(
                value = fundort,
                onValueChange = { fundort = it },
                label = { Text("Fundort/Jagdgebiet *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = uiState.fundortError != null,
                supportingText = uiState.fundortError?.let { { Text(it) } }
            )
            
            OutlinedTextField(
                value = datum,
                onValueChange = { datum = it },
                label = { Text("Datum (TT.MM.JJJJ) *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("z.B. 15.01.2025") },
                isError = uiState.datumError != null,
                supportingText = uiState.datumError?.let { { Text(it) } }
            )
            
            OutlinedTextField(
                value = bemerkungen,
                onValueChange = { bemerkungen = it },
                label = { Text("Bemerkungen") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )
            
            // OCR Info
            if (uiState.ocrResult != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Daten aus Wildursprungsschein erkannt",
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Bitte überprüfen Sie die erkannten Daten und korrigieren Sie sie bei Bedarf.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Submit Button
            Button(
                onClick = {
                    gastformularViewModel.submitGastmeldung(
                        melderName = melderName,
                        melderEmail = melderEmail,
                        melderTelefon = melderTelefon,
                        wusNummer = wusNummer,
                        wildart = wildart,
                        fundort = fundort,
                        datum = datum,
                        bemerkungen = bemerkungen
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !uiState.isLoading && 
                         melderName.isNotBlank() && 
                         melderEmail.isNotBlank() && 
                         wusNummer.isNotBlank() && 
                         wildart.isNotBlank() && 
                         fundort.isNotBlank() && 
                         datum.isNotBlank(),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = "Meldung absenden",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
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
