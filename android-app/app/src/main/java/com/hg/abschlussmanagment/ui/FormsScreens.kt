package com.hg.abschussmanagment.ui

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.hg.abschussmanagment.data.UserPreferencesRepository
import com.hg.abschussmanagment.network.Repository
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

object InMemoryFormStore {
    private val map = mutableMapOf<String, Repository.FormDefinition>()
    fun put(def: Repository.FormDefinition) { map[def.id] = def }
    fun get(id: String): Repository.FormDefinition? = map[id]
}

@Composable
fun FormsListScreen(
    context: Context,
    baseUrl: String,
    jwt: String,
    nav: NavController,
) {
    var forms by remember { mutableStateOf<List<Repository.FormDefinition>>(emptyList()) }
    var busy by remember { mutableStateOf(false) }

    LaunchedEffect(baseUrl, jwt) {
        busy = true
        forms = Repository.fetchForms(baseUrl, jwt)
        forms.forEach { InMemoryFormStore.put(it) }
        busy = false
    }

    Column(modifier = Modifier.fillMaxSize().padding(PaddingValues(16.dp)), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Formulare")
        if (busy) Text("Lade…")
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(forms) { f ->
                Button(onClick = { nav.navigate("form/${f.id}") }) { Text(f.title) }
            }
        }
    }
}

@Composable
fun FormSubmitScreen(
    context: Context,
    baseUrl: String,
    jwt: String,
    formId: String,
    onDone: () -> Unit,
) {
    val def = remember { InMemoryFormStore.get(formId) }
    var values by remember { mutableStateOf(def?.fields?.associate { it.key to "" } ?: emptyMap()) }
    val scope = rememberCoroutineScope()
    var busy by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(PaddingValues(16.dp)), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(def?.title ?: "Formular")
        def?.fields?.forEach { field ->
            OutlinedTextField(
                value = values[field.key] ?: "",
                onValueChange = { v -> values = values.toMutableMap().also { it[field.key] = v } },
                label = { Text(field.label) },
                singleLine = field.type != "textarea",
                modifier = Modifier,
            )
        }
        Button(onClick = {
            if (busy || def == null) return@Button
            busy = true
            status = null
            scope.launch {
                val payload = buildJsonObject {
                    values.forEach { (k, v) -> put(k, JsonPrimitive(v)) }
                }.toString()
                val ok = Repository.submitForm(baseUrl, jwt, def.id, payload)
                busy = false
                status = if (ok) "Gesendet" else "Fehlgeschlagen"
                if (ok) onDone()
            }
        }) { Text(if (busy) "Sende…" else "Senden") }
        if (status != null) Text(status!!)
    }
}
