package com.moonx.app.relay

import android.content.Context
import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.InputStream

object RelayUploader {
    private const val TAG = "RelayUploader"


    // VPS base upload URL - change to your server
    private const val VPS_UPLOAD_URL = "https://your-vps.example.com/api/upload_stream"


    fun uploadStream(context: Context, remotePath: String, input: InputStream) {
        val client = OkHttpClient.Builder().build()


        val requestBody = object : RequestBody() {
            override fun contentType() = "application/octet-stream".toMediaType()
            override fun writeTo(sink: BufferedSink) {
                val buffer = ByteArray(16 * 1024)
                var read = input.read(buffer)
                var total: Long = 0
                while (read != -1) {
                    sink.write(buffer, 0, read)
                    total += read
                    read = input.read(buffer)
                }
                input.close()
                Log.d(TAG, "Wrote total bytes: $total")
            }
        }


        val req = Request.Builder()
            .url("$VPS_UPLOAD_URL?path=$remotePath")
            .addHeader("Authorization", "Bearer YOUR_APP_UPLOAD_TOKEN")
            .post(requestBody)
            .build()


        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) throw Exception("Upload failed: ${resp.code}")
            Log.d(TAG, "Upload successful: ${resp.code}")
        }
    }
}