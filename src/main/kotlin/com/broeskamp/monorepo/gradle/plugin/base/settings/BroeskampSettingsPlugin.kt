@file:Suppress("ConvertLambdaToReference")

package com.broeskamp.monorepo.gradle.plugin.base.settings

import com.broeskamp.monorepo.gradle.plugin.BuildConfig
import com.broeskamp.monorepo.gradle.plugin.base.BroestechBasePlugin
import io.quarkus.gradle.QuarkusPlugin
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.api.internal.catalog.DefaultVersionCatalogBuilder
import org.gradle.api.internal.catalog.VersionModel
import org.gradle.api.logging.Logging
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType
import org.gradle.plugin.management.PluginManagementSpec
import org.gradle.plugin.management.PluginRequest
import org.gradle.plugin.management.PluginResolveDetails
import org.gradle.plugin.management.internal.DefaultPluginResolutionStrategy


@Suppress("unused")
open class BroeskampSettingsPlugin : Plugin<Settings> {

  companion object {
    const val PROJECT_EXTENSION_NAME: String = "BROESKAMP"
    private const val KOTLIN_ID = "org.jetbrains.kotlin."
    private const val ANDROID_ID = "com.android."
    private val replaceVersionRegEx = "^\\$(.*)$".toRegex()
  }

  private var disableMessages: Boolean = false

  private val logger = Logging.getLogger(this.javaClass)

  @Suppress("ConvertLambdaToReference")
  override fun apply(settings: Settings): Unit = settings.run {

    gradle.beforeProject {
      val versionConstraints: Map<String, VersionModel> = settings.versionConstraints("libs")
      pluginManagement.forcePluginVersion(versionConstraints)
    }
    gradle.projectsLoaded {
      allprojects {
        if (this == rootProject) {
          apply<BroestechBasePlugin>()
        }
        plugins.withType<MavenPublishPlugin> {
          afterEvaluate {
            the<PublishingExtension>().publications.all {
              if (this is MavenPublication) {
                versionMapping {
                  allVariants {
                    fromResolutionResult()
                  }
                }
              }
            }
          }
        }
      }
    }

    dependencyResolutionManagement {
      versionCatalogs {
        @Suppress("DuplicatedCode")
        create("libs") {
          library(
            "kotlinx-serialization",
            "org.jetbrains.kotlinx",
            "kotlinx-serialization-json"
          )
            .versionRef("kotlinx-serialization")

          // Ktor
          library(
            "ktor",
            "io.ktor",
            "ktor-client-core"
          )
            .versionRef("ktor")
          library(
            "ktor-serialization",
            "io.ktor",
            "ktor-serialization-kotlinx-json"
          )
            .versionRef("ktor")
          library(
            "ktor-logging",
            "io.ktor",
            "ktor-client-logging"
          )
            .versionRef("ktor")
          library(
            "ktor-logging-napier",
            "io.github.aakira",
            "napier"
          )
            .versionRef("ktor-logging-napier")
          library(
            "ktor-negotiation",
            "io.ktor",
            "ktor-client-content-negotiation"
          )
            .versionRef("ktor")
          library(
            "kotlinx-coroutines",
            "org.jetbrains.kotlinx",
            "kotlinx-coroutines-core"
          )
            .versionRef("kotlinx-coroutines")

          library(
            "ktor-okhttp",
            "io.ktor",
            "ktor-client-okhttp"
          )
            .versionRef("ktor")

          bundle(
            "ktor",
            listOf(
              "ktor",
              "ktor-logging",
              "ktor-logging-napier",
              "ktor-negotiation",
              "ktor-serialization",
              "kotlinx-coroutines"
            )
          )

          library(
            "androidx-compose-ui",
            "androidx.compose.ui",
            "ui"
          )
            .versionRef("androidx.compose.ui")
          library(
            "androidx-compose-ui-tooling",
            "androidx.compose.ui",
            "ui-tooling"
          )
            .versionRef("androidx.compose.ui")
          library(
            "androidx-compose-ui-tooling-preview",
            "androidx.compose.ui",
            "ui-tooling-preview"
          )
            .versionRef("androidx.compose.ui")

          bundle(
            "androidx-compose-ui",
            listOf(
              "androidx-compose-ui",
              "androidx-compose-ui-tooling",
              "androidx-compose-ui-tooling-preview",
            )
          )

          library(
            "androidx-compose-foundation",
            "androidx.compose.foundation",
            "foundation"
          )
            .versionRef("androidx.compose.foundation")

          library(
            "androidx-compose-material",
            "androidx.compose.material",
            "material"
          )
            .versionRef("androidx.compose.material")

          library(
            "androidx-activity-compose",
            "androidx.activity",
            "activity-compose"
          )
            .versionRef("androidx.activity.compose")
          library(
            "androidx-activity-ktx",
            "androidx.activity",
            "activity-ktx"
          )
            .versionRef("androidx.activity.compose")

          bundle(
            "androidx-activity",
            listOf(
              "androidx-activity-compose",
              "androidx-activity-ktx"
            )
          )

        }
      }
    }
  }

  private fun PluginManagementSpec.forcePluginVersion(versionConstraints: Map<String, VersionModel>) {
    val lockedField = DefaultPluginResolutionStrategy::class.java.getDeclaredField("locked")
    lockedField.isAccessible = true
    resolutionStrategy {
      this as DefaultPluginResolutionStrategy
      lockedField.set(this, false)
      eachPlugin {
        if (requested.id.id.startsWith(BuildConfig.PLUGIN_ID_PREFIX)) {
          forceVersion("broestech", BuildConfig.VERSION)
          return@eachPlugin
        }
        if (requested.idOrNamespaceStartsWith(KOTLIN_ID)) {
          forceVersion("kotlin", versionConstraints)
          return@eachPlugin
        }
        if (requested.idOrNamespaceStartsWith(QuarkusPlugin.ID)) {
          forceVersion("quarkus", versionConstraints)
          return@eachPlugin
        }
        if (
          requested.idOrNamespaceStartsWith(ANDROID_ID)
          || requested.id.id == "android"
          || requested.id.id == "android-library"
          || requested.id.id == "android-reporting"
        ) {
          forceVersion("android", versionConstraints)
          return@eachPlugin
        }
        requested.module?.run {
          if (group.startsWith(KOTLIN_ID) || group.startsWith(QuarkusPlugin.ID))
            useVersion(version)
        }
      }
      lockedField.set(this, true)
    }
  }

  private fun PluginResolveDetails.forceVersion(
    versionName: String,
    versionConstraints: Map<String, VersionModel>
  ) {
    if (!versionConstraints.containsKey(versionName)) {
      throw GradleException("Missing version for '$versionName'! Please define in settings.gradle[.kts] 'dependencyResolutionManagement.versionCatalogs' block or inside 'gradle/libs.versions.toml' file!")
    }
    forceVersion(versionName, versionConstraints[versionName]!!.version.toString())
  }

  private fun PluginResolveDetails.forceVersion(
    versionName: String,
    version: String
  ) {
    logger.info("BROESKAMP: set $versionName plugin version to '$version'")
    useVersion(version)
  }

  private fun PluginRequest.idOrNamespaceStartsWith(prefix: String): Boolean =
    id.id.startsWith(prefix)
        || id.namespace?.startsWith(prefix) ?: false

  @Suppress("UNCHECKED_CAST")
  private fun Settings.versionConstraints(versionCatalogsName: String): Map<String, VersionModel> =
    DefaultVersionCatalogBuilder::class.java.getDeclaredField("versionConstraints").let {
      it.isAccessible = true
      val value = it.get(dependencyResolutionManagement.versionCatalogs[versionCatalogsName])
      return value as Map<String, VersionModel>
    }

}
