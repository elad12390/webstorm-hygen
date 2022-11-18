package com.github.elad12390.webstormhygen.groups

import com.github.elad12390.webstormhygen.actions.CreateComponentAction
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.*

class HygenDefaultActionGroup : ActionGroup() {
    override fun update(e: AnActionEvent) {
        val isProject = e.getData(CommonDataKeys.PROJECT) !== null
        e.presentation.isEnabled = isProject
        e.presentation.icon = AllIcons.Actions.AddFile
    }

    override fun getChildren(e: AnActionEvent?): Array<AnAction> {
        return arrayOf(
            CreateComponentAction("Test")
        )
    }
}