package com.stark.podtrail

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.stark.podtrail.data.AppSettings
import com.stark.podtrail.data.SettingsRepository
import com.stark.podtrail.data.ThemeMode
import com.stark.podtrail.ui.AppIcons
import com.stark.podtrail.ui.CalendarScreen
import com.stark.podtrail.ui.DiscoverScreen
import com.stark.podtrail.ui.EnhancedEpisodeListScreen
import com.stark.podtrail.ui.EpisodeDetailScreen
import com.stark.podtrail.ui.HomeScreen
import com.stark.podtrail.ui.PodcastInfoScreen
import com.stark.podtrail.ui.PodcastViewModel
import com.stark.podtrail.ui.ProfileScreen
import com.stark.podtrail.ui.SearchScreen
import com.stark.podtrail.ui.SettingsScreen
import com.stark.podtrail.ui.SidebarDrawer
import com.stark.podtrail.ui.theme.PodTrailTheme
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepo: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        androidx.activity.enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val appSettings by settingsRepo.settings.collectAsState(initial = AppSettings())
            
            PodTrailTheme(
                darkTheme = when(appSettings.themeMode) {
                    ThemeMode.LIGHT -> false
                    ThemeMode.DARK -> true
                    else -> isSystemInDarkTheme()
                },
                dynamicColor = appSettings.useDynamicColor,
                amoled = appSettings.useAmoled,
                customColor = appSettings.customColor
            ) {
                PodTrailApp(settingsRepo = settingsRepo)
            }
        }
    }
}

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Discover : Screen("discover")
    object Calendar : Screen("calendar")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
    object Search : Screen("search")
    object EpisodeList : Screen("episodes/{podcastId}") {
        fun createRoute(podcastId: Long) = "episodes/$podcastId"
    }
    object EpisodeDetail : Screen("episode_detail/{episodeId}") {
        fun createRoute(episodeId: Long) = "episode_detail/$episodeId"
    }
    object PodcastInfo : Screen("podcast_info/{podcastId}") {
        fun createRoute(podcastId: Long) = "podcast_info/$podcastId"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PodTrailApp(settingsRepo: SettingsRepository) {
    val navController = rememberNavController()
    val vm: PodcastViewModel = hiltViewModel()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            SidebarDrawer(
                onNavigate = { index ->
                    val route = when(index) {
                        0 -> Screen.Home.route
                        1 -> Screen.Discover.route
                        2 -> Screen.Calendar.route
                        3 -> Screen.Profile.route
                        else -> Screen.Home.route
                    }
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                    scope.launch { drawerState.close() }
                },
                onClose = { scope.launch { drawerState.close() } },
                onSettings = { 
                    navController.navigate(Screen.Settings.route)
                    scope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                val hideTopBar = currentRoute?.startsWith("episodes/") == true || 
                                 currentRoute?.startsWith("episode_detail/") == true ||
                                 currentRoute == Screen.Settings.route ||
                                 currentRoute?.startsWith("podcast_info/") == true ||
                                 currentRoute == Screen.Search.route

                if (!hideTopBar) {
                    CenterAlignedTopAppBar(
                        title = { 
                            Text(
                                when(currentRoute) {
                                    Screen.Home.route -> "PodTrail"
                                    Screen.Discover.route -> "Discover"
                                    Screen.Calendar.route -> "Calendar"
                                    Screen.Profile.route -> "Profile"
                                    else -> "PodTrail"
                                }, 
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            ) 
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        },
                        actions = {
                            if (currentRoute == Screen.Home.route) {
                                IconButton(onClick = { navController.navigate(Screen.Search.route) }) { 
                                    Icon(Icons.Default.Add, contentDescription = "Add") 
                                }
                                val isRefreshing by vm.isRefreshing.collectAsState()
                                if (isRefreshing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.padding(12.dp).size(24.dp),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    IconButton(onClick = { vm.refreshAllPodcasts() }) { 
                                        Icon(Icons.Default.Refresh, contentDescription = "Refresh") 
                                    }
                                } 
                            }
                        },
                        scrollBehavior = scrollBehavior
                    )
                }
            },
            bottomBar = {
                val showBottomBar = currentRoute in listOf(Screen.Home.route, Screen.Discover.route, Screen.Calendar.route, Screen.Profile.route)
                if (showBottomBar) {
                    NavigationBar {
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                            label = { Text("Home") },
                            selected = currentRoute == Screen.Home.route,
                            onClick = {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                        NavigationBarItem(
                            icon = { Icon(AppIcons.Explore, contentDescription = "Discover") },
                            label = { Text("Discover") },
                            selected = currentRoute == Screen.Discover.route,
                            onClick = {
                                navController.navigate(Screen.Discover.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                        NavigationBarItem(
                            icon = { Icon(AppIcons.DateRange, contentDescription = "Calendar") },
                            label = { Text("Calendar") },
                            selected = currentRoute == Screen.Calendar.route,
                            onClick = {
                                navController.navigate(Screen.Calendar.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                            label = { Text("Profile") },
                            selected = currentRoute == Screen.Profile.route,
                            onClick = {
                                navController.navigate(Screen.Profile.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        ) { padding ->
            NavHost(navController, startDestination = Screen.Home.route, modifier = Modifier.padding(padding)) {
                composable(Screen.Home.route) {
                    HomeScreen(
                        vm = vm,
                        onOpenPodcast = { p -> navController.navigate(Screen.EpisodeList.createRoute(p.id)) },
                        onOpenEpisode = { e -> navController.navigate(Screen.EpisodeDetail.createRoute(e.id)) },
                        onOpenPodcastInfo = { p -> navController.navigate(Screen.PodcastInfo.createRoute(p.id)) }
                    )
                }
                composable(Screen.Discover.route) { DiscoverScreen(vm) }
                composable(Screen.Calendar.route) { CalendarScreen(vm, onEpisodeClick = { e -> navController.navigate(Screen.EpisodeDetail.createRoute(e.id)) }) }
                composable(Screen.Profile.route) {
                     val appSettings by settingsRepo.settings.collectAsState(initial = AppSettings())
                     ProfileScreen(vm, settingsRepo, appSettings) 
                }
                composable(Screen.Settings.route) {
                    val appSettings by settingsRepo.settings.collectAsState(initial = AppSettings())
                    SettingsScreen(settingsRepo, vm.repo, appSettings, onBack = { navController.popBackStack() })
                }
                composable(Screen.Search.route) {
                    SearchScreen(
                        vm = vm,
                        onPodcastClick = { p -> navController.navigate(Screen.EpisodeList.createRoute(p.id)) },
                        onEpisodeClick = { e -> navController.navigate(Screen.EpisodeDetail.createRoute(e.id)) }
                    )
                }
                composable(
                    Screen.EpisodeList.route,
                    arguments = listOf(navArgument("podcastId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val podcastId = backStackEntry.arguments?.getLong("podcastId") ?: 0L
                    EnhancedEpisodeListScreen(
                        vm = vm,
                        podcastId = podcastId,
                        onBack = { navController.popBackStack() },
                        onDetails = { e -> navController.navigate(Screen.EpisodeDetail.createRoute(e.id)) }
                    )
                }
                composable(
                    Screen.EpisodeDetail.route,
                    arguments = listOf(navArgument("episodeId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val episodeId = backStackEntry.arguments?.getLong("episodeId") ?: 0L
                    val fullEpisode by vm.getEpisodeFlow(episodeId).collectAsState(initial = null)
                    LaunchedEffect(episodeId) { vm.fetchAndUpdateDescription(episodeId) }
                    
                    if (fullEpisode != null) {
                        EpisodeDetailScreen(episode = fullEpisode!!, vm = vm, onClose = { navController.popBackStack() })
                    } else {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    }
                }
                composable(
                    Screen.PodcastInfo.route,
                    arguments = listOf(navArgument("podcastId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val podcastId = backStackEntry.arguments?.getLong("podcastId") ?: 0L
                    val podcasts by vm.podcasts.collectAsState()
                    val pStats = podcasts.find { it.podcast.id == podcastId }
                    if (pStats != null) {
                        PodcastInfoScreen(
                            podcast = pStats.podcast,
                            stats = pStats,
                            vm = vm,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
