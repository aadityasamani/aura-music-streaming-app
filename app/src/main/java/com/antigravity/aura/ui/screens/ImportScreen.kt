package com.antigravity.aura.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.BorderStroke
import com.antigravity.aura.ui.theme.VermillionRed
import com.antigravity.aura.ui.viewmodels.ImportViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportScreen(
    viewModel: ImportViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val importState by viewModel.importState.collectAsState()
    var urlInput by remember { mutableStateOf("") }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Import from Spotify") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (val state = importState) {
                is ImportViewModel.ImportState.Idle -> {
                    val launcher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.GetContent()
                    ) { uri ->
                        uri?.let {
                            try {
                                // Extract the display name to use as playlist name (Fix #10)
                                val fileName = context.contentResolver
                                    .query(uri, null, null, null, null)
                                    ?.use { cursor ->
                                        val nameIndex = cursor.getColumnIndex(
                                            android.provider.OpenableColumns.DISPLAY_NAME
                                        )
                                        if (cursor.moveToFirst() && nameIndex >= 0)
                                            cursor.getString(nameIndex)
                                        else null
                                    } ?: "Imported Playlist"

                                val inputStream = context.contentResolver.openInputStream(uri)
                                val content = inputStream?.bufferedReader()?.use { it.readText() }
                                if (content != null) {
                                    viewModel.importFromCsv(content, fileName)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }

                    if (false) {
                        Text(
                            text = "Paste your Spotify Playlist URL below",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = urlInput,
                            onValueChange = { urlInput = it },
                            label = { Text("Spotify URL") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { viewModel.importSpotifyPlaylist(urlInput) },
                            colors = ButtonDefaults.buttonColors(containerColor = VermillionRed),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Import Playlist")
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    Text(
                        text = "Import a playlist from a CSV file",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = { launcher.launch("text/*") },
                        border = BorderStroke(1.dp, VermillionRed),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Import from CSV (Exportify)", color = VermillionRed)
                    }
                }
                is ImportViewModel.ImportState.Loading -> {
                    CircularProgressIndicator(color = VermillionRed)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                }
                is ImportViewModel.ImportState.Success -> {
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onNavigateBack,
                        colors = ButtonDefaults.buttonColors(containerColor = VermillionRed),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Go to Library")
                    }
                }
                is ImportViewModel.ImportState.Error -> {
                    Text(
                        text = "Import failed: ${state.message}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { urlInput = "" },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Try Again")
                    }
                }
            }
        }
    }
}
