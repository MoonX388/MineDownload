package com.moonx.app.api


import android.content.Context
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request


object VPSApi {
    private val client = OkHttpClient()
    private const val BASE = "https://your-vps.example.com/api"


    fun requestRelayDownload(ctx: Context, packageName: String, versionCode: String, arch: String, channel: String): Boolean {
        val body = FormBody.Builder()
            .add("package", packageName)
            .add("versionCode", versionCode)
            .add("arch", arch)
            .add("channel", channel)
            .build()


        val req = Request.Builder()
            .url("$BASE/request_download")
            .post(body)
            .addHeader("Authorization", "Bearer YOUR_APP_UPLOAD_TOKEN")
            .build()


        client.newCall(req).execute().use { resp ->
            return resp.isSuccessful
        }
    }
}