package com.stark.podtrail.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

private inline fun createIcon(name: String, block: ImageVector.Builder.() -> Unit): ImageVector {
    return ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply(block).build()
}

object AppIcons {
    val Podcasts: ImageVector = createIcon("Podcasts") {
        path(fill = SolidColor(Color(0xFF000000))) {
            moveTo(12f, 12f)
            // Center Dot
            moveTo(12f, 10f)
            arcTo(2f, 2f, 0f, true, true, 11.99f, 10f)
            close()
            // Antennas / Waves
            moveTo(12f, 2f)
            arcTo(10f, 10f, 0f, false, false, 2f, 12f)
            arcTo(10f, 10f, 0f, false, false, 3.5f, 17f)
            lineTo(4.9f, 15.6f)
            arcTo(8f, 8f, 0f, false, true, 4f, 12f)
            arcTo(8f, 8f, 0f, false, true, 12f, 4f)
            arcTo(8f, 8f, 0f, false, true, 20f, 12f)
            arcTo(8f, 8f, 0f, false, true, 19.1f, 15.6f)
            lineTo(20.5f, 17f)
            arcTo(10f, 10f, 0f, false, false, 22f, 12f)
            arcTo(10f, 10f, 0f, false, false, 12f, 2f)
            close()
            // Inner wave & stem
            moveTo(12f, 6f)
            arcTo(6f, 6f, 0f, false, false, 6f, 12f)
            arcTo(6f, 6f, 0f, false, false, 7.3f, 15.3f)
            lineTo(8.7f, 13.9f)
            arcTo(4f, 4f, 0f, false, true, 8f, 12f)
            arcTo(4f, 4f, 0f, false, true, 12f, 8f)
            arcTo(4f, 4f, 0f, false, true, 16f, 12f)
            arcTo(4f, 4f, 0f, false, true, 15.3f, 13.9f)
            lineTo(16.7f, 15.3f)
            arcTo(6f, 6f, 0f, false, false, 18f, 12f)
            arcTo(6f, 6f, 0f, false, false, 12f, 6f)
            close()
        }
    }

    val PlaylistAdd: ImageVector = createIcon("PlaylistAdd") {
        path(fill = SolidColor(Color(0xFF000000))) {
            moveTo(14f, 10f); lineTo(3f, 10f); lineTo(3f, 12f); lineTo(14f, 12f); lineTo(14f, 10f); close()
            moveTo(14f, 6f); lineTo(3f, 6f); lineTo(3f, 8f); lineTo(14f, 8f); lineTo(14f, 6f); close()
            moveTo(14f, 14f); lineTo(3f, 14f); lineTo(3f, 16f); lineTo(14f, 16f); lineTo(14f, 14f); close()
            moveTo(18f, 10f); lineTo(18f, 6f); lineTo(16f, 6f); lineTo(16f, 10f); lineTo(12f, 10f); lineTo(12f, 12f); lineTo(16f, 12f); lineTo(16f, 16f); lineTo(18f, 16f); lineTo(18f, 12f); lineTo(22f, 12f); lineTo(22f, 10f); close()
        }
    }

    val PlaylistPlay: ImageVector = createIcon("PlaylistPlay") {
        path(fill = SolidColor(Color(0xFF000000))) {
            moveTo(19f, 9f); lineTo(3f, 9f); lineTo(3f, 11f); lineTo(19f, 11f); lineTo(19f, 9f); close()
            moveTo(19f, 5f); lineTo(3f, 5f); lineTo(3f, 7f); lineTo(19f, 7f); lineTo(19f, 5f); close()
            moveTo(14f, 13f); lineTo(3f, 13f); lineTo(3f, 15f); lineTo(14f, 15f); lineTo(14f, 13f); close()
            moveTo(17f, 13f); lineTo(17f, 19f); lineTo(22f, 16f); close()
        }
    }

    val Explore: ImageVector = createIcon("Explore") {
        path(fill = SolidColor(Color(0xFF000000))) {
            moveTo(12f, 2f)
            arcTo(10f, 10f, 0f, true, false, 22f, 12f)
            arcTo(10f, 10f, 0f, false, false, 12f, 2f)
            close()
            moveTo(14.12f, 14.12f)
            lineTo(7f, 17f)
            lineTo(9.88f, 9.88f)
            lineTo(17f, 7f)
            close()
        }
    }

    val DateRange: ImageVector = createIcon("DateRange") {
        path(fill = SolidColor(Color(0xFF000000))) {
            moveTo(19f, 4f); lineTo(18f, 4f); lineTo(18f, 2f); lineTo(16f, 2f); lineTo(16f, 4f); lineTo(8f, 4f); lineTo(8f, 2f); lineTo(6f, 2f); lineTo(6f, 4f); lineTo(5f, 4f)
            arcTo(2f, 2f, 0f, false, false, 3f, 6f)
            lineTo(3f, 20f)
            arcTo(2f, 2f, 0f, false, false, 5f, 22f)
            lineTo(19f, 22f)
            arcTo(2f, 2f, 0f, false, false, 21f, 20f)
            lineTo(21f, 6f)
            arcTo(2f, 2f, 0f, false, false, 19f, 4f)
            close()
            moveTo(19f, 20f); lineTo(5f, 20f); lineTo(5f, 10f); lineTo(19f, 10f); close()
            moveTo(19f, 8f); lineTo(5f, 8f); lineTo(5f, 6f); lineTo(19f, 6f); close()
        }
    }

    val ChevronLeft: ImageVector = createIcon("ChevronLeft") {
        path(fill = SolidColor(Color(0xFF000000))) {
            moveTo(15.41f, 7.41f); lineTo(14f, 6f); lineTo(8f, 12f); lineTo(14f, 18f); lineTo(15.41f, 16.59f); lineTo(10.83f, 12f); close()
        }
    }

    val ChevronRight: ImageVector = createIcon("ChevronRight") {
        path(fill = SolidColor(Color(0xFF000000))) {
            moveTo(10f, 6f); lineTo(8.59f, 7.41f); lineTo(13.17f, 12f); lineTo(8.59f, 16.59f); lineTo(10f, 18f); lineTo(16f, 12f); close()
        }
    }

    val Star: ImageVector = createIcon("Star") {
        path(fill = SolidColor(Color(0xFF000000))) {
            moveTo(12f, 17.27f); lineTo(18.18f, 21f); lineTo(16.54f, 13.97f); lineTo(22f, 9.24f); lineTo(14.81f, 8.63f); lineTo(12f, 2f); lineTo(9.19f, 8.63f); lineTo(2f, 9.24f); lineTo(7.46f, 13.97f); lineTo(5.82f, 21f); close()
        }
    }

    val Favorite: ImageVector = createIcon("Favorite") {
        path(fill = SolidColor(Color(0xFF000000))) {
            moveTo(12f, 21.35f)
            lineTo(10.55f, 20.03f)
            arcTo(10.5f, 10.5f, 0f, false, true, 2f, 8.5f)
            arcTo(5.5f, 5.5f, 0f, false, true, 7.5f, 3f)
            arcTo(5.5f, 5.5f, 0f, false, true, 12f, 5.09f)
            arcTo(5.5f, 5.5f, 0f, false, true, 16.5f, 3f)
            arcTo(5.5f, 5.5f, 0f, false, true, 22f, 8.5f)
            arcTo(10.5f, 10.5f, 0f, false, true, 13.45f, 20.03f)
            close()
        }
    }

    val FavoriteBorder: ImageVector = createIcon("FavoriteBorder") {
        path(fill = SolidColor(Color(0xFF000000))) {
            moveTo(16.5f, 3f)
            arcTo(5.5f, 5.5f, 0f, false, false, 12f, 5.09f)
            arcTo(5.5f, 5.5f, 0f, false, false, 7.5f, 3f)
            arcTo(5.5f, 5.5f, 0f, false, false, 2f, 8.5f)
            arcTo(10.5f, 10.5f, 0f, false, false, 10.55f, 20.03f)
            lineTo(12f, 21.35f)
            lineTo(13.45f, 20.03f)
            arcTo(10.5f, 10.5f, 0f, false, false, 22f, 8.5f)
            arcTo(5.5f, 5.5f, 0f, false, false, 16.5f, 3f)
            close()
            moveTo(12.1f, 18.55f)
            arcTo(8.5f, 8.5f, 0f, false, true, 4f, 8.5f)
            arcTo(3.5f, 3.5f, 0f, false, true, 7.5f, 5f)
            arcTo(3.5f, 3.5f, 0f, false, true, 11f, 7.36f)
            lineTo(13f, 7.36f)
            arcTo(3.5f, 3.5f, 0f, false, true, 16.5f, 5f)
            arcTo(3.5f, 3.5f, 0f, false, true, 20f, 8.5f)
            arcTo(8.5f, 8.5f, 0f, false, true, 11.9f, 18.55f)
            close()
        }
    }

    val CheckCircle: ImageVector = createIcon("CheckCircle") {
        path(fill = SolidColor(Color(0xFF000000))) {
            moveTo(12f, 2f)
            arcTo(10f, 10f, 0f, true, false, 22f, 12f)
            arcTo(10f, 10f, 0f, false, false, 12f, 2f)
            close()
            moveTo(10f, 17f)
            lineTo(5f, 12f)
            lineTo(6.41f, 10.59f)
            lineTo(10f, 14.17f)
            lineTo(17.59f, 6.58f)
            lineTo(19f, 8f)
            close()
        }
    }

    val CheckCircleOutline: ImageVector = createIcon("CheckCircleOutline") {
        path(fill = SolidColor(Color(0xFF000000))) {
            moveTo(12f, 2f)
            arcTo(10f, 10f, 0f, true, false, 22f, 12f)
            arcTo(10f, 10f, 0f, false, false, 12f, 2f)
            close()
            moveTo(12f, 20f)
            arcTo(8f, 8f, 0f, true, true, 20f, 12f)
            arcTo(8f, 8f, 0f, false, true, 12f, 20f)
            close()
            moveTo(16.59f, 7.58f)
            lineTo(10f, 14.17f)
            lineTo(7.41f, 11.59f)
            lineTo(6f, 13f)
            lineTo(10f, 17f)
            lineTo(18f, 9f)
            close()
        }
    }

    val RadioButtonUnchecked: ImageVector = createIcon("RadioButtonUnchecked") {
        path(fill = SolidColor(Color(0xFF000000))) {
            moveTo(12f, 2f)
            arcTo(10f, 10f, 0f, true, false, 22f, 12f)
            arcTo(10f, 10f, 0f, false, false, 12f, 2f)
            close()
            moveTo(12f, 20f)
            arcTo(8f, 8f, 0f, true, true, 20f, 12f)
            arcTo(8f, 8f, 0f, false, true, 12f, 20f)
            close()
        }
    }

    val FilterList: ImageVector = createIcon("FilterList") {
        path(fill = SolidColor(Color(0xFF000000))) {
            moveTo(10f, 18f); lineTo(14f, 18f); lineTo(14f, 16f); lineTo(10f, 16f); close()
            moveTo(3f, 6f); lineTo(3f, 8f); lineTo(21f, 8f); lineTo(21f, 6f); close()
            moveTo(6f, 13f); lineTo(18f, 13f); lineTo(18f, 11f); lineTo(6f, 11f); close()
        }
    }

    val Checklist: ImageVector = createIcon("Checklist") {
        path(fill = SolidColor(Color(0xFF000000))) {
            moveTo(22f, 7f); lineTo(20.59f, 5.59f); lineTo(13.41f, 12.77f); lineTo(10.83f, 10.19f); lineTo(9.41f, 11.61f); lineTo(13.41f, 15.61f); close()
            moveTo(2f, 7f); lineTo(8f, 7f); lineTo(8f, 9f); lineTo(2f, 9f); close()
            moveTo(2f, 12f); lineTo(8f, 12f); lineTo(8f, 14f); lineTo(2f, 14f); close()
            moveTo(2f, 17f); lineTo(16f, 17f); lineTo(16f, 19f); lineTo(2f, 19f); close()
        }
    }

    val Timer: ImageVector = createIcon("Timer") {
        path(fill = SolidColor(Color(0xFF000000))) {
            moveTo(15f, 1f); lineTo(9f, 1f); lineTo(9f, 3f); lineTo(15f, 3f); close()
            moveTo(11f, 14f); lineTo(13f, 14f); lineTo(13f, 8f); lineTo(11f, 8f); close()
            moveTo(19.03f, 7.39f); lineTo(20.45f, 5.97f)
            arcTo(9f, 9f, 0f, true, false, 12f, 22f)
            arcTo(9f, 9f, 0f, false, false, 19.03f, 7.39f)
            close()
            moveTo(12f, 20f)
            arcTo(7f, 7f, 0f, true, true, 19f, 13f)
            arcTo(7f, 7f, 0f, false, true, 12f, 20f)
            close()
        }
    }

    val LocalFireDepartment: ImageVector = createIcon("LocalFireDepartment") {
        path(fill = SolidColor(Color(0xFF000000))) {
            moveTo(12f, 12.9f)
            arcTo(3f, 3f, 0f, false, false, 9f, 15.9f)
            arcTo(3f, 3f, 0f, false, false, 12f, 18.9f)
            arcTo(3f, 3f, 0f, false, false, 15f, 15.9f)
            arcTo(3f, 3f, 0f, false, false, 12f, 12.9f)
            close()
            moveTo(18f, 7.5f)
            arcTo(9f, 9f, 0f, false, false, 12f, 2f)
            arcTo(9f, 9f, 0f, false, false, 6f, 7.5f)
            arcTo(10f, 10f, 0f, false, false, 2f, 13.89f)
            arcTo(10f, 10f, 0f, false, false, 12f, 23.89f)
            arcTo(10f, 10f, 0f, false, false, 22f, 13.89f)
            arcTo(10f, 10f, 0f, false, false, 18f, 7.5f)
            close()
        }
    }

    val Mic: ImageVector = createIcon("Mic") {
        path(fill = SolidColor(Color(0xFF000000))) {
            moveTo(12f, 14f)
            arcTo(3f, 3f, 0f, false, false, 15f, 11f)
            lineTo(15f, 5f)
            arcTo(3f, 3f, 0f, false, false, 9f, 5f)
            lineTo(9f, 11f)
            arcTo(3f, 3f, 0f, false, false, 12f, 14f)
            close()
            moveTo(17.3f, 11f)
            arcTo(5.3f, 5.3f, 0f, false, true, 12f, 16.3f)
            arcTo(5.3f, 5.3f, 0f, false, true, 6.7f, 11f)
            lineTo(5f, 11f)
            arcTo(7f, 7f, 0f, false, false, 11f, 17.72f)
            lineTo(11f, 21f)
            lineTo(13f, 21f)
            lineTo(13f, 17.72f)
            arcTo(7f, 7f, 0f, false, false, 19f, 11f)
            close()
        }
    }

    val Download: ImageVector = createIcon("Download") {
        path(fill = SolidColor(Color(0xFF000000))) {
            moveTo(19f, 9f); lineTo(15f, 9f); lineTo(15f, 3f); lineTo(9f, 3f); lineTo(9f, 9f); lineTo(5f, 9f); lineTo(12f, 16f); close()
            moveTo(5f, 18f); lineTo(5f, 20f); lineTo(19f, 20f); lineTo(19f, 18f); close()
        }
    }

    val GridView: ImageVector = createIcon("GridView") {
        path(fill = SolidColor(Color(0xFF000000))) {
            moveTo(3f, 3f); lineTo(3f, 11f); lineTo(11f, 11f); lineTo(11f, 3f); close()
            moveTo(5f, 5f); lineTo(9f, 5f); lineTo(9f, 9f); lineTo(5f, 9f); close()
            moveTo(13f, 3f); lineTo(13f, 11f); lineTo(21f, 11f); lineTo(21f, 3f); close()
            moveTo(15f, 5f); lineTo(19f, 5f); lineTo(19f, 9f); lineTo(15f, 9f); close()
            moveTo(3f, 13f); lineTo(3f, 21f); lineTo(11f, 21f); lineTo(11f, 13f); close()
            moveTo(5f, 15f); lineTo(9f, 15f); lineTo(9f, 19f); lineTo(5f, 19f); close()
            moveTo(13f, 13f); lineTo(13f, 21f); lineTo(21f, 21f); lineTo(21f, 13f); close()
            moveTo(15f, 15f); lineTo(19f, 15f); lineTo(19f, 19f); lineTo(15f, 19f); close()
        }
    }

    val List: ImageVector = createIcon("List") {
        path(fill = SolidColor(Color(0xFF000000))) {
            moveTo(3f, 13f); lineTo(5f, 13f); lineTo(5f, 11f); lineTo(3f, 11f); close()
            moveTo(3f, 17f); lineTo(5f, 17f); lineTo(5f, 15f); lineTo(3f, 15f); close()
            moveTo(3f, 9f); lineTo(5f, 9f); lineTo(5f, 7f); lineTo(3f, 7f); close()
            moveTo(7f, 13f); lineTo(21f, 13f); lineTo(21f, 11f); lineTo(7f, 11f); close()
            moveTo(7f, 17f); lineTo(21f, 17f); lineTo(21f, 15f); lineTo(7f, 15f); close()
            moveTo(7f, 7f); lineTo(7f, 9f); lineTo(21f, 9f); lineTo(21f, 7f); close()
        }
    }
}
