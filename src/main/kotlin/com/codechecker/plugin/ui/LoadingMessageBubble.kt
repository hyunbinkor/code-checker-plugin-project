package com.codechecker.plugin.ui

import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import javax.swing.JProgressBar
import javax.swing.Timer

/**
 * 검사 중 로딩 버블.
 * - indeterminate 프로그레스 바
 * - 경과 시간 매초 업데이트
 */
class LoadingMessageBubble(fileName: String) : JBPanel<LoadingMessageBubble>(BorderLayout(0, JBUI.scale(4))) {

    private val elapsedLabel = JBLabel("0초 경과...")
    private var elapsedSeconds = 0L
    private val timer = Timer(1000) {
        elapsedSeconds++
        elapsedLabel.text = formatElapsed(elapsedSeconds)
    }

    init {
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
            maximumSize = Dimension(Int.MAX_VALUE, preferredSize.height)
        }

        elapsedLabel.apply {
            foreground = JBColor.GRAY
            alignmentX = Component.LEFT_ALIGNMENT
            maximumSize = Dimension(Int.MAX_VALUE, preferredSize.height)
        }

        add(titleLabel, BorderLayout.NORTH)
        add(progressBar, BorderLayout.CENTER)
        add(elapsedLabel, BorderLayout.SOUTH)
    }

    fun startTimer() { timer.start() }
    fun stopTimer() { timer.stop() }

    fun updateElapsed(seconds: Long) {
        elapsedSeconds = seconds
        elapsedLabel.text = formatElapsed(seconds)
    }

    private fun formatElapsed(seconds: Long): String = when {
        seconds < 60 -> "${seconds}초 경과..."
        else -> {
            val m = seconds / 60
            val s = seconds % 60
            "${m}분 ${s}초 경과..."
        }
    }
}