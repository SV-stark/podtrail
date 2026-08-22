package com.stark.podtrail.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.stark.podtrail.data.EpisodeListItem
import com.stark.podtrail.data.PlaylistCollection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedEpisodeListScreen(
    vm: com.stark.podtrail.ui.PodcastViewModel,
    podcastId: Long,
    listState: androidx.compose.foundation.lazy.LazyListState = rememberLazyListState(),
    onBack: () -> Unit,
    onDetails: (EpisodeListItem) -> Unit
) {
    val episodes by vm.episodesFor(podcastId).collectAsState(initial = emptyList())
    val sortOption by vm.sortOption.collectAsState()
    
    // Selection state
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedEpisodes by remember { mutableStateOf(setOf<Long>()) }
    
    // Show filters panel
    var showFilters by remember { mutableStateOf(false) }

    // Playlist dialog state
    var showPlaylistDialog by remember { mutableStateOf(false) }
    val collections by vm.playlistCollections.collectAsState()

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { 
                    Text(
                        "Episodes",
                        fontWeight = FontWeight.Bold 
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { 
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") 
                    }
                },
                actions = {
                    // Selection mode toggle
                    IconButton(onClick = { isSelectionMode = !isSelectionMode }) {
                        Icon(
                            imageVector = if (isSelectionMode) Icons.Default.Close else AppIcons.Checklist,
                            contentDescription = if (isSelectionMode) "Cancel selection" else "Select episodes"
                        )
                    }
                    
                    // Filters toggle
                    IconButton(onClick = { showFilters = !showFilters }) {
                        Icon(
                            AppIcons.FilterList,
                            contentDescription = "Filter and sort"
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Filter panels (animated)
                AnimatedVisibility(
                    visible = showFilters,
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(vertical = 8.dp)
                    ) {
                        // Sort dropdown
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Sort by:",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            EpisodeSortMenu(
                                selectedSort = sortOption,
                                onSortChanged = { vm.setSortOption(it) },
                                modifier = Modifier.weight(2f)
                            )
                        }
                    }
                }
                
                HorizontalDivider()
                
                // Episode list
                Box(modifier = Modifier.weight(1f)) {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(
                            bottom = if (isSelectionMode) 80.dp else 16.dp,
                            start = 16.dp,
                            end = 16.dp,
                            top = 16.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = episodes,
                            key = { it.id }
                        ) { episode ->
                            EnhancedEpisodeCard(
                                episode = episode,
                                isSelectionMode = isSelectionMode,
                                isSelected = episode.id in selectedEpisodes,
                                showNewBadge = false,
                                onToggle = { vm.setListened(episode, !episode.listened) },
                                onSelect = { 
                                    selectedEpisodes = if (episode.id in selectedEpisodes) {
                                        selectedEpisodes - episode.id
                                    } else {
                                        selectedEpisodes + episode.id
                                    }
                                },
                                onDetails = { onDetails(episode) }
                            )
                        }
                    }
                }
            }
            
            // Batch actions bar (animated)
            AnimatedVisibility(
                visible = isSelectionMode && selectedEpisodes.isNotEmpty(),
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                BatchActionBar(
                    selectedCount = selectedEpisodes.size,
                    onMarkListened = { 
                        vm.setListenedBatch(selectedEpisodes, true)
                        isSelectionMode = false
                        selectedEpisodes = emptySet()
                    },
                    onMarkUnlistened = { 
                        vm.setListenedBatch(selectedEpisodes, false)
                        isSelectionMode = false
                        selectedEpisodes = emptySet()
                    },
                    onDelete = { 
                        vm.clearTrackingBatch(selectedEpisodes)
                        isSelectionMode = false
                        selectedEpisodes = emptySet()
                    },
                    onAddToPlaylist = { 
                        showPlaylistDialog = true
                    },
                    onClearSelection = { selectedEpisodes = emptySet() },
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }

    if (showPlaylistDialog) {
        PlaylistSelectionDialog(
            collections = collections,
            onDismiss = { showPlaylistDialog = false },
            onSelectCollection = { name ->
                vm.addEpisodesToPlaylistBatch(selectedEpisodes, name)
                showPlaylistDialog = false
                isSelectionMode = false
                selectedEpisodes = emptySet()
            },
            onCreateCollection = { name ->
                vm.createPlaylistCollection(name)
                vm.addEpisodesToPlaylistBatch(selectedEpisodes, name)
                showPlaylistDialog = false
                isSelectionMode = false
                selectedEpisodes = emptySet()
            }
        )
    }
}

@Composable
fun PlaylistSelectionDialog(
    collections: List<PlaylistCollection>,
    onDismiss: () -> Unit,
    onSelectCollection: (String) -> Unit,
    onCreateCollection: (String) -> Unit
) {
    var newCollectionName by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to Playlist/Collection") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (collections.isEmpty()) {
                    Text("No collections found. Create one below:", style = MaterialTheme.typography.bodyMedium)
                } else {
                    Text("Select a collection:", style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.height(8.dp))
                    LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                        items(collections) { col ->
                            TextButton(
                                onClick = { onSelectCollection(col.name) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(col.name)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(Modifier.height(16.dp))
                Text("Create new collection:", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = newCollectionName,
                    onValueChange = { newCollectionName = it },
                    placeholder = { Text("Collection name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (newCollectionName.isNotBlank()) {
                        onCreateCollection(newCollectionName)
                    }
                },
                enabled = newCollectionName.isNotBlank()
            ) {
                Text("Create & Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
