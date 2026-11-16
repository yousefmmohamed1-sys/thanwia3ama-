package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.ImportResult
import com.example.data.quotes.QuotesRepository
import com.example.ui.theme.AmberGold
import com.example.ui.theme.OrangeAccent
import com.example.ui.viewmodel.MainUiState
import com.example.utils.ShareUtils
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    uiState: MainUiState,
    onBack: () -> Unit,
    onEditProfile: () -> Unit,
    onSaveBlockedApps: (Set<String>) -> Unit,
    onToggleMotivationalNotifications: (Boolean) -> Unit,
    onSendTestQuoteNotification: () -> Unit,
    onToggleDarkMode: (Boolean) -> Unit,
    onExportToUri: suspend (Uri) -> Boolean,
    onBuildExportJson: suspend () -> String,
    onImportFromUri: suspend (Uri, Boolean) -> Result<ImportResult>
) {
    BackHandler(onBack = onBack)

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showBlockedAppsDialog by remember { mutableStateOf(false) }
    var showExportChoiceDialog by remember { mutableStateOf(false) }
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }

    val createJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val ok = onExportToUri(uri)
                Toast.makeText(
                    context,
                    if (ok) "تم حفظ ملف الجدول والتقدم بنجاح ✅" else "حصل خطأ أثناء التصدير",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    val openJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            pendingImportUri = uri
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("settings_back_button")
                ) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "رجوع")
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "الإعدادات والمشاركة ⚙️",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        // Profile Summary + Edit Button
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
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = uiState.profile.name.ifBlank { "طالب الثانوية" },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${uiState.profile.track.arabicLabel} • ${uiState.profile.language.arabicLabel}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AmberGold
                        )
                    }
                    OutlinedButton(
                        onClick = onEditProfile,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.testTag("edit_profile_button")
                    ) {
                        Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تعديل بياناتي")
                    }
                }
            }
        }

        // Motivational Notifications Toggle
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.NotificationsActive,
                                contentDescription = null,
                                tint = OrangeAccent
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "اشعارات تحفيزية كل ساعة",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${QuotesRepository.totalCombinationsCount} رسالة مصرية أوفلاين",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = uiState.motivationalNotificationsEnabled,
                            onCheckedChange = onToggleMotivationalNotifications,
                            colors = SwitchDefaults.colors(checkedTrackColor = OrangeAccent),
                            modifier = Modifier.testTag("motivational_notifs_switch")
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = {
                            onSendTestQuoteNotification()
                            Toast.makeText(context, "بعتنالك إشعار تحفيزي تجريبي!", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text("جرب إشعار تحفيزي دلوقتي 🔔", color = OrangeAccent)
                    }
                }
            }
        }

        // Dark Mode Toggle
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
                        Icon(Icons.Rounded.DarkMode, contentDescription = null, tint = AmberGold)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "الوضع الداكن (Dark Mode)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "مريح للعين وقت السهر والمذاكرة",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = uiState.isDarkMode,
                        onCheckedChange = onToggleDarkMode,
                        colors = SwitchDefaults.colors(checkedTrackColor = OrangeAccent),
                        modifier = Modifier.testTag("dark_mode_switch")
                    )
                }
            }
        }

        // Blocked Apps Config
        item {
            SettingsActionCard(
                icon = Icons.Rounded.Apps,
                title = "تعديل التطبيقات المحظورة",
                subtitle = "محظور حالياً ${uiState.blockedApps.size} تطبيق وقت التركيز",
                testTag = "settings_blocked_apps_button",
                onClick = { showBlockedAppsDialog = true }
            )
        }

        // Export / Import JSON
        item {
            SettingsActionCard(
                icon = Icons.Rounded.UploadFile,
                title = "تصدير جدولي وتقدمي",
                subtitle = "احفظ ملف .json أو ابعته لصاحبك على واتساب",
                testTag = "export_json_button",
                onClick = { showExportChoiceDialog = true }
            )
        }

        item {
            SettingsActionCard(
                icon = Icons.Rounded.Download,
                title = "استيراد من صاحبك",
                subtitle = "اختار ملف .json من واتساب لاستيراد جدوله (من غير ما يغير اسمك أو شعبتك)",
                testTag = "import_json_button",
                onClick = { openJsonLauncher.launch(arrayOf("application/json", "*/*")) }
            )
        }

        // Share App
        item {
            SettingsActionCard(
                icon = Icons.Rounded.Share,
                title = "ابعت التطبيق لصاحبك",
                subtitle = "شارك ملف التطبيق أو رابط التثبيت مع زمايلك في الثانوية",
                testTag = "settings_share_app_button",
                onClick = { ShareUtils.shareAppToFriend(context, uiState.profile.name) }
            )
        }
    }

    if (showBlockedAppsDialog) {
        BlockedAppsPickerDialog(
            installedApps = uiState.installedApps,
            initiallySelected = uiState.blockedApps,
            isFirstTimeSetup = !uiState.blockedAppsConfigured,
            onDismiss = { showBlockedAppsDialog = false },
            onSave = { selected ->
                onSaveBlockedApps(selected)
                showBlockedAppsDialog = false
            }
        )
    }

    if (showExportChoiceDialog) {
        AlertDialog(
            onDismissRequest = { showExportChoiceDialog = false },
            title = { Text("تصدير جدولي وتقدمي 📤", fontWeight = FontWeight.Bold) },
            text = {
                Text("عايز تبعت ملف .json لصاحبك على واتساب مباشرة ولا تحفظه على الموبايل؟")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExportChoiceDialog = false
                        scope.launch {
                            val json = onBuildExportJson()
                            ShareUtils.shareJsonFile(context, json)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent)
                ) {
                    Text("مشاركة على واتساب / التطبيقات")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showExportChoiceDialog = false
                        createJsonLauncher.launch("rafiq_schedule_backup.json")
                    }
                ) {
                    Text("حفظ في الجهاز")
                }
            }
        )
    }

    pendingImportUri?.let { uri ->
        AlertDialog(
            onDismissRequest = { pendingImportUri = null },
            title = { Text("استيراد جدول صاحبك 📥", fontWeight = FontWeight.Bold) },
            text = {
                Text("اسمك وشعبتك وتقدمك الشخصي مش هيتأثروا. تحب تستبدل جدولك الحالي بجدول صاحبك ولا تضيف الحصص عليه؟")
            },
            confirmButton = {
                Button(
                    onClick = {
                        pendingImportUri = null
                        scope.launch {
                            onImportFromUri(uri, true).onSuccess { res ->
                                Toast.makeText(
                                    context,
                                    "تم استيراد ${res.importedClassesCount} حصة بنجاح ✅",
                                    Toast.LENGTH_LONG
                                ).show()
                            }.onFailure {
                                Toast.makeText(
                                    context,
                                    "الملف مش بصيغة جدول رفيق الثانوية الصحيحة",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent)
                ) {
                    Text("استبدال جدولي")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        pendingImportUri = null
                        scope.launch {
                            onImportFromUri(uri, false).onSuccess { res ->
                                Toast.makeText(
                                    context,
                                    "تمت إضافة ${res.importedClassesCount} حصة لجدولك ✅",
                                    Toast.LENGTH_LONG
                                ).show()
                            }.onFailure {
                                Toast.makeText(
                                    context,
                                    "الملف مش بصيغة جدول رفيق الثانوية الصحيحة",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                ) {
                    Text("إضافة لجدولي")
                }
            }
        )
    }
}

@Composable
private fun SettingsActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = OrangeAccent,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
