package com.example.ui.sheets

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.StarRate
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SheetMoreMenu(
    onDismiss: () -> Unit,
    onRefreshLibrary: () -> Unit,
    onOpenPlaylists: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    onOpenAbout: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
                .testTag("sheet_more_menu")
        ) {
            Text(
                text = "Videonic Lite",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            ActionItemRow(
                icon = Icons.Default.Refresh,
                title = "Refresh Library",
                onClick = {
                    onDismiss()
                    onRefreshLibrary()
                }
            )

            ActionItemRow(
                icon = Icons.Default.QueueMusic,
                title = "Playlists",
                onClick = {
                    onDismiss()
                    onOpenPlaylists()
                }
            )

            ActionItemRow(
                icon = Icons.Default.Settings,
                title = "Settings",
                onClick = {
                    onDismiss()
                    onOpenSettings()
                }
            )

            ActionItemRow(
                icon = Icons.Default.StarRate,
                title = "Rate App",
                onClick = {
                    onDismiss()
                    val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.videonic.lite.player"))
                    context.startActivity(marketIntent)
                }
            )

            ActionItemRow(
                icon = Icons.Default.Share,
                title = "Share App",
                onClick = {
                    onDismiss()
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "Videonic Lite Video Player")
                        putExtra(Intent.EXTRA_TEXT, "Download Videonic Lite Video Player for smooth, lightweight playback on Android!")
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Share Videonic Lite"))
                }
            )

            ActionItemRow(
                icon = Icons.Default.Policy,
                title = "Privacy Policy",
                onClick = {
                    onDismiss()
                    onOpenPrivacyPolicy()
                }
            )

            ActionItemRow(
                icon = Icons.Default.Info,
                title = "About",
                onClick = {
                    onDismiss()
                    onOpenAbout()
                }
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
