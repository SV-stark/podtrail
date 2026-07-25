package com.stark.podtrail.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SidebarDrawer(
    onNavigate: (Int) -> Unit,
    onClose: () -> Unit,
    onSettings: () -> Unit
) {
    ModalDrawerSheet {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .padding(16.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            Column {
                Text("PodTrack", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                Text("Your personal podcast companion", style = MaterialTheme.typography.bodySmall)
            }
        }
        
        HorizontalDivider()
        Spacer(Modifier.height(16.dp))

        NavigationDrawerItem(
            label = { Text("Home") },
            selected = false,
            onClick = { onNavigate(0); onClose() },
            icon = { Icon(Icons.Default.Home, null) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
        NavigationDrawerItem(
            label = { Text("Discover") },
            selected = false,
            onClick = { onNavigate(1); onClose() },
            icon = { Icon(AppIcons.Explore, null) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
        NavigationDrawerItem(
            label = { Text("Calendar") },
            selected = false,
            onClick = { onNavigate(2); onClose() },
            icon = { Icon(AppIcons.DateRange, null) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
        NavigationDrawerItem(
            label = { Text("Profile") },
            selected = false,
            onClick = { onNavigate(3); onClose() },
            icon = { Icon(Icons.Default.Person, null) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
        
        Spacer(Modifier.weight(1f))
        
        NavigationDrawerItem(
            label = { Text("Settings") },
            selected = false,
            onClick = { onSettings(); onClose() },
            icon = { Icon(Icons.Default.Settings, null) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
        
        Spacer(Modifier.height(16.dp))
    }
}
