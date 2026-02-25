package com.codechecker.plugin.ui

import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.util.ui.JBUI
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import javax.swing.BoxLayout
import javax.swing.JProgressBar

/**
 * 검사 중 로딩 버블.
 * - indeterminate 프로그레스 바
 * - 경과 시간은 ChatPanel의 Timer가 updateElapsedTime()을 호출하여 갱신
 */
class LoadingMessageBubble(fileName: String) : JBPanel<LoadingMessageBubble>() {

    private val elapsedLabel = JBLabel("0초 경과").apply {
        foreground = JBColor.GRAY
        alignmentX = Component.LEFT_ALIGNMENT
        maximumSize = Dimension(Int.MAX_VALUE, preferredSize.height)
    }

    private val statusLabel = JBLabel("서버 응답 대기 중...").apply {
        foreground = JBColor.GRAY
        font = font.deriveFont(font.size2D - 1f)
        alignmentX = Component.LEFT_ALIGNMENT
        maximumSize = Dimension(Int.MAX_VALUE, preferredSize.height)
    }

    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        isOpaque = true
        background = JBColor(Color(0xF5F5F5), Color(0x2D2D2D))
        border = JBUI.Borders.compound(
            JBUI.Borders.empty(2, 0),
            JBUI.Borders.empty(8)
        )
        alignmentX = Component.LEFT_ALIGNMENT
        maximumSize = Dimension(Int.MAX_VALUE, Int.MAX_VALUE)

        val titleLabel = JBLabel("⏳ $fileName 검사 중...").apply {
            alignmentX = Component.LEFT_ALIGNMENT
            maximumSize = Dimension(Int.MAX_VALUE, preferredSize.height)
        }

        val progressBar = JProgressBar().apply {
            isIndeterminate = true
            alignmentX = Component.LEFT_ALIGNMENT
            maximumSize = Dimension(Int.MAX_VALUE, JBUI.scale(4))
            border = JBUI.Borders.empty(4, 0)
        }

        add(titleLabel)
        add(progressBar)
        add(elapsedLabel)
        add(statusLabel)
    }

    /**
     * ChatPanel의 Timer(1000ms)가 매 초 호출.
     * EDT에서 호출되어야 함.
     */
    fun updateElapsedTime(seconds: Int) {
        val min = seconds / 60
        val sec = seconds % 60
        elapsedLabel.text = when {
            min > 0 -> "${min}분 ${sec}초 경과"
            else    -> "${sec}초 경과"
        }
    }

    /**
     * heartbeat 수신 시 상태 텍스트 갱신.
     * EDT에서 호출되어야 함.
     */
    fun onHeartbeatReceived() {
        statusLabel.text = "서버 처리 중..."
        statusLabel.foreground = JBColor(Color(0x1565C0), Color(0x64B5F6))
    }
}