package com.github.elad12390.webstormhygen.services

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@State(
    name = "com.github.elad12390.webstormhygen.services.ProjectStateService",
    storages = [Storage("HygenPluginSettings.xml")]
)
class ProjectStateService : PersistentStateComponent<ProjectStateService> {
    var folderPath: String = ""

    companion object {
        val instance: ProjectStateService
            get() = ApplicationManager.getApplication().getService(ProjectStateService::class.java)
    }

    override fun getState() = this

    override fun loadState(state: ProjectStateService) {
        XmlSerializerUtil.copyBean(state, this)
    }
}