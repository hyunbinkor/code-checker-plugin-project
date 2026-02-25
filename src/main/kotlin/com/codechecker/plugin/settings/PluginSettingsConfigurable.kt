package com.codechecker.plugin.settings

import com.intellij.openapi.options.Configurable
import javax.swing.JComponent

class PluginSettingsConfigurable : Configurable {

    private var settingsPanel: PluginSettingsPanel? = null

    override fun getDisplayName(): String = "Code Quality Checker"

    override fun createComponent(): JComponent {
        // TODO: PluginSettingsPanel 구현 후 연결
        settingsPanel = PluginSettingsPanel()
        return settingsPanel!!.root
    }

    override fun isModified(): Boolean {
        // TODO: 현재 UI 값이 저장된 값과 다른지 비교
        return false
    }

    override fun apply() {
        // TODO: UI 값을 PluginSettings에 저장
    }

    override fun reset() {
        // TODO: PluginSettings 값을 UI에 반영
    }

    override fun disposeUIResources() {
        settingsPanel = null
    }
}