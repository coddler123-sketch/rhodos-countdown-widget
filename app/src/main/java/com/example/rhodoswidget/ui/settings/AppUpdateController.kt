package com.example.rhodoswidget.ui.settings
import com.example.rhodoswidget.*

import com.example.rhodoswidget.R
import com.example.rhodoswidget.MainActivity
import com.example.rhodoswidget.ui.home.*
import com.example.rhodoswidget.ui.compass.*
import com.example.rhodoswidget.ui.travel.*
import com.example.rhodoswidget.ui.news.*
import com.example.rhodoswidget.ui.weather.*
import com.example.rhodoswidget.ui.theme.*
import com.example.rhodoswidget.widget.*

import android.content.Context
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal enum class UpdateInstallResult {
    INSTALLED,
    NOT_AVAILABLE,
    DOWNLOAD_FAILED,
    BUSY
}

@Stable
internal class AppUpdateController(context: Context) {
    private val appContext = context.applicationContext

    var isWorking by mutableStateOf(false)
        private set

    var availableUpdate by mutableStateOf<AppUpdate?>(null)
        private set

    var showStartupDialog by mutableStateOf(false)
        private set

    suspend fun checkOnStartup() {
        if (isWorking) return
        isWorking = true
        try {
            val update = withContext(Dispatchers.IO) { AppUpdateRepository.checkLatest() }
            if (update != null) {
                availableUpdate = update
                showStartupDialog = true
            }
        } finally {
            isWorking = false
        }
    }

    suspend fun install(update: AppUpdate): UpdateInstallResult {
        if (isWorking) return UpdateInstallResult.BUSY
        isWorking = true
        return try {
            if (downloadAndLaunch(update)) {
                UpdateInstallResult.INSTALLED
            } else {
                UpdateInstallResult.DOWNLOAD_FAILED
            }
        } finally {
            isWorking = false
        }
    }

    suspend fun checkAndInstall(): UpdateInstallResult {
        if (isWorking) return UpdateInstallResult.BUSY
        isWorking = true
        return try {
            val update = availableUpdate
                ?: withContext(Dispatchers.IO) { AppUpdateRepository.checkLatest() }
                ?: return UpdateInstallResult.NOT_AVAILABLE
            availableUpdate = update
            if (downloadAndLaunch(update)) {
                UpdateInstallResult.INSTALLED
            } else {
                UpdateInstallResult.DOWNLOAD_FAILED
            }
        } finally {
            isWorking = false
        }
    }

    fun dismissStartupDialog() {
        showStartupDialog = false
    }

    private suspend fun downloadAndLaunch(update: AppUpdate): Boolean {
        val apk = withContext(Dispatchers.IO) {
            AppUpdateRepository.download(appContext, update)
        } ?: return false
        appContext.startActivity(AppUpdateRepository.installIntent(appContext, apk))
        return true
    }
}
