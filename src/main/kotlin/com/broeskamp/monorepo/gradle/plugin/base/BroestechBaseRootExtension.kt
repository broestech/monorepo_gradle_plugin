package com.broeskamp.monorepo.gradle.plugin.base

import org.gradle.api.JavaVersion
import org.gradle.api.JavaVersion.VERSION_17
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property

open class BroestechBaseRootExtension(project: Project) {
  val javaVersion: Property<JavaVersion> = project.objects.property()
  val group: Property<String> = project.objects.property()
  val dockerRegistryUsername: Property<String> = project.objects.property()
  val dockerRegistryPassword: Property<String> = project.objects.property()
  val dockerRegistry: Property<String> = project.objects.property()

  init {
    javaVersion.convention(VERSION_17)
    //if env prop is null the property is unset!
    dockerRegistryUsername.convention(
        System.getenv("DOCKER_REGISTRY_USERNAME") ?: "AWS"
    )
    //if env prop is null the property is unset!
    dockerRegistryPassword.convention(
        System.getenv("DOCKER_REGISTRY_PASSWORD") ?: ""
    )
    dockerRegistry.convention(
        System.getenv("DOCKER_REGISTRY") ?: ""
    )
  }
}
