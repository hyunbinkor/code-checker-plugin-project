package com.codechecker.plugin.ui

import com.codechecker.plugin.model.CheckResult
import com.codechecker.plugin.model.ErrorType
import com.codechecker.plugin.service.CodeCheckService
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import java.awt.CardLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Font
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.util.WeakHashMap
import javax.swing.Box
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JSeparator
import javax.swing.SwingUtilities
import javax.swing.Timer

class ChatPanel(private val project: Project) : JBPanel<ChatPanel>(java.awt.BorderLayout()) {

    // ── 버튼 ─────────────────────────────────────

    private val checkSelectionButton = JButton("선택 검사").apply {
        toolTipText = "에디터에서 드래그로 선택한 코드 영역을 검사합니다"
    }
    private val checkFileButton = JButton("파일 검사").apply {
        toolTipText = "현재 열린 파일 전체를 검사합니다"
    }

    // ── 메시지 목록 ───────────────────────────────

    private val messagesBox: Box = Box.createVerticalBox()
    private val scrollPane: JBScrollPane = JBScrollPane(messagesBox).apply {
        border = JBUI.Borders.empty()
        verticalScrollBar.unitIncrement = JBUI.scale(16)
    }

    // ── CardLayout (빈 상태 ↔ 메시지 목록 전환) ──

    private val cardLayout = CardLayout()
    private val cardPanel = JPanel(cardLayout)

    // ── 로딩 상태 추적 ────────────────────────────

    private var loadingBubble: LoadingMessageBubble? = null

    // ── 초기화 ───────────────────────────────────

    init {
        border = JBUI.Borders.empty(4)
        add(buildCenterPanel(), java.awt.BorderLayout.CENTER)
        add(buildSouthPanel(), java.awt.BorderLayout.SOUTH)
        setupButtons()
    }

    // ── 레이아웃 빌드 ─────────────────────────────

    private fun buildCenterPanel(): JPanel {
        messagesBox.add(Box.createRigidArea(Dimension(0, JBUI.scale(4))))

        cardPanel.add(buildEmptyStatePanel(), "EMPTY")
        cardPanel.add(scrollPane, "MESSAGES")
        cardLayout.show(cardPanel, "EMPTY")

        return cardPanel
    }

    private fun buildEmptyStatePanel(): JPanel {
        return JBPanel<JBPanel<*>>(GridBagLayout()).apply {
            isOpaque = false

            val gbc = GridBagConstraints().apply {
                gridx = 0
                gridy = 0
                anchor = GridBagConstraints.CENTER
            }

            val box = Box.createVerticalBox()

            val iconLabel = JBLabel("🔍").apply {
                font = font.deriveFont(32f)
                alignmentX = Component.CENTER_ALIGNMENT
            }

            val titleLabel = JBLabel("Code Quality Checker").apply {
                font = font.deriveFont(Font.BOLD, 14f)
                foreground = JBColor.foreground()
                alignmentX = Component.CENTER_ALIGNMENT
            }

            val descLabel = JBLabel(
                "<html><center>" +
                        "· <b>파일 검사</b>: 현재 열린 Java 파일 전체를 검사합니다<br><br>" +
                        "· <b>선택 검사</b>: 에디터에서 코드를 드래그로 선택한 후<br>" +
                        "검사 버튼을 눌러 선택 영역만 검사합니다" +
                        "</center></html>"
            ).apply {
                foreground = JBColor.GRAY
                font = font.deriveFont(12f)
                alignmentX = Component.CENTER_ALIGNMENT
            }

            box.add(iconLabel)
            box.add(Box.createRigidArea(Dimension(0, JBUI.scale(8))))
            box.add(titleLabel)
            box.add(Box.createRigidArea(Dimension(0, JBUI.scale(12))))
            box.add(descLabel)

            add(box, gbc)
        }
    }

    private fun buildSouthPanel(): JPanel {
        val buttonPanel = JPanel(FlowLayout(FlowLayout.RIGHT, JBUI.scale(4), JBUI.scale(4))).apply {
            isOpaque = false
            add(checkSelectionButton)
            add(checkFileButton)
        }

        return JPanel(java.awt.BorderLayout()).apply {
            isOpaque = false
            add(JSeparator(), java.awt.BorderLayout.NORTH)
            add(buttonPanel, java.awt.BorderLayout.CENTER)
        }
    }

    // ── 버튼 액션 ─────────────────────────────────

    private fun setupButtons() {
        checkSelectionButton.addActionListener { requestCheckSelection() }
        checkFileButton.addActionListener { requestCheckFile() }
    }

    /**
     * 선택 검사 버튼 동작.
     * .java 파일 여부 확인 → 선택 영역 확인 → submitCheck()
     */
    private fun requestCheckSelection() {
        val editor = FileEditorManager
            .getInstance(project)
            .selectedTextEditor ?: run {
            addSystemMessage("열린 파일이 없습니다. Java 파일을 열고 다시 시도하세요.")
            return
        }

        // .java 파일 여부 확인
        val fileName = getCurrentFileName() ?: "unknown.java"
        if (!fileName.endsWith(".java")) {
            addSystemMessage("Java 파일만 검사할 수 있습니다. (.java) 현재 파일: $fileName")
            return
        }

        // 선택 영역 확인
        val selectedText = editor.selectionModel.selectedText
        if (selectedText.isNullOrBlank()) {
            addSystemMessage("코드를 먼저 선택해주세요. 에디터에서 코드를 드래그로 선택한 후 버튼을 눌러주세요.")
            return
        }

        submitCheck(selectedText, fileName, selectedText.lines().size)
    }

    /**
     * 파일 검사 버튼 동작.
     * .java 파일 여부 확인 → 파일 내용 확인 → submitCheck()
     */
    private fun requestCheckFile() {
        val editor = FileEditorManager
            .getInstance(project)
            .selectedTextEditor ?: run {
            addSystemMessage("열린 파일이 없습니다. Java 파일을 열고 다시 시도하세요.")
            return
        }

        // .java 파일 여부 확인
        val fileName = getCurrentFileName() ?: "unknown.java"
        if (!fileName.endsWith(".java")) {
            addSystemMessage("Java 파일만 검사할 수 있습니다. (.java) 현재 파일: $fileName")
            return
        }

        // 파일 내용 확인
        val code = editor.document.text
        if (code.isBlank()) {
            addSystemMessage("파일이 비어 있습니다.")
            return
        }

        submitCheck(code, fileName, editor.document.lineCount)
    }

    private fun getCurrentFileName(): String? =
        FileEditorManager.getInstance(project).selectedFiles.firstOrNull()?.name

    // ── 공개 API ──────────────────────────────────

    /**
     * 코드 검사 실행.
     * Action 클래스(CheckSelectionAction, CheckFileAction)에서도 직접 호출 가능.
     */
    fun submitCheck(code: String, fileName: String, lineCount: Int) {
        // 1. 사용자 메시지 추가
        addUserMessage(fileName, lineCount, code)

        // 2. 로딩 메시지 추가
        val loading = addLoadingMessage(fileName)

        // 3. 버튼 비활성화
        setButtonsEnabled(false)

        // 4. 경과 시간 Timer 시작 (EDT에서 동작, 1초 간격)
        var elapsed = 0
        val timer = Timer(1000) {
            elapsed++
            loading.updateElapsedTime(elapsed)
        }.apply {
            isRepeats = true
            start()
        }

        // 5. 백그라운드에서 검사 실행
        ApplicationManager.getApplication().executeOnPooledThread {
            val result = CodeCheckService.getInstance().checkCode(
                code = code,
                fileName = fileName,
                onHeartbeat = {
                    SwingUtilities.invokeLater {
                        loading.onHeartbeatReceived()
                    }
                }
            )

            // 6. EDT에서 UI 업데이트
            SwingUtilities.invokeLater {
                timer.stop()
                removeLoadingMessage()

                when (result) {
                    is CheckResult.Success -> addResultMessage(result)
                    is CheckResult.Failure -> addErrorMessage(result.message, result.type)
                }

                setButtonsEnabled(true)
            }
        }
    }

    /**
     * 메시지 전부 제거 후 빈 상태 화면으로 복귀.
     */
    fun clear() {
        messagesBox.removeAll()
        messagesBox.add(Box.createRigidArea(Dimension(0, JBUI.scale(4))))
        loadingBubble = null
        messagesBox.revalidate()
        messagesBox.repaint()
        cardLayout.show(cardPanel, "EMPTY")
    }

    // ── 메시지 추가 내부 메서드 ───────────────────

    private fun addUserMessage(fileName: String, lineCount: Int, code: String) {
        appendMessage(UserMessageBubble(fileName, lineCount, code))
    }

    private fun addLoadingMessage(fileName: String): LoadingMessageBubble {
        val bubble = LoadingMessageBubble(fileName)
        loadingBubble = bubble
        appendMessage(bubble)
        return bubble
    }

    private fun removeLoadingMessage() {
        val bubble = loadingBubble ?: return
        messagesBox.remove(bubble)
        loadingBubble = null
        messagesBox.revalidate()
        messagesBox.repaint()
    }

    private fun addResultMessage(result: CheckResult.Success) {
        appendMessage(ResultMessageBubble(result.response, project))
    }

    private fun addErrorMessage(message: String, type: ErrorType) {
        appendMessage(ErrorMessageBubble(message, type))
    }

    /**
     * 시스템 안내 메시지 (회색, 이탤릭).
     * 선택 없음 / 파일 없음 / Java 아님 등 경고 안내에 사용.
     */
    private fun addSystemMessage(message: String) {
        val label = JBLabel("<html>$message</html>").apply {
            foreground = JBColor.GRAY
            font = font.deriveFont(Font.ITALIC)
            border = JBUI.Borders.empty(4, 8)
            alignmentX = Component.LEFT_ALIGNMENT
            maximumSize = Dimension(Int.MAX_VALUE, preferredSize.height)
        }
        appendMessage(label)
    }

    private fun appendMessage(component: Component) {
        cardLayout.show(cardPanel, "MESSAGES")

        messagesBox.add(Box.createRigidArea(Dimension(0, JBUI.scale(8))))
        messagesBox.add(component)
        messagesBox.revalidate()
        messagesBox.repaint()

        SwingUtilities.invokeLater {
            val vsb = scrollPane.verticalScrollBar
            vsb.value = vsb.maximum
        }
    }

    private fun setButtonsEnabled(enabled: Boolean) {
        checkSelectionButton.isEnabled = enabled
        checkFileButton.isEnabled = enabled
    }

    // ── 싱글톤 접근 ──────────────────────────────

    companion object {
        private val panels = WeakHashMap<Project, ChatPanel>()

        fun getInstance(project: Project): ChatPanel? = panels[project]

        internal fun register(project: Project, panel: ChatPanel) {
            panels[project] = panel
        }
    }
}