package com.codechecker.plugin.ui

import com.codechecker.plugin.model.Issue
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

        add(buildHeaderRow())
        add(buildMessageRow())
        issue.suggestion?.let { add(buildSuggestionRow(it)) }
        add(buildFooterRow())

        setupHoverEffect()
    }

    private fun buildHeaderRow(): Component {
        val row = Box.createHorizontalBox().apply {
            alignmentX = Component.LEFT_ALIGNMENT
            maximumSize = Dimension(Int.MAX_VALUE, JBUI.scale(24))
        }

        row.add(JBLabel(issue.severity.getIcon()).apply {
            font = font.deriveFont(font.size2D + 1f)
        })
        row.add(Box.createRigidArea(Dimension(JBUI.scale(4), 0)))

        row.add(JBLabel("[${issue.ruleId}]").apply {
            font = font.deriveFont(Font.BOLD)
            foreground = issue.severity.getColor()
        })
        row.add(Box.createRigidArea(Dimension(JBUI.scale(4), 0)))

        row.add(JBLabel(issue.title).apply {
            font = font.deriveFont(Font.BOLD)
        })

        row.add(Box.createHorizontalGlue())

        if (issue.line != null) {
            row.add(JBLabel("L.${issue.line}").apply {
                foreground = JBColor.GRAY
                font = font.deriveFont(font.size2D - 1f)
            })
        }

        return row
    }

    private fun buildMessageRow(): Component {
        // issue.message → issue.displayMessage 로 변경
        return JBLabel(
            "<html><body style='width:350px'>${issue.displayMessage}</body></html>"
        ).apply {
            alignmentX = Component.LEFT_ALIGNMENT
            maximumSize = Dimension(Int.MAX_VALUE, Int.MAX_VALUE)
            border = JBUI.Borders.empty(JBUI.scale(4), 0, 0, 0)
        }
    }

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

    private fun buildFooterRow(): Component {
        val row = Box.createHorizontalBox().apply {
            alignmentX = Component.LEFT_ALIGNMENT
            maximumSize = Dimension(Int.MAX_VALUE, JBUI.scale(24))
            border = JBUI.Borders.empty(JBUI.scale(6), 0, 0, 0)
        }

        val locationText = buildLocationText()
        if (locationText != null) {
            row.add(JBLabel(locationText).apply {
                foreground = JBColor.GRAY
                font = font.deriveFont(font.size2D - 1f)
            })
        }

        row.add(Box.createHorizontalGlue())

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

    private fun navigateToLine() {
        ApplicationManager.getApplication().invokeLater {
            val editor = FileEditorManager.getInstance(project)
                .selectedTextEditor ?: return@invokeLater

            val line = ((issue.line ?: 1) - 1).coerceAtLeast(0)
            editor.caretModel.moveToLogicalPosition(LogicalPosition(line, 0))
            editor.scrollingModel.scrollToCaret(ScrollType.CENTER)
        }
    }

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

    private class SeverityLeftBorder(private val color: java.awt.Color) : Border {
        private val thickness = JBUI.scale(3)

        override fun paintBorder(c: Component, g: Graphics, x: Int, y: Int, width: Int, height: Int) {
            val g2 = g.create() as java.awt.Graphics2D
            g2.color = color
            g2.fillRect(x, y, thickness, height)
            g2.dispose()
        }

        override fun getBorderInsets(c: Component): Insets = Insets(0, thickness, 0, 0)
        override fun isBorderOpaque(): Boolean = true
    }
}