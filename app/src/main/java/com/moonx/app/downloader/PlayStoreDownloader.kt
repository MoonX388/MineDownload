package com.moonx.app.downloader


import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.moonx.app.auth.GoogleAuth
import com.moonx.app.playstore.PlayStoreApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.InputStream


/**
 * PlayStoreDownloader
 * -------------------
 * Download APK dari Play Store menggunakan Internal API (undocumented)
 * Fallback ke ekstraksi dari aplikasi yang sudah terinstall.
 * 
 * WARNING: Menggunakan undocumented API, melanggar ToS Google.
 * Perlu Google Cloud Console setup untuk auth token yang proper.
 */
class PlayStoreDownloader(private val context: Context) {
    
    private val TAG = "PlayStoreDownloader"
    private val playStoreClient = PlayStoreApiClient(context)
    
    /**
     * Download APK dari Play Store atau ekstrak dari yang terinstall.
     * 
     * Flow:
     * 1. Coba download dari Play Store API (jika ada auth token)
     * 2. Fallback: ekstrak dari aplikasi yang sudah terinstall
     */
    suspend fun openDownloadStream(packageName: String, versionCode: String): InputStream {
        // Coba download dari Play Store dulu
        val authToken = GoogleAuth.getIdToken(context)
        if (authToken != null) {
            Log.d(TAG, "Attempting to download from Play Store API...")
            try {
                val stream = playStoreClient.downloadApkFullFlow(
                    packageName, 
                    versionCode.toIntOrNull() ?: 0, 
                    authToken
                )
                if (stream != null) {
                    Log.d(TAG, "Successfully downloaded from Play Store")
                    return stream
                }
            } catch (e: Exception) {
                Log.w(TAG, "Play Store download failed: ${e.message}, falling back to installed APK")
            }
        } else {
            Log.w(TAG, "No auth token, skipping Play Store download")
        }
        
        // Fallback: ekstrak dari aplikasi yang terinstall
        return extractInstalledApk(packageName, versionCode)
    }
    
    /**
     * Ekstrak APK dari aplikasi yang sudah terinstall.
     */
    private fun extractInstalledApk(packageName: String, versionCode: String): InputStream {
        try {
            // Cek apakah aplikasi terinstall
            val packageManager = context.packageManager
            val appInfo = try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    packageManager.getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(0))
                } else {
                    @Suppress("DEPRECATION")
                    packageManager.getApplicationInfo(packageName, 0)
                }
            } catch (e: PackageManager.NameNotFoundException) {
                throw IllegalArgumentException("Package $packageName is not installed on this device")
            }
            
            // Dapatkan path APK
            val apkPath = appInfo.sourceDir
            Log.d(TAG, "Found APK at: $apkPath")
            
            // Verifikasi file exists
            val apkFile = File(apkPath)
            if (!apkFile.exists()) {
                throw IllegalStateException("APK file not found at: $apkPath")
            }
            
            // Verifikasi version code (optional)
            val packageInfo = try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
                } else {
                    @Suppress("DEPRECATION")
                    packageManager.getPackageInfo(packageName, 0)
                }
            } catch (e: PackageManager.NameNotFoundException) {
                throw IllegalArgumentException("Package info not found for $packageName")
            }
            
            val installedVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode.toString()
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toString()
            }
            
            Log.d(TAG, "Requested version: $versionCode, Installed version: $installedVersionCode")
            
            // Warning jika version tidak match
            if (versionCode != installedVersionCode) {
                Log.w(TAG, "Version mismatch! Requested: $versionCode, but installed: $installedVersionCode")
                Log.w(TAG, "Proceeding with installed version...")
            }
            
            // Return FileInputStream dari APK
            Log.d(TAG, "Opening stream for APK: ${apkFile.name} (${apkFile.length()} bytes)")
            return FileInputStream(apkFile)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open APK stream: ${e.message}", e)
            throw e
        }
    }
}