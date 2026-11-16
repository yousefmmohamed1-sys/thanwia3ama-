package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.curriculum.CurriculumData
import com.example.data.model.CurriculumLesson
import com.example.focus.FocusRuntimeState
import com.example.focus.InstalledAppsHelper
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CoralDanger
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.NavyVoid
import com.example.ui.theme.OrangeAccent
import com.example.ui.theme.OrangeBright
import com.example.ui.viewmodel.MainUiState
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusScreen(
    uiState: MainUiState,
    focusState: FocusRuntimeState,
    preselectedLesson: CurriculumLesson?,
    onClearPreselectedLesson: () -> Unit,
    onSaveBlockedApps: (Set<String>) -> Unit,
    onStartFocus: (lesson: CurriculumLesson, subjectName: String, minutes: Int) -> Unit,
    onQuitEarly: (reason: String) -> Unit,
    onCelebrationChoice: (markCompleted: Boolean, markNeedsReview: Boolean) -> Unit,
    onDismissCelebration: () -> Unit
) {
    val context = LocalContext.current
    val allLessons = remember(uiState.subjects) {
        uiState.subjects.flatMap { subject ->
            subject.units.flatMap { unit ->
                unit.lessons.map { lesson -> subject to lesson }
            }
        }
    }

    var selectedMinutes by remember { mutableIntStateOf(25) }
    var customMinutesDialogOpen by remember { mutableStateOf(false) }
    var selectedLessonPair by remember(allLessons, preselectedLesson) {
        mutableStateOf(
            if (preselectedLesson != null) {
                allLessons.firstOrNull { it.second.id == preselectedLesson.id } ?: allLessons.firstOrNull()
            } else {
                allLessons.firstOrNull()
            }
        )
    }
    var lessonDropdownExpanded by remember { mutableStateOf(false) }
    var showBlockedAppsDialog by remember { mutableStateOf(false) }
    var showPermissionsDialog by remember { mutableStateOf(false) }
    var pendingStartAfterSetup by remember { mutableStateOf(false) }

    // Active full-screen countdown mode
    if (focusState.isActive) {
        ActiveFocusCountdownView(
            focusState = focusState,
            dailyQuote = uiState.dailyQuote,
            onQuitEarly = onQuitEarly
        )
        return
    }

    // Post-timer celebration screen
    if (focusState.showCelebration) {
        FocusCelebrationOverlay(
            studentName = uiState.profile.name,
            minutesFocused = focusState.plannedMinutes,
            lessonTitle = focusState.lessonTitle,
            onMarkDone = { onCelebrationChoice(true, false) },
            onMarkNeedsReview = { onCelebrationChoice(false, true) },
            onDismiss = onDismissCelebration
        )
        return
    }

    val durationOptions = listOf(
        25 to "25 دقيقة",
        50 to "50 دقيقة",
        90 to "90 دقيقة",
        120 to "ساعتين"
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "وضع التركيز وحظر المشتتات 🎯",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "اختار المدة والدرس، وإحنا هنمنع أي تطبيق مشتت يفتح وقت المذاكرة.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Big Timer Preview in Middle
        item {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 26.dp, horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.size(190.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawArc(
                                color = OrangeAccent.copy(alpha = 0.18f),
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                            )
                            drawArc(
                                brush = Brush.sweepGradient(listOf(OrangeAccent, AmberGold, OrangeAccent)),
                                startAngle = -90f,
                                sweepAngle = 270f,
                                useCenter = false,
                                style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = String.format(Locale.US, "%02d:00", selectedMinutes),
                                style = MaterialTheme.typography.displayLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "$selectedMinutes دقيقة",
                                style = MaterialTheme.typography.labelLarge,
                                color = OrangeBright
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Duration Chips
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(durationOptions) { (mins, label) ->
                            FilterChip(
                                selected = selectedMinutes == mins,
                                onClick = { selectedMinutes = mins },
                                label = { Text(label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = OrangeAccent,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("duration_chip_$mins")
                            )
                        }
                        item {
                            val isCustom = durationOptions.none { it.first == selectedMinutes }
                            FilterChip(
                                selected = isCustom,
                                onClick = { customMinutesDialogOpen = true },
                                label = {
                                    Text(if (isCustom) "$selectedMinutes د (مخصص)" else "وقت مخصص")
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AmberGold,
                                    selectedLabelColor = Color.Black
                                ),
                                modifier = Modifier.testTag("duration_chip_custom")
                            )
                        }
                    }
                }
            }
        }

        // Lesson Picker
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "هتذاكر درس إيه؟ 📖",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val currentPair = selectedLessonPair
                    ExposedDropdownMenuBox(
                        expanded = lessonDropdownExpanded,
                        onExpandedChange = { lessonDropdownExpanded = !lessonDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = if (currentPair != null) {
                                "${currentPair.first.emoji} ${currentPair.first.shortDisplayName(uiState.profile.language)} — ${currentPair.second.displayTitle(uiState.profile.language)}"
                            } else {
                                "اختار درس من منهجك"
                            },
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = lessonDropdownExpanded)
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                                .testTag("focus_lesson_dropdown")
                        )
                        ExposedDropdownMenu(
                            expanded = lessonDropdownExpanded,
                            onDismissRequest = { lessonDropdownExpanded = false }
                        ) {
                            allLessons.forEach { pair ->
                                val (subject, lesson) = pair
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "${subject.emoji} ${subject.shortDisplayName(uiState.profile.language)}: ${lesson.displayTitle(uiState.profile.language)}",
                                            maxLines = 2
                                        )
                                    },
                                    onClick = {
                                        selectedLessonPair = pair
                                        onClearPreselectedLesson()
                                        lessonDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Blocked Apps Summary Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Shield,
                            contentDescription = null,
                            tint = OrangeAccent,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "التطبيقات المحظورة",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (!uiState.blockedAppsConfigured) {
                                    "لسه مختارتش التطبيقات — هيتم اختيارهم دلوقتي"
                                } else {
                                    "محظور ${uiState.blockedApps.size} تطبيق أثناء التركيز"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    OutlinedButton(
                        onClick = { showBlockedAppsDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("edit_blocked_apps_focus_button")
                    ) {
                        Icon(Icons.Rounded.Apps, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("اختيار")
                    }
                }
            }
        }

        // Big Start Focus Button
        item {
            Button(
                onClick = {
                    val pair = selectedLessonPair ?: return@Button
                    if (!uiState.blockedAppsConfigured) {
                        pendingStartAfterSetup = true
                        showBlockedAppsDialog = true
                    } else {
                        val hasUsage = InstalledAppsHelper.hasUsageStatsPermission(context)
                        val hasOverlay = InstalledAppsHelper.hasOverlayPermission(context)
                        val hasAcc = InstalledAppsHelper.isAccessibilityServiceEnabled(context)
                        if (uiState.blockedApps.isNotEmpty() && (!hasUsage || !hasOverlay || !hasAcc)) {
                            showPermissionsDialog = true
                        } else {
                            onStartFocus(
                                pair.second,
                                pair.first.shortDisplayName(uiState.profile.language),
                                selectedMinutes
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .testTag("start_focus_button"),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent)
            ) {
                Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ابدأ تركيز دلوقتي 🔥",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }

    if (customMinutesDialogOpen) {
        var customText by remember { mutableStateOf(selectedMinutes.toString()) }
        AlertDialog(
            onDismissRequest = { customMinutesDialogOpen = false },
            title = { Text("وقت تركيز مخصص (بالدقائق)", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = customText,
                    onValueChange = { customText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("عدد الدقائق (من 5 إلى 240)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = customText.toIntOrNull()?.coerceIn(5, 240) ?: 25
                        selectedMinutes = parsed
                        customMinutesDialogOpen = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent)
                ) {
                    Text("تحديد")
                }
            },
            dismissButton = {
                TextButton(onClick = { customMinutesDialogOpen = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    if (showBlockedAppsDialog) {
        BlockedAppsPickerDialog(
            installedApps = uiState.installedApps,
            initiallySelected = uiState.blockedApps,
            isFirstTimeSetup = !uiState.blockedAppsConfigured,
            onDismiss = {
                showBlockedAppsDialog = false
                pendingStartAfterSetup = false
            },
            onSave = { selected ->
                onSaveBlockedApps(selected)
                showBlockedAppsDialog = false
                if (pendingStartAfterSetup) {
                    pendingStartAfterSetup = false
                    val hasUsage = InstalledAppsHelper.hasUsageStatsPermission(context)
                    val hasOverlay = InstalledAppsHelper.hasOverlayPermission(context)
                    val hasAcc = InstalledAppsHelper.isAccessibilityServiceEnabled(context)
                    if (selected.isNotEmpty() && (!hasUsage || !hasOverlay || !hasAcc)) {
                        showPermissionsDialog = true
                    } else {
                        val pair = selectedLessonPair
                        if (pair != null) {
                            onStartFocus(
                                pair.second,
                                pair.first.shortDisplayName(uiState.profile.language),
                                selectedMinutes
                            )
                        }
                    }
                }
            }
        )
    }

    if (showPermissionsDialog) {
        val hasUsage = InstalledAppsHelper.hasUsageStatsPermission(context)
        val hasOverlay = InstalledAppsHelper.hasOverlayPermission(context)
        val hasAcc = InstalledAppsHelper.isAccessibilityServiceEnabled(context)

        BlockerPermissionsDialog(
            hasUsage = hasUsage,
            hasOverlay = hasOverlay,
            hasAccessibility = hasAcc,
            onDismiss = { showPermissionsDialog = false },
            onContinueTimerOnly = {
                showPermissionsDialog = false
                val pair = selectedLessonPair
                if (pair != null) {
                    onStartFocus(
                        pair.second,
                        pair.first.shortDisplayName(uiState.profile.language),
                        selectedMinutes
                    )
                }
            }
        )
    }
}

@Composable
private fun ActiveFocusCountdownView(
    focusState: FocusRuntimeState,
    dailyQuote: String,
    onQuitEarly: (String) -> Unit
) {
    // Prevent accidental back button exit during focus session
    BackHandler(enabled = true) { }

    var showQuitDialog by remember { mutableStateOf(false) }
    var holdProgress by remember { mutableFloatStateOf(0f) }
    var isHolding by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val mins = focusState.remainingSeconds / 60
    val secs = focusState.remainingSeconds % 60
    val timeText = String.format(Locale.US, "%02d:%02d", mins, secs)
    val progress = if (focusState.totalSeconds > 0) {
        focusState.remainingSeconds.toFloat() / focusState.totalSeconds.toFloat()
    } else 0f

    LaunchedEffect(isHolding) {
        if (isHolding) {
            val steps = 30
            for (i in 1..steps) {
                if (!isHolding) break
                delay(100L) // 30 * 100ms = 3 full seconds!
                holdProgress = i / steps.toFloat()
                if (i == steps) {
                    isHolding = false
                    holdProgress = 0f
                    showQuitDialog = true
                }
            }
        } else {
            holdProgress = 0f
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(NavyVoid, NavyDeep)))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    color = OrangeAccent.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "🛡️ وضع التركيز مفعّل — ${focusState.subjectName}",
                        style = MaterialTheme.typography.labelLarge,
                        color = AmberGold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = focusState.lessonTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            // Big Circular Countdown
            Box(
                modifier = Modifier.size(250.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawArc(
                        color = Color.White.copy(alpha = 0.12f),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 18.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawArc(
                        brush = Brush.sweepGradient(listOf(OrangeAccent, AmberGold, OrangeAccent)),
                        startAngle = -90f,
                        sweepAngle = 360f * progress,
                        useCenter = false,
                        style = Stroke(width = 18.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = timeText,
                        fontSize = 52.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "كمل يا بطل!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFCBD5E1)
                    )
                }
            }

            // Quote + Hold-for-3-seconds Exit Button
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = dailyQuote,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFFE2E8F0),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = CoralDanger.copy(alpha = 0.16f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    scope.launch {
                                        isHolding = true
                                        tryAwaitRelease()
                                        isHolding = false
                                    }
                                }
                            )
                        }
                        .testTag("hold_to_quit_focus_button")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isHolding) "خليك دايس لحد ما الشريط يكتمل..." else "اضغط مطولاً 3 ثواني للخروج المبكر",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color(0xFFFCA5A5)
                        )
                        if (holdProgress > 0f) {
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { holdProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(6.dp)),
                                color = CoralDanger,
                                trackColor = Color.White.copy(alpha = 0.15f)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showQuitDialog) {
        QuitReasonDialog(
            onDismiss = { showQuitDialog = false },
            onConfirmQuit = { reason ->
                showQuitDialog = false
                onQuitEarly(reason)
            }
        )
    }
}
