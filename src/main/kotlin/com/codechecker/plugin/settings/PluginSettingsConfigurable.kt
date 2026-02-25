package com.codechecker.plugin.settings

import com.intellij.openapi.options.Configurable
import javax.swing.JComponent

/**
 * Settings → Tools → Code Quality Checker 진입점.
 *
 * plugin.xml에서 applicationConfigurable (parentId="tools")로 등록.
 * projectConfigurable이 아닌 이유: PluginSettings가 application 레벨 서비스이므로
 * 프로젝트와 무관하게 IDE 전역 설정으로 관리함.
 *
 * UI 레이아웃은 [PluginSettingsPanel]에 위임하여
 * Configurable 생명주기 코드와 UI 코드를 분리함.
 */
class PluginSettingsConfigurable : Configurable {

    private var panel: PluginSettingsPanel? = null

    override fun getDisplayName(): String = "Code Quality Checker"

    override fun createComponent(): JComponent {
        val newPanel = PluginSettingsPanel()
        panel = newPanel
        return newPanel.root
    }

    override fun isModified(): Boolean =
        panel?.isModified() ?: false

    override fun apply() {
        panel?.apply()
    }

    override fun reset() {
        panel?.reset()
    }

    override fun disposeUIResources() {
        panel = null
    }
}