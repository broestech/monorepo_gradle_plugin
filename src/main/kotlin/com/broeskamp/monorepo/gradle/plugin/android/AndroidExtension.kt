package com.broeskamp.monorepo.gradle.plugin.android

import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property
import org.jetbrains.kotlin.gradle.plugin.extraProperties

open class AndroidExtension(val project: Project) {
  fun setAndroidDefaultProperties() {
    project.extraProperties.set("android.useAndroidX", "true")
    project.extraProperties.set("android.nonTransitiveRClass", "true")
  }

  val applicationId: Property<String> = project.objects.property()
  val namespace: Property<String> = project.objects.property()
  val compileSdk: Property<Int> = project.objects.property()
  val minSdk: Property<Int> = project.objects.property()
  val targetSdk: Property<Int> = project.objects.property()
  val isMinifyEnabled: Property<Boolean> = project.objects.property()
  val kotlinCompilerExtensionVersion: Property<String> = project.objects.property()
  val androidExtension: Property<Boolean> = project.objects.property()

  init {
    namespace.convention("${project.group}.${project.name}")
    applicationId.convention(namespace)
    compileSdk.convention(33)
    minSdk.convention(24)
    targetSdk.convention(33)
    isMinifyEnabled.convention(false)
    androidExtension.convention(true)
    kotlinCompilerExtensionVersion.convention("1.4.3")
  }
}
