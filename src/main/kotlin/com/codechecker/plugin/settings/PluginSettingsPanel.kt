package com.codechecker.plugin.settings

import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import java.awt.BorderLayout
import javax.swing.JPanel

/**
 * PluginSettingsConfigurable에서 사용하는 설정 UI 패널.
 * TODO: 서버 URL 입력, 타임아웃 입력, 출력 형식 콤보박스, 연결 테스트 버튼 구현
 */
class PluginSettingsPanel {

    val root: JPanel = JBPanel<JBPanel<*>>(BorderLayout()).apply {
        // TODO: 실제 설정 UI 컴포넌트 추가
        add(JBLabel("설정 UI 준비 중..."), BorderLayout.CENTER)
    }
}