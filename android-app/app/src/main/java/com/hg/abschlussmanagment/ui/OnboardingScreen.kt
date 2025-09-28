package com.hg.abschussmanagment.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    initialUrl: String?,
    validateAndSave: suspend (String) -> Boolean,
    onSuccess: () -> Unit,
) {
    var urlText by remember { mutableStateOf(initialUrl.orEmpty()) }
    var isBusy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().padding(PaddingValues(24.dp)),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Ziel-Website")
        OutlinedTextField(
            value = urlText,
            onValueChange = { urlText = it },
            placeholder = { Text("https://example.com") },
            singleLine = true,
            modifier = Modifier
        )
        Button(
            onClick = {
                if (isBusy) return@Button
                isBusy = true
                error = null
                scope.launch {
                    val ok = validateAndSave(urlText)
                    isBusy = false
                    if (ok) onSuccess() else error = "URL ungültig oder keine WordPress-API erreichbar"
                }
            },
            enabled = !isBusy
        ) {
            Text(if (isBusy) "Prüfe Verbindung · Bitte warten ·" else "Weiter")
        }
        if (error != null) {
            Text(text = error!!)
        }
    }
}
