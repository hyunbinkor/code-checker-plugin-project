package com.codechecker.plugin.service

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.codechecker.plugin.model.CheckOptions
import com.codechecker.plugin.model.CheckRequest
import com.codechecker.plugin.model.CheckResponse
import com.codechecker.plugin.model.CheckResult
import com.codechecker.plugin.model.ErrorType
import com.codechecker.plugin.settings.PluginSettings
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

@Service(Service.Level.APP)
class CodeCheckService {

    private val gson = Gson()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    // ────────────────────────────────────────────
    // 공개 API
    // ────────────────────────────────────────────

    fun checkCode(
        code: String,
        fileName: String,
        onHeartbeat: (() -> Unit)? = null
    ): CheckResult {
        // Mock 모드 분기
        if (PluginSettings.getInstance().useMockMode) {
            return MockCheckService.checkCode(code, fileName, onHeartbeat)
        }
        return executeCheckCode(code, fileName, onHeartbeat)
    }

    fun testConnection(): Boolean {
        if (PluginSettings.getInstance().useMockMode) {
            return MockCheckService.testConnection()
        }
        return executeTestConnection()
    }

    // ────────────────────────────────────────────
    // 실제 HTTP 구현
    // ────────────────────────────────────────────

    private fun buildClient(): OkHttpClient {
        val settings = PluginSettings.getInstance()
        return OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .callTimeout(settings.readTimeoutSeconds.toLong(), TimeUnit.SECONDS)
            .build()
    }

    private fun executeCheckCode(
        code: String,
        fileName: String,
        onHeartbeat: (() -> Unit)?
    ): CheckResult {
        val settings = PluginSettings.getInstance()
        val url = "${settings.serverUrl.trimEnd('/')}/api/check"

        val bodyJson = gson.toJson(
            CheckRequest(
                code = code,
                fileName = fileName,
                options = CheckOptions(format = settings.outputFormat)
            )
        )

        val request = Request.Builder()
            .url(url)
            .post(bodyJson.toRequestBody(jsonMediaType))
            .header("Accept", "application/json")
            .build()

        return try {
            buildClient().newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errorBody = runCatching { response.body?.string()?.trim() }.getOrNull()
                    val serverMessage = tryParseErrorMessage(errorBody) ?: "HTTP ${response.code} 오류"
                    return CheckResult.Failure(serverMessage, ErrorType.SERVER_ERROR)
                }

                val source = response.body!!.source()
                val buffer = StringBuilder()

                while (!source.exhausted()) {
                    val line = source.readUtf8Line() ?: break
                    if (line.isBlank()) {
                        onHeartbeat?.invoke()
                        continue
                    }
                    buffer.append(line)
                }

                parseResponse(buffer.toString().trim())
            }
        } catch (e: SocketTimeoutException) {
            CheckResult.Failure(
                "서버 응답 시간이 초과되었습니다. (${settings.readTimeoutSeconds}초)",
                ErrorType.TIMEOUT
            )
        } catch (e: ConnectException) {
            CheckResult.Failure(
                "서버에 연결할 수 없습니다. URL을 확인하세요: ${settings.serverUrl}",
                ErrorType.NETWORK
            )
        } catch (e: Exception) {
            val isNetworkError = e.cause is ConnectException
                    || e.message?.contains("Failed to connect") == true
                    || e.message?.contains("Unable to resolve host") == true

            if (isNetworkError) {
                CheckResult.Failure(
                    "서버에 연결할 수 없습니다. URL을 확인하세요: ${settings.serverUrl}",
                    ErrorType.NETWORK
                )
            } else {
                CheckResult.Failure("예기치 않은 오류: ${e.message}", ErrorType.SERVER_ERROR)
            }
        }
    }

    private fun executeTestConnection(): Boolean {
        val settings = PluginSettings.getInstance()
        val url = "${settings.serverUrl.trimEnd('/')}/health"

        val request = Request.Builder().url(url).get().build()

        val client = OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .callTimeout(5, TimeUnit.SECONDS)
            .build()

        return try {
            client.newCall(request).execute().use { it.isSuccessful }
        } catch (e: Exception) {
            false
        }
    }

    // ────────────────────────────────────────────
    // 내부 유틸
    // ────────────────────────────────────────────

    private fun parseResponse(json: String): CheckResult {
        if (json.isEmpty()) {
            return CheckResult.Failure("서버 응답이 비어 있습니다.", ErrorType.PARSE_ERROR)
        }
        return try {
            val response = gson.fromJson(json, CheckResponse::class.java)
            if (response.success) {
                CheckResult.Success(response)
            } else {
                val message = response.message ?: response.error ?: "알 수 없는 서버 오류"
                CheckResult.Failure(message, ErrorType.SERVER_ERROR)
            }
        } catch (e: JsonSyntaxException) {
            CheckResult.Failure("응답 파싱 실패: ${e.message?.take(100)}", ErrorType.PARSE_ERROR)
        }
    }

    private fun tryParseErrorMessage(body: String?): String? {
        if (body.isNullOrBlank()) return null
        return try {
            val response = gson.fromJson(body, CheckResponse::class.java)
            response.message ?: response.error
        } catch (e: JsonSyntaxException) {
            null
        }
    }

    // ────────────────────────────────────────────
    // 싱글톤 접근
    // ────────────────────────────────────────────

    companion object {
        fun getInstance(): CodeCheckService = service()
    }
}