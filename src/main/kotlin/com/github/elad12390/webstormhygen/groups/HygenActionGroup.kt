package com.github.elad12390.webstormhygen.groups

import com.github.elad12390.webstormhygen.actions.CreateComponentAction
import com.github.elad12390.webstormhygen.services.ProjectStateService
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.containers.map2Array
import java.io.File

class HygenActionGroup : ActionGroup() {
    override fun update(e: AnActionEvent) {
        e.presentation.icon = AllIcons.Actions.AddFile

        if (e.getData(CommonDataKeys.PROJECT) === null) {
            e.presentation.isEnabled = false
            return
        }

        val selectedFiles = (e.dataContext.getData("VCS_VIRTUAL_FILES") as? Iterable<VirtualFile>)?.toList()
        if (selectedFiles == null) {
            e.presentation.isEnabled = false
            return
        }

        if (selectedFiles.isEmpty() || (selectedFiles.size > 1) || !selectedFiles.first().isDirectory) {
            e.presentation.isEnabled = false
            return
        }

        e.presentation.isEnabled = true
    }

    override fun getChildren(e: AnActionEvent?): Array<AnAction> {
        return ProjectStateService.instance.folderPath.let {
            val folder = VfsUtil.findFile(File(it).toPath(), true)
            folder?.let { folderVirtualFile ->
                val actions = groupDirectoryByActions(folderVirtualFile)
                actions.map { action ->
                    object : ActionGroup(action.key, true) {
                        override fun getChildren(e: AnActionEvent?): Array<AnAction> {
                            return action.value.map2Array { templateVirtualFile -> CreateComponentAction(action.key, templateVirtualFile) }
                        }
                    }
                }.toTypedArray()
                //folderVirtualFile.children.map2Array { templateVirtualFile -> CreateComponentAction(templateVirtualFile) }
            } ?: let {
                Messages.showErrorDialog(getEventProject(e), "Could not find hygen folder path!", "Hygen Generator Error")
                arrayOf()
            }
        }
    }

    private fun groupDirectoryByActions(directory: VirtualFile): MutableMap<String, MutableSet<VirtualFile>> {
        val actions = mutableMapOf<String, MutableSet<VirtualFile>>()
        for (template in directory.children) {
            if (template.children.isEmpty()) {
                continue
            }

            for (action in template.children) {
                val actionMapping = actions.getOrDefault(action.name, mutableSetOf())
                actionMapping.add(template)
                actions[action.name] = actionMapping
            }
        }
        return actions
    }
}