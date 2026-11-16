package com.example.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.quotes.QuotesRepository
import java.io.File

object ShareUtils {

    fun shareAppToFriend(context: Context, studentName: String) {
        val shareText = buildString {
            appendLine("📚 رفيق الثانوية — تطبيق المذاكرة الأوفلاين لطلاب تالتة ثانوي 🇪🇬")
            appendLine()
            appendLine("بيساعدك تنظم جدول دروسك، تتابع تقدمك في المنهج الكامل، وتدخل وضع التركيز اللي بيقفل السوشيال ميديا وقت المذاكرة، مع ${QuotesRepository.totalCombinationsCount} رسالة تحفيزية!")
            if (studentName.isNotBlank()) {
                appendLine()
                appendLine("مرسل من: $studentName 💪")
            }
        }

        // Try sharing the installed APK file directly via FileProvider so friends can install offline!
        val apkUri: Uri? = runCatching {
            val sourceApk = File(context.applicationInfo.sourceDir)
            if (sourceApk.exists() && sourceApk.canRead()) {
                val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
                val destApk = File(exportDir, "RafiqAlThanaweya.apk")
                sourceApk.copyTo(destApk, overwrite = true)
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    destApk
                )
            } else {
                null
            }
        }.getOrNull()

        val shareIntent = if (apkUri != null) {
            Intent(Intent.ACTION_SEND).apply {
                type = "application/vnd.android.package-archive"
                putExtra(Intent.EXTRA_STREAM, apkUri)
                putExtra(Intent.EXTRA_TEXT, shareText)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        } else {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
        }

        val chooser = Intent.createChooser(shareIntent, "ابعت التطبيق لصاحبك").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    fun shareJsonFile(context: Context, jsonContent: String, fileName: String = "rafiq_schedule_backup.json") {
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val jsonFile = File(exportDir, fileName)
        jsonFile.writeText(jsonContent, Charsets.UTF_8)

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            jsonFile
        )

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "ده جدولي في تطبيق رفيق الثانوية — استورده عندك من الإعدادات!")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(sendIntent, "تصدير جدولي وتقدمي").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }
}
