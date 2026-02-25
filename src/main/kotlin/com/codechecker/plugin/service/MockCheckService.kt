package com.codechecker.plugin.service

import com.codechecker.plugin.model.CheckResponse
import com.codechecker.plugin.model.CheckResult
import com.codechecker.plugin.model.CheckStats
import com.codechecker.plugin.model.Issue
import com.codechecker.plugin.model.Summary

object MockCheckService {

    private const val SIMULATE_SECONDS = 2
    private const val HEARTBEAT_INTERVAL_MS = 1000L

    /**
     * 실제 서버 없이 UI 테스트용 Mock 응답 반환.
     * [onHeartbeat]는 매 1초마다 호출되고, 총 [SIMULATE_SECONDS]초 후 결과 반환.
     */
    fun checkCode(
        code: String,
        fileName: String,
        onHeartbeat: (() -> Unit)? = null
    ): CheckResult {
        repeat(SIMULATE_SECONDS) {
            Thread.sleep(HEARTBEAT_INTERVAL_MS)
            onHeartbeat?.invoke()
        }

        val issues = buildMockIssues()
        val response = CheckResponse(
            success = true,
            fileName = fileName,
            lineCount = code.lines().size,
            chunked = false,
            processingTimeMs = (SIMULATE_SECONDS * 1000).toLong(),
            format = "json",
            issues = issues,
            summary = buildSummary(issues),
            tags = listOf("IS_DAO", "USES_CONNECTION", "HAS_SQL_CONCATENATION"),
            matchedRulesCount = 12,
            stats = CheckStats(llmCalls = 3, processingTime = 1800),
            error = null,
            message = null
        )

        return CheckResult.Success(response)
    }

    fun testConnection(): Boolean = true

    // ────────────────────────────────────────────
    // Mock 데이터
    // ────────────────────────────────────────────

    private fun buildMockIssues(): List<Issue> = listOf(
        Issue(
            ruleId = "SEC-001",
            title = "SQL Injection 위험",
            severityRaw = "CRITICAL",
            line = 10,
            column = 12,
            endLine = 10,
            endColumn = 55,
            message = "SQL 쿼리에 문자열 연결(+)을 사용하고 있습니다. SQL Injection 공격에 취약합니다.",
            suggestion = "PreparedStatement와 파라미터 바인딩(?)을 사용하세요.",
            category = "security",
            className = "UserDao",
            methodName = "findByUsername"
        ),
        Issue(
            ruleId = "RES-001",
            title = "Connection 미반환",
            severityRaw = "HIGH",
            line = 25,
            column = 8,
            endLine = 25,
            endColumn = 42,
            message = "Connection 객체가 finally 블록 또는 try-with-resources에서 닫히지 않습니다.",
            suggestion = "try-with-resources 구문 또는 finally 블록에서 connection.close()를 호출하세요.",
            category = "resource_management",
            className = "UserDao",
            methodName = "updateUser"
        ),
        Issue(
            ruleId = "ERR-002",
            title = "빈 catch 블록",
            severityRaw = "MEDIUM",
            line = 42,
            column = 9,
            endLine = 44,
            endColumn = 9,
            message = "catch 블록이 비어 있어 예외가 무시됩니다.",
            suggestion = "예외를 로깅하거나 적절히 처리하세요. 최소한 log.error()로 기록해야 합니다.",
            category = "exception_handling",
            className = "OrderService",
            methodName = "processOrder"
        ),
        Issue(
            ruleId = "PERF-001",
            title = "루프 내 DB 호출",
            severityRaw = "LOW",
            line = 67,
            column = 16,
            endLine = 67,
            endColumn = 48,
            message = "반복문 내부에서 DB를 호출하고 있습니다. N+1 쿼리 문제가 발생할 수 있습니다.",
            suggestion = "IN 절 또는 JOIN을 사용하여 단일 쿼리로 처리하세요.",
            category = "performance",
            className = "OrderService",
            methodName = "loadOrderDetails"
        ),
        Issue(
            ruleId = "STY-003",
            title = "매직 넘버 사용",
            severityRaw = "LOW",
            line = 89,
            column = 20,
            endLine = 89,
            endColumn = 22,
            message = "의미를 알 수 없는 숫자 리터럴(30)을 직접 사용하고 있습니다.",
            suggestion = "상수(static final int MAX_RETRY_COUNT = 30)로 추출하여 의미를 명확히 하세요.",
            category = "code_style",
            className = "RetryHandler",
            methodName = "execute"
        )
    )

    private fun buildSummary(issues: List<Issue>): Summary {
        val bySeverity = issues
            .groupingBy { it.severityRaw }
            .eachCount()

        val byCategory = issues
            .groupingBy { it.category ?: "general" }
            .eachCount()

        return Summary(
            totalIssues = issues.size,
            bySeverity = bySeverity,
            byCategory = byCategory
        )
    }
}