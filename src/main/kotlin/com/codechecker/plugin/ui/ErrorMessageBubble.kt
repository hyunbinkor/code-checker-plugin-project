package com.codechecker.plugin.ui

import com.codechecker.plugin.model.ErrorType
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Dimension

/**
 * 검사 실패 에러 버블.
 */
class ErrorMessageBubble(
    message: String,
    type: ErrorType
) : JBPanel<ErrorMessageBubble>(BorderLayout()) {

    init {
        isOpaque = true
        background = JBColor(Color(0xFFEBEE), Color(0x3A1A1A))
        border = JBUI.Borders.compound(
            JBUI.Borders.empty(2, 0),
            JBUI.Borders.empty(8)
        )
        alignmentX = Component.LEFT_ALIGNMENT
        maximumSize = Dimension(Int.MAX_VALUE, Int.MAX_VALUE)

        val icon = if (type == ErrorType.NETWORK) "🔌" else "❌"
        val displayMessage = if (type == ErrorType.NETWORK) {
            "🔌 서버에 연결할 수 없습니다. Settings에서 URL을 확인하세요."
        } else {
            "$icon 검사 실패: $message"
        }

        val label = JBLabel("<html>${displayMessage.replace("\n", "<br>")}</html>").apply {
            foreground = JBColor(Color(0xC62828), Color(0xEF9A9A))
            alignmentX = Component.LEFT_ALIGNMENT
            maximumSize = Dimension(Int.MAX_VALUE, preferredSize.height)
        }

        add(label, BorderLayout.CENTER)
    }
}