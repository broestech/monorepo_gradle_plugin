package com.broeskamp.monorepo.gradle.plugin.web

import com.github.gradle.node.NodeExtension
import com.github.gradle.node.NodePlugin
import com.github.gradle.node.npm.task.NpmTask
import com.github.gradle.node.task.NodeSetupTask
import com.github.gradle.node.util.PlatformHelper
import com.github.gradle.node.variant.VariantComputer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.tasks.bundling.Zip
import org.gradle.kotlin.dsl.*

class VueTsWebAppPlugin : Plugin<Project> {
  override fun apply(project: Project): Unit = project.run {

    val extension = project.extensions.create<VueTsWebAppExtension>("broesVue")

    configureNodePlugin(extension)

    val webArchives = configurations.register("webArchives") {
      isCanBeConsumed = true
      isCanBeResolved = false
    }

    val kotlinJs = configurations.register("kotlinJs") {
      isCanBeConsumed = false
      isCanBeResolved = true
    }

    val npmInstall = tasks.named("npmInstall")
    val buildVue = tasks.register<NpmTask>("buildVue") {
      dependsOn(npmInstall)
      npmCommand.set(listOf("run", "build"))
      execOverrides {
        standardOutput = System.out
      }
      inputs.files(
          fileTree("src/main/webapp")
      )
          .withPropertyName("sourceFiles")
          .withPathSensitivity(org.gradle.api.tasks.PathSensitivity.RELATIVE)
      outputs.files(
          fileTree(extension.outputDir)
      )
          .withPropertyName("outputFiles")
    }
    val zip = tasks.register<Zip>("zip") {
      dependsOn(buildVue)
      from(extension.outputDir)
    }

    tasks.named(BasePlugin.ASSEMBLE_TASK_NAME).configure {
      dependsOn(zip)
    }

    artifacts {
      add(webArchives.name, zip)
    }
    npmInstall.configure {
      dependsOn(kotlinJs)
    }

  }

  private fun Project.configureNodePlugin(extension: VueTsWebAppExtension) {
    apply<NodePlugin>()
    configure<NodeExtension> {
      version.set(extension.nodeVersion)
      download.set(true)
      workDir.set(rootProject.layout.projectDirectory.dir(".gradle/nodejs"))
    }
    val nodeConfiguration = configurations.register("nodeConfiguration") {
      isTransitive = false
    }
    afterEvaluate {
      the<NodeExtension>().run {
        if (download.get()) {
          dependencies {
            add(
                nodeConfiguration.name,
                VariantComputer(PlatformHelper.INSTANCE)
                    .computeNodeArchiveDependency(this@run)
            )
          }
          tasks.named<NodeSetupTask>(NodeSetupTask.NAME) {
            nodeArchiveFile.set(
                project.layout.file(
                    provider {
                      nodeConfiguration.get().singleFile
                    }
                )
            )
          }
        }
      }
    }
  }
}
