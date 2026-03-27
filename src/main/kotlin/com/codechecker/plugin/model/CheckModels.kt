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
    val success: Boolean = false,

    @SerializedName("fileName")
    val fileName: String?,

    @SerializedName("format")
    val format: String?,

    @SerializedName("chunked")
    val chunked: Boolean?,

    @SerializedName("processingTimeMs")
    val processingTimeMs: Long?,

    @SerializedName("issues")
    val issues: List<Issue>?,

    @SerializedName("summary")
    val summary: Summary?,

    @SerializedName("stats")
    val stats: CheckStats?,

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

    @SerializedName("description")
    val description: String?,

    @SerializedName("suggestion")
    val suggestion: String?,

    @SerializedName("category")
    val category: String?,

    @SerializedName("className")
    val className: String?,

    @SerializedName("methodName")
    val methodName: String?
) {
    val severity: Severity
        get() = Severity.fromString(severityRaw)

    val displayMessage: String
        get() = description?.takeIf { it.isNotBlank() } ?: title
}

// ────────────────────────────────────────────
// 요약
// ────────────────────────────────────────────

data class Summary(
    @SerializedName("totalIssues")
    val totalIssues: Int,

    @SerializedName("bySeverity")
    val bySeverity: Map<String, Int>,

    @SerializedName("byClass")
    val byClass: Map<String, Int>?,

    @SerializedName("byMethod")
    val byMethod: Map<String, Int>?,

    @SerializedName("byCategory")
    val byCategory: Map<String, Int>?
)

// ────────────────────────────────────────────
// 통계
// ────────────────────────────────────────────

data class CheckStats(
    @SerializedName("totalChecks")
    val totalChecks: Int?,

    @SerializedName("pureRegexViolations")
    val pureRegexViolations: Int?,

    @SerializedName("llmCandidates")
    val llmCandidates: Int?,

    @SerializedName("llmCalls")
    val llmCalls: Int?,

    @SerializedName("falsePositivesFiltered")
    val falsePositivesFiltered: Int?,

    @SerializedName("processingTime")
    val processingTime: Long?
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
    NETWORK,
    TIMEOUT,
    SERVER_ERROR,
    PARSE_ERROR
}