package com.example.focus

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent

class FocusBlockerAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val pkgName = event.packageName?.toString() ?: return
        if (pkgName == packageName) return

        val focusState = FocusStateHolder.state.value
        if (!focusState.isActive) return

        if (pkgName in focusState.blockedPackages) {
            val intent = Intent(this, FocusMonitorService::class.java).apply {
                action = FocusMonitorService.ACTION_SHOW_BLOCK_OVERLAY
                putExtra(FocusMonitorService.EXTRA_BLOCKED_PACKAGE, pkgName)
            }
            runCatching { startService(intent) }
        }
    }

    override fun onInterrupt() {
        // No-op
    }
}
