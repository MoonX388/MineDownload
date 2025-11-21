package com.moonx.app.downloader
private val TAG = "RelayDownloadService"


override fun onBind(intent: Intent?): IBinder? = null


override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    val pkg = intent?.getStringExtra("pkg") ?: return START_NOT_STICKY
    val vc = intent.getStringExtra("vc") ?: return START_NOT_STICKY
    val arch = intent.getStringExtra("arch") ?: "arm64-v8a"
    val channel = intent.getStringExtra("channel") ?: "release"


    Log.d(TAG, "Start relay job for $pkg vc=$vc $arch $channel")


// launch background coroutine
    CoroutineScope(Dispatchers.IO).launch {
        try {
// 1) get id token locally (note: id token != Play Store auth; placeholder)
            val ctx = applicationContext
            val idToken = GoogleAuth.getIdToken(ctx)


// 2) Use a PlayStoreDownloader implementation to open a stream from Play Store on device
// IMPORTANT: we do NOT implement fake-device or internal Play API here.
// In a legal setup, device should be able to obtain a download stream for the package the device owns.


            val downloader = PlayStoreDownloader(ctx)
            val stream = downloader.openDownloadStream(packageName = pkg, versionCode = vc) // TODO: this requires Play APIs


// 3) Stream bytes to VPS via RelayUploader
            val remotePath = "$vc/$channel/$arch/$pkg.apk"
            RelayUploader.uploadStream(ctx, remotePath, stream)


// 4) notify or write metadata
            Log.d(TAG, "Upload finished for $pkg @$vc")


        } catch (e: Exception) {
            Log.e(TAG, "Relay job failed: ${e.message}")
        } finally {
            stopSelf()
        }
    }


    return START_STICKY
}
}