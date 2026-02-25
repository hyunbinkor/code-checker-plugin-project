package com.codechecker.plugin.ui

import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBTextArea
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Dimension

/**
 * 사용자 요청 메시지 버블.
 * "📄 {fileName} 검사 요청 ({lineCount}줄)" + 코드 미리보기 (최대 3줄)
 */
class UserMessageBubble(
    fileName: String,
    lineCount: Int,
    code: String
) : JBPanel<UserMessageBubble>(BorderLayout(0, JBUI.scale(4))) {

    init {
        isOpaque = true
        background = JBColor(Color(0xE3F2FD), Color(0x1A3A4A))
        border = JBUI.Borders.compound(
            JBUI.Borders.empty(2, 0),
            JBUI.Borders.empty(8)
        )
        alignmentX = Component.LEFT_ALIGNMENT
        maximumSize = Dimension(Int.MAX_VALUE, Int.MAX_VALUE)

        // 제목
        val title = JBLabel("📄 $fileName 검사 요청 (${lineCount}줄)").apply {
            font = font.deriveFont(font.size2D + 0.5f)
            alignmentX = Component.LEFT_ALIGNMENT
            maximumSize = Dimension(Int.MAX_VALUE, preferredSize.height)
        }
        add(title, BorderLayout.NORTH)

        // 코드 미리보기
        val preview = buildPreview(code)
        add(preview, BorderLayout.CENTER)
    }

    private fun buildPreview(code: String): Component {
        val lines = code.lines()
        val previewLines = lines.take(3)
        val extra = lines.size - previewLines.size

        val previewText = buildString {
            append(previewLines.joinToString("\n"))
            if (extra > 0) append("\n...외 ${extra}줄")
        }

        return JBTextArea(previewText).apply {
            isEditable = false
            isOpaque = false
            font = JBUI.Fonts.create("Monospaced", 11)
            foreground = JBColor(Color(0x546E7A), Color(0x90A4AE))
            border = JBUI.Borders.empty(4, 0, 0, 0)
            lineWrap = true
            wrapStyleWord = false
            alignmentX = Component.LEFT_ALIGNMENT
            maximumSize = Dimension(Int.MAX_VALUE, Int.MAX_VALUE)
        }
    }
}