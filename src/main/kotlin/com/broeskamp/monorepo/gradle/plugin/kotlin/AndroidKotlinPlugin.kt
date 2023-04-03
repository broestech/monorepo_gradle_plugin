package com.broeskamp.monorepo.gradle.plugin.kotlin

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import com.broeskamp.monorepo.gradle.plugin.android.AndroidExtension
import com.broeskamp.monorepo.gradle.plugin.base.BroestechBaseRootExtension
import com.broeskamp.monorepo.gradle.plugin.mmp.BroestechMultiplatformExtension
import com.broeskamp.monorepo.gradle.plugin.util.Version
import me.qoomon.gradle.gitversioning.GitVersioningPluginExtension
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.the
import org.jetbrains.kotlin.gradle.plugin.KotlinAndroidPluginWrapper

class AndroidKotlinPlugin : Plugin<Project> {
  override fun apply(project: Project): Unit = project.run {

    val androidExtension = extensions.create<AndroidExtension>("broestechAndroid")
    val mmpExtension = extensions.create<BroestechMultiplatformExtension>("broestechMmp")
    val rootExtension = rootProject.the<BroestechBaseRootExtension>()

    mmpExtension.setDefaultProperties()

    apply<AppPlugin>()
    apply<KotlinAndroidPluginWrapper>()

    @Suppress("UnstableApiUsage")
    configure<BaseAppModuleExtension> {
      namespace = androidExtension.namespace.get()
      compileSdk = androidExtension.compileSdk.get()
      compileOptions {
        sourceCompatibility = rootExtension.javaVersion.get()
        targetCompatibility = rootExtension.javaVersion.get()
      }
      defaultConfig {
        applicationId = androidExtension.applicationId.get()
        minSdk = androidExtension.minSdk.get()
        targetSdk = androidExtension.targetSdk.get()
        versionCode = project.buildVersionCode()
        versionName = project.version.toString()

        tasks.register("androidVersions") {
          println("Project version: $version")
          println("Project version code: $versionCode")
        }

      }
      buildFeatures {
        compose = androidExtension.androidExtension.get()
      }
      composeOptions {
        kotlinCompilerExtensionVersion =
          androidExtension.kotlinCompilerExtensionVersion.get()
      }
      packagingOptions {
        resources {
          excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
      }
      buildTypes {
        getByName("release") {
          isMinifyEnabled = androidExtension.isMinifyEnabled.get()
        }
      }
    }
  }

  protected fun Project.buildVersionCode(): Int = if (project.hasProperty("android.version.code")) {
    project.property("android.version.code").toString().toInt()
  } else {
    val versionObject = Version.parse("$version")
    var versionCode = versionObject.major * 100_000_000
    versionCode += versionObject.minor * 100_000
    versionCode += versionObject.patch * 100
    if (versionObject.qualifiers.isNotEmpty()) {
      /**
      if version has qualifiers SemVers defines it as 'smaller' than the same version without any qualifiers!
      e.g. 0.1.0-beta-1 is smaller than 0.1.0
      to take this in account we downgrade the version without qualifiers to the next smaller path version (-100)
      after this we add the git distance since last tag, so it will be grater than the 'last' patch version!
      e.g.: 1.1.2-rc.1 must be smaller than 1.1.2! So it will become: 100100200 - 100 = 100100100 but this is
      the versioncode for 1.1.1. so we add the git distance (e.g. 4 commits ahead) and we get 100100104

      this is not without conflicts but should be ok for 99% of the cases
       **/
      versionCode -= 100
      versionCode += rootProject.the<GitVersioningPluginExtension>()
        .globalFormatPlaceholderMap["describe.distance"]!!.get().toInt()
    }
    if (versionCode < 1) {
      throw GradleException(
        "android.defaultConfig.versionCode is set to $versionCode, but it should be a positive integer."
            + "\nSet the first version tag e.g. v0.0.1!"
      )
    }
    versionCode
  }
}
