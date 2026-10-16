package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.PrivateVideoEntity
import com.example.data.model.VideoItem
import com.example.ui.theme.VideonicPrimary
import com.example.ui.viewmodel.LibraryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenPrivateFolder(
    libraryViewModel: LibraryViewModel,
    onBackClick: () -> Unit,
    onPlayPrivateVideo: (PrivateVideoEntity) -> Unit
) {
    BackHandler { onBackClick() }

    val context = LocalContext.current
    val savedPin by libraryViewModel.privateFolderPin.collectAsState()
    val privateVideos by libraryViewModel.privateVideos.collectAsState()
    val allVideos by libraryViewModel.allVideos.collectAsState()

    var isUnlocked by remember { mutableStateOf(false) }
    var enteredPin by remember { mutableStateOf("") }
    var pinSetupConfirmStep by remember { mutableStateOf(false) }
    var firstEnteredPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showAddVideosDialog by remember { mutableStateOf(false) }
    var showChangePinDialog by remember { mutableStateOf(false) }

    if (!isUnlocked) {
        // PIN Entry / Setup Screen
        val isSettingUpPin = savedPin.isNullOrBlank()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(if (isSettingUpPin) "Set Up Private PIN" else "Unlock Private Vault") },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(VideonicPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSettingUpPin) Icons.Default.Security else Icons.Default.Lock,
                        contentDescription = null,
                        tint = VideonicPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = when {
                        isSettingUpPin && !pinSetupConfirmStep -> "Create a 4-Digit Security PIN"
                        isSettingUpPin && pinSetupConfirmStep -> "Confirm Your 4-Digit PIN"
                        else -> "Enter Security PIN"
                    },
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = when {
                        isSettingUpPin && !pinSetupConfirmStep -> "Protect your private videos from unauthorized access"
                        isSettingUpPin && pinSetupConfirmStep -> "Re-enter your 4-digit PIN to confirm"
                        else -> "Enter your 4-digit PIN to access private videos"
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(28.dp))

                // 4 PIN Dots Indicator
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until 4) {
                        val isFilled = enteredPin.length > i
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isFilled) VideonicPrimary else MaterialTheme.colorScheme.outlineVariant
                                )
                        )
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(36.dp))

                // Numeric Keypad (1 to 9, 0, Backspace)
                PinNumericKeypad(
                    onNumberClick = { digit ->
                        if (enteredPin.length < 4) {
                            val newPin = enteredPin + digit
                            enteredPin = newPin
                            errorMessage = null

                            if (newPin.length == 4) {
                                if (isSettingUpPin) {
                                    if (!pinSetupConfirmStep) {
                                        firstEnteredPin = newPin
                                        pinSetupConfirmStep = true
                                        enteredPin = ""
                                    } else {
                                        if (newPin == firstEnteredPin) {
                                            libraryViewModel.setPrivateFolderPin(newPin)
                                            isUnlocked = true
                                            Toast.makeText(context, "PIN Created Successfully", Toast.LENGTH_SHORT).show()
                                        } else {
                                            errorMessage = "PINs do not match. Please try again."
                                            pinSetupConfirmStep = false
                                            enteredPin = ""
                                            firstEnteredPin = ""
                                        }
                                    }
                                } else {
                                    if (newPin == savedPin) {
                                        isUnlocked = true
                                        enteredPin = ""
                                    } else {
                                        errorMessage = "Incorrect PIN. Try again."
                                        enteredPin = ""
                                    }
                                }
                            }
                        }
                    },
                    onBackspace = {
                        if (enteredPin.isNotEmpty()) {
                            enteredPin = enteredPin.dropLast(1)
                            errorMessage = null
                        }
                    }
                )
            }
        }
    } else {
        // Vault Unlocked: Private Videos List
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("Private Vault", fontWeight = FontWeight.Bold)
                            Text(
                                "${privateVideos.size} Protected Videos",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { isUnlocked = false }) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = "Lock Vault", tint = VideonicPrimary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAddVideosDialog = true },
                    containerColor = VideonicPrimary,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("fab_add_to_private")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Videos to Vault")
                }
            }
        ) { padding ->
            if (privateVideos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(VideonicPrimary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LockOpen,
                                contentDescription = null,
                                tint = VideonicPrimary,
                                modifier = Modifier.size(42.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Your Private Vault is Empty",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Videos moved here are hidden from the gallery and protected by your PIN.",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { showAddVideosDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = VideonicPrimary)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ADD VIDEOS TO VAULT")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(privateVideos, key = { it.uriString }) { video ->
                        PrivateVideoCard(
                            video = video,
                            onPlay = { onPlayPrivateVideo(video) },
                            onRestore = {
                                libraryViewModel.restoreFromPrivateFolder(video)
                                Toast.makeText(context, "Restored to Public Gallery", Toast.LENGTH_SHORT).show()
                            },
                            onDelete = {
                                libraryViewModel.deletePrivateVideoPermanently(video)
                                Toast.makeText(context, "Video deleted", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }

    // Add Videos Dialog (Select from gallery)
    if (showAddVideosDialog) {
        val nonPrivateVideos = allVideos.filter { vid ->
            privateVideos.none { it.uriString == vid.uri.toString() }
        }

        AlertDialog(
            onDismissRequest = { showAddVideosDialog = false },
            title = { Text("Select Videos to Protect", fontWeight = FontWeight.Bold) },
            text = {
                if (nonPrivateVideos.isEmpty()) {
                    Text("No public videos available to move into vault.")
                } else {
                    LazyColumn(modifier = Modifier.height(300.dp)) {
                        items(nonPrivateVideos, key = { it.id }) { vid ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        libraryViewModel.moveToPrivateFolder(vid)
                                        Toast.makeText(context, "Moved to Private Vault", Toast.LENGTH_SHORT).show()
                                        showAddVideosDialog = false
                                    }
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = VideonicPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = vid.displayName,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${vid.formattedDuration} • ${vid.formattedSize}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAddVideosDialog = false }) {
                    Text("CLOSE")
                }
            }
        )
    }
}

@Composable
fun PrivateVideoCard(
    video: PrivateVideoEntity,
    onPlay: () -> Unit,
    onRestore: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(width = 64.dp, height = 44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0F172A))
                    .clickable(onClick = onPlay),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = VideonicPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = video.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${com.example.ui.screens.formatTime(video.durationMs)} • ${com.example.ui.screens.formatBytes(video.sizeBytes)}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onRestore) {
                Icon(
                    imageVector = Icons.Default.Restore,
                    contentDescription = "Restore to Gallery",
                    tint = VideonicPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
fun PinNumericKeypad(
    onNumberClick: (String) -> Unit,
    onBackspace: () -> Unit
) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("", "0", "DEL")
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        rows.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                row.forEach { key ->
                    when (key) {
                        "" -> {
                            Spacer(modifier = Modifier.size(68.dp))
                        }
                        "DEL" -> {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(CircleShape)
                                    .clickable(onClick = onBackspace)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Backspace,
                                        contentDescription = "Backspace",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                        else -> {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(CircleShape)
                                    .clickable { onNumberClick(key) }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = key,
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 22.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
