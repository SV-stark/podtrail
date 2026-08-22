package com.stark.podtrail.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.stark.podtrail.data.Episode
import com.stark.podtrail.network.SearchResult
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(vm: PodcastViewModel) {
    val discoverPodcasts by vm.discoverPodcasts.collectAsState()
    val title by vm.discoverTitle.collectAsState()
    var showPreviewPodcast by remember { mutableStateOf<SearchResult?>(null) }
    
    val genres = remember {
        listOf(
            "Top" to null,
            "Comedy" to 1303L,
            "News" to 1489L,
            "Tech" to 1310L,
            "True Crime" to 1488L,
            "History" to 1487L,
            "Science" to 1315L,
            "Business" to 1321L
        )
    }
    
    var selectedGenreIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        if (discoverPodcasts.isEmpty()) vm.refreshDiscover()
    }

    if (showPreviewPodcast != null) {
        val p = showPreviewPodcast!!
        AlertDialog(
            onDismissRequest = { showPreviewPodcast = null },
            title = { Text(p.collectionName ?: "Podcast") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AsyncImage(
                        model = p.artworkUrl600 ?: p.artworkUrl100,
                        contentDescription = null,
                        modifier = Modifier.size(200.dp).clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop,
                        placeholder = rememberVectorPainter(AppIcons.Podcasts),
                        error = rememberVectorPainter(AppIcons.Podcasts)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(p.artistName ?: "", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)
                    Spacer(Modifier.height(16.dp))
                    Text("Genre: ${p.primaryGenreName ?: "Unknown"}", style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                Button(onClick = {
                    vm.subscribeToSearchResult(p)
                    showPreviewPodcast = null
                }) { Text("Subscribe") }
            },
            dismissButton = {
                TextButton(onClick = { showPreviewPodcast = null }) { Text("Close") }
            }
        )
    }

    Column(Modifier.fillMaxSize()) {
        Text("Discover", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(16.dp))
        
        androidx.compose.foundation.lazy.LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(genres.size) { index ->
                val (name, id) = genres[index]
                FilterChip(
                    selected = selectedGenreIndex == index,
                    onClick = {
                        selectedGenreIndex = index
                        vm.selectDiscoverGenre(id, name)
                    },
                    label = { Text(name) },
                    leadingIcon = if (selectedGenreIndex == index) {
                        { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                    } else null
                )
            }
        }
        
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 16.dp))
        if (discoverPodcasts.isEmpty()) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 140.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(6) {
                    ShimmerItemPlaceholder(height = 180.dp)
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 140.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(discoverPodcasts) { podcast ->
                    Card(
                         onClick = { showPreviewPodcast = podcast },
                         shape = RoundedCornerShape(12.dp),
                         elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column {
                            AsyncImage(
                                model = podcast.artworkUrl600 ?: podcast.artworkUrl100,
                                contentDescription = null,
                                modifier = Modifier.aspectRatio(1f).fillMaxWidth(),
                                contentScale = ContentScale.Crop,
                                placeholder = rememberVectorPainter(AppIcons.Podcasts),
                                error = rememberVectorPainter(AppIcons.Podcasts)
                            )
                            Box(Modifier.padding(12.dp)) {
                                Text(
                                    text = podcast.collectionName ?: "Unknown",
                                    style = MaterialTheme.typography.titleSmall,
                                    maxLines = 2,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
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
fun EpisodeDetailScreen(episode: Episode, vm: PodcastViewModel, onClose: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxWidth().weight(0.35f)) {
            AsyncImage(
                 model = episode.imageUrl,
                 contentDescription = null,
                 modifier = Modifier.fillMaxSize(),
                 contentScale = ContentScale.Crop,
                 alpha = 0.4f
            )
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha=0.3f)))
            
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopStart)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), CircleShape)
            ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurface) }

            AsyncImage(
                model = episode.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxHeight(0.85f)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .shadow(8.dp),
                contentScale = ContentScale.Crop,
                error = rememberVectorPainter(AppIcons.Podcasts),
                placeholder = rememberVectorPainter(AppIcons.Podcasts)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.65f)
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            Text(episode.title, style = MaterialTheme.typography.titleLarge, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (episode.episodeNumber != null) {
                    Text("Ep ${episode.episodeNumber}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text("•", style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.width(8.dp))
                }
                if (episode.pubDate > 0) {
                     Text(SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(episode.pubDate)), style = MaterialTheme.typography.labelMedium)
                     Spacer(Modifier.width(8.dp))
                     Text("•", style = MaterialTheme.typography.labelMedium)
                     Spacer(Modifier.width(8.dp))
                }
                if (episode.durationMillis != null) {
                    Text(formatMillis(episode.durationMillis), style = MaterialTheme.typography.labelMedium)
                }
            }
            
            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

             Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                if (!episode.description.isNullOrBlank()) {
                    val decodedDescription = try {
                        android.text.Html.fromHtml(episode.description, android.text.Html.FROM_HTML_MODE_COMPACT).toString()
                    } catch (e: Exception) { episode.description }
                    
                    Text(
                        text = decodedDescription, 
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                } else {
                    Text("No description available.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                if (episode.listened) {
                    Spacer(Modifier.height(24.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(16.dp))
                    
                    Text("Your Log", style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    
                    // Star Rating (1 to 10 stars)
                    val currentRating = episode.userRating ?: 0
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        (1..10).forEach { star ->
                            val isSelected = star <= currentRating
                            Icon(
                                imageVector = if (isSelected) AppIcons.Star else AppIcons.FavoriteBorder,
                                contentDescription = "$star Stars",
                                tint = if (isSelected) Color(0xFFFFC107) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable {
                                        val newRating = if (currentRating == star) null else star
                                        vm.updateEpisodeRatingAndNotes(episode.id, newRating, episode.userNotes)
                                    }
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    // Private Notes / Review
                    var notesText by remember(episode.id, episode.userNotes) { mutableStateOf(episode.userNotes ?: "") }
                    var isEditingNotes by remember { mutableStateOf(false) }
                    
                    OutlinedTextField(
                        value = notesText,
                        onValueChange = { 
                            notesText = it
                            isEditingNotes = true
                        },
                        label = { Text("Private listening notes / review") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4,
                        trailingIcon = {
                            if (isEditingNotes) {
                                IconButton(onClick = {
                                    vm.updateEpisodeRatingAndNotes(episode.id, if (currentRating == 0) null else currentRating, if (notesText.isBlank()) null else notesText)
                                    isEditingNotes = false
                                }) {
                                    Icon(Icons.Default.Check, contentDescription = "Save Notes", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    )
                }
             }

             Spacer(Modifier.height(16.dp))
             
             Button(onClick = { vm.setListened(episode, !episode.listened) }, modifier = Modifier.fillMaxWidth()) {
                 Text(if (episode.listened) "Remove Listened" else "Mark Listened")
            }
        }
    }
}

private fun formatMillis(ms: Long): String {
    val s = ms / 1000
    val hh = s / 3600
    val mm = (s % 3600) / 60
    val ss = s % 60
    return if (hh > 0) String.format("%d:%02d:%02d", hh, mm, ss) else String.format("%02d:%02d", mm, ss)
}
