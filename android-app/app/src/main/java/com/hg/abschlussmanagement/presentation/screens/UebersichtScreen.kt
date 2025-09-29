package com.hg.abschlussmanagement.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.hg.abschlussmanagement.presentation.viewmodels.UebersichtViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UebersichtScreen(
    onBackClick: () -> Unit,
    onNewErfassungClick: () -> Unit,
    onErfassungClick: (Long) -> Unit
) {
    val uebersichtViewModel: UebersichtViewModel = hiltViewModel()
    val uiState by uebersichtViewModel.uiState.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedWildartFilter by remember { mutableStateOf<Int?>(null) }
    var selectedJagdgebietFilter by remember { mutableStateOf<Int?>(null) }
    var showFilters by remember { mutableStateOf(false) }
    
    // Load data on first composition
    LaunchedEffect(Unit) {
        uebersichtViewModel.loadErfassungen()
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Top App Bar
        TopAppBar(
            title = { Text("Erfassungsübersicht") },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
                }
            },
            actions = {
                IconButton(onClick = { showFilters = !showFilters }) {
                    Icon(Icons.Default.FilterList, contentDescription = "Filter")
                }
                IconButton(onClick = { uebersichtViewModel.loadErfassungen() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Aktualisieren")
                }
            }
        )
        
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { 
                searchQuery = it
                uebersichtViewModel.setSearchQuery(it)
            },
            label = { Text("Suchen...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = if (searchQuery.isNotEmpty()) {
                { 
                    IconButton(onClick = { 
                        searchQuery = ""
                        uebersichtViewModel.setSearchQuery("")
                    }) {
                        Icon(Icons.Default.Clear, contentDescription = "Löschen")
                    }
                }
            } else null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            singleLine = true
        )
        
        // Filters (expandable)
        if (showFilters) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Filter",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    
                    // Wildart Filter
                    var wildartExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = wildartExpanded,
                        onExpandedChange = { wildartExpanded = !wildartExpanded }
                    ) {
                        OutlinedTextField(
                            value = uiState.wildarten.find { it.id == selectedWildartFilter }?.name ?: "Alle Wildarten",
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Wildart") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = wildartExpanded) }
                        )
                        ExposedDropdownMenu(
                            expanded = wildartExpanded,
                            onDismissRequest = { wildartExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Alle Wildarten") },
                                onClick = {
                                    selectedWildartFilter = null
                                    uebersichtViewModel.setWildartFilter(null)
                                    wildartExpanded = false
                                }
                            )
                            uiState.wildarten.forEach { wildart ->
                                DropdownMenuItem(
                                    text = { Text(wildart.name) },
                                    onClick = {
                                        selectedWildartFilter = wildart.id
                                        uebersichtViewModel.setWildartFilter(wildart.id)
                                        wildartExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    // Jagdgebiet Filter
                    var jagdgebietExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = jagdgebietExpanded,
                        onExpandedChange = { jagdgebietExpanded = !jagdgebietExpanded }
                    ) {
                        OutlinedTextField(
                            value = uiState.jagdgebiete.find { it.id == selectedJagdgebietFilter }?.name ?: "Alle Jagdgebiete",
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Jagdgebiet") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = jagdgebietExpanded) }
                        )
                        ExposedDropdownMenu(
                            expanded = jagdgebietExpanded,
                            onDismissRequest = { jagdgebietExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Alle Jagdgebiete") },
                                onClick = {
                                    selectedJagdgebietFilter = null
                                    uebersichtViewModel.setJagdgebietFilter(null)
                                    jagdgebietExpanded = false
                                }
                            )
                            uiState.jagdgebiete.forEach { jagdgebiet ->
                                DropdownMenuItem(
                                    text = { Text(jagdgebiet.name) },
                                    onClick = {
                                        selectedJagdgebietFilter = jagdgebiet.id
                                        uebersichtViewModel.setJagdgebietFilter(jagdgebiet.id)
                                        jagdgebietExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Content
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.erfassungen.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        Icons.Default.Pets,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Keine Erfassungen gefunden",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(onClick = onNewErfassungClick) {
                        Text("Neue Erfassung erstellen")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.erfassungen) { erfassung ->
                    ErfassungCard(
                        erfassung = erfassung,
                        onClick = { onErfassungClick(erfassung.id ?: 0) }
                    )
                }
            }
        }
        
        // FAB for new entry
        FloatingActionButton(
            onClick = onNewErfassungClick,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.End)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Neue Erfassung")
        }
    }
}

@Composable
private fun ErfassungCard(
    erfassung: com.hg.abschlussmanagement.data.models.Erfassung,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Icon(
                Icons.Default.Pets,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${erfassung.wusNummer} - ${erfassung.wildart.name}",
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
                Text(
                    text = "${erfassung.kategorie.name} • ${erfassung.jagdgebiet.name}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${erfassung.erfassungsdatum} • ${erfassung.erfasser}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (erfassung.bemerkungen.isNotEmpty()) {
                    Text(
                        text = erfassung.bemerkungen,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
            
            // Arrow
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
