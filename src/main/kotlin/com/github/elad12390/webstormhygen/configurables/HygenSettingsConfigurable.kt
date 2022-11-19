package com.github.elad12390.webstormhygen.configurables

import com.github.elad12390.webstormhygen.services.ProjectStateService
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.layout.panel
import javax.swing.JComponent
import com.intellij.openapi.project.guessCurrentProject

class HygenSettingsConfigurable(val project: Project) : Configurable {
    private var settingsPanel: DialogPanel? = null

    private val pathTextField
        get() = (settingsPanel?.getComponent(1) as? TextFieldWithBrowseButton)

    private val generateFolderPath
        get() = (settingsPanel?.getComponent(3) as? TextFieldWithBrowseButton)

    override fun getPreferredFocusedComponent(): JComponent? = settingsPanel?.preferredFocusedComponent

    override fun createComponent(): JComponent {
        settingsPanel = panel {
            row("Hygen Folder path") {
                textFieldWithBrowseButton(
                    fileChooserDescriptor = FileChooserDescriptor(
                        false,
                        true,
                        false,
                        false,
                        false,
                        false
                    )
                )
            }
            row("Where to generate components? (base directory)") {
                textFieldWithBrowseButton(
                    fileChooserDescriptor = FileChooserDescriptor(
                        false,
                        true,
                        false,
                        false,
                        false,
                        false
                    )
                )
            }
        }
        return settingsPanel!!
    }

    override fun isModified(): Boolean {
        return pathTextField?.let { ProjectStateService.getInstance(project).folderPath != it.text } ?: false ||
                generateFolderPath?.let { ProjectStateService.getInstance(project).generateFolderPath != it.text } ?: false
    }

    override fun apply() {
        val state = ProjectStateService.getInstance(project)
        pathTextField?.let { state.folderPath = it.text }
        generateFolderPath?.let { state.generateFolderPath = it.text }
    }

    override fun reset() {
        val state = ProjectStateService.getInstance(project)
        pathTextField?.let { it.text = state.folderPath }
        generateFolderPath?.let { it.text = state.generateFolderPath }
    }

    override fun disposeUIResources() {
        settingsPanel = null
    }

    override fun getDisplayName(): String = "Hygen Configuration"
}