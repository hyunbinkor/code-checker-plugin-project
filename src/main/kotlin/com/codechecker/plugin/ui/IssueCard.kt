package com.codechecker.plugin.ui

import com.codechecker.plugin.model.Issue
//import com.intellij.ide.ui.laf.darcula.ui.DarculaButtonUI
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.labels.LinkLabel
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import java.awt.Font
import java.awt.Graphics
import java.awt.Insets
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.border.Border

/**
 * 이슈 하나를 표시하는 카드 컴포넌트.
 *
 * 구조 (위→아래):
 * 1. 헤더: [아이콘] [ruleId] [title] ... [L.line]
 * 2. message (HTML 자동 줄바꿈)
 * 3. suggestion (nullable, 💡 아이콘 + 이탤릭)
 * 4. 하단: [📍 className.method()] ... [라인으로 이동]
 */
class IssueCard(
    private val issue: Issue,
    private val project: Project
) : JBPanel<IssueCard>() {

    private val normalColor = JBColor(Color(0xFAFAFA), Color(0x323232))
    private val hoverColor  = JBColor(Color(0xF0F0F0), Color(0x3A3A3A))

    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        isOpaque = true
        background = normalColor

        border = BorderFactory.createCompoundBorder(
            SeverityLeftBorder(issue.severity.getColor()),
            JBUI.Borders.empty(8, 12, 8, 8)
        )

        alignmentX = Component.LEFT_ALIGNMENT
        maximumSize = Dimension(Int.MAX_VALUE, Int.MAX_VALUE)

        // 구성 요소 추가
        add(buildHeaderRow())
        add(buildMessageRow())
        issue.suggestion?.let { add(buildSuggestionRow(it)) }
        add(buildFooterRow())

        // 호버 효과
        setupHoverEffect()
    }

    // ── 1. 헤더 라인 ─────────────────────────────

    private fun buildHeaderRow(): Component {
        val row = Box.createHorizontalBox().apply {
            alignmentX = Component.LEFT_ALIGNMENT
            maximumSize = Dimension(Int.MAX_VALUE, JBUI.scale(24))
        }

        // severity 아이콘
        row.add(JBLabel(issue.severity.getIcon()).apply {
            font = font.deriveFont(font.size2D + 1f)
        })
        row.add(Box.createRigidArea(Dimension(JBUI.scale(4), 0)))

        // [ruleId]
        row.add(JBLabel("[${issue.ruleId}]").apply {
            font = font.deriveFont(Font.BOLD)
            foreground = issue.severity.getColor()
        })
        row.add(Box.createRigidArea(Dimension(JBUI.scale(4), 0)))

        // title
        row.add(JBLabel(issue.title).apply {
            font = font.deriveFont(Font.BOLD)
        })

        // 가변 공백
        row.add(Box.createHorizontalGlue())

        // L.{line}
        if (issue.line != null) {
            row.add(JBLabel("L.${issue.line}").apply {
                foreground = JBColor.GRAY
                font = font.deriveFont(font.size2D - 1f)
            })
        }

        return row
    }

    // ── 2. message ───────────────────────────────

    private fun buildMessageRow(): Component {
        return JBLabel(
            "<html><body style='width:350px'>${issue.message}</body></html>"
        ).apply {
            alignmentX = Component.LEFT_ALIGNMENT
            maximumSize = Dimension(Int.MAX_VALUE, Int.MAX_VALUE)
            border = JBUI.Borders.empty(JBUI.scale(4), 0, 0, 0)
        }
    }

    // ── 3. suggestion ────────────────────────────

    private fun buildSuggestionRow(suggestion: String): Component {
        return JBLabel(
            "<html><body style='width:350px'>💡 $suggestion</body></html>"
        ).apply {
            alignmentX = Component.LEFT_ALIGNMENT
            maximumSize = Dimension(Int.MAX_VALUE, Int.MAX_VALUE)
            foreground = JBColor.GRAY
            font = font.deriveFont(Font.ITALIC)
            border = JBUI.Borders.empty(JBUI.scale(4), 0, 0, 0)
        }
    }

    // ── 4. 하단 라인 ─────────────────────────────

    private fun buildFooterRow(): Component {
        val row = Box.createHorizontalBox().apply {
            alignmentX = Component.LEFT_ALIGNMENT
            maximumSize = Dimension(Int.MAX_VALUE, JBUI.scale(24))
            border = JBUI.Borders.empty(JBUI.scale(6), 0, 0, 0)
        }

        // 📍 className.methodName()
        val locationText = buildLocationText()
        if (locationText != null) {
            row.add(JBLabel(locationText).apply {
                foreground = JBColor.GRAY
                font = font.deriveFont(font.size2D - 1f)
            })
        }

        row.add(Box.createHorizontalGlue())

        // 라인으로 이동 링크
        if (issue.line != null) {
            val link = LinkLabel<Unit>("라인으로 이동", null) { _, _ ->
                navigateToLine()
            }
            row.add(link)
        }

        return row
    }

    private fun buildLocationText(): String? {
        val cls = issue.className
        val method = issue.methodName
        return when {
            cls != null && method != null -> "📍 $cls.$method()"
            cls != null                   -> "📍 $cls"
            else                          -> null
        }
    }

    // ── 라인 이동 ─────────────────────────────────

    private fun navigateToLine() {
        ApplicationManager.getApplication().invokeLater {
            val editor = FileEditorManager.getInstance(project)
                .selectedTextEditor ?: return@invokeLater

            val line = ((issue.line ?: 1) - 1).coerceAtLeast(0)
            editor.caretModel.moveToLogicalPosition(LogicalPosition(line, 0))
            editor.scrollingModel.scrollToCaret(ScrollType.CENTER)
        }
    }

    // ── 호버 효과 ─────────────────────────────────

    private fun setupHoverEffect() {
        addMouseListener(object : MouseAdapter() {
            override fun mouseEntered(e: MouseEvent) {
                background = hoverColor
                repaint()
            }

            override fun mouseExited(e: MouseEvent) {
                background = normalColor
                repaint()
            }
        })
    }

    // ── 왼쪽 severity 컬러 보더 ───────────────────

    private class SeverityLeftBorder(private val color: Color) : Border {

        private val thickness = JBUI.scale(3)

        override fun paintBorder(c: Component, g: Graphics, x: Int, y: Int, width: Int, height: Int) {
            val g2 = g.create() as java.awt.Graphics2D
            g2.color = color
            g2.fillRect(x, y, thickness, height)
            g2.dispose()
        }

        override fun getBorderInsets(c: Component): Insets =
            Insets(0, thickness, 0, 0)

        override fun isBorderOpaque(): Boolean = true
    }
}