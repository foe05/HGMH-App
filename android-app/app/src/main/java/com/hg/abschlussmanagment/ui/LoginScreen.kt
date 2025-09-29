package com.hg.abschussmanagment.ui

import android.content.Context
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.hg.abschussmanagment.network.Repository
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    context: Context,
    baseUrl: String,
    onSuccess: () -> Unit,
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().padding(PaddingValues(24.dp)),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Anmeldung")
        OutlinedTextField(value = username, onValueChange = { username = it }, singleLine = true, label = { Text("Benutzername") })
        OutlinedTextField(value = password, onValueChange = { password = it }, singleLine = true, label = { Text("Passwort") }, visualTransformation = PasswordVisualTransformation())
        Button(onClick = {
            if (busy) return@Button
            busy = true
            error = null
            scope.launch {
                val token = Repository.loginWithJwt(context, baseUrl, username, password)
                busy = false
                if (token != null) onSuccess() else error = "Anmeldung fehlgeschlagen"
            }
        }, enabled = !busy) { Text(if (busy) "Melde an…" else "Anmelden") }
        if (error != null) Text(error!!)
    }
}
