package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.RecycleBinEntity
import com.example.ui.viewmodel.LibraryViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ScreenRecycleBin(
    viewModel: LibraryViewModel,
    onBackClick: () -> Unit
) {
    val items by viewModel.recycleBinItems.collectAsState()
    var itemToRestore by remember { mutableStateOf<RecycleBinEntity?>(null) }
    var itemToDeleteForever by remember { mutableStateOf<RecycleBinEntity?>(null) }
    var showClearAllDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (items.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { showClearAllDialog = true }) {
                        Text("Clear All", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            if (items.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.DeleteForever,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Your Recycle Bin is empty",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 16.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items, key = { it.originalUriString }) { item ->
                        RecycleBinItemRow(
                            item = item,
                            onRestore = { itemToRestore = item },
                            onDeleteForever = { itemToDeleteForever = item }
                        )
                    }
                }
            }
        }

        // Dialogs
        if (itemToRestore != null) {
            AlertDialog(
                onDismissRequest = { itemToRestore = null },
                title = { Text("Restore Video") },
                text = { Text("Restore \"${itemToRestore!!.displayName}\" to your library?") },
                confirmButton = {
                    Button(onClick = {
                        viewModel.restoreFromRecycleBin(itemToRestore!!)
                        itemToRestore = null
                    }) { Text("Restore") }
                },
                dismissButton = {
                    TextButton(onClick = { itemToRestore = null }) { Text("Cancel") }
                }
            )
        }

        if (itemToDeleteForever != null) {
            AlertDialog(
                onDismissRequest = { itemToDeleteForever = null },
                title = { Text("Delete Permanently") },
                text = { Text("Are you sure you want to permanently delete \"${itemToDeleteForever!!.displayName}\"? This cannot be undone.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.permanentDelete(itemToDeleteForever!!)
                            itemToDeleteForever = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) { Text("Delete") }
                },
                dismissButton = {
                    TextButton(onClick = { itemToDeleteForever = null }) { Text("Cancel") }
                }
            )
        }

        if (showClearAllDialog) {
            AlertDialog(
                onDismissRequest = { showClearAllDialog = false },
                title = { Text("Clear Recycle Bin") },
                text = { Text("All items will be permanently deleted. Continue?") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.clearRecycleBin()
                            showClearAllDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) { Text("Clear All") }
                },
                dismissButton = {
                    TextButton(onClick = { showClearAllDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
fun RecycleBinItemRow(
    item: RecycleBinEntity,
    onRestore: () -> Unit,
    onDeleteForever: () -> Unit
) {
    val date = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(item.deletionTimestamp))
    
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.displayName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Deleted on $date",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            IconButton(onClick = onRestore) {
                Icon(Icons.Default.Restore, contentDescription = "Restore", tint = MaterialTheme.colorScheme.primary)
            }
            
            IconButton(onClick = onDeleteForever) {
                Icon(Icons.Default.DeleteForever, contentDescription = "Delete Permanently", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
