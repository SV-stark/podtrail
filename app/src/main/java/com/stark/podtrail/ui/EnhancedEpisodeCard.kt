package com.stark.podtrail.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.stark.podtrail.data.EpisodeListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedEpisodeCard(
    episode: EpisodeListItem,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    isSwipeInProgress: Boolean = false,
    showNewBadge: Boolean = false,
    onToggle: () -> Unit = {},
    onSelect: () -> Unit = {},
    onDetails: () -> Unit,
    onSwipeLeft: () -> Unit = {},
    onSwipeRight: () -> Unit = {}
) {
    val cardBackgroundColor by animateColorAsState(
        targetValue = when {
            isSelectionMode && isSelected -> MaterialTheme.colorScheme.primaryContainer
            isSwipeInProgress -> MaterialTheme.colorScheme.tertiaryContainer
            episode.listened -> MaterialTheme.colorScheme.surfaceVariant
            else -> MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(durationMillis = 200),
        label = "backgroundColor"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = if (isSelectionMode) onSelect else onDetails),
        colors = CardDefaults.cardColors(
            containerColor = cardBackgroundColor
        ),
        shape = RoundedCornerShape(ResponsiveDimensions.cornerRadiusMedium()),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
        )
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(ResponsiveDimensions.spacingMedium()),
                verticalAlignment = Alignment.Top
            ) {
                // Selection Checkbox
                if (isSelectionMode) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onSelect() },
                        modifier = Modifier.padding(end = ResponsiveDimensions.spacingSmall())
                    )
                }

                // Episode Artwork
                EpisodeThumbnail(
                    imageUrl = episode.imageUrl,
                    modifier = Modifier.size(ResponsiveDimensions.iconSizeExtraLarge()),
                    isListened = episode.listened
                )

                Spacer(modifier = Modifier.width(ResponsiveDimensions.spacingMedium()))

                // Main Content Column
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // Header Row with Title and New Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = episode.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (episode.listened) FontWeight.Normal else FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color = if (episode.listened) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )

                        if (showNewBadge && !episode.listened) {
                            Spacer(modifier = Modifier.width(ResponsiveDimensions.spacingSmall()))
                            NewEpisodeBadge()
                        }
                    }

                    Spacer(modifier = Modifier.height(ResponsiveDimensions.spacingTiny()))

                    // Metadata Row (Duration, Date, Status)
                    EpisodeMetadata(
                        pubDate = episode.pubDate,
                        durationMillis = episode.durationMillis,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // User Rating & Notes
                    if (episode.userRating != null && episode.userRating > 0) {
                        Spacer(modifier = Modifier.height(ResponsiveDimensions.spacingTiny()))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = AppIcons.Star,
                                contentDescription = "Rating",
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${episode.userRating}/10",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    if (!episode.userNotes.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(ResponsiveDimensions.spacingTiny()))
                        Text(
                            text = episode.userNotes,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Progress Bar for partially listened episodes
                    if (episode.playbackPosition > 0 && !episode.listened && episode.durationMillis != null && episode.durationMillis > 0) {
                        Spacer(modifier = Modifier.height(ResponsiveDimensions.spacingSmall()))
                        val progress = (episode.playbackPosition.toFloat() / episode.durationMillis.toFloat()).coerceIn(0f, 1f)
                        PlaybackProgressIndicator(
                            progress = progress,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.width(ResponsiveDimensions.spacingSmall()))

                // Action Button (Listened Toggle or Options)
                EpisodeActionButton(
                    isListened = episode.listened,
                    isSelectionMode = isSelectionMode,
                    onToggle = onToggle
                )
            }
        }
    }
}

@Composable
fun EpisodeThumbnail(
    imageUrl: String?,
    modifier: Modifier = Modifier,
    isListened: Boolean = false
) {
    Box(modifier = modifier) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(ResponsiveDimensions.cornerRadiusSmall())),
            contentScale = ContentScale.Crop,
            error = rememberVectorPainter(AppIcons.Podcasts),
            placeholder = rememberVectorPainter(AppIcons.Podcasts)
        )
        
        // Overlay for listened episodes
        if (isListened) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .clip(RoundedCornerShape(ResponsiveDimensions.cornerRadiusSmall())),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = AppIcons.CheckCircle,
                    contentDescription = "Listened",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(ResponsiveDimensions.iconSizeMedium())
                )
            }
        }
    }
}

@Composable
fun EpisodeMetadata(
    pubDate: Long,
    durationMillis: Long?,
    modifier: Modifier = Modifier
) {
    val dateStr = if (pubDate > 0) 
        java.text.SimpleDateFormat("d MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date(pubDate)) 
    else ""
    
    val durStr = if (durationMillis != null) 
        formatMillis(durationMillis) 
    else ""
    
    val metadata = listOfNotNull(
        dateStr.takeIf { it.isNotEmpty() },
        durStr.takeIf { it.isNotEmpty() }
    ).joinToString(" • ")
    
    Text(
        text = metadata,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}

@Composable
fun PlaybackProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier
) {
    LinearProgressIndicator(
        progress = { progress },
        modifier = modifier
            .height(3.dp)
            .clip(RoundedCornerShape(1.5.dp)),
        color = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant
    )
}

@Composable
fun EpisodeActionButton(
    isListened: Boolean,
    isSelectionMode: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isSelectionMode) {
        // Show more options button in selection mode
        IconButton(onClick = { }, modifier = modifier) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More options",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        // Show listened toggle
        Box(
            modifier = modifier
                .clip(CircleShape)
                .clickable(onClick = onToggle)
                .background(
                    color = if (isListened) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    shape = CircleShape
                )
                .border(
                    width = 1.5.dp,
                    color = if (isListened) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isListened) Icons.Default.Check else Icons.Default.Check,
                contentDescription = if (isListened) "Mark as unlistened" else "Mark as listened",
                tint = if (isListened) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(ResponsiveDimensions.iconSizeSmall())
            )
        }
    }
}

@Composable
fun NewEpisodeBadge(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        shape = RoundedCornerShape(ResponsiveDimensions.cornerRadiusSmall())
    ) {
        Text(
            text = "New",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.padding(
                horizontal = ResponsiveDimensions.spacingTiny(),
                vertical = 1.dp
            )
        )
    }
}

@Composable
fun EpisodeEmptyState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        EmptyState(
            icon = AppIcons.Podcasts,
            title = "No episodes found",
            message = "Try adjusting your filters or check back later for new episodes.",
            action = null
        )
    }
}

private fun formatMillis(ms: Long): String {
    val s = ms / 1000
    val hh = s / 3600
    val mm = (s % 3600) / 60
    val ss = s % 60
    return if (hh > 0) String.format("%d:%02d:%02d", hh, mm, ss) else String.format("%02d:%02d", mm, ss)
}