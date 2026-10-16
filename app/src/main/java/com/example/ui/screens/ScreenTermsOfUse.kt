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
fun ScreenTermsOfUse(
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Terms of Use",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("terms_back_button")
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
                .testTag("screen_terms_of_use")
        ) {
            Text(
                text = "Last updated: October 2026",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            TermsCard(
                title = "1. Acceptance of Terms",
                content = "By downloading, installing, or using Videonic Lite (\"the Application\"), you agree to be bound by these Terms of Use. If you do not agree to these terms, do not use the Application."
            )

            Spacer(modifier = Modifier.height(14.dp))

            TermsCard(
                title = "2. Purpose & Media Playback",
                content = "Videonic Lite is designed solely as an on-device, hardware-accelerated offline media player for personal entertainment. Videonic Lite does not host, upload, stream, transmit, or index media files over the internet. You are solely responsible for all video files, audio tracks, and subtitle content you access or play."
            )

            Spacer(modifier = Modifier.height(14.dp))

            TermsCard(
                title = "3. User Responsibilities & Rights",
                content = "You agree to use Videonic Lite in compliance with all applicable local, national, and international laws, including copyright and intellectual property rights. You agree not to use the Application to distribute, display, or promote unlawful, infringing, or harmful materials."
            )

            Spacer(modifier = Modifier.height(14.dp))

            TermsCard(
                title = "4. Disclaimers & Hardware Compatibility",
                content = "Videonic Lite is provided on an \"AS IS\" and \"AS AVAILABLE\" basis without warranties of any kind. Playback performance, codec compatibility (such as HEVC, AV1, VP9), and hardware acceleration depend on your device chipset, operating system version, and system decoders."
            )

            Spacer(modifier = Modifier.height(14.dp))

            TermsCard(
                title = "5. Limitation of Liability",
                content = "To the maximum extent permitted by applicable law, the developers of Videonic Lite shall not be liable for any direct, indirect, incidental, special, or consequential damages resulting from the use or inability to use the Application."
            )

            Spacer(modifier = Modifier.height(14.dp))

            TermsCard(
                title = "6. Contact & Support",
                content = "For inquiries, legal notices, or feedback regarding these Terms, please reach out to us at contact@instantcod.online."
            )

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun TermsCard(title: String, content: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                ),
                color = VideonicPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.5.sp,
                    lineHeight = 20.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
