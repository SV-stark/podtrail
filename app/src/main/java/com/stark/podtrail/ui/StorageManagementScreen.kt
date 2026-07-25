package com.stark.podtrail.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.stark.podtrail.storage.CleanupOption
import com.stark.podtrail.storage.CleanupResult
import com.stark.podtrail.storage.StorageStats
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageManagementScreen(
    vm: PodcastViewModel,
    onBack: () -> Unit
) {
    val stats by vm.storageStats.collectAsState()
    val isPerformingCleanup by vm.isPerformingCleanup.collectAsState()
    val lastCleanupResults by vm.lastCleanupResults.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Storage Management") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (stats == null) {
            LoadingFullScreen("Analyzing storage...")
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(ResponsiveDimensions.spacingMedium()),
                verticalArrangement = Arrangement.spacedBy(ResponsiveDimensions.spacingMedium())
            ) {
                // Storage Overview Card
                item {
                    StorageOverviewCard(stats = stats!!)
                }

                // Auto Cleanup Section
                item {
                    AutoCleanupCard(
                        onPerformAutoCleanup = { vm.performAutoCleanup() }
                    )
                }

                // Manual Cleanup Options
                item {
                    Text(
                        text = "Manual Cleanup Options",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(CleanupOption.entries.toTypedArray()) { option ->
                    CleanupOptionCard(
                        option = option,
                        stats = stats!!,
                        onClick = { vm.performCleanup(option) }
                    )
                }

                // Last Cleanup Results
                if (lastCleanupResults.isNotEmpty()) {
                    item {
                        HorizontalDivider(modifier = Modifier.padding(vertical = ResponsiveDimensions.spacingSmall()))
                        Text(
                            text = "Recent Cleanup Results",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    items(lastCleanupResults) { result ->
                        CleanupResultCard(result = result)
                    }
                }
            }
        }

        if (isPerformingCleanup) {
            LoadingFullScreen("Performing cleanup...")
        }
    }
}

@Composable
fun StorageOverviewCard(stats: StorageStats) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(ResponsiveDimensions.spacingMedium())
        ) {
            Text(
                text = "Storage Overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(ResponsiveDimensions.spacingSmall()))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Total Episodes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${stats.totalEpisodes}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Column {
                    Text(
                        text = "Estimated DB Size",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = String.format(Locale.getDefault(), "%.1f MB", stats.totalSizeMB),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(ResponsiveDimensions.spacingSmall()))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Old Unlistened",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${stats.oldUnlistenedEpisodes}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                
                Column {
                    Text(
                        text = "Short Descriptions",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${stats.episodesWithoutDescription}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
fun CleanupOptionCard(
    option: CleanupOption,
    stats: StorageStats,
    onClick: () -> Unit
) {
    val info = when (option) {
        CleanupOption.OLD_UNLISTENED_EPISODES -> CleanupDisplayInfo(
            "Remove Old Unlistened Episodes",
            "Delete episodes older than 6 months that haven't been listened to",
            Icons.Default.Delete,
            stats.oldUnlistenedEpisodes > 0
        )
        CleanupOption.TRUNCATE_DESCRIPTIONS -> CleanupDisplayInfo(
            "Truncate Long Descriptions",
            "Limit episode descriptions to 200 characters to save space",
            AppIcons.Checklist,
            true
        )
        CleanupOption.REMOVE_INACTIVE_PODCASTS -> CleanupDisplayInfo(
            "Remove Inactive Podcasts",
            "Delete podcasts not updated in the last year",
            AppIcons.Podcasts,
            stats.podcastsLastUpdated.values.any { it < System.currentTimeMillis() - 365L * 24 * 60 * 60 * 1000 }
        )
        CleanupOption.COMPACT_DATABASE -> CleanupDisplayInfo(
            "Compact Database",
            "Optimize database file size and improve performance",
            AppIcons.Explore,
            true
        )
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        enabled = info.available,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(ResponsiveDimensions.spacingMedium()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = info.icon,
                contentDescription = null,
                modifier = Modifier.size(ResponsiveDimensions.iconSizeMedium()),
                tint = if (info.available) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(ResponsiveDimensions.spacingMedium()))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = info.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (info.available) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = info.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (info.available) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (!info.available) {
                Text(
                    text = "Not Available",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

data class CleanupDisplayInfo(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val available: Boolean
)

@Composable
fun AutoCleanupCard(
    onPerformAutoCleanup: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(ResponsiveDimensions.spacingMedium())
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    AppIcons.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(ResponsiveDimensions.spacingSmall()))
                Text(
                    text = "Auto Cleanup",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(ResponsiveDimensions.spacingSmall()))
            Text(
                text = "Automatically clean up old data and optimize storage based on predefined thresholds.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(ResponsiveDimensions.spacingSmall()))
            Button(
                onClick = onPerformAutoCleanup,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Perform Auto Cleanup")
            }
        }
    }
}

@Composable
fun CleanupResultCard(result: CleanupResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(ResponsiveDimensions.spacingSmall()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                AppIcons.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(ResponsiveDimensions.spacingSmall()))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when (result.option) {
                        CleanupOption.OLD_UNLISTENED_EPISODES -> "Old Episodes Removed"
                        CleanupOption.TRUNCATE_DESCRIPTIONS -> "Descriptions Truncated"
                        CleanupOption.REMOVE_INACTIVE_PODCASTS -> "Inactive Podcasts Removed"
                        CleanupOption.COMPACT_DATABASE -> "Database Compacted"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${result.itemsAffected} items affected • ${String.format(Locale.getDefault(), "%.2f", result.spaceSavedMB)} MB saved",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}