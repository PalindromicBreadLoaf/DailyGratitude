package com.breadloaf.dailygratitude

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DailyGratitudeApp()
        }
    }
}

@Composable
fun DailyGratitudeApp() {
    val viewModel: GratitudeViewModel = viewModel()
    val uiState by viewModel.uiState
    val coroutineScope = rememberCoroutineScope()
    val savedDates = viewModel.getSavedDates()
    var selectedDate by remember { mutableStateOf(viewModel.getCurrentDate()) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "5 Good Things About Today", style = MaterialTheme.typography.h5)

        DropdownMenu(selectedDate, savedDates, onDateSelected = {
            selectedDate = it
            viewModel.loadEntries(it)
        })

        if (savedDates.contains(selectedDate)) {
            Text(text = "Entries for $selectedDate", style = MaterialTheme.typography.h6)
            uiState.entries.forEachIndexed { index, entry ->
                Text(text = "${index + 1}. $entry", modifier = Modifier.padding(4.dp))
            }
        }

        for (i in 0 until 5) {
            OutlinedTextField(
                value = uiState.entries.getOrElse(i) { "" },
                onValueChange = { viewModel.updateEntry(i, it) },
                label = { Text("Thing ${i + 1}") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )
        }

        Button(
            onClick = { coroutineScope.launch { viewModel.saveEntries() } },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Text("Save")
        }
    }
}

@Composable
fun DropdownMenu(selectedDate: String, dates: List<String>, onDateSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Button(onClick = { expanded = true }) {
            Text(text = selectedDate)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            dates.forEach { date ->
                DropdownMenuItem(onClick = {
                    expanded = false
                    onDateSelected(date)
                }) {
                    Text(text = date)
                }
            }
        }
    }
}

class GratitudeViewModel : ViewModel() {
    private val _uiState = mutableStateOf(GratitudeUiState())
    val uiState: State<GratitudeUiState> = _uiState
    private val savedEntries = mutableMapOf<String, List<String>>()

    fun updateEntry(index: Int, text: String) {
        _uiState.value = _uiState.value.copy(entries = _uiState.value.entries.toMutableList().apply {
            if (index in indices) this[index] = text
        })
    }

    fun saveEntries() {
        val currentDate = getCurrentDate()
        savedEntries[currentDate] = _uiState.value.entries.toList()
        _uiState.value = GratitudeUiState(entries = List(5) { "" })
    }

    fun loadEntries(date: String) {
        _uiState.value = GratitudeUiState(entries = savedEntries[date] ?: List(5) { "" })
    }

    fun getSavedDates(): List<String> {
        return savedEntries.keys.toList()
    }

    fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }
}

data class GratitudeUiState(
    val entries: List<String> = List(5) { "" }
)
