package com.codechecker.plugin.toolwindow

import com.codechecker.plugin.ui.ChatPanel
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

class CodeCheckerToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val chatPanel = ChatPanel(project)

        // Action에서 ChatPanel.getInstance(project)로 접근할 수 있도록 등록
        ChatPanel.register(project, chatPanel)

        val content = ContentFactory.getInstance()
            .createContent(chatPanel, "", false)

        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project): Boolean = true
}