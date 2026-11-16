package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.model.CurriculumLesson
import com.example.notifications.NotificationHelper
import com.example.ui.screens.CurriculumScreen
import com.example.ui.screens.FocusScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.ScheduleScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.theme.OrangeAccent
import com.example.ui.theme.RafiqTheme
import com.example.ui.viewmodel.MainViewModel
import kotlinx.serialization.Serializable

@Serializable data object HomeRoute
@Serializable data object CurriculumRoute
@Serializable data object ScheduleRoute
@Serializable data object FocusRoute
@Serializable data object StatsRoute
@Serializable data object SettingsRoute

private data class BottomNavItem(
    val route: Any,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MainViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val focusState by viewModel.focusRuntimeState.collectAsStateWithLifecycle()

            RafiqTheme(darkTheme = uiState.isDarkMode) {
                when {
                    uiState.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = OrangeAccent)
                        }
                    }

                    !uiState.profile.onboardingCompleted -> {
                        var isEditingFromSettings by remember {
                            mutableStateOf(uiState.schedulePromptDone)
                        }
                        OnboardingScreen(
                            initialProfile = uiState.profile,
                            onCompleteOnboarding = { name, track, language ->
                                viewModel.saveOnboardingProfile(
                                    name = name,
                                    track = track,
                                    language = language,
                                    isEditingFromSettings = isEditingFromSettings
                                )
                            }
                        )
                    }

                    !uiState.schedulePromptDone -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .statusBarsPadding()
                                .navigationBarsPadding()
                        ) {
                            ScheduleScreen(
                                uiState = uiState,
                                isInitialSetupMode = true,
                                onFinishInitialSetup = { viewModel.finishInitialScheduleSetup() },
                                onAddClass = viewModel::addScheduleClass,
                                onDeleteClass = viewModel::deleteScheduleClass
                            )
                        }
                    }

                    else -> {
                        MainAppShell(
                            viewModel = viewModel,
                            uiState = uiState,
                            focusState = focusState
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MainAppShell(
    viewModel: MainViewModel,
    uiState: com.example.ui.viewmodel.MainUiState,
    focusState: com.example.focus.FocusRuntimeState
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDest = backStackEntry?.destination

    var curriculumReviewFilter by remember { mutableStateOf(false) }
    var focusPreselectedLesson by remember { mutableStateOf<CurriculumLesson?>(null) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !NotificationHelper.hasNotificationPermission(context)
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val navItems = remember {
        listOf(
            BottomNavItem(
                route = HomeRoute,
                label = "الرئيسية",
                selectedIcon = Icons.Rounded.Home,
                unselectedIcon = Icons.Outlined.Home,
                testTag = "nav_home"
            ),
            BottomNavItem(
                route = CurriculumRoute,
                label = "منهجي",
                selectedIcon = Icons.AutoMirrored.Rounded.MenuBook,
                unselectedIcon = Icons.AutoMirrored.Outlined.MenuBook,
                testTag = "nav_curriculum"
            ),
            BottomNavItem(
                route = ScheduleRoute,
                label = "جدولي",
                selectedIcon = Icons.Rounded.CalendarMonth,
                unselectedIcon = Icons.Outlined.CalendarMonth,
                testTag = "nav_schedule"
            ),
            BottomNavItem(
                route = FocusRoute,
                label = "تركيز",
                selectedIcon = Icons.Rounded.Timer,
                unselectedIcon = Icons.Outlined.Timer,
                testTag = "nav_focus"
            ),
            BottomNavItem(
                route = StatsRoute,
                label = "إحصائياتي",
                selectedIcon = Icons.Rounded.BarChart,
                unselectedIcon = Icons.Outlined.BarChart,
                testTag = "nav_stats"
            )
        )
    }

    val isSettingsRoute = currentDest?.hasRoute(SettingsRoute::class) == true
    val hideChrome = focusState.isActive || focusState.showCelebration || isSettingsRoute

    val navigateToTab: (Any) -> Unit = { targetRoute ->
        navController.navigate(targetRoute) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavigationRail = maxWidth >= 700.dp

        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                if (!hideChrome && !useNavigationRail) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        navItems.forEach { item ->
                            val selected = currentDest?.hasRoute(item.route::class) == true
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateToTab(item.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.label
                                    )
                                },
                                label = { Text(item.label) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = OrangeAccent,
                                    selectedTextColor = OrangeAccent,
                                    indicatorColor = OrangeAccent.copy(alpha = 0.16f)
                                ),
                                modifier = Modifier.testTag(item.testTag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .statusBarsPadding()
            ) {
                if (!hideChrome && useNavigationRail) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        navItems.forEach { item ->
                            val selected = currentDest?.hasRoute(item.route::class) == true
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateToTab(item.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.label
                                    )
                                },
                                label = { Text(item.label) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = OrangeAccent,
                                    selectedTextColor = OrangeAccent,
                                    indicatorColor = OrangeAccent.copy(alpha = 0.16f)
                                ),
                                modifier = Modifier.testTag(item.testTag)
                            )
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = HomeRoute,
                    modifier = Modifier.weight(1f)
                ) {
                    composable<HomeRoute> {
                        HomeScreen(
                            uiState = uiState,
                            onRefreshQuote = viewModel::refreshDailyQuote,
                            onOpenCurriculum = {
                                curriculumReviewFilter = false
                                navigateToTab(CurriculumRoute)
                            },
                            onOpenCurriculumReview = {
                                curriculumReviewFilter = true
                                navigateToTab(CurriculumRoute)
                            },
                            onOpenSchedule = { navigateToTab(ScheduleRoute) },
                            onOpenFocus = { navigateToTab(FocusRoute) },
                            onOpenSettings = { navController.navigate(SettingsRoute) }
                        )
                    }

                    composable<CurriculumRoute> {
                        CurriculumScreen(
                            uiState = uiState,
                            initialReviewFilter = curriculumReviewFilter,
                            onClearInitialReviewFilter = { curriculumReviewFilter = false },
                            onToggleCompleted = viewModel::toggleLessonCompleted,
                            onToggleNeedsReview = viewModel::toggleLessonNeedsReview,
                            onStartFocusForLesson = { lesson, _ ->
                                focusPreselectedLesson = lesson
                                navigateToTab(FocusRoute)
                            }
                        )
                    }

                    composable<ScheduleRoute> {
                        ScheduleScreen(
                            uiState = uiState,
                            isInitialSetupMode = false,
                            onAddClass = viewModel::addScheduleClass,
                            onDeleteClass = viewModel::deleteScheduleClass
                        )
                    }

                    composable<FocusRoute> {
                        FocusScreen(
                            uiState = uiState,
                            focusState = focusState,
                            preselectedLesson = focusPreselectedLesson,
                            onClearPreselectedLesson = { focusPreselectedLesson = null },
                            onSaveBlockedApps = viewModel::saveBlockedApps,
                            onStartFocus = viewModel::startFocusSession,
                            onQuitEarly = viewModel::quitFocusSessionEarly,
                            onCelebrationChoice = viewModel::handleCelebrationLessonChoice,
                            onDismissCelebration = viewModel::dismissCelebration
                        )
                    }

                    composable<StatsRoute> {
                        StatsScreen(uiState = uiState)
                    }

                    composable<SettingsRoute> {
                        SettingsScreen(
                            uiState = uiState,
                            onBack = { navController.popBackStack() },
                            onEditProfile = {
                                navController.popBackStack()
                                viewModel.reopenOnboardingForEdit()
                            },
                            onSaveBlockedApps = viewModel::saveBlockedApps,
                            onToggleMotivationalNotifications = viewModel::setMotivationalNotificationsEnabled,
                            onSendTestQuoteNotification = viewModel::sendTestMotivationalNotification,
                            onToggleDarkMode = viewModel::setDarkMode,
                            onExportToUri = viewModel::writeExportToUri,
                            onBuildExportJson = viewModel::buildExportJson,
                            onImportFromUri = viewModel::importScheduleFromUri
                        )
                    }
                }
            }
        }
    }
}
