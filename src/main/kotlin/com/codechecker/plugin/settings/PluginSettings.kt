package com.codechecker.plugin.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service

@Service(Service.Level.APP)
@State(
    name = "CodeCheckerSettings",
    storages = [Storage("CodeCheckerPlugin.xml")]
)
class PluginSettings : PersistentStateComponent<PluginSettings.State> {

    data class State(
        var serverUrl: String = "http://localhost:3000",
        var readTimeoutSeconds: Int = 900,
        var outputFormat: String = "json",
        var useMockMode: Boolean = false
    )

    private var state = State()

    override fun getState(): State = state

    override fun loadState(state: State) {
        this.state = state
    }

    var serverUrl: String
        get() = state.serverUrl
        set(value) { state.serverUrl = value }

    var readTimeoutSeconds: Int
        get() = state.readTimeoutSeconds
        set(value) { state.readTimeoutSeconds = value }

    var outputFormat: String
        get() = state.outputFormat
        set(value) { state.outputFormat = value }

    var useMockMode: Boolean
        get() = state.useMockMode
        set(value) { state.useMockMode = value }

    companion object {
        fun getInstance(): PluginSettings = service()
    }
}