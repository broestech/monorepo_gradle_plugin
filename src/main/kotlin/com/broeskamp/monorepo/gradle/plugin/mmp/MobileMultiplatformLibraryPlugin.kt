package com.broeskamp.monorepo.gradle.plugin.mmp

import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.broeskamp.monorepo.gradle.plugin.android.AndroidExtension
import com.broeskamp.monorepo.gradle.plugin.base.BroestechBaseRootExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper

//import org.jetbrains.kotlin.gradle.plugin.cocoapods.KotlinCocoapodsPlugin
//import org.jetbrains.kotlin.gradle.plugin.cocoapods.KotlinCocoapodsPlugin.Companion.COCOAPODS_EXTENSION_NAME

class MobileMultiplatformLibraryPlugin : Plugin<Project> {
  override fun apply(target: Project): Unit = target.run {

    val rootExtension = rootProject.the<BroestechBaseRootExtension>()
    val androidExtension = extensions.create<AndroidExtension>("broestechAndroid")
//        val iOSExtension = extensions.create<iOSExtension>("broestechiOS")
    val mmpExtension = extensions.create<BroestechMultiplatformExtension>("broestechMmp")

    androidExtension.setAndroidDefaultProperties()
    mmpExtension.setDefaultProperties()

    apply<KotlinMultiplatformPluginWrapper>()

    apply<LibraryPlugin>()
//        apply<KotlinCocoapodsPlugin>()

    configure<KotlinMultiplatformExtension> {
      android {
        compilations.all {
          kotlinOptions {
            jvmTarget = rootExtension.javaVersion.get().toString()
          }
        }
      }

//            iosX64()
//            iosArm64()
//            iosSimulatorArm64()

      @Suppress("UNUSED_VARIABLE", "DuplicatedCode")
      sourceSets {
        val commonMain by getting
        val commonTest by getting {
          dependencies {
            implementation(kotlin("test"))
          }
        }
        val androidMain by getting
        val androidUnitTest by getting
//                val iosX64Main by getting
//                val iosArm64Main by getting
//                val iosSimulatorArm64Main by getting
//                val iosMain by creating {
//                    dependsOn(commonMain)
//                    iosX64Main.dependsOn(this)
//                    iosArm64Main.dependsOn(this)
//                    iosSimulatorArm64Main.dependsOn(this)
//                }
//                val iosX64Test by getting
//                val iosArm64Test by getting
//                val iosSimulatorArm64Test by getting
//                val iosTest by creating {
//                    dependsOn(commonTest)
//                    iosX64Test.dependsOn(this)
//                    iosArm64Test.dependsOn(this)
//                    iosSimulatorArm64Test.dependsOn(this)
//                }
      }

//            ((this as ExtensionAware).extensions[COCOAPODS_EXTENSION_NAME] as CocoapodsExtension).apply {
//                summary = iOSExtension.podSummary.getOrElse("Some description for the Shared Module")
//                homepage = iOSExtension.podHomepage.getOrElse("Link to the Shared Module homepage")
//                version = project.version.toString()
//                ios.deploymentTarget = iOSExtension.deploymentTarget.get()
//                podfile = layout.projectDirectory.file("../iosApp/Podfile").asFile
//                framework {
//                    baseName = "${project.rootProject.name}${project.path.replace(":", "-")}"
//                    println("IOS: $baseName")
//                }
//            }
      //    cocoapods {
//        summary = "Some description for the Shared Module"
//        homepage = "Link to the Shared Module homepage"
//        version = "1.0"
//        ios.deploymentTarget = "14.1"
//        podfile = project.file("../iosApp/Podfile")
//        framework {
//            baseName = "middleware"
//        }
//    }
    }

    configure<LibraryExtension> {
      namespace = androidExtension.namespace.get()
      compileSdk = androidExtension.compileSdk.get()
      compileOptions {
        this as com.android.build.gradle.internal.CompileOptions
        sourceCompatibility = rootExtension.javaVersion.get()
        targetCompatibility = rootExtension.javaVersion.get()
        incremental = true
        encoding = "UTF-8"
      }
      defaultConfig {
        minSdk = androidExtension.minSdk.get()
        @Suppress("DEPRECATION", "UnstableApiUsage")
        targetSdk = androidExtension.targetSdk.get()
      }
    }

  }
}
