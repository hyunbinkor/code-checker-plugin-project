package com.codechecker.plugin.action

import com.codechecker.plugin.ui.ChatPanel
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.wm.ToolWindowManager

class CheckSelectionAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return

        // 선택 영역 확인
        val selectedText = editor.selectionModel.selectedText
        if (selectedText.isNullOrBlank()) {
            NotificationGroupManager.getInstance()
                .getNotificationGroup("CodeChecker.Notification")
                .createNotification(
                    "선택된 코드가 없습니다.",
                    "에디터에서 코드를 드래그로 선택한 후 다시 시도하세요.",
                    NotificationType.WARNING
                )
                .notify(project)
            return
        }

        val fileName = e.getData(CommonDataKeys.VIRTUAL_FILE)?.name ?: "unknown.java"
        val lineCount = selectedText.lines().size

        // Tool Window 열기
        val toolWindow = ToolWindowManager.getInstance(project)
            .getToolWindow("CodeChecker")
        toolWindow?.show()

        // ChatPanel에 검사 요청
        val chatPanel = ChatPanel.getInstance(project) ?: return
        chatPanel.submitCheck(selectedText, fileName, lineCount)
    }

    override fun update(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR)
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)

        // .java 파일이 열려 있고 선택 영역이 있을 때만 활성화
        val isJavaFile = file?.name?.endsWith(".java") == true
        val hasSelection = editor?.selectionModel?.hasSelection() == true

        e.presentation.isEnabledAndVisible = isJavaFile && hasSelection
    }
}