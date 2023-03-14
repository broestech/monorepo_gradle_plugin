package com.broeskamp.monorepo.gradle.plugin.base

import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property

open class BroestechBaseExtension(project: Project) {
  val dockerRegistryUsername: Property<String> = project.objects.property()
  val dockerRegistryPassword: Property<String> = project.objects.property()
  val dockerRegistry: Property<String> = project.objects.property()

}
