package com.codechecker.plugin.toolwindow

import com.codechecker.plugin.ui.ChatPanel
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.components.JBPanel
import com.intellij.openapi.wm.ex.ToolWindowEx
import java.awt.BorderLayout

class CodeCheckerToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val chatPanel = ChatPanel(project)

        // Action에서 접근할 수 있도록 project UserData에 저장
        project.putUserData(CHAT_PANEL_KEY, chatPanel)

        val content = ContentFactory.getInstance()
            .createContent(chatPanel, "", false)

        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project): Boolean = true

    companion object {
        /**
         * Action 클래스에서 ChatPanel에 접근하는 키.
         *
         * 사용 예:
         * val chatPanel = e.project?.getUserData(CodeCheckerToolWindowFactory.CHAT_PANEL_KEY)
         */
        val CHAT_PANEL_KEY: Key<ChatPanel> = Key.create("CodeChecker.ChatPanel")
    }
}