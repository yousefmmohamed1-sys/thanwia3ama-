package com.example.ui.screens

import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Schedule
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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.ScheduleClassEntity
import com.example.data.model.ClassPlace
import com.example.data.model.DayOfWeekAr
import com.example.data.model.Subject
import com.example.notifications.ClassAlarmScheduler
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CoralDanger
import com.example.ui.theme.OrangeAccent
import com.example.ui.theme.OrangeBright
import com.example.ui.viewmodel.MainUiState

@Composable
fun ScheduleScreen(
    uiState: MainUiState,
    isInitialSetupMode: Boolean = false,
    onFinishInitialSetup: () -> Unit = {},
    onAddClass: (
        dayIndex: Int,
        subject: Subject,
        startHour: Int,
        startMinute: Int,
        endHour: Int,
        endMinute: Int,
        place: ClassPlace,
        teacherName: String
    ) -> Unit,
    onDeleteClass: (Long) -> Unit
) {
    var selectedDayIndex by rememberSaveable { mutableIntStateOf(DayOfWeekAr.today().index) }
    var showFullWeekGrid by rememberSaveable { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }

    val selectedDay = DayOfWeekAr.fromIndex(selectedDayIndex)
    val dayClasses = uiState.scheduleClasses.filter { it.dayOfWeekIndex == selectedDayIndex }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = OrangeAccent,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_schedule_class_fab")
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "إضافة حصة أو درس")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column {
                    Text(
                        text = if (isInitialSetupMode) "ابني جدولك الأسبوعي 📅" else "جدولي الأسبوعي 📅",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "اكتبه مرة واحدة وهنفكرك قبل كل درس بـ 60 دقيقة وعند بداية الحصة.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (isInitialSetupMode) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = onFinishInitialSetup,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("finish_initial_schedule_button"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent)
                            ) {
                                Text("حفظ الجدول والمتابعة")
                            }
                            OutlinedButton(
                                onClick = onFinishInitialSetup,
                                modifier = Modifier.height(48.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text("تخطي الآن")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = OrangeAccent.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.NotificationsActive,
                                contentDescription = null,
                                tint = OrangeAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "تنبيه تلقائي قبل الدرس بساعة + عند بداية الدرس",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FilterChip(
                            selected = !showFullWeekGrid,
                            onClick = { showFullWeekGrid = false },
                            label = { Text("عرض باليوم") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = OrangeAccent,
                                selectedLabelColor = Color.White
                            )
                        )
                        FilterChip(
                            selected = showFullWeekGrid,
                            onClick = { showFullWeekGrid = true },
                            label = { Text("الأسبوع كامل (${uiState.scheduleClasses.size})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = OrangeAccent,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            if (!showFullWeekGrid) {
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(DayOfWeekAr.entries, key = { it.index }) { day ->
                            val isSelected = day.index == selectedDayIndex
                            val isToday = day == DayOfWeekAr.today()
                            val count = uiState.scheduleClasses.count { it.dayOfWeekIndex == day.index }

                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) OrangeAccent else MaterialTheme.colorScheme.surfaceVariant,
                                border = if (isToday && !isSelected) BorderStroke(1.dp, AmberGold) else null,
                                modifier = Modifier
                                    .clickable { selectedDayIndex = day.index }
                                    .testTag("day_tab_${day.index}")
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = day.arabicName,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (count > 0) {
                                        Text(
                                            text = "$count حصة",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isSelected) Color.White.copy(alpha = 0.9f) else AmberGold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (dayClasses.isEmpty()) {
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
                                Text(
                                    text = "يوم ${selectedDay.arabicName} فاضي حالياً",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "اضغط على زر + لإضافة دروس السنتر أو الأونلاين أو البرايفت.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(dayClasses, key = { it.id }) { item ->
                        ScheduleClassCard(
                            item = item,
                            onDelete = { onDeleteClass(item.id) }
                        )
                    }
                }
            } else {
                DayOfWeekAr.entries.forEach { day ->
                    val itemsForDay = uiState.scheduleClasses.filter { it.dayOfWeekIndex == day.index }
                    item(key = "header_${day.index}") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = day.arabicName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (day == DayOfWeekAr.today()) AmberGold else OrangeAccent
                            )
                            TextButton(
                                onClick = {
                                    selectedDayIndex = day.index
                                    showAddDialog = true
                                }
                            ) {
                                Text("+ إضافة")
                            }
                        }
                    }

                    if (itemsForDay.isEmpty()) {
                        item(key = "empty_${day.index}") {
                            Text(
                                text = "لا توجد حصص مسجلة",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    } else {
                        items(itemsForDay, key = { it.id }) { item ->
                            ScheduleClassCard(
                                item = item,
                                onDelete = { onDeleteClass(item.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog && uiState.subjects.isNotEmpty()) {
        AddClassDialog(
            initialDayIndex = selectedDayIndex,
            subjects = uiState.subjects,
            language = uiState.profile.language,
            onDismiss = { showAddDialog = false },
            onConfirm = { dayIdx, subject, sH, sM, eH, eM, place, teacher ->
                onAddClass(dayIdx, subject, sH, sM, eH, eM, place, teacher)
                selectedDayIndex = dayIdx
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun ScheduleClassCard(
    item: ScheduleClassEntity,
    onDelete: () -> Unit
) {
    val place = runCatching { ClassPlace.valueOf(item.place) }.getOrDefault(ClassPlace.CENTER)
    val startStr = ClassAlarmScheduler.formatArabicTime(item.startHour, item.startMinute)
    val endStr = ClassAlarmScheduler.formatArabicTime(item.endHour, item.endMinute)

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("schedule_item_${item.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.subjectName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = OrangeAccent.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "${place.emoji} ${place.arabicLabel}",
                            style = MaterialTheme.typography.labelSmall,
                            color = AmberGold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "⏰ من $startStr إلى $endStr",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OrangeBright
                )
                if (item.teacherName.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "👨‍🏫 أ/ ${item.teacherName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier.testTag("delete_class_${item.id}")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = "حذف الحصة",
                    tint = CoralDanger
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddClassDialog(
    initialDayIndex: Int,
    subjects: List<Subject>,
    language: com.example.data.model.StudyLanguage,
    onDismiss: () -> Unit,
    onConfirm: (
        dayIndex: Int,
        subject: Subject,
        startHour: Int,
        startMinute: Int,
        endHour: Int,
        endMinute: Int,
        place: ClassPlace,
        teacherName: String
    ) -> Unit
) {
    val context = LocalContext.current
    var selectedDayIndex by remember { mutableIntStateOf(initialDayIndex) }
    var selectedSubject by remember { mutableStateOf(subjects.first()) }
    var subjectMenuExpanded by remember { mutableStateOf(false) }
    var dayMenuExpanded by remember { mutableStateOf(false) }
    var startHour by remember { mutableIntStateOf(16) }
    var startMinute by remember { mutableIntStateOf(0) }
    var endHour by remember { mutableIntStateOf(18) }
    var endMinute by remember { mutableIntStateOf(0) }
    var selectedPlace by remember { mutableStateOf(ClassPlace.CENTER) }
    var teacherName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("إضافة حصة جديدة 📅", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Day Picker
                ExposedDropdownMenuBox(
                    expanded = dayMenuExpanded,
                    onExpandedChange = { dayMenuExpanded = !dayMenuExpanded }
                ) {
                    OutlinedTextField(
                        value = DayOfWeekAr.fromIndex(selectedDayIndex).arabicName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("اليوم") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dayMenuExpanded) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = dayMenuExpanded,
                        onDismissRequest = { dayMenuExpanded = false }
                    ) {
                        DayOfWeekAr.entries.forEach { day ->
                            DropdownMenuItem(
                                text = { Text(day.arabicName) },
                                onClick = {
                                    selectedDayIndex = day.index
                                    dayMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // Subject Dropdown (from user's track)
                ExposedDropdownMenuBox(
                    expanded = subjectMenuExpanded,
                    onExpandedChange = { subjectMenuExpanded = !subjectMenuExpanded }
                ) {
                    OutlinedTextField(
                        value = "${selectedSubject.emoji} ${selectedSubject.shortDisplayName(language)}",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("المادة") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectMenuExpanded) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = subjectMenuExpanded,
                        onDismissRequest = { subjectMenuExpanded = false }
                    ) {
                        subjects.forEach { subject ->
                            DropdownMenuItem(
                                text = { Text("${subject.emoji} ${subject.displayName(language)}") },
                                onClick = {
                                    selectedSubject = subject
                                    subjectMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // Start & End Time Pickers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            TimePickerDialog(
                                context,
                                { _, hour, minute ->
                                    startHour = hour
                                    startMinute = minute
                                },
                                startHour,
                                startMinute,
                                false
                            ).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Rounded.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("من ${ClassAlarmScheduler.formatArabicTime(startHour, startMinute)}")
                    }

                    OutlinedButton(
                        onClick = {
                            TimePickerDialog(
                                context,
                                { _, hour, minute ->
                                    endHour = hour
                                    endMinute = minute
                                },
                                endHour,
                                endMinute,
                                false
                            ).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("إلى ${ClassAlarmScheduler.formatArabicTime(endHour, endMinute)}")
                    }
                }

                // Place selector
                Text(
                    text = "المكان",
                    style = MaterialTheme.typography.labelLarge
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ClassPlace.entries.forEach { place ->
                        val selected = selectedPlace == place
                        FilterChip(
                            selected = selected,
                            onClick = { selectedPlace = place },
                            label = { Text("${place.emoji} ${place.arabicLabel}") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = OrangeAccent,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Teacher Name (optional)
                OutlinedTextField(
                    value = teacherName,
                    onValueChange = { teacherName = it },
                    label = { Text("اسم المدرس (اختياري)") },
                    placeholder = { Text("مثال: مستر أحمد") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        selectedDayIndex,
                        selectedSubject,
                        startHour,
                        startMinute,
                        endHour,
                        endMinute,
                        selectedPlace,
                        teacherName
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                modifier = Modifier.testTag("confirm_add_class_button")
            ) {
                Text("حفظ الحصة")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
