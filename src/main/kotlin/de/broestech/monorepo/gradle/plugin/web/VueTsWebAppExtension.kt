package de.broestech.monorepo.gradle.plugin.web

import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property

open class VueTsWebAppExtension(project: Project) {
  val outputDir: DirectoryProperty = project.objects.directoryProperty()
  val nodeVersion: Property<String> = project.objects.property()

  init {
    outputDir.convention(project.layout.buildDirectory.dir("vite"))
    nodeVersion.convention("18.12.1")
  }
}
