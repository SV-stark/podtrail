package com.stark.podtrail.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.stark.podtrail.data.EpisodeListItem
import com.stark.podtrail.data.Podcast
import kotlinx.coroutines.flow.MutableStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class SearchScope { ALL, PODCASTS, EPISODES }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    vm: PodcastViewModel,
    onPodcastClick: (Podcast) -> Unit,
    onEpisodeClick: (EpisodeListItem) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var searchScope by remember { mutableStateOf(SearchScope.ALL) }
    
    val podcasts by remember(searchQuery, searchScope) { 
        if (searchQuery.isBlank() || (searchScope != SearchScope.PODCASTS && searchScope != SearchScope.ALL)) 
            MutableStateFlow(emptyList())
        else vm.searchPodcasts(searchQuery)
    }.collectAsState(initial = emptyList())
    
    val episodes by remember(searchQuery, searchScope) {
        if (searchQuery.isBlank() || (searchScope != SearchScope.EPISODES && searchScope != SearchScope.ALL))
            MutableStateFlow(emptyList())
        else vm.searchEpisodes(searchQuery)
    }.collectAsState(initial = emptyList())
    
    val isLoading = searchQuery.isNotBlank() && podcasts.isEmpty() && episodes.isEmpty()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Search Bar and Scope
        Column(
            modifier = Modifier.padding(ResponsiveDimensions.spacingSmall())
        ) {
            SearchBar(
                inputField = {
                    SearchBarDefaults.InputField(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        onSearch = { },
                        expanded = false,
                        onExpandedChange = { },
                        placeholder = { Text("Search podcasts and episodes...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        },
                        trailingIcon = {
                            if (isLoading) {
                                LoadingSpinner(size = 20.dp)
                            }
                        }
                    )
                },
                expanded = false,
                onExpandedChange = { },
                modifier = Modifier.fillMaxWidth()
            ) { }
            
            // Search Scope Selector
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = ResponsiveDimensions.spacingSmall()),
                horizontalArrangement = Arrangement.spacedBy(ResponsiveDimensions.spacingSmall())
            ) {
                FilterChip(
                    selected = searchScope == SearchScope.ALL,
                    onClick = { searchScope = SearchScope.ALL },
                    label = { Text("All") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
                )
                FilterChip(
                    selected = searchScope == SearchScope.PODCASTS,
                    onClick = { searchScope = SearchScope.PODCASTS },
                    label = { Text("Podcasts") },
                    leadingIcon = { Icon(AppIcons.Podcasts, contentDescription = null) }
                )
                FilterChip(
                    selected = searchScope == SearchScope.EPISODES,
                    onClick = { searchScope = SearchScope.EPISODES },
                    label = { Text("Episodes") },
                    leadingIcon = { Icon(AppIcons.Podcasts, contentDescription = null) }
                )
            }
        }
        
        HorizontalDivider()
        
        // Search Results
        when {
            searchQuery.isBlank() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyState(
                        icon = Icons.Default.Search,
                        title = "Search for content",
                        message = "Enter keywords to find podcasts and episodes",
                        action = null
                    )
                }
            }
            isLoading -> {
                LoadingFullScreen("Searching...")
            }
            podcasts.isEmpty() && episodes.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyState(
                        icon = Icons.Default.Search,
                        title = "No results found",
                        message = "Try different keywords or check spelling",
                        action = null
                    )
                }
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(ResponsiveDimensions.spacingSmall()),
                    verticalArrangement = Arrangement.spacedBy(ResponsiveDimensions.spacingSmall())
                ) {
                    // Podcasts Section
                    if (searchScope == SearchScope.ALL || searchScope == SearchScope.PODCASTS) {
                        if (podcasts.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Podcasts (${podcasts.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(vertical = ResponsiveDimensions.spacingTiny())
                                )
                            }
                            items(podcasts) { podcast ->
                                PodcastSearchResult(
                                    podcast = podcast,
                                    onClick = { onPodcastClick(podcast) }
                                )
                            }
                        }
                    }
                    
                    // Episodes Section
                    if (searchScope == SearchScope.ALL || searchScope == SearchScope.EPISODES) {
                        if (episodes.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Episodes (${episodes.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(vertical = ResponsiveDimensions.spacingTiny())
                                )
                            }
                            items(episodes) { episode ->
                                EpisodeSearchResult(
                                    episode = episode,
                                    onClick = { onEpisodeClick(episode) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PodcastSearchResult(
    podcast: Podcast,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(ResponsiveDimensions.spacingSmall()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = podcast.imageUrl,
                contentDescription = podcast.title,
                modifier = Modifier
                    .size(ResponsiveDimensions.iconSizeLarge())
                    .clip(RoundedCornerShape(ResponsiveDimensions.cornerRadiusSmall()))
            )
            Spacer(modifier = Modifier.width(ResponsiveDimensions.spacingSmall()))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = podcast.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1
                )
                if (podcast.description?.isNotBlank() == true) {
                    Text(
                        text = podcast.description.take(100),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }
        }
    }
}

@Composable
fun EpisodeSearchResult(
    episode: EpisodeListItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(ResponsiveDimensions.spacingSmall()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = episode.imageUrl,
                contentDescription = episode.title,
                modifier = Modifier
                    .size(ResponsiveDimensions.iconSizeMedium())
                    .clip(RoundedCornerShape(ResponsiveDimensions.cornerRadiusSmall()))
            )
            Spacer(modifier = Modifier.width(ResponsiveDimensions.spacingSmall()))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = episode.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2
                )
                Text(
                    text = formatTime(episode.pubDate),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatTime(millis: Long): String {
    return SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(millis))
}