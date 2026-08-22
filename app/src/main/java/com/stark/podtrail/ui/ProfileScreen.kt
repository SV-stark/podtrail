package com.stark.podtrail.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.stark.podtrail.data.AppSettings
import com.stark.podtrail.data.PodcastWithStats
import com.stark.podtrail.data.SettingsRepository
import kotlinx.coroutines.launch
import androidx.compose.ui.text.style.TextAlign

@Composable
fun ProfileScreen(
    vm: PodcastViewModel,
    settingsRepo: SettingsRepository,
    appSettings: AppSettings
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    var selectedBadgeForShare by remember { mutableStateOf<com.stark.podtrail.ui.Badge?>(null) }
    
    // Stats from VM
    val podcasts by vm.podcasts.collectAsState()
    val totalPodcasts = podcasts.size
    val totalEpisodesListened = podcasts.sumOf { it.listenedEpisodes }
    val totalTimeListened by vm.totalTimeListened.collectAsState()
    val currentStreak by vm.currentStreak.collectAsState()
    val badges by vm.badges.collectAsState()
    
    // Genre Breakdown
    val genreMap = remember(podcasts) {
        podcasts.groupBy { it.podcast.primaryGenre ?: "Other" }
            .mapValues { entry -> entry.value.size }
    }

    val favoritePodcasts by vm.favoritePodcasts.collectAsState()

    // Image Pickers
    val profileImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch { settingsRepo.setProfileImage(uri.toString()) }
        }
    }

    val bgImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch { settingsRepo.setProfileBg(uri.toString()) }
        }
    }

    // State for background selection
    var showBgSelectionDialog by remember { mutableStateOf(false) }
    var showPodcastPicker by remember { mutableStateOf(false) }
    var showNameEditDialog by remember { mutableStateOf(false) }
    var tempName by remember { mutableStateOf("") }

    if (showBgSelectionDialog) {
        AlertDialog(
            onDismissRequest = { showBgSelectionDialog = false },
            title = { Text("Change Cover") },
            text = {
                Column {
                    ListItem(
                        headlineContent = { Text("Choose from Podcast Art") },
                        leadingContent = { Icon(AppIcons.GridView, null) },
                        modifier = Modifier.clickable { 
                            showBgSelectionDialog = false
                            showPodcastPicker = true
                        }
                    )
                    ListItem(
                        headlineContent = { Text("Upload Image") },
                        leadingContent = { Icon(AppIcons.Explore, null) },
                        modifier = Modifier.clickable { 
                            showBgSelectionDialog = false
                            bgImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                    )
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showBgSelectionDialog = false }) { Text("Cancel") } }
        )
    }

    if (showPodcastPicker) {
        AlertDialog(
            onDismissRequest = { showPodcastPicker = false },
            title = { Text("Select Podcast Art") },
            text = {
                Box(Modifier.height(300.dp)) {
                    if (podcasts.isEmpty()) {
                        Text("No podcasts subscribed.", modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(podcasts) { pStats ->
                                AsyncImage(
                                    model = pStats.podcast.imageUrl,
                                    contentDescription = pStats.podcast.title,
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            scope.launch { settingsRepo.setProfileBg(pStats.podcast.imageUrl ?: "") }
                                            showPodcastPicker = false
                                        },
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showPodcastPicker = false }) { Text("Cancel") } }
        )
    }

    if (showNameEditDialog) {
        AlertDialog(
            onDismissRequest = { showNameEditDialog = false },
            title = { Text("Edit Name") },
            text = {
                OutlinedTextField(
                    value = tempName,
                    onValueChange = { tempName = it },
                    label = { Text("Your Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (tempName.isNotBlank()) {
                        vm.setUserName(tempName)
                        showNameEditDialog = false
                    }
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showNameEditDialog = false }) { Text("Cancel") } }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(MaterialTheme.colorScheme.background)
    ) {
        // --- Header Section ---
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.BottomCenter
        ) {
            // Cover Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .align(Alignment.TopCenter)
            ) {
                if (appSettings.profileBgUri != null) {
                    AsyncImage(
                        model = appSettings.profileBgUri,
                        contentDescription = "Cover Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { showBgSelectionDialog = true }
                    )
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.2f)))
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.surface
                                    )
                                )
                            )
                            .clickable { showBgSelectionDialog = true }
                    )
                }
                
                // Edit Cover Button
                SmallFloatingActionButton(
                    onClick = { showBgSelectionDialog = true },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    Icon(Icons.Default.Edit, "Edit Cover", modifier = Modifier.size(20.dp))
                }
            }

            // Profile Picture (Overlapping)
            Box(
                modifier = Modifier
                    .offset(y = 50.dp) // Push down to overlap half out
                    .size(120.dp)
            ) {
                 AsyncImage(
                     model = appSettings.profileImageUri ?: "https://www.gravatar.com/avatar/00000000000000000000000000000000?d=mp&f=y",
                     contentDescription = "Profile Picture",
                     contentScale = ContentScale.Crop,
                     modifier = Modifier
                         .fillMaxSize()
                         .clip(CircleShape)
                         .border(4.dp, MaterialTheme.colorScheme.background, CircleShape)
                         .clickable { 
                             profileImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                         }
                 )
                 // Edit Profile Pic Badge
                 Box(
                     modifier = Modifier
                         .align(Alignment.BottomEnd)
                         .padding(4.dp)
                         .size(32.dp)
                         .background(MaterialTheme.colorScheme.primary, CircleShape)
                         .border(2.dp, MaterialTheme.colorScheme.background, CircleShape),
                     contentAlignment = Alignment.Center
                 ) {
                     Icon(
                         AppIcons.Mic, 
                         contentDescription = null, 
                         tint = MaterialTheme.colorScheme.onPrimary,
                         modifier = Modifier.size(16.dp)
                     )
                 }
            }
        }

        Spacer(Modifier.height(60.dp)) // Clearance for profile pic

        // User Greeting
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome Back",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { 
                    tempName = appSettings.userName ?: "PodTrail User"
                    showNameEditDialog = true 
                }
            ) {
                Text(
                    text = appSettings.userName ?: "PodTrail User",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Default.Edit, "Edit Name", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(Modifier.height(32.dp))

        // --- Statistics Section ---
        SectionHeader("Statistics")
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard(
                title = "Total Episodes",
                value = totalEpisodesListened.toString(),
                icon = AppIcons.PlaylistPlay,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Total Time",
                value = formatTimeListenedShort(totalTimeListened),
                icon = AppIcons.Timer,
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(Modifier.height(16.dp))
        
         Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard(
                title = "Subscribed",
                value = totalPodcasts.toString(),
                icon = AppIcons.Podcasts,
                color = MaterialTheme.colorScheme.tertiaryContainer,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Streak",
                value = "$currentStreak days",
                icon = AppIcons.LocalFireDepartment,
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(32.dp))

        // --- Achievements Section ---
        SectionHeader("Achievements")
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            badges.forEach { badge ->
                 BadgeCard(badge, onClick = { selectedBadgeForShare = badge })
            }
        }

        Spacer(Modifier.height(32.dp))

        // --- Insights Section ---
        SectionHeader("Listening Insights")
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text("Genre Breakdown", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                
                if (totalPodcasts == 0) {
                     Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                         Text("Subscribe to podcasts to see insights.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                     }
                } else {
                    val chartColors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.tertiary,
                        MaterialTheme.colorScheme.secondary,
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.tertiaryContainer,
                        MaterialTheme.colorScheme.secondaryContainer
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Legend
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            var colorIndex = 0
                            genreMap.entries.sortedByDescending { it.value }.take(5).forEach { entry ->
                                val color = chartColors[colorIndex % chartColors.size]
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(12.dp).background(color, CircleShape))
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        text = entry.key,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(Modifier.weight(1f))
                                    Text(
                                        text = "${entry.value}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                colorIndex++
                            }
                        }

                        Spacer(Modifier.width(16.dp))

                        // Chart
                        Box(
                            modifier = Modifier.size(140.dp),
                            contentAlignment = Alignment.Center
                        ) {
                             Canvas(modifier = Modifier.size(120.dp)) {
                                 val total = totalPodcasts.toFloat()
                                 var startAngle = -90f
                                 var colorIndex = 0
                                 
                                 val gapAngle = 4f
                                 genreMap.entries.sortedByDescending { it.value }.forEach { entry ->
                                     val color = chartColors[colorIndex % chartColors.size]
                                    val sweepAngle = (entry.value / total) * 360f
                                    val drawSweep = if (totalPodcasts > 1) sweepAngle - gapAngle else sweepAngle
                                    
                                    if (drawSweep > 0) {
                                        drawArc(
                                            color = color,
                                            startAngle = startAngle + (if (totalPodcasts > 1) gapAngle / 2 else 0f),
                                            sweepAngle = drawSweep,
                                            useCenter = false,
                                            style = Stroke(width = 20.dp.toPx(), cap = StrokeCap.Round)
                                        )
                                    }
                                    startAngle += sweepAngle
                                    colorIndex++
                                }
                             }
                             // Center text
                             Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                  Text(totalPodcasts.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                  Text("Subs", style = MaterialTheme.typography.labelSmall)
                             }
                        }
                    }
                }
            }
        }
        
        

        
        Spacer(Modifier.height(32.dp))

        // --- Heatmap Section ---
        val history by vm.history.collectAsState()
        ListeningHeatmap(history)

        Spacer(Modifier.height(32.dp))

        // --- Weekly Activity ---
        val weeklyActivity by vm.weeklyActivity.collectAsState()
        WeeklyActivityChart(weeklyActivity)

        Spacer(Modifier.height(32.dp))

        // --- Top Podcasts ---
        val topPodcasts by vm.topPodcasts.collectAsState()
        if (topPodcasts.isNotEmpty()) {
            TopPodcastsList(topPodcasts)
            Spacer(Modifier.height(32.dp))
        }

        // --- Favorites Section ---
        if (favoritePodcasts.isNotEmpty()) {
            SectionHeader("Favorites")
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.height(160.dp) // Fixed height for a row or small grid
            ) {
                items(favoritePodcasts) { p ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(100.dp)
                    ) {
                        AsyncImage(
                            model = p.imageUrl,
                            contentDescription = p.title,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = p.title,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }
        if (selectedBadgeForShare != null) {
            BadgeShareDialog(badge = selectedBadgeForShare!!, onDismiss = { selectedBadgeForShare = null })
        }
        
        Spacer(Modifier.height(48.dp))
    }
}

// --- New Composable: Weekly Activity Chart ---
@Composable
fun WeeklyActivityChart(activityMap: Map<Int, Long>) {
    val days = (0..6).map { i ->
        val cal = java.util.Calendar.getInstance()
        cal.add(java.util.Calendar.DAY_OF_YEAR, -i)
        cal
    }.reversed() // Show Mon -> Sun or just chronological

    val maxVal = activityMap.values.maxOrNull() ?: 1L
    val maxMinutes = kotlin.math.max(maxVal / 60000f, 1f) // Normalize to minutes

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(200.dp)
    ) {
        SectionHeader("Weekly Activity")
        
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            days.forEach { date ->
                val dayKey = date.get(java.util.Calendar.DAY_OF_YEAR)
                val millis = activityMap[dayKey] ?: 0L
                val minutes = millis / 60000f
                val heightRatio = (minutes / maxMinutes).coerceIn(0.1f, 1f)
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    // Bar
                    Box(
                        modifier = Modifier
                            .width(12.dp)
                            .fillMaxHeight(heightRatio)
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Spacer(Modifier.height(8.dp))
                    // Label (Day Name)
                    Text(
                        text = java.text.SimpleDateFormat("E", java.util.Locale.getDefault()).format(date.time).take(1),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// --- New Composable: Top Podcasts List ---
@Composable
fun TopPodcastsList(podcasts: List<PodcastWithStats>) {
    Column(Modifier.padding(horizontal = 16.dp)) {
        SectionHeader("Top Podcasts")
        
        podcasts.forEachIndexed { index, p ->
            val timeString = formatTimeListenedShort(p.timeListened ?: 0L)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "#${index + 1}", 
                    style = MaterialTheme.typography.labelLarge, 
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.width(32.dp)
                )
                
                AsyncImage(
                    model = p.podcast.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                
                Spacer(Modifier.width(12.dp))
                
                Column(Modifier.weight(1f)) {
                    Text(
                        p.podcast.title, 
                        style = MaterialTheme.typography.bodyMedium, 
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Text(
                        "$timeString listened",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun StatCard(
    title: String, 
    value: String, 
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(110.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha=0.4f)), // Lighter shade
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween, // Icon top, Content bottom
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha=0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                 Icon(icon, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurface)
            }
            
            Column {
                Text(
                    value, 
                    style = MaterialTheme.typography.titleLarge, 
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    title, 
                    style = MaterialTheme.typography.labelMedium, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun BadgeCard(badge: com.stark.podtrail.ui.Badge, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (badge.unlocked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.width(110.dp).height(130.dp).clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (badge.unlocked) MaterialTheme.colorScheme.primary.copy(alpha=0.2f) else MaterialTheme.colorScheme.surface.copy(alpha=0.5f), 
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (badge.unlocked) badge.icon else AppIcons.CheckCircleOutline,
                    contentDescription = null,
                    tint = if (badge.unlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.5f),
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                badge.name, 
                style = MaterialTheme.typography.labelMedium, 
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                fontWeight = if (badge.unlocked) FontWeight.Bold else FontWeight.Normal,
                color = if (badge.unlocked) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun BadgeShareDialog(badge: com.stark.podtrail.ui.Badge, onDismiss: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Achievement Unlocked!", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                                MaterialTheme.colorScheme.surface
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (badge.unlocked) badge.icon else AppIcons.CheckCircleOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                }
                
                Spacer(Modifier.height(16.dp))
                
                Text(
                    badge.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(Modifier.height(8.dp))
                
                Text(
                    badge.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                
                if (!badge.unlocked) {
                    Spacer(Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(AppIcons.CheckCircleOutline, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.width(8.dp))
                        Text("Locked", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val shareText = if (badge.unlocked) {
                        "I unlocked the '${badge.name}' achievement on PodTrail! 🎧\n\n${badge.description}\n\nJoin me in tracking your podcast journey!"
                    } else {
                        "I'm working on unlocking the '${badge.name}' achievement on PodTrail! 🎧\n\n${badge.description}"
                    }
                    val sendIntent = android.content.Intent().apply {
                        action = android.content.Intent.ACTION_SEND
                        putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                        type = "text/plain"
                    }
                    val shareIntent = android.content.Intent.createChooser(sendIntent, "Share Achievement")
                    context.startActivity(shareIntent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Share, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Share Achievement")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Close")
            }
        }
    )
}

@Composable
fun ListeningHeatmap(history: List<com.stark.podtrail.data.Episode>) {
    val context = LocalContext.current
    val heatmapData = remember(history) {
        val cal = java.util.Calendar.getInstance()
        cal.add(java.util.Calendar.WEEK_OF_YEAR, -11)
        cal.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.SUNDAY)
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        
        val startMillis = cal.timeInMillis
        val oneDayMillis = 24 * 3600 * 1000L
        val dayCounts = mutableMapOf<Int, Int>()

        // Single O(history) pass
        history.forEach { ep ->
            val listenedTime = ep.listenedAt ?: 0L
            if (listenedTime >= startMillis) {
                val dayIndex = ((listenedTime - startMillis) / oneDayMillis).toInt()
                if (dayIndex in 0 until 84) {
                    dayCounts[dayIndex] = (dayCounts[dayIndex] ?: 0) + 1
                }
            }
        }

        // Build result in O(84)
        List(84) { i ->
            val dayStart = startMillis + i * oneDayMillis
            Pair(dayStart, dayCounts[i] ?: 0)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        SectionHeader("Activity Heatmap")
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Last 12 weeks of logging activity",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (week in 0 until 12) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            for (day in 0 until 7) {
                                val index = week * 7 + day
                                val item = heatmapData.getOrNull(index) ?: Pair(0L, 0)
                                val date = java.util.Date(item.first)
                                val count = item.second
                                
                                val color = when {
                                    count == 0 -> MaterialTheme.colorScheme.surfaceVariant
                                    count in 1..2 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                    count in 3..4 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                    else -> MaterialTheme.colorScheme.primary
                                }
                                
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(color)
                                        .clickable {
                                            val dateString = java.text.SimpleDateFormat("d MMM yyyy", java.util.Locale.getDefault()).format(date)
                                            android.widget.Toast.makeText(
                                                context,
                                                "Logged $count episodes on $dateString",
                                                android.widget.Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Less", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(4.dp))
                    Box(Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(MaterialTheme.colorScheme.surfaceVariant))
                    Spacer(Modifier.width(2.dp))
                    Box(Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)))
                    Spacer(Modifier.width(2.dp))
                    Box(Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)))
                    Spacer(Modifier.width(2.dp))
                    Box(Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(MaterialTheme.colorScheme.primary))
                    Spacer(Modifier.width(4.dp))
                    Text("More", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

private fun formatTimeListenedShort(millis: Long): String {
    val seconds = millis / 1000
    val days = seconds / (24 * 3600)
    val hours = (seconds % (24 * 3600)) / 3600
    val minutes = (seconds % 3600) / 60
    
    return when {
        days > 0 -> "${days}d ${hours}h"
        hours > 0 -> "${hours}h ${minutes}m"
        else -> "${minutes}m"
    }
}

