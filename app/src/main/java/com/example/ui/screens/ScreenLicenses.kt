package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.VideonicPrimary

data class OpenSourceLibrary(
    val name: String,
    val author: String,
    val licenseType: String,
    val description: String,
    val licenseNotice: String
)

val openSourceLibraries = listOf(
    OpenSourceLibrary(
        name = "AndroidX Media3 ExoPlayer",
        author = "The Android Open Source Project",
        licenseType = "Apache License 2.0",
        description = "Next-generation media playback engine for Android supporting hardware-accelerated video decoding, multi-track audio, and subtitles.",
        licenseNotice = """
            Licensed under the Apache License, Version 2.0 (the "License");
            you may not use this file except in compliance with the License.
            You may obtain a copy of the License at

               http://www.apache.org/licenses/LICENSE-2.0
        """.trimIndent()
    ),
    OpenSourceLibrary(
        name = "Jetpack Compose & Material 3",
        author = "Google LLC",
        licenseType = "Apache License 2.0",
        description = "Modern declarative UI toolkit and Material Design 3 design system for Android.",
        licenseNotice = """
            Licensed under the Apache License, Version 2.0 (the "License");
            you may not use this file except in compliance with the License.
        """.trimIndent()
    ),
    OpenSourceLibrary(
        name = "AndroidX Room Database",
        author = "The Android Open Source Project",
        licenseType = "Apache License 2.0",
        description = "SQLite object mapping library providing robust offline persistence for playlists, watch history, and favorites.",
        licenseNotice = """
            Licensed under the Apache License, Version 2.0 (the "License");
            you may not use this file except in compliance with the License.
        """.trimIndent()
    ),
    OpenSourceLibrary(
        name = "Coil Image Loader",
        author = "Colin White",
        licenseType = "Apache License 2.0",
        description = "Coroutines-backed image and video frame decoding library for Android.",
        licenseNotice = """
            Copyright 2024 Coil Contributors
            Licensed under the Apache License, Version 2.0.
        """.trimIndent()
    ),
    OpenSourceLibrary(
        name = "KotlinX Coroutines",
        author = "JetBrains s.r.o.",
        licenseType = "Apache License 2.0",
        description = "Asynchronous and concurrent programming primitives for Kotlin.",
        licenseNotice = """
            Copyright 2000-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
            Licensed under the Apache License, Version 2.0.
        """.trimIndent()
    ),
    OpenSourceLibrary(
        name = "AndroidX DataStore",
        author = "The Android Open Source Project",
        licenseType = "Apache License 2.0",
        description = "Robust transactional key-value data storage solution replacing SharedPreferences.",
        licenseNotice = """
            Licensed under the Apache License, Version 2.0 (the "License");
            you may not use this file except in compliance with the License.
        """.trimIndent()
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenLicenses(
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Open Source Licenses",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("licenses_back_button")
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("screen_licenses")
        ) {
            items(openSourceLibraries, key = { it.name }) { lib ->
                var expanded by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable { expanded = !expanded },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = lib.name,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${lib.author} • ${lib.licenseType}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                    color = VideonicPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Icon(
                                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (expanded) "Collapse" else "Expand",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = lib.description,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (expanded) {
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = lib.licenseNotice,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
