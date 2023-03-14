package de.broestech.monorepo.gradle.plugin.quarkus

import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property

open class QuarkusExtension(project: Project) {
  private val quarkusBuilderImageVersion: Property<String> = project.objects.property()
  val quarkusBuilderImage: Property<String> = project.objects.property()
  val quarkusBuilderXmx: Property<String> = project.objects.property()
  val dockerfile: RegularFileProperty = project.objects.fileProperty()

  init {
    quarkusBuilderImageVersion.convention("22.3.1.0-Final-java17-arm64")
    quarkusBuilderImageVersion.convention(project.provider {
      "quay.io/quarkus/ubi-quarkus-mandrel-builder-image:${quarkusBuilderImageVersion.get()}"
    })
    quarkusBuilderXmx.convention("6g")
    dockerfile.convention(project.layout.projectDirectory.file("src/main/docker/Dockerfile.native-micro"))

  }
}
