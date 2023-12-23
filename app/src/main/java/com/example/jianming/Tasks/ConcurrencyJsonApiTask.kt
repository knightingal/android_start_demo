package com.example.jianming.Tasks

import com.example.jianming.util.NetworkUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

object ConcurrencyJsonApiTask {

    fun startDownload(url: String, callBack: (body: String) -> Unit): Job {
        return MainScope().launch {
            val body = makeRequest(url)
            callBack(body)
        }
    }

    fun startPost(url: String, body: String, callBack: (body: String) -> Unit): Unit {
        MainScope().launch {
            val body = makePost(url, body)
            callBack(body)
        }
    }

    private suspend fun makePost(url: String, body: String): String {
        return withContext(Dispatchers.IO) {
            val requestBody = body.toRequestBody("application/json; charset=utf-8".toMediaType())
            var request = Request.Builder().url(url).method("POST", requestBody).build()

            var body = NetworkUtil.okHttpClient.newCall(request).execute().body.string()

            body
        }
    }
    suspend fun makeRequest(url: String): String {
        return withContext(Dispatchers.IO) {
            var request = Request.Builder().url(url).build()

            var body = NetworkUtil.okHttpClient.newCall(request).execute().body.string()

            body
        }
    }

}