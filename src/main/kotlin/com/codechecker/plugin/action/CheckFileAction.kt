package com.codechecker.plugin.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys

class CheckFileAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        // TODO: 현재 파일 전체 코드 추출 후 CodeCheckService 호출
    }

    override fun update(e: AnActionEvent) {
        // 에디터가 열려 있을 때만 활성화
        val editor = e.getData(CommonDataKeys.EDITOR)
        e.presentation.isEnabled = editor != null
    }
}