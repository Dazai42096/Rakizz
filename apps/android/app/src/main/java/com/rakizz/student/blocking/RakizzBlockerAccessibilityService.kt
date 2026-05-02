package com.rakizz.student.blocking

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.rakizz.student.MainActivity

class RakizzBlockerAccessibilityService : AccessibilityService() {

    private var lastBlockedPackage: String = ""
    private var lastBlockTime: Long = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) {
            return
        }

        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            return
        }

        val openedPackage = event.packageName?.toString().orEmpty()

        if (openedPackage.isBlank()) {
            return
        }

        // don't block Rakizz itself
        if (openedPackage == packageName) {
            return
        }

        val shouldBlock = FocusRuleCache.isPackageBlockedNow(
            context = this,
            packageName = openedPackage
        )

        if (!shouldBlock) {
            return
        }

        val now = System.currentTimeMillis()

        if (lastBlockedPackage == openedPackage && now - lastBlockTime < 5000L) {
            return
        }

        lastBlockedPackage = openedPackage
        lastBlockTime = now

        // accessibility already detected that this app is blocked
        // so we force the unlock quiz screen to generate a quiz
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)

            putExtra(MainActivity.EXTRA_UNLOCK_PACKAGE, openedPackage)
            putExtra(MainActivity.EXTRA_FORCE_UNLOCK, true)
        }

        startActivity(intent)
    }

    override fun onInterrupt() {
        // required function
    }
}