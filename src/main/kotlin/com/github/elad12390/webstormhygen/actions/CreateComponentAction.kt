package com.github.elad12390.webstormhygen.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.ui.Messages

class CreateComponentAction(componentName: String?) : AnAction("Create a $componentName") {
    override fun actionPerformed(e: AnActionEvent) {
//        Messages.showInputDialog(e.project, "Test input", "WebstormHygen", Messages.getInformationIcon())
    }

}