package com.moonx.app.util


import android.util.Log


object Logger {
    fun i(tag: String, msg: String) = Log.i(tag, msg)
    fun d(tag: String, msg: String) = Log.d(tag, msg)
    fun e(tag: String, msg: String) = Log.e(tag, msg)
}