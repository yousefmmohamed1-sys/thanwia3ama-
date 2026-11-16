package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.InstalledAppInfo
import com.example.focus.InstalledAppsHelper
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CoralDanger
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.NavyVoid
import com.example.ui.theme.OrangeAccent

@Composable
fun BlockedAppsPickerDialog(
    installedApps: List<InstalledAppInfo>,
    initiallySelected: Set<String>,
    isFirstTimeSetup: Boolean,
    onDismiss: () -> Unit,
    onSave: (Set<String>) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedPackages by remember(installedApps, initiallySelected) {
        mutableStateOf(
            if (initiallySelected.isNotEmpty() || !isFirstTimeSetup) {
                initiallySelected
            } else {
                // First time: pre-select Facebook, WhatsApp, TikTok, Instagram, YouTube, Snapchat
                installedApps.filter { it.isSuggestedDistraction }.map { it.packageName }.toSet()
            }
        )
    }

    val filtered = remember(installedApps, searchQuery) {
        if (searchQuery.isBlank()) installedApps
        else installedApps.filter {
            it.appName.contains(searchQuery, ignoreCase = true) ||
                it.packageName.contains(searchQuery, ignoreCase = true)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "اختار التطبيقات المشتتة 🔒",
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "لو فتحت أي تطبيق منهم وأنت في وضع التركيز هيظهرلك حاجز يرجعك للمذاكرة.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("ابحث عن تطبيق...") },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))

                if (filtered.isEmpty()) {
                    Text(
                        text = "مفيش تطبيقات متطابقة مع البحث.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(filtered, key = { it.packageName }) { app ->
                            val checked = app.packageName in selectedPackages
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        selectedPackages = if (checked) {
                                            selectedPackages - app.packageName
                                        } else {
                                            selectedPackages + app.packageName
                                        }
                                    }
                                    .padding(horizontal = 4.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = checked,
                                    onCheckedChange = { isChecked ->
                                        selectedPackages = if (isChecked) {
                                            selectedPackages + app.packageName
                                        } else {
                                            selectedPackages - app.packageName
                                        }
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = OrangeAccent)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = app.appName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    if (app.isSuggestedDistraction) {
                                        Text(
                                            text = "مشتت مقترح",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = AmberGold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(selectedPackages) },
                colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                modifier = Modifier.testTag("save_blocked_apps_button")
            ) {
                Text("حفظ (${selectedPackages.size})")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@Composable
fun BlockerPermissionsDialog(
    hasUsage: Boolean,
    hasOverlay: Boolean,
    hasAccessibility: Boolean,
    onDismiss: () -> Unit,
    onContinueTimerOnly: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Lock, contentDescription = null, tint = OrangeAccent)
                Spacer(modifier = Modifier.width(8.dp))
                Text("صلاحيات حظر التطبيقات", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "عشان التطبيق يقفل التطبيقات لازم تديله الصلاحية دي:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )

                PermissionStatusRow(
                    title = "1. الوصول لاستخدام التطبيقات (Usage Access)",
                    granted = hasUsage,
                    onGrantClick = { InstalledAppsHelper.openUsageAccessSettings(context) }
                )
                PermissionStatusRow(
                    title = "2. الظهور فوق التطبيقات الأخرى (Overlay)",
                    granted = hasOverlay,
                    onGrantClick = { InstalledAppsHelper.openOverlaySettings(context) }
                )
                PermissionStatusRow(
                    title = "3. خدمة إمكانية الوصول (Accessibility)",
                    granted = hasAccessibility,
                    onGrantClick = { InstalledAppsHelper.openAccessibilitySettings(context) }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onContinueTimerOnly,
                colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                modifier = Modifier.testTag("continue_focus_after_perms_button")
            ) {
                Text(
                    if (hasUsage && hasOverlay && hasAccessibility) "كله تمام — ابدأ التركيز!"
                    else "ابدأ المؤقت دلوقتي"
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("رجوع")
            }
        }
    )
}

@Composable
private fun PermissionStatusRow(
    title: String,
    granted: Boolean,
    onGrantClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                Text(
                    text = if (granted) "✅ مفعّلة" else "⚠️ غير مفعّلة",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (granted) EmeraldSuccess else AmberGold
                )
            }
            if (!granted) {
                TextButton(onClick = onGrantClick) {
                    Text("تفعيل", color = OrangeAccent)
                }
            } else {
                Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = EmeraldSuccess)
            }
        }
    }
}

@Composable
fun QuitReasonDialog(
    onDismiss: () -> Unit,
    onConfirmQuit: (String) -> Unit
) {
    var reason by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Warning, contentDescription = null, tint = CoralDanger)
                Spacer(modifier = Modifier.width(8.dp))
                Text("ليه عايز توقف الجلسة؟", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column {
                Text(
                    text = "الجلسة هتتسجل في إحصائياتك كـ \"جلسة غير مكتملة\". اكتب السبب بوضوح عشان تعرف تتغلب عليه المرة الجاية:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = reason,
                    onValueChange = {
                        reason = it
                        if (it.isNotBlank()) showError = false
                    },
                    label = { Text("سبب الخروج المبكر") },
                    placeholder = { Text("مثال: جالي ظرف طارئ / تعبت جداً") },
                    isError = showError,
                    supportingText = if (showError) {
                        { Text("لازم تكتب السبب الأول") }
                    } else null,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quit_reason_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (reason.isBlank()) {
                        showError = true
                    } else {
                        onConfirmQuit(reason)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CoralDanger),
                modifier = Modifier.testTag("confirm_quit_focus_button")
            ) {
                Text("تأكيد الخروج")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("لا، هرجع أكمل!")
            }
        }
    )
}

@Composable
fun FocusCelebrationOverlay(
    studentName: String,
    minutesFocused: Int,
    lessonTitle: String,
    onMarkDone: () -> Unit,
    onMarkNeedsReview: () -> Unit,
    onDismiss: () -> Unit
) {
    val cleanName = studentName.ifBlank { "بطل" }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(NavyVoid, NavyDeep)))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier.size(164.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_focus_victory),
                    contentDescription = "إنجاز جلسة التركيز",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.height(22.dp))
            Text(
                text = "عاش يا $cleanName! خلصت $minutesFocused دقيقة تركيز 🎉",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = lessonTitle,
                style = MaterialTheme.typography.titleMedium,
                color = AmberGold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = "خلصت الدرس؟",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onMarkDone,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("celebration_lesson_done_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
            ) {
                Text(
                    text = "أيوة، علّم عليه خلص ✅",
                    style = MaterialTheme.typography.labelLarge
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = onMarkNeedsReview,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("celebration_lesson_review_button"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("لسه، ضيفه لقائمة المراجعة 📌", color = AmberGold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onDismiss) {
                Text("تخطي", color = Color(0xFFCBD5E1))
            }
        }
    }
}
