package com.github.elad12390.webstormhygen.configurables

import com.github.elad12390.webstormhygen.services.ProjectStateService
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.layout.panel
import javax.swing.JComponent

class HygenSettingsConfigurable : Configurable {
    private var settingsPanel: DialogPanel? = null

    private val pathTextField
        get() = (settingsPanel?.getComponent(1) as? TextFieldWithBrowseButton)

    override fun getPreferredFocusedComponent(): JComponent? = settingsPanel?.preferredFocusedComponent

    override fun createComponent(): JComponent {
        settingsPanel = panel {
            row("Hygen Folder path") {
                textFieldWithBrowseButton(fileChooserDescriptor = FileChooserDescriptor(false, true, false, false, false, false)) {
                    it.path
                }
            }
        }
        return settingsPanel!!
    }

    override fun isModified() = pathTextField?.let {
        ProjectStateService.instance.folderPath != it.text
    } ?: false

    override fun apply() {
        pathTextField?.let {
            val state = ProjectStateService.instance
            state.folderPath = it.text
        }
    }

    override fun reset() {
        pathTextField?.let {
            val state = ProjectStateService.instance
            it.text = state.folderPath
        }
    }

    override fun disposeUIResources() {
        settingsPanel = null
    }

    override fun getDisplayName(): String = "Hygen Configuration"
}