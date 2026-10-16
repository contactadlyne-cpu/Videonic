package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import com.example.data.model.VideoItem
import com.example.data.preferences.VideoSortOrder
import com.example.data.preferences.ViewLayout
import com.example.ui.theme.VideonicPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideonicTopBar(
    title: String = "Videonic Lite",
    showActions: Boolean = true,
    viewLayout: ViewLayout = ViewLayout.LIST,
    onSearchClick: () -> Unit = {},
    onRefreshClick: () -> Unit = {},
    onViewLayoutChange: (ViewLayout) -> Unit = {},
    onSortOrderChange: (VideoSortOrder) -> Unit = {}
) {
    var showSortMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
        },
        actions = {
            if (showActions) {
                IconButton(
                    onClick = onSearchClick,
                    modifier = Modifier.testTag("top_search_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                IconButton(
                    onClick = onRefreshClick,
                    modifier = Modifier.testTag("top_refresh_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Library",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Box {
                    IconButton(
                        onClick = { showSortMenu = true },
                        modifier = Modifier.testTag("top_sort_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Sort,
                            contentDescription = "Sort Videos",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Date (Newest first)") },
                            onClick = {
                                onSortOrderChange(VideoSortOrder.DATE_DESC)
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Date (Oldest first)") },
                            onClick = {
                                onSortOrderChange(VideoSortOrder.DATE_ASC)
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Name (A to Z)") },
                            onClick = {
                                onSortOrderChange(VideoSortOrder.NAME_ASC)
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Name (Z to A)") },
                            onClick = {
                                onSortOrderChange(VideoSortOrder.NAME_DESC)
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Size (Largest first)") },
                            onClick = {
                                onSortOrderChange(VideoSortOrder.SIZE_DESC)
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Duration (Longest first)") },
                            onClick = {
                                onSortOrderChange(VideoSortOrder.DURATION_DESC)
                                showSortMenu = false
                            }
                        )
                    }
                }
                var showViewMenu by remember { mutableStateOf(false) }
                Box {
                    IconButton(
                        onClick = { showViewMenu = true },
                        modifier = Modifier.testTag("top_toggle_view_button")
                    ) {
                        Icon(
                            imageVector = if (viewLayout == ViewLayout.GRID) Icons.Default.GridView else Icons.Default.ViewList,
                            contentDescription = "View Options",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    DropdownMenu(
                        expanded = showViewMenu,
                        onDismissRequest = { showViewMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("List View") },
                            leadingIcon = { Icon(Icons.Default.ViewList, contentDescription = null) },
                            onClick = {
                                onViewLayoutChange(ViewLayout.LIST)
                                showViewMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Grid View") },
                            leadingIcon = { Icon(Icons.Default.GridView, contentDescription = null) },
                            onClick = {
                                onViewLayoutChange(ViewLayout.GRID)
                                showViewMenu = false
                            }
                        )
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

enum class HomeTab(val label: String) {
    VIDEOS("Videos"),
    FOLDERS("Folders"),
    RECENT("Recent"),
    FAVORITES("Favorites")
}

@Composable
fun VideonicTabs(
    selectedTab: HomeTab,
    onTabSelected: (HomeTab) -> Unit
) {
    val tabs = HomeTab.values()
    val selectedIndex = tabs.indexOf(selectedTab)

    TabRow(
        selectedTabIndex = selectedIndex,
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.primary,
        indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                height = 3.dp,
                color = MaterialTheme.colorScheme.primary
            )
        },
        divider = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            )
        }
    ) {
        tabs.forEach { tab ->
            val isSelected = tab == selectedTab
            Tab(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                modifier = Modifier.testTag("tab_${tab.name.lowercase()}"),
                text = {
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp
                        ),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
        }
    }
}

@Composable
fun VideoThumbnail(
    video: VideoItem,
    modifier: Modifier = Modifier,
    showDuration: Boolean = true,
    showThumbnailImage: Boolean = true
) {
    val context = LocalContext.current
    val imageRequest = if (showThumbnailImage) {
        ImageRequest.Builder(context)
            .data(video.uri)
            .decoderFactory { result, options, _ -> VideoFrameDecoder(result.source, options) }
            .videoFrameMillis(1000)
            .crossfade(true)
            .build()
    } else null

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (showThumbnailImage && imageRequest != null) {
            AsyncImage(
                model = imageRequest,
                contentDescription = video.displayName,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Center semi-transparent play badge if no image loaded (No background 'dot')
        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.8f),
            modifier = Modifier.size(28.dp).align(Alignment.Center)
        )

        // Duration overlay badge
        if (showDuration && video.durationMs > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xCC000000))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = video.formattedDuration,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
