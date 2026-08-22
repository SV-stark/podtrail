package com.stark.podtrail.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

@Composable
fun SwipeableEpisodeCard(
    episode: com.stark.podtrail.data.EpisodeListItem,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onSwipeLeft: () -> Unit = {},
    onSwipeRight: () -> Unit = {},
    onTap: () -> Unit = {},
    onSelect: () -> Unit = {},
    content: @Composable () -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    val swipeThreshold = with(LocalDensity.current) { 100.dp.toPx() }
    val scope = rememberCoroutineScope()
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Background swipe actions
        if (offsetX < -swipeThreshold) {
            SwipeActionBackground(
                icon = Icons.Default.Check,
                text = "Mark Listened",
                color = MaterialTheme.colorScheme.primary,
                alignment = Alignment.CenterStart,
                modifier = Modifier.fillMaxSize()
            )
        } else if (offsetX > swipeThreshold) {
            SwipeActionBackground(
                icon = Icons.Default.Delete,
                text = "Delete",
                color = MaterialTheme.colorScheme.error,
                alignment = Alignment.CenterEnd,
                modifier = Modifier.fillMaxSize()
            )
        }
        
        // Card content
        androidx.compose.animation.AnimatedVisibility(
            visible = offsetX.absoluteValue < swipeThreshold || offsetX == 0f,
            modifier = Modifier.offset { androidx.compose.ui.unit.IntOffset(offsetX.roundToInt(), 0) }
        ) {
            content()
        }
        
        // Gesture handling
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (offsetX < -swipeThreshold) {
                                onSwipeLeft()
                                offsetX = 0f
                            } else if (offsetX > swipeThreshold) {
                                onSwipeRight()
                                offsetX = 0f
                            } else {
                                // Animate back to center
                                scope.launch {
                                    androidx.compose.animation.core.animate(
                                        initialValue = offsetX,
                                        targetValue = 0f,
                                        animationSpec = tween(300)
                                    ) { value, _ -> offsetX = value }
                                }
                            }
                        }
                    ) { change, dragAmount ->
                        change.consume()
                        val newOffset = offsetX + dragAmount
                        offsetX = newOffset.coerceIn(-swipeThreshold * 1.5f, swipeThreshold * 1.5f)
                    }
                }
                .noRippleClickable {
                    if (offsetX.absoluteValue < swipeThreshold) {
                        if (isSelectionMode) {
                            onSelect()
                        } else {
                            onTap()
                        }
                    }
                }
        )
    }
}

@Composable
fun SwipeActionBackground(
    icon: ImageVector,
    text: String,
    color: Color,
    alignment: Alignment,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.background(color),
        contentAlignment = alignment
    ) {
        Row(
            modifier = Modifier.padding(ResponsiveDimensions.spacingMedium()),
            horizontalArrangement = Arrangement.spacedBy(ResponsiveDimensions.spacingSmall()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = text,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun EpisodeSwipeActions(
    episode: com.stark.podtrail.data.EpisodeListItem,
    onMarkListened: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(ResponsiveDimensions.spacingMedium()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(ResponsiveDimensions.spacingMedium())
        ) {
            // Mark as listened action
            ActionButton(
                icon = AppIcons.CheckCircle,
                text = if (episode.listened) "Mark as Unlistened" else "Mark as Listened",
                color = MaterialTheme.colorScheme.primary,
                onClick = onMarkListened,
                modifier = Modifier.fillMaxWidth()
            )
            
            // Delete action
            ActionButton(
                icon = Icons.Default.Delete,
                text = "Delete Episode",
                color = MaterialTheme.colorScheme.error,
                onClick = onDelete,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun ActionButton(
    icon: ImageVector,
    text: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = Color.White
        )
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(ResponsiveDimensions.spacingSmall()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null
            )
            Text(text)
        }
    }
}

// Extension function to remove ripple effect
@Composable
fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier {
    return this.pointerInput(Unit) {
        detectTapGestures(
            onTap = { 
                onClick()
            }
        )
    }
}