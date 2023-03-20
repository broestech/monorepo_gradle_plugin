package com.broeskamp.monorepo.gradle.plugin.util

import org.gradle.api.GradleException
import org.gradle.api.artifacts.VersionCatalogsExtension
import java.util.*
import kotlin.jvm.optionals.getOrNull

@Suppress("MemberVisibilityCanBePrivate")
fun VersionCatalogsExtension.version(versionCatalogName: String, versionName: String): String =
  find(versionCatalogName).orElseThrow {
    throw GradleException("Missing version catalog '$versionCatalogName! Please define in settings.gradle[.kts] 'dependencyResolutionManagement.versionCatalogs' block or inside 'gradle/libs.versions.toml' file!")
  }.run {
    findVersion(versionName).orElseThrow {
      throw GradleException("Missing version '$versionName' in catalog '$versionCatalogName! Please define in settings.gradle[.kts] 'dependencyResolutionManagement.versionCatalogs' block or inside 'gradle/libs.versions.toml' file!")
    }.toString()
  }

fun VersionCatalogsExtension.version(versionName: String): String = version("libs", versionName)

@Suppress("MemberVisibilityCanBePrivate")
fun VersionCatalogsExtension.findVersion(versionCatalogName: String, versionName: String): Optional<String> =
  find(versionCatalogName)
    .getOrNull()
    ?.findVersion(versionName)
    ?.map { it.toString() }
    ?: Optional.empty()

fun VersionCatalogsExtension.findVersion(versionName: String): Optional<String> = findVersion("libs", versionName)
