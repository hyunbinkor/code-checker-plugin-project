package com.codechecker.plugin.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@State(
    name = "com.codechecker.plugin.settings.PluginSettings",
    storages = [Storage("CodeQualityChecker.xml")]
)
class PluginSettings : PersistentStateComponent<PluginSettings.State> {

    data class State(
        var serverUrl: String = "http://localhost:3000",
        var readTimeoutSeconds: Int = 900,
        var outputFormat: String = "json"
    )

    private var state = State()

    override fun getState(): State = state

    override fun loadState(state: State) {
        this.state = state
    }

    // 편의 접근자
    var serverUrl: String
        get() = state.serverUrl
        set(value) { state.serverUrl = value }

    var readTimeoutSeconds: Int
        get() = state.readTimeoutSeconds
        set(value) { state.readTimeoutSeconds = value }

    var outputFormat: String
        get() = state.outputFormat
        set(value) { state.outputFormat = value }

    companion object {
        fun getInstance(): PluginSettings =
            ApplicationManager.getApplication().getService(PluginSettings::class.java)
    }
}