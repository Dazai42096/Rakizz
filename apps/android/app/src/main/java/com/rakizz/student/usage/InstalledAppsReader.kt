package com.rakizz.student.usage

import android.content.Context
import android.content.pm.ApplicationInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

data class PhoneAppInfo(
    val packageName: String,
    val appName: String
)

class InstalledAppsReader @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun readInstalledApps(): List<PhoneAppInfo> {
        val packageManager = context.packageManager

        // get apps installed on this phone
        val apps = packageManager.getInstalledApplications(0)

        return apps
            .filter { app ->
                // hide our app from the parent list
                app.packageName != context.packageName
            }
            .map { app ->
                PhoneAppInfo(
                    packageName = app.packageName,
                    appName = getAppName(app)
                )
            }
            .filter { app ->
                app.appName.isNotBlank()
            }
            .distinctBy { app ->
                app.packageName
            }
            .sortedBy { app ->
                app.appName.lowercase()
            }
    }

    private fun getAppName(app: ApplicationInfo): String {
        return try {
            context.packageManager.getApplicationLabel(app).toString()
        } catch (_: Exception) {
            app.packageName
        }
    }
}