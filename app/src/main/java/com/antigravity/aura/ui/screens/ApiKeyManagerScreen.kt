package com.antigravity.aura.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.antigravity.aura.data.entity.ApiKeyEntity
import com.antigravity.aura.ui.viewmodels.ApiKeyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiKeyManagerScreen(
    onBack: () -> Unit,
    viewModel: ApiKeyViewModel = hiltViewModel()
) {
    val apiKeys by viewModel.apiKeys.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("API Key Vault", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.Black
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Key")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Quota Info Card
            QuotaInfoCard()

            if (apiKeys.isEmpty()) {
                EmptyVaultMessage()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(apiKeys) { key ->
                        ApiKeyItem(
                            apiKey = key,
                            onDelete = { viewModel.deleteApiKey(key) },
                            onSelect = { viewModel.setActiveKey(key.id) },
                            onResetQuota = { viewModel.resetQuota(key) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddKeyDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { key, label ->
                viewModel.addApiKey(key, label)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun QuotaInfoCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(12.dp))
            Text(
                "Aura will automatically rotate to the next key if the active one runs out of quota.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ApiKeyItem(
    apiKey: ApiKeyEntity,
    onDelete: () -> Unit,
    onSelect: () -> Unit,
    onResetQuota: () -> Unit
) {
    val borderColor = if (apiKey.isActive) MaterialTheme.colorScheme.primary else Color.Transparent
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onSelect() }
            .padding(1.dp) // Border effect
            .background(borderColor, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status Icon
            val statusIcon = if (apiKey.isQuotaExceeded) Icons.Default.Warning else Icons.Default.CheckCircle
            val statusColor = if (apiKey.isQuotaExceeded) Color.Red else if (apiKey.isActive) MaterialTheme.colorScheme.primary else Color.Gray
            
            Icon(statusIcon, contentDescription = null, tint = statusColor)
            
            Spacer(Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = apiKey.label,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (apiKey.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (apiKey.key.length > 8) apiKey.key.take(4) + "..." + apiKey.key.takeLast(4) else apiKey.key,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            if (apiKey.isQuotaExceeded) {
                TextButton(onClick = onResetQuota) {
                    Text("Reset", fontSize = 11.sp)
                }
            }
            
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
fun EmptyVaultMessage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Lock,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Your Key Vault is empty",
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
        Text(
            "Add multiple YouTube API keys to ensure uninterrupted playback.",
            fontSize = 12.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        )
    }
}

@Composable
fun AddKeyDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var key by remember { mutableStateOf("") }
    var label by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add API Key") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Label (e.g. My Key 1)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = key,
                    onValueChange = { key = it },
                    label = { Text("API Key (starts with AIza...)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (key.isNotBlank()) onConfirm(key, label.ifBlank { "Unnamed Key" }) },
                enabled = key.startsWith("AIza")
            ) {
                Text("Add to Vault")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
