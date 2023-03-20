package com.broeskamp.monorepo.gradle.plugin.mmp

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.extraProperties

open class BroestechMultiplatformExtension(val project: Project) {
  fun setDefaultProperties() {
    project.extraProperties.set("kotlin.mpp.enableCInteropCommonization", "true")
    project.extraProperties.set("kotlin.mpp.androidSourceSetLayoutVersion", "2")
  }
}
