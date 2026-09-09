package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

data class UpdateInfo(
    val hasUpdate: Boolean,
    val latestVersion: String,
    val downloadUrl: String,
    val releaseNotes: String = ""
)

sealed class AppUpdateState {
    object Idle : AppUpdateState()
    object Checking : AppUpdateState()
    data class UpdateAvailable(val info: UpdateInfo) : AppUpdateState()
    data class Downloading(val progress: Float) : AppUpdateState()
    data class ReadyToInstall(val apkFile: File) : AppUpdateState()
    data class Error(val message: String) : AppUpdateState()
}

object AppUpdateManager {
    private const val TAG = "AppUpdateManager"
    private const val GITHUB_REPO_API = "https://api.github.com/repos/Shafaq-Teach/yoklima10/releases/latest"
    private const val FALLBACK_DOWNLOAD_URL = "https://github.com/Shafaq-Teach/yoklima10/releases/latest/download/app-debug.apk"

    private val _updateState = MutableStateFlow<AppUpdateState>(AppUpdateState.Idle)
    val updateState: StateFlow<AppUpdateState> = _updateState.asStateFlow()

    fun resetState() {
        _updateState.value = AppUpdateState.Idle
    }

    /**
     * Checks GitHub for a newer release than current BuildConfig.VERSION_NAME
     */
    suspend fun checkForUpdates(currentVersion: String = BuildConfig.VERSION_NAME) {
        withContext(Dispatchers.IO) {
            try {
                _updateState.value = AppUpdateState.Checking
                val url = URL(GITHUB_REPO_API)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.setRequestProperty("Accept", "application/vnd.github+json")
                conn.setRequestProperty("User-Agent", "YoklimaAttendanceApp")
                conn.connectTimeout = 10000
                conn.readTimeout = 10000

                val code = conn.responseCode
                if (code == 200) {
                    val response = conn.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(response)
                    val tagName = json.optString("tag_name", "").trim()
                    val body = json.optString("body", "")

                    // Find APK asset download URL
                    var apkDownloadUrl = FALLBACK_DOWNLOAD_URL
                    val assets = json.optJSONArray("assets")
                    if (assets != null) {
                        for (i in 0 until assets.length()) {
                            val asset = assets.getJSONObject(i)
                            val name = asset.optString("name", "")
                            if (name.endsWith(".apk", ignoreCase = true)) {
                                apkDownloadUrl = asset.optString("browser_download_url", FALLBACK_DOWNLOAD_URL)
                                break
                            }
                        }
                    }

                    if (isNewerVersion(currentVersion, tagName)) {
                        Log.i(TAG, "New version found: $tagName (current: $currentVersion)")
                        val info = UpdateInfo(
                            hasUpdate = true,
                            latestVersion = tagName,
                            downloadUrl = apkDownloadUrl,
                            releaseNotes = body
                        )
                        _updateState.value = AppUpdateState.UpdateAvailable(info)
                    } else {
                        Log.i(TAG, "App is up to date: $currentVersion vs remote $tagName")
                        _updateState.value = AppUpdateState.Idle
                    }
                } else {
                    Log.w(TAG, "GitHub release check returned response code: $code")
                    _updateState.value = AppUpdateState.Idle
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking for app updates", e)
                _updateState.value = AppUpdateState.Idle
            }
        }
    }

    /**
     * Downloads the APK file with progress reporting and triggers installer.
     */
    suspend fun startDownload(context: Context, downloadUrl: String) {
        withContext(Dispatchers.IO) {
            try {
                _updateState.value = AppUpdateState.Downloading(0.01f)
                val targetDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.cacheDir
                val apkFile = File(targetDir, "yoqlima_update.apk")
                if (apkFile.exists()) {
                    apkFile.delete()
                }

                downloadFileWithRedirects(downloadUrl, apkFile) { progress ->
                    _updateState.value = AppUpdateState.Downloading(progress)
                }

                _updateState.value = AppUpdateState.ReadyToInstall(apkFile)
                withContext(Dispatchers.Main) {
                    installApk(context, apkFile)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Download failed", e)
                _updateState.value = AppUpdateState.Error("چۈشۈرۈش مەغلۇپ بولدى: ${e.localizedMessage ?: "توردىن خاتالىق كۆرۈلدى"}")
            }
        }
    }

    /**
     * Launches the Android system package installer for the downloaded APK.
     */
    fun installApk(context: Context, apkFile: File) {
        try {
            val apkUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(installIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch installer", e)
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(FALLBACK_DOWNLOAD_URL)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(browserIntent)
            } catch (ignored: Exception) {}
        }
    }

    private fun downloadFileWithRedirects(initialUrl: String, destination: File, onProgress: (Float) -> Unit) {
        var currentUrl = initialUrl
        var redirects = 0
        val maxRedirects = 6

        while (redirects < maxRedirects) {
            val url = URL(currentUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.instanceFollowRedirects = false
            connection.setRequestProperty("User-Agent", "YoklimaAttendanceApp")
            connection.connectTimeout = 15000
            connection.readTimeout = 30000
            connection.connect()

            val responseCode = connection.responseCode
            if (responseCode in 300..399) {
                val newUrl = connection.getHeaderField("Location")
                connection.disconnect()
                if (newUrl != null) {
                    currentUrl = newUrl
                    redirects++
                    continue
                }
            }

            if (responseCode == 200) {
                val fileLength = connection.contentLength
                connection.inputStream.use { input ->
                    FileOutputStream(destination).use { output ->
                        val buffer = ByteArray(8192)
                        var bytesRead: Int
                        var totalBytes: Long = 0

                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            totalBytes += bytesRead
                            if (fileLength > 0) {
                                val progress = (totalBytes.toFloat() / fileLength).coerceIn(0f, 1f)
                                onProgress(progress)
                            }
                        }
                    }
                }
                connection.disconnect()
                return
            } else {
                connection.disconnect()
                throw Exception("HTTP $responseCode from server")
            }
        }
        throw Exception("Too many redirects")
    }

    /**
     * Semver comparator: returns true if remote > current.
     */
    fun isNewerVersion(current: String, remote: String): Boolean {
        val cleanCurrent = current.removePrefix("v").trim()
        val cleanRemote = remote.removePrefix("v").trim()
        if (cleanRemote.isBlank()) return false

        val curParts = cleanCurrent.split(".").mapNotNull { it.toIntOrNull() }
        val remParts = cleanRemote.split(".").mapNotNull { it.toIntOrNull() }

        val maxLen = maxOf(curParts.size, remParts.size)
        for (i in 0 until maxLen) {
            val c = curParts.getOrElse(i) { 0 }
            val r = remParts.getOrElse(i) { 0 }
            if (r > c) return true
            if (r < c) return false
        }
        return false
    }
}