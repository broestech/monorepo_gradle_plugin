package com.broeskamp.monorepo.gradle.plugin.android

import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property

open class AndroidExtension(project: Project) {
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
