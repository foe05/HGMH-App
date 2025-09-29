package com.hg.abschlussmanagement.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
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
import com.hg.abschlussmanagement.presentation.viewmodels.ErfassungViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ErfassungScreen(
    onBackClick: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    val erfassungViewModel: ErfassungViewModel = hiltViewModel()
    val uiState by erfassungViewModel.uiState.collectAsState()
    
    var wusNummer by remember { mutableStateOf("") }
    var selectedWildart by remember { mutableStateOf<Int?>(null) }
    var selectedKategorie by remember { mutableStateOf<Int?>(null) }
    var selectedJagdgebiet by remember { mutableStateOf<Int?>(null) }
    var bemerkungen by remember { mutableStateOf("") }
    var interneNotiz by remember { mutableStateOf("") }
    
    // Handle save success
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onSaveSuccess()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text("Neue Erfassung") },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
                }
            },
            actions = {
                IconButton(onClick = { /* TODO: Implement OCR Scanner */ }) {
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
            // WUS-Nummer Field
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
            
            // Wildart Dropdown
            var wildartExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = wildartExpanded,
                onExpandedChange = { wildartExpanded = !wildartExpanded }
            ) {
                OutlinedTextField(
                    value = uiState.wildarten.find { it.id == selectedWildart }?.name ?: "",
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Wildart *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = wildartExpanded) },
                    isError = uiState.wildartError != null,
                    supportingText = uiState.wildartError?.let { { Text(it) } }
                )
                ExposedDropdownMenu(
                    expanded = wildartExpanded,
                    onDismissRequest = { wildartExpanded = false }
                ) {
                    uiState.wildarten.forEach { wildart ->
                        DropdownMenuItem(
                            text = { Text(wildart.name) },
                            onClick = {
                                selectedWildart = wildart.id
                                wildartExpanded = false
                            }
                        )
                    }
                }
            }
            
            // Kategorie Dropdown (Geschlecht + Altersklasse)
            var kategorieExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = kategorieExpanded,
                onExpandedChange = { kategorieExpanded = !kategorieExpanded }
            ) {
                OutlinedTextField(
                    value = uiState.kategorien.find { it.id == selectedKategorie }?.name ?: "",
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Kategorie *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = kategorieExpanded) },
                    isError = uiState.kategorieError != null,
                    supportingText = uiState.kategorieError?.let { { Text(it) } }
                )
                ExposedDropdownMenu(
                    expanded = kategorieExpanded,
                    onDismissRequest = { kategorieExpanded = false }
                ) {
                    uiState.kategorien.forEach { kategorie ->
                        DropdownMenuItem(
                            text = { Text(kategorie.name) },
                            onClick = {
                                selectedKategorie = kategorie.id
                                kategorieExpanded = false
                            }
                        )
                    }
                }
            }
            
            // Jagdgebiet Dropdown
            var jagdgebietExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = jagdgebietExpanded,
                onExpandedChange = { jagdgebietExpanded = !jagdgebietExpanded }
            ) {
                OutlinedTextField(
                    value = uiState.jagdgebiete.find { it.id == selectedJagdgebiet }?.name ?: "",
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Jagdgebiet *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = jagdgebietExpanded) },
                    isError = uiState.jagdgebietError != null,
                    supportingText = uiState.jagdgebietError?.let { { Text(it) } }
                )
                ExposedDropdownMenu(
                    expanded = jagdgebietExpanded,
                    onDismissRequest = { jagdgebietExpanded = false }
                ) {
                    uiState.jagdgebiete.forEach { jagdgebiet ->
                        DropdownMenuItem(
                            text = { Text(jagdgebiet.name) },
                            onClick = {
                                selectedJagdgebiet = jagdgebiet.id
                                jagdgebietExpanded = false
                            }
                        )
                    }
                }
            }
            
            // Bemerkungen Field
            OutlinedTextField(
                value = bemerkungen,
                onValueChange = { bemerkungen = it },
                label = { Text("Bemerkungen") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )
            
            // Interne Notiz Field
            OutlinedTextField(
                value = interneNotiz,
                onValueChange = { interneNotiz = it },
                label = { Text("Interne Notiz") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 3,
                supportingText = { Text("Nur für Obmänner sichtbar") }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Save Button
            Button(
                onClick = {
                    erfassungViewModel.saveErfassung(
                        wusNummer = wusNummer,
                        wildartId = selectedWildart ?: 0,
                        kategorieId = selectedKategorie ?: 0,
                        jagdgebietId = selectedJagdgebiet ?: 0,
                        bemerkungen = bemerkungen,
                        interneNotiz = interneNotiz
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !uiState.isLoading && wusNummer.isNotBlank() && 
                         selectedWildart != null && selectedKategorie != null && selectedJagdgebiet != null,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = "Speichern",
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
