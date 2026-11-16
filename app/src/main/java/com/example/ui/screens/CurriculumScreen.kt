package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.data.model.CurriculumLesson
import com.example.data.model.Subject
import com.example.ui.theme.AmberGold
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.OrangeAccent
import com.example.ui.viewmodel.MainUiState

@Composable
fun CurriculumScreen(
    uiState: MainUiState,
    initialReviewFilter: Boolean,
    onClearInitialReviewFilter: () -> Unit,
    onToggleCompleted: (CurriculumLesson) -> Unit,
    onToggleNeedsReview: (CurriculumLesson) -> Unit,
    onStartFocusForLesson: (CurriculumLesson, String) -> Unit
) {
    var selectedSubjectId by rememberSaveable { mutableStateOf<String?>(null) }
    var showReviewOnly by remember(initialReviewFilter) { mutableStateOf(initialReviewFilter) }

    val selectedSubject = uiState.subjects.firstOrNull { it.id == selectedSubjectId }

    if (selectedSubject != null) {
        BackHandler {
            selectedSubjectId = null
        }
        SubjectDetailView(
            subject = selectedSubject,
            uiState = uiState,
            onBack = { selectedSubjectId = null },
            onToggleCompleted = onToggleCompleted,
            onToggleNeedsReview = onToggleNeedsReview,
            onStartFocusForLesson = onStartFocusForLesson
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column {
                    Text(
                        text = "منهجي (${uiState.profile.track.arabicLabel})",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "المنهج الكامل للثانوية العامة 2025/2026 — علّم على اللي خلصته",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FilterChip(
                            selected = !showReviewOnly,
                            onClick = {
                                showReviewOnly = false
                                onClearInitialReviewFilter()
                            },
                            label = { Text("كل المواد (${uiState.subjects.size})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = OrangeAccent,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("filter_all_subjects")
                        )
                        FilterChip(
                            selected = showReviewOnly,
                            onClick = { showReviewOnly = true },
                            label = { Text("محتاج مراجعة (${uiState.reviewLessons.size})") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Bookmark,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AmberGold,
                                selectedLabelColor = Color.Black
                            ),
                            modifier = Modifier.testTag("filter_needs_review")
                        )
                    }
                }
            }

            if (showReviewOnly) {
                if (uiState.reviewLessons.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.BookmarkBorder,
                                    contentDescription = null,
                                    tint = AmberGold,
                                    modifier = Modifier.size(44.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "مفيش دروس متعلمة للمراجعة دلوقتي",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "افتح أي مادة واضغط على علامة الحفظ جنب أي درس صعب عشان يظهرلك هنا.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(uiState.reviewLessons, key = { it.second.id }) { (subject, lesson) ->
                        val progress = uiState.progressMap[lesson.id]
                        LessonItemCard(
                            lesson = lesson,
                            subjectBadge = "${subject.emoji} ${subject.shortDisplayName(uiState.profile.language)}",
                            displayTitle = lesson.displayTitle(uiState.profile.language),
                            isCompleted = progress?.isCompleted == true,
                            needsReview = progress?.needsReview == true,
                            onToggleCompleted = { onToggleCompleted(lesson) },
                            onToggleNeedsReview = { onToggleNeedsReview(lesson) },
                            onStartFocus = {
                                onStartFocusForLesson(
                                    lesson,
                                    subject.shortDisplayName(uiState.profile.language)
                                )
                            }
                        )
                    }
                }
            } else {
                items(uiState.subjects, key = { it.id }) { subject ->
                    val stats = uiState.subjectProgressMap[subject.id]
                    val percent = stats?.percent ?: 0
                    val completed = stats?.completedCount ?: 0
                    val total = stats?.totalCount ?: subject.totalLessonsCount
                    val shortName = subject.shortDisplayName(uiState.profile.language)

                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { selectedSubjectId = subject.id }
                            .testTag("subject_card_${subject.id}")
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = subject.emoji,
                                                style = MaterialTheme.typography.headlineSmall
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = subject.displayName(uiState.profile.language),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "$completed من $total درس",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Surface(
                                    color = if (percent == 100) {
                                        EmeraldSuccess.copy(alpha = 0.2f)
                                    } else {
                                        OrangeAccent.copy(alpha = 0.16f)
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "$percent%",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (percent == 100) EmeraldSuccess else OrangeAccent,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            LinearProgressIndicator(
                                progress = { percent / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                color = if (percent == 100) EmeraldSuccess else OrangeAccent,
                                trackColor = MaterialTheme.colorScheme.surface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "خلصت $percent% من منهج $shortName",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SubjectDetailView(
    subject: Subject,
    uiState: MainUiState,
    onBack: () -> Unit,
    onToggleCompleted: (CurriculumLesson) -> Unit,
    onToggleNeedsReview: (CurriculumLesson) -> Unit,
    onStartFocusForLesson: (CurriculumLesson, String) -> Unit
) {
    val stats = uiState.subjectProgressMap[subject.id]
    val percent = stats?.percent ?: 0
    val shortName = subject.shortDisplayName(uiState.profile.language)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("subject_detail_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "رجوع للمواد"
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${subject.emoji} ${subject.displayName(uiState.profile.language)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                }

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "خلصت $percent% من منهج $shortName",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${stats?.completedCount ?: 0}/${stats?.totalCount ?: 0}",
                                style = MaterialTheme.typography.labelLarge,
                                color = OrangeAccent
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { percent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            color = EmeraldSuccess,
                            trackColor = MaterialTheme.colorScheme.surface
                        )
                    }
                }
            }
        }

        subject.units.forEach { unit ->
            item(key = unit.id) {
                Text(
                    text = unit.displayTitle(uiState.profile.language),
                    style = MaterialTheme.typography.titleMedium,
                    color = OrangeAccent,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                )
            }

            items(unit.lessons, key = { it.id }) { lesson ->
                val progress = uiState.progressMap[lesson.id]
                LessonItemCard(
                    lesson = lesson,
                    subjectBadge = null,
                    displayTitle = lesson.displayTitle(uiState.profile.language),
                    isCompleted = progress?.isCompleted == true,
                    needsReview = progress?.needsReview == true,
                    onToggleCompleted = { onToggleCompleted(lesson) },
                    onToggleNeedsReview = { onToggleNeedsReview(lesson) },
                    onStartFocus = { onStartFocusForLesson(lesson, shortName) }
                )
            }
        }
    }
}

@Composable
private fun LessonItemCard(
    lesson: CurriculumLesson,
    subjectBadge: String?,
    displayTitle: String,
    isCompleted: Boolean,
    needsReview: Boolean,
    onToggleCompleted: () -> Unit,
    onToggleNeedsReview: () -> Unit,
    onStartFocus: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("lesson_card_${lesson.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isCompleted,
                onCheckedChange = { onToggleCompleted() },
                colors = CheckboxDefaults.colors(
                    checkedColor = EmeraldSuccess,
                    checkmarkColor = Color.White
                ),
                modifier = Modifier.testTag("lesson_checkbox_${lesson.id}")
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onToggleCompleted)
                    .padding(horizontal = 4.dp)
            ) {
                if (subjectBadge != null) {
                    Text(
                        text = subjectBadge,
                        style = MaterialTheme.typography.labelSmall,
                        color = OrangeAccent
                    )
                }
                Text(
                    text = displayTitle,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (isCompleted) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${lesson.estimatedMinutes} دقيقة",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (isCompleted) {
                        Spacer(modifier = Modifier.width(10.dp))
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = EmeraldSuccess,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "تم",
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldSuccess
                        )
                    }
                }
            }

            IconButton(
                onClick = onToggleNeedsReview,
                modifier = Modifier.testTag("lesson_review_${lesson.id}")
            ) {
                Icon(
                    imageVector = if (needsReview) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                    contentDescription = if (needsReview) "إلغاء علامة المراجعة" else "تحديد كمحتاج مراجعة",
                    tint = if (needsReview) AmberGold else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = onStartFocus,
                modifier = Modifier.testTag("lesson_focus_${lesson.id}")
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = "ابدأ جلسة تركيز على الدرس",
                    tint = OrangeAccent
                )
            }
        }
    }
}
