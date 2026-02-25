package com.codechecker.plugin.settings

import com.codechecker.plugin.service.CodeCheckService
import com.intellij.openapi.application.ApplicationManager
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JPanel

/**
 * 설정 UI 레이아웃.
 * [PluginSettingsConfigurable]에서 생성하며, [root]를 Settings 패널로 사용함.
 */
class PluginSettingsPanel {

    // ── UI 컴포넌트 ──────────────────────────────

    private val serverUrlField = JBTextField()
    private val testConnectionButton = JButton("연결 테스트")
    private val connectionStatusLabel = JBLabel("").apply {
        isVisible = false
    }

    private val readTimeoutField = JBTextField().apply {
        // 숫자만 입력 허용 (문서 필터)
        (document as javax.swing.text.AbstractDocument).documentFilter =
            DigitsOnlyFilter()
    }

    private val outputFormatCombo = JComboBox(arrayOf("json", "sarif"))

    private val useMockModeCheckBox = JBCheckBox("Mock 모드 사용 (실제 서버 없이 UI 테스트)")

    // ── 루트 패널 ────────────────────────────────

    val root: JPanel = buildRootPanel()

    // ── 초기화 ───────────────────────────────────

    init {
        reset()                          // 저장된 설정값으로 UI 초기화
        setupConnectionTestButton()
    }

    // ── Configurable 위임 메서드 ─────────────────

    fun isModified(): Boolean {
        val settings = PluginSettings.getInstance()
        return serverUrlField.text.trim() != settings.serverUrl
                || readTimeoutField.text.trim().toIntOrNull() != settings.readTimeoutSeconds
                || outputFormatCombo.selectedItem as? String != settings.outputFormat
                || useMockModeCheckBox.isSelected != settings.useMockMode
    }

    fun apply() {
        val settings = PluginSettings.getInstance()
        settings.serverUrl = serverUrlField.text.trim()
        settings.readTimeoutSeconds =
            readTimeoutField.text.trim().toIntOrNull() ?: PluginSettings.State().readTimeoutSeconds
        settings.outputFormat = outputFormatCombo.selectedItem as? String ?: "json"
        settings.useMockMode = useMockModeCheckBox.isSelected
    }

    fun reset() {
        val settings = PluginSettings.getInstance()
        serverUrlField.text = settings.serverUrl
        readTimeoutField.text = settings.readTimeoutSeconds.toString()
        outputFormatCombo.selectedItem = settings.outputFormat
        useMockModeCheckBox.isSelected = settings.useMockMode
        hideConnectionStatus()
    }

    // ── UI 빌드 ──────────────────────────────────

    private fun buildRootPanel(): JPanel {
        val panel = JBPanel<JBPanel<*>>(GridBagLayout())
        panel.border = JBUI.Borders.empty(8)

        val gbc = GridBagConstraints().apply {
            fill = GridBagConstraints.HORIZONTAL
            anchor = GridBagConstraints.WEST
            insets = JBUI.insets(4, 0, 4, 0)
        }

        var row = 0

        // ── 서버 URL ──
        gbc.gridy = row++
        gbc.gridx = 0
        gbc.gridwidth = 1
        gbc.weightx = 0.0
        panel.add(JBLabel("서버 URL:").apply {
            preferredSize = Dimension(JBUI.scale(130), preferredSize.height)
        }, gbc)

        gbc.gridx = 1
        gbc.weightx = 1.0
        panel.add(serverUrlField, gbc)

        // ── 연결 테스트 버튼 ──
        gbc.gridy = row++
        gbc.gridx = 1
        gbc.weightx = 0.0
        val buttonPanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0)).apply {
            isOpaque = false
            add(testConnectionButton)
            add(connectionStatusLabel.apply {
                border = BorderFactory.createEmptyBorder(0, JBUI.scale(8), 0, 0)
            })
        }
        panel.add(buttonPanel, gbc)

        // ── 읽기 타임아웃 ──
        gbc.gridy = row++
        gbc.gridx = 0
        gbc.weightx = 0.0
        panel.add(JBLabel("읽기 타임아웃 (초):"), gbc)

        gbc.gridx = 1
        gbc.weightx = 1.0
        val timeoutPanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0)).apply {
            isOpaque = false
            readTimeoutField.preferredSize = Dimension(JBUI.scale(80), readTimeoutField.preferredSize.height)
            add(readTimeoutField)
            add(JBLabel("  (기본값: 900, 최대 파일 검사 시간)").apply {
                foreground = JBColor.GRAY
            })
        }
        panel.add(timeoutPanel, gbc)

        // ── 출력 형식 ──
        gbc.gridy = row++
        gbc.gridx = 0
        gbc.weightx = 0.0
        panel.add(JBLabel("출력 형식:"), gbc)

        gbc.gridx = 1
        gbc.weightx = 1.0
        val formatPanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0)).apply {
            isOpaque = false
            add(outputFormatCombo)
        }
        panel.add(formatPanel, gbc)

        // ── Mock 모드 ──
        gbc.gridy = row++
        gbc.gridx = 0
        gbc.gridwidth = 2
        gbc.weightx = 1.0
        gbc.insets = JBUI.insets(12, 0, 4, 0)
        panel.add(buildSectionSeparator("개발/테스트"), gbc)

        gbc.gridy = row++
        gbc.insets = JBUI.insets(4, 0, 4, 0)
        panel.add(useMockModeCheckBox, gbc)

        // ── 하단 여백 채우기 ──
        gbc.gridy = row
        gbc.gridx = 0
        gbc.gridwidth = 2
        gbc.weightx = 1.0
        gbc.weighty = 1.0
        gbc.fill = GridBagConstraints.BOTH
        panel.add(JPanel().apply { isOpaque = false }, gbc)

        return panel
    }

    private fun buildSectionSeparator(title: String): JPanel {
        val panel = JPanel(BorderLayout(JBUI.scale(6), 0)).apply {
            isOpaque = false
        }
        panel.add(JBLabel(title).apply {
            foreground = JBColor.GRAY
        }, BorderLayout.WEST)
        return panel
    }

    // ── 연결 테스트 버튼 동작 ────────────────────

    private fun setupConnectionTestButton() {
        testConnectionButton.addActionListener {
            val url = serverUrlField.text.trim()
            if (url.isEmpty()) {
                showConnectionStatus(false, "서버 URL을 입력하세요.")
                return@addActionListener
            }

            // 버튼 비활성화 + 테스트 중 표시
            testConnectionButton.isEnabled = false
            showConnectionStatus(null, "연결 테스트 중...")

            // 백그라운드에서 연결 테스트
            ApplicationManager.getApplication().executeOnPooledThread {
                // 현재 입력된 URL로 임시 테스트 (아직 apply 전이므로 직접 전달)
                val originalUrl = PluginSettings.getInstance().serverUrl
                PluginSettings.getInstance().serverUrl = url

                val success = try {
                    CodeCheckService.getInstance().testConnection()
                } catch (e: Exception) {
                    false
                } finally {
                    // 테스트용으로 바꾼 URL 복원 (apply 전이므로)
                    PluginSettings.getInstance().serverUrl = originalUrl
                }

                // EDT에서 UI 업데이트
                ApplicationManager.getApplication().invokeLater {
                    testConnectionButton.isEnabled = true
                    if (success) {
                        showConnectionStatus(true, "✅ 연결 성공")
                    } else {
                        showConnectionStatus(false, "❌ 연결 실패: 서버에 응답이 없습니다.")
                    }
                }
            }
        }
    }

    /**
     * @param success true=성공(초록), false=실패(빨강), null=진행중(기본색)
     */
    private fun showConnectionStatus(success: Boolean?, message: String) {
        connectionStatusLabel.text = message
        connectionStatusLabel.foreground = when (success) {
            true  -> JBColor(Color(0x2E7D32), Color(0x4CAF50))
            false -> JBColor(Color(0xC62828), Color(0xEF5350))
            null  -> JBColor.foreground()
        }
        connectionStatusLabel.isVisible = true
    }

    private fun hideConnectionStatus() {
        connectionStatusLabel.isVisible = false
    }

    // ── 숫자 전용 문서 필터 ──────────────────────

    private class DigitsOnlyFilter : javax.swing.text.DocumentFilter() {
        override fun insertString(
            fb: FilterBypass,
            offset: Int,
            string: String?,
            attr: javax.swing.text.AttributeSet?
        ) {
            if (string?.all { it.isDigit() } == true) {
                super.insertString(fb, offset, string, attr)
            }
        }

        override fun replace(
            fb: FilterBypass,
            offset: Int,
            length: Int,
            string: String?,
            attr: javax.swing.text.AttributeSet?
        ) {
            if (string?.all { it.isDigit() } == true) {
                super.replace(fb, offset, length, string, attr)
            }
        }
    }
}