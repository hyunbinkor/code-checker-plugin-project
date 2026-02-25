package com.codechecker.plugin.action

import com.codechecker.plugin.ui.ChatPanel
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.wm.ToolWindowManager

class CheckFileAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return

        val code = editor.document.text
        if (code.isBlank()) {
            NotificationGroupManager.getInstance()
                .getNotificationGroup("CodeChecker.Notification")
                .createNotification(
                    "파일이 비어 있습니다.",
                    "내용이 있는 Java 파일을 열고 다시 시도하세요.",
                    NotificationType.WARNING
                )
                .notify(project)
            return
        }

        val fileName = e.getData(CommonDataKeys.VIRTUAL_FILE)?.name ?: "unknown.java"
        val lineCount = editor.document.lineCount

        // Tool Window 열기
        val toolWindow = ToolWindowManager.getInstance(project)
            .getToolWindow("CodeChecker")
        toolWindow?.show()

        // ChatPanel에 검사 요청
        val chatPanel = ChatPanel.getInstance(project) ?: return
        chatPanel.submitCheck(code, fileName, lineCount)
    }

    override fun update(e: AnActionEvent) {
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)

        // .java 파일이 열려 있을 때만 활성화
        val isJavaFile = file?.name?.endsWith(".java") == true

        e.presentation.isEnabledAndVisible = isJavaFile
    }
}