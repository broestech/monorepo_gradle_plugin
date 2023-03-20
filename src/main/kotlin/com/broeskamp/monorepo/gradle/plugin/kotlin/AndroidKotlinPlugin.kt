package com.broeskamp.monorepo.gradle.plugin.kotlin

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import com.broeskamp.monorepo.gradle.plugin.android.AndroidExtension
import com.broeskamp.monorepo.gradle.plugin.base.BroestechBaseRootExtension
import com.broeskamp.monorepo.gradle.plugin.mmp.BroestechMultiplatformExtension
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

    androidExtension.setAndroidDefaultProperties()
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
        //FIXME generate from version number
        versionCode = 1
        versionName = project.version.toString()
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
}
