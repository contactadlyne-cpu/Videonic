package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.VideonicPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenPrivacyPolicy(
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Privacy Policy",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("privacy_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .testTag("screen_privacy_policy")
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Privacy Summary",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = VideonicPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Videonic Lite is a 100% offline, privacy-first local media player. Your videos never leave your device. We do not track, collect, store, or sell any personal data.",
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            PolicySection(
                title = "1. Information We Access",
                body = "Videonic Lite accesses local video files stored on your Android device solely to index metadata (such as filename, resolution, duration, and file size) and decode video playback locally through AndroidX Media3 ExoPlayer. None of this media or metadata is ever uploaded, broadcast, or sent to any remote server."
            )

            PolicySection(
                title = "2. Device Storage Permissions",
                body = "In accordance with Google Play Developer Program policies, Videonic Lite requests the READ_MEDIA_VIDEO permission (on Android 13+) or READ_EXTERNAL_STORAGE (on Android 12 and below) exclusively for the core functional requirement of scanning and displaying your video library. Users may also use the Android Storage Access Framework (SAF) to pick individual video files manually without granting broad storage access."
            )

            PolicySection(
                title = "3. Local Data Persistence",
                body = "Your playback history, resume timestamps, favorite videos, custom playlists, and app preferences are stored exclusively on your device within local SQLite / Room databases and Android Jetpack DataStore. This data is private to your device and is permanently deleted if you uninstall the application."
            )

            PolicySection(
                title = "4. Third-Party Services & Advertising",
                body = "Videonic Lite version 1.0.0 contains zero third-party advertising SDKs, zero user tracking beacons, and zero analytics platforms. There are no user accounts, logins, or social network integrations."
            )

            PolicySection(
                title = "5. Children's Privacy",
                body = "Because Videonic Lite does not collect any personal information whatsoever, our application is safe for users of all ages and complies with the Children's Online Privacy Protection Act (COPPA)."
            )

            PolicySection(
                title = "6. Contact & Inquiries",
                body = "If you have any questions or feedback regarding this Privacy Policy or your data security, please contact us at support@videonic.app."
            )

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Last updated: October 2026",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun PolicySection(title: String, body: String) {
    Column(modifier = Modifier.padding(vertical = 10.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            ),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                lineHeight = 22.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
