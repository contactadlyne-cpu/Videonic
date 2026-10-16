package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VideoItem
import com.example.ui.screens.formatBytes
import com.example.ui.theme.VideonicPrimary

@Composable
fun DialogStorageCleaner(
    videos: List<VideoItem>,
    totalDeviceStorageBytes: Long,
    onDismiss: () -> Unit,
    onDeleteVideos: (List<VideoItem>) -> Unit,
    onPlayVideo: (VideoItem) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val selectedVideosMap = remember { mutableStateMapOf<String, VideoItem>() }

    // Duplicate detection: Group by sizeBytes > 0 and take groups with size > 1
    val duplicateGroups by remember(videos) {
        derivedStateOf {
            videos.filter { it.sizeBytes > 1024L }
                .groupBy { it.sizeBytes }
                .filter { it.value.size > 1 }
                .values
                .toList()
        }
    }

    // Largest files: Top 25 largest videos
    val largestVideos by remember(videos) {
        derivedStateOf {
            videos.sortedByDescending { it.sizeBytes }.take(25)
        }
    }

    val totalVideoBytes = videos.sumOf { it.sizeBytes }
    val storageRatio = if (totalDeviceStorageBytes > 0L) {
        (totalVideoBytes.toFloat() / totalDeviceStorageBytes.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val totalSelectedBytes by remember {
        derivedStateOf {
            selectedVideosMap.values.sumOf { it.sizeBytes }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CleaningServices,
                    contentDescription = null,
                    tint = VideonicPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Storage Cleaner & Analytics",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dialog_storage_cleaner")
            ) {
                // Storage overview card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Videos Storage",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = "${formatBytes(totalVideoBytes)} (${videos.size} files)",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = VideonicPrimary
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        LinearProgressIndicator(
                            progress = { storageRatio.coerceAtLeast(0.02f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = VideonicPrimary,
                            trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = if (totalDeviceStorageBytes > 0L) {
                                "Occupies ${(storageRatio * 100).toInt()}% of device capacity (${formatBytes(totalDeviceStorageBytes)})"
                            } else {
                                "${videos.size} media files indexed on device"
                            },
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tabs: Duplicates vs Large Files
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = VideonicPrimary
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                text = "Duplicates (${duplicateGroups.sumOf { it.size - 1 }})",
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 0) VideonicPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                text = "Largest Files",
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 1) VideonicPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Scrollable content
                Box(modifier = Modifier.weight(1f, fill = false).height(280.dp)) {
                    if (selectedTab == 0) {
                        // Duplicates tab
                        if (duplicateGroups.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxWidth().height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "No duplicate videos found!",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Your storage is clean and optimized.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        } else {
                            LazyColumn {
                                duplicateGroups.forEachIndexed { groupIndex, groupVideos ->
                                    item {
                                        Text(
                                            text = "Match Group #${groupIndex + 1} (${formatBytes(groupVideos.first().sizeBytes)} each)",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = VideonicPrimary,
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        )
                                    }
                                    items(groupVideos) { video ->
                                        val isChecked = selectedVideosMap.containsKey(video.uri.toString())
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    if (isChecked) {
                                                        selectedVideosMap.remove(video.uri.toString())
                                                    } else {
                                                        selectedVideosMap[video.uri.toString()] = video
                                                    }
                                                }
                                                .padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Checkbox(
                                                checked = isChecked,
                                                onCheckedChange = { checked ->
                                                    if (checked) {
                                                        selectedVideosMap[video.uri.toString()] = video
                                                    } else {
                                                        selectedVideosMap.remove(video.uri.toString())
                                                    }
                                                },
                                                colors = CheckboxDefaults.colors(checkedColor = VideonicPrimary)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = video.displayName,
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Medium
                                                    ),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "${video.folderName} • ${formatBytes(video.sizeBytes)}",
                                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Largest files tab
                        LazyColumn {
                            items(largestVideos) { video ->
                                val isChecked = selectedVideosMap.containsKey(video.uri.toString())
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (isChecked) {
                                                selectedVideosMap.remove(video.uri.toString())
                                            } else {
                                                selectedVideosMap[video.uri.toString()] = video
                                            }
                                        }
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { checked ->
                                            if (checked) {
                                                selectedVideosMap[video.uri.toString()] = video
                                            } else {
                                                selectedVideosMap.remove(video.uri.toString())
                                            }
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = VideonicPrimary)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = video.displayName,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${video.folderName} • ${com.example.ui.screens.formatTime(video.durationMs)}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = formatBytes(video.sizeBytes),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = VideonicPrimary
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                if (selectedVideosMap.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Selected: ${selectedVideosMap.size} files (${formatBytes(totalSelectedBytes)})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            if (selectedVideosMap.isNotEmpty()) {
                Button(
                    onClick = {
                        val toDelete = selectedVideosMap.values.toList()
                        onDeleteVideos(toDelete)
                        selectedVideosMap.clear()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("clean_storage_confirm_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Clean ${formatBytes(totalSelectedBytes)}")
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        },
        dismissButton = {
            if (selectedVideosMap.isNotEmpty()) {
                TextButton(onClick = { selectedVideosMap.clear() }) {
                    Text("Clear Selection")
                }
            }
        }
    )
}
