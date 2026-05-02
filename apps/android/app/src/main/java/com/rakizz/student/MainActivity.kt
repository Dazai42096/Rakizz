package com.rakizz.student

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.rakizz.student.presentation.navigation.RakizzNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        const val EXTRA_UNLOCK_PACKAGE = "extra_unlock_package"
        const val EXTRA_FORCE_UNLOCK = "extra_force_unlock"
    }

    private var blockedPackageFromService by mutableStateOf<String?>(null)
    private var forceUnlockFromService by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        readUnlockIntent(intent)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RakizzNavHost(
                        startUnlockPackage = blockedPackageFromService,
                        forceUnlock = forceUnlockFromService,
                        onUnlockPackageHandled = {
                            blockedPackageFromService = null
                            forceUnlockFromService = false
                        }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        setIntent(intent)
        readUnlockIntent(intent)
    }

    private fun readUnlockIntent(intent: Intent?) {
        if (intent == null) {
            return
        }

        blockedPackageFromService = intent.getStringExtra(EXTRA_UNLOCK_PACKAGE)
        forceUnlockFromService = intent.getBooleanExtra(EXTRA_FORCE_UNLOCK, false)
    }
}