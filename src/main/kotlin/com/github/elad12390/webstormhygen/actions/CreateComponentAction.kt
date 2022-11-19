package com.github.elad12390.webstormhygen.actions

import com.github.elad12390.webstormhygen.services.ProjectStateService
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.KillableColoredProcessHandler
import com.intellij.execution.process.ScriptRunnerUtil
import com.intellij.icons.AllIcons
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.impl.LoadTextUtil
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.terminal.actions.TerminalActionUtil


val DEFINITION_REGEX_PATTERN = "\\s*(?<DEFINITION>\\{([\\n\\s]*(type|message|name|default):.*)+[\\n\\s]*})".toRegex()
val DEFINITION_VALUES_REGEX_PATTERN =
    "(type: [\"'`](?<TYPE>.*)[\"'`])|(name: [\"'`](?<NAME>.*)[\"'`])|(message: [\"'`](?<MESSAGE>.*)[\"'`])|(default: [\"'`](?<DEFAULT>.*)[\"'`])".toRegex()

class CreateComponentAction(
    private val actionFolderName: String,
    private val templateFolder: VirtualFile
) : AnAction("$actionFolderName ${templateFolder.name}") {
    override fun actionPerformed(e: AnActionEvent) {
        if (e.project === null) return

        val folderRightClicked = (e.dataContext.getData("VCS_VIRTUAL_FILES") as Iterable<VirtualFile>).first()

        templateFolder.findChild(actionFolderName)?.let { actionFolder ->
            val definitionFile = actionFolder.findChild("prompt.js") ?: actionFolder.findChild("index.js")
            if (definitionFile !== null) {
                calculateArgumentMapping(
                    e.project!!,
                    LoadTextUtil.loadText(definitionFile),
                    folderRightClicked
                )?.let { argsMapping ->
                    // run generate in cli
                    ProgressManager.getInstance().run(
                        object : Task.Backgroundable(e.project!!, "Title") {
                            override fun run(indicator: ProgressIndicator) {
                                indicator.fraction = .1
                                indicator.text = "Generating files with npx..."

                                val executableName = if (SystemInfo.isWindows) {
                                    "npx.cmd"
                                } else {
                                    "npx"
                                }
                                val output = ScriptRunnerUtil.getProcessOutput(
                                    GeneralCommandLine(
                                        executableName,
                                        "hygen",
                                        templateFolder.name,
                                        actionFolderName,
                                        *argsMapping.map { listOf("--${it.key}", "${it.value}") }.flatten().toTypedArray()
                                    ).apply {
                                        withWorkDirectory(e.project!!.basePath)
                                        withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.SYSTEM)
                                    }
                                )

                                indicator.fraction = .8
                                indicator.text = "Indexing files"
                                project.guessProjectDir()?.refresh(true, true)

                                indicator.fraction = 1.0
                                indicator.text = "Finished"
                                Notifications.Bus.notify(
                                    Notification(
                                        "Hygen Generator Group",
                                        "Hygen Generator Finished,\n$output",
                                        NotificationType.INFORMATION
                                    )
                                )
                            }
                        }
                    )
                }
            }
        }
    }

    private fun calculateArgumentMapping(
        project: Project,
        contents: CharSequence,
        folderRightClicked: VirtualFile
    ): MutableMap<String, String?>? {
        val matches = DEFINITION_REGEX_PATTERN.findAll(contents)
        val argumentMapping = mutableMapOf<String, String?>()

        matches.forEach {
            it.groups["DEFINITION"]?.value?.let { argText ->
                val argumentDefinition = getArgumentDefinition(argText)
                if (listOf("path", "directory", "dir").contains(argumentDefinition["NAME"]?.lowercase())) {
                    argumentDefinition["DEFAULT"] =
                        folderRightClicked.path.substring(ProjectStateService.getInstance(project).generateFolderPath.length)
                }
                val inputValueOrNull = getInput(
                    project,
                    argumentDefinition["NAME"],
                    argumentDefinition["TYPE"],
                    argumentDefinition["MESSAGE"],
                    argumentDefinition["DEFAULT"]
                )
                if (inputValueOrNull === null) return@calculateArgumentMapping null

                argumentMapping[argumentDefinition["NAME"] ?: "unknown"] = inputValueOrNull
            }

        }

        return argumentMapping
    }

    private fun getArgumentDefinition(argText: String): MutableMap<String, String?> {
        val argumentDefinition = mutableMapOf<String, String?>()
        DEFINITION_VALUES_REGEX_PATTERN.findAll(argText).toList().forEach { argDefinition ->
            val message = argDefinition.groups["MESSAGE"]?.value
            if (message !== null) argumentDefinition["MESSAGE"] = message

            val name = argDefinition.groups["NAME"]?.value
            if (name !== null) argumentDefinition["NAME"] = name

            val type = argDefinition.groups["TYPE"]?.value
            if (type !== null) argumentDefinition["TYPE"] = type

            val default = argDefinition.groups["DEFAULT"]?.value
            if (default !== null) argumentDefinition["DEFAULT"] = default
        }
        return argumentDefinition
    }

    private fun getInput(project: Project, name: String?, type: String?, message: String?, default: String?): String? {
        return when (type) {
            "input" -> {
                return Messages.showInputDialog(
                    project,
                    "[$name]: $message",
                    "$actionFolderName ${templateFolder.name}",
                    AllIcons.Actions.Commit,
                    default ?: "",
                    null
                )
            }
            "confirm" -> {
                val response = Messages.showYesNoCancelDialog(
                    project,
                    "[$name]: $message",
                    "$actionFolderName ${templateFolder.name}",
                    "True",
                    "False",
                    "Cancel",
                    AllIcons.Actions.Commit
                )
                return when (response) {
                    Messages.CANCEL -> null
                    Messages.OK -> "true"
                    else -> "false"
                }
            }
            else -> null
        }
    }
}