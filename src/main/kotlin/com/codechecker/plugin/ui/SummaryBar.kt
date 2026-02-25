package com.codechecker.plugin.ui

import com.codechecker.plugin.model.Issue
import com.codechecker.plugin.model.Severity
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.util.ui.JBUI
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints

/**
 * 검사 결과 요약 바.
 * "🔍 N개 이슈  🔴CRITICAL(n) 🟠HIGH(n) 🟡MEDIUM(n) ⚪LOW(n)"
 */
class SummaryBar(issues: List<Issue>) : JBPanel<SummaryBar>(
    FlowLayout(FlowLayout.LEFT, JBUI.scale(6), JBUI.scale(4))
) {

    init {
        isOpaque = true
        background = JBColor(Color(0xF5F5F5), Color(0x3C3C3C))
        border = JBUI.Borders.compound(
            JBUI.Borders.empty(0, 0, 8, 0),
            JBUI.Borders.empty(4, 8)
        )
        alignmentX = Component.LEFT_ALIGNMENT
        maximumSize = Dimension(Int.MAX_VALUE, Int.MAX_VALUE)

        // "🔍 N개 이슈" 레이블
        val totalLabel = JBLabel("🔍 ${issues.size}개 이슈").apply {
            font = font.deriveFont(java.awt.Font.BOLD)
        }
        add(totalLabel)

        // severity별 뱃지 (0건이면 생략)
        val countMap = issues.groupingBy { it.severity }.eachCount()
        listOf(Severity.CRITICAL, Severity.HIGH, Severity.MEDIUM, Severity.LOW).forEach { severity ->
            val count = countMap[severity] ?: 0
            if (count > 0) {
                add(SeverityBadge(severity, count))
            }
        }
    }

    // ── 둥근 모서리 severity 뱃지 ────────────────

    private class SeverityBadge(
        severity: Severity,
        count: Int
    ) : JBPanel<SeverityBadge>(FlowLayout(FlowLayout.CENTER, 0, 0)) {

        private val bgColor: Color = severity.getColor()
        private val text: String = "${severity.getIcon()}${severity.name}($count)"

        init {
            isOpaque = false
            border = JBUI.Borders.empty(
                JBUI.scale(2),
                JBUI.scale(6),
                JBUI.scale(2),
                JBUI.scale(6)
            )

            val label = JBLabel(text).apply {
                foreground = Color.WHITE
                font = font.deriveFont(java.awt.Font.BOLD, font.size2D - 1f)
            }
            add(label)
        }

        override fun paintComponent(g: Graphics) {
            val g2 = g.create() as Graphics2D
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g2.color = bgColor
            g2.fillRoundRect(0, 0, width, height, JBUI.scale(12), JBUI.scale(12))
            g2.dispose()
            super.paintComponent(g)
        }

        override fun getPreferredSize(): Dimension {
            val base = super.getPreferredSize()
            return Dimension(base.width, JBUI.scale(20))
        }
    }
}