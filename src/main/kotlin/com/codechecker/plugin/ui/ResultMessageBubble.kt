package com.codechecker.plugin.ui

import com.codechecker.plugin.model.CheckResponse
import com.codechecker.plugin.model.Severity
import com.intellij.openapi.project.Project
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import javax.swing.Box
import javax.swing.BoxLayout

/**
 * 검사 결과 버블.
 * - 이슈 있음: SummaryBar + IssueCard 목록 + 처리 정보
 * - 이슈 없음: "✅ 이슈가 발견되지 않았습니다"
 */
class ResultMessageBubble(
    response: CheckResponse,
    project: Project
) : JBPanel<ResultMessageBubble>(BorderLayout(0, JBUI.scale(4))) {

    init {
        isOpaque = false
        border = JBUI.Borders.empty(2, 0)
        alignmentX = Component.LEFT_ALIGNMENT
        maximumSize = Dimension(Int.MAX_VALUE, Int.MAX_VALUE)

        val issues = response.issues.orEmpty()

        if (issues.isEmpty()) {
            buildNoIssuePanel()
        } else {
            buildIssuePanel(response, project)
        }
    }

    private fun buildNoIssuePanel() {
        val label = JBLabel("✅ 이슈가 발견되지 않았습니다").apply {
            foreground = JBColor(Color(0x2E7D32), Color(0x4CAF50))
            border = JBUI.Borders.empty(8)
        }

        val panel = JBPanel<JBPanel<*>>(BorderLayout()).apply {
            isOpaque = true
            background = JBColor(Color(0xE8F5E9), Color(0x1B3A1F))
            border = JBUI.Borders.empty(8)
            alignmentX = Component.LEFT_ALIGNMENT
            maximumSize = Dimension(Int.MAX_VALUE, Int.MAX_VALUE)
            add(label, BorderLayout.CENTER)
        }

        add(panel, BorderLayout.CENTER)
    }

    private fun buildIssuePanel(response: CheckResponse, project: Project) {
        val issues = response.issues.orEmpty()
            .sortedBy { it.severity.priority }

        val container = JBPanel<JBPanel<*>>().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            isOpaque = false
            alignmentX = Component.LEFT_ALIGNMENT
            maximumSize = Dimension(Int.MAX_VALUE, Int.MAX_VALUE)
        }

        // SummaryBar
        val summaryBar = SummaryBar(issues)
        summaryBar.alignmentX = Component.LEFT_ALIGNMENT
        summaryBar.maximumSize = Dimension(Int.MAX_VALUE, summaryBar.preferredSize.height)
        container.add(summaryBar)
        container.add(Box.createRigidArea(Dimension(0, JBUI.scale(6))))

        // IssueCard 목록
        issues.forEach { issue ->
            val card = IssueCard(issue, project)
            card.alignmentX = Component.LEFT_ALIGNMENT
            card.maximumSize = Dimension(Int.MAX_VALUE, Int.MAX_VALUE)
            container.add(card)
            container.add(Box.createRigidArea(Dimension(0, JBUI.scale(4))))
        }

        // 처리 정보
        val infoText = buildString {
            response.processingTimeMs?.let { append("⏱️ ${it}ms") }
            response.matchedRulesCount?.let { append(" · ${it}개 규칙 매칭") }
        }
        if (infoText.isNotBlank()) {
            val infoLabel = JBLabel(infoText).apply {
                foreground = JBColor.GRAY
                font = font.deriveFont(font.size2D - 1f)
                alignmentX = Component.LEFT_ALIGNMENT
                maximumSize = Dimension(Int.MAX_VALUE, preferredSize.height)
            }
            container.add(Box.createRigidArea(Dimension(0, JBUI.scale(4))))
            container.add(infoLabel)
        }

        add(container, BorderLayout.CENTER)
    }
}