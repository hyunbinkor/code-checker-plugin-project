package com.codechecker.plugin.model

import com.google.gson.annotations.SerializedName
import com.intellij.ui.JBColor
import java.awt.Color

// ────────────────────────────────────────────
// 요청
// ────────────────────────────────────────────

data class CheckRequest(
    @SerializedName("code")
    val code: String,

    @SerializedName("fileName")
    val fileName: String,

    @SerializedName("options")
    val options: CheckOptions = CheckOptions()
)

data class CheckOptions(
    @SerializedName("format")
    val format: String = "json"
)

// ────────────────────────────────────────────
// 응답
// ────────────────────────────────────────────

data class CheckResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("fileName")
    val fileName: String?,

    @SerializedName("lineCount")
    val lineCount: Int?,

    @SerializedName("chunked")
    val chunked: Boolean?,

    @SerializedName("processingTimeMs")
    val processingTimeMs: Long?,

    @SerializedName("format")
    val format: String?,

    @SerializedName("issues")
    val issues: List<Issue>?,

    @SerializedName("summary")
    val summary: Summary?,

    @SerializedName("tags")
    val tags: List<String>?,

    @SerializedName("matchedRulesCount")
    val matchedRulesCount: Int?,

    @SerializedName("stats")
    val stats: CheckStats?,

    // 에러 응답 필드
    @SerializedName("error")
    val error: String?,

    @SerializedName("message")
    val message: String?
)

// ────────────────────────────────────────────
// 이슈
// ────────────────────────────────────────────

data class Issue(
    @SerializedName("ruleId")
    val ruleId: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("severity")
    val severityRaw: String,

    @SerializedName("line")
    val line: Int?,

    @SerializedName("column")
    val column: Int?,

    @SerializedName("endLine")
    val endLine: Int?,

    @SerializedName("endColumn")
    val endColumn: Int?,

    @SerializedName("message")
    val message: String,

    @SerializedName("suggestion")
    val suggestion: String?,

    @SerializedName("category")
    val category: String?,

    @SerializedName("className")
    val className: String?,

    @SerializedName("methodName")
    val methodName: String?
) {
    // Gson이 severityRaw를 채운 뒤 편의 접근용으로 사용
    val severity: Severity
        get() = Severity.fromString(severityRaw)
}

// ────────────────────────────────────────────
// 요약
// ────────────────────────────────────────────

data class Summary(
    @SerializedName("totalIssues")
    val totalIssues: Int,

    // { "CRITICAL": 1, "HIGH": 2, ... }
    @SerializedName("bySeverity")
    val bySeverity: Map<String, Int>,

    // { "security": 1, "exception_handling": 1, ... }
    @SerializedName("byCategory")
    val byCategory: Map<String, Int>
)

data class CheckStats(
    @SerializedName("llmCalls")
    val llmCalls: Int,

    @SerializedName("processingTime")
    val processingTime: Long
)

// ────────────────────────────────────────────
// Severity enum
// ────────────────────────────────────────────

enum class Severity(
    val displayName: String,
    val priority: Int
) {
    CRITICAL("치명적", 0),
    HIGH("높음", 1),
    MEDIUM("보통", 2),
    LOW("낮음", 3);

    fun getColor(): JBColor = when (this) {
        CRITICAL -> JBColor(Color(0xE53935), Color(0xEF5350))
        HIGH     -> JBColor(Color(0xFB8C00), Color(0xFFA726))
        MEDIUM   -> JBColor(Color(0xFDD835), Color(0xFFEE58))
        LOW      -> JBColor(Color(0xBDBDBD), Color(0x9E9E9E))
    }

    fun getIcon(): String = when (this) {
        CRITICAL -> "🔴"
        HIGH     -> "🟠"
        MEDIUM   -> "🟡"
        LOW      -> "⚪"
    }

    companion object {
        fun fromString(s: String): Severity =
            entries.find { it.name.equals(s.trim(), ignoreCase = true) } ?: LOW
    }
}

// ────────────────────────────────────────────
// 검사 결과 (Plugin 내부 상태 표현)
// ────────────────────────────────────────────

sealed class CheckResult {
    data class Success(val response: CheckResponse) : CheckResult()
    data class Failure(val message: String, val type: ErrorType) : CheckResult()
}

enum class ErrorType {
    /** 서버에 연결할 수 없음 (UnknownHostException, ConnectException 등) */
    NETWORK,

    /** 서버 응답이 제한 시간 내에 오지 않음 */
    TIMEOUT,

    /** 서버가 success=false 응답 반환 */
    SERVER_ERROR,

    /** 응답 JSON 파싱 실패 */
    PARSE_ERROR
}