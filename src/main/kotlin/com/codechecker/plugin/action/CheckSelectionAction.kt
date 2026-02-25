package com.codechecker.plugin.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class CheckSelectionAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        // TODO: 에디터 선택 영역 추출 후 CodeCheckService 호출
    }

    override fun update(e: AnActionEvent) {
        // 에디터가 열려 있고 선택 영역이 있을 때만 활성화
        val editor = e.getData(com.intellij.openapi.actionSystem.CommonDataKeys.EDITOR)
        e.presentation.isEnabled = editor?.selectionModel?.hasSelection() == true
    }
}