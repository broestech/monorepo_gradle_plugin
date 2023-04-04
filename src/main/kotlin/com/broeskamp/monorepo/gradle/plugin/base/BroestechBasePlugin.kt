package com.broeskamp.monorepo.gradle.plugin.base

import me.qoomon.gradle.gitversioning.GitVersioningPlugin
import me.qoomon.gradle.gitversioning.GitVersioningPluginExtension
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformJvmPlugin
import org.jetbrains.kotlin.gradle.plugin.extraProperties
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/**
 * The BroestechBasePlugin is a Gradle plugin that applies basic configuration for all projects in a multi-project build.
 * This plugin applies the GitVersioningPlugin and creates extensions for the root project and all subprojects.
 * @constructor Creates a new instance of the BroestechBasePlugin.
 */
class BroestechBasePlugin : Plugin<Project> {

  /**
   * Applies the BroestechBasePlugin plugin to the specified Project.
   * @param project The Project to apply the plugin to.
   * @throws GradleException if the plugin is not applied to the root project.
   */
  override fun apply(project: Project) = project.run {
    if (project != project.rootProject) {
      throw GradleException("BroestechBasePlugin (id: broestech-base) is only allowed @ root project")
    }

    apply<GitVersioningPlugin>()
    val rootExtension = extensions.create<BroestechBaseRootExtension>("broestechRoot")

    gradle.startParameter.isContinueOnFailure = true
    defaultTasks("assemble")

    version = "0.0.0-SNAPSHOT"
    val nextVersionPattern =
      "\${describe.tag.version.major}.\${describe.tag.version.minor}.\${describe.tag.version.patch.next}"
    val branchNamePattern = "\${ref.name}-SNAPSHOT"
    configure<GitVersioningPluginExtension> {
      apply {
        describeTagPattern = "v(?<version>.+)"
        refs {
          considerTagsOnBranches = true
          tag("v(?<version>.+)") {
            version = "\${ref.version}"
          }
          branch("master") {
            version = "$nextVersionPattern-\${commit.short}-SNAPSHOT"
          }
          branch("feature/(?<name>.+)") {
            version = "$nextVersionPattern-\${commit.short}-f-$branchNamePattern"
          }
          branch("bugfix/(.+)") {
            version = "$nextVersionPattern-\${commit.short}-b-$branchNamePattern"
          }
          branch("hotfix/(.+)") {
            version = "$nextVersionPattern-\${commit.short}-h-$branchNamePattern"
          }
        }

        // optional fallback configuration in case of no matching ref configuration
        rev {
          version = "SNAPSHOT"
        }
      }
    }

    allprojects {
      apply<BasePlugin>()

      extraProperties.set(KOTLIN_CODE_STYLE, KOTLIN_CODE_STYLE_VALUE)
      extraProperties.set(KOTLIN_JS_COMPILER, KOTLIN_JS_COMPILER_VALUE)

      val extension = extensions.create<BroestechBaseExtension>("broestech")
      extension.dockerRegistryUsername.convention(rootExtension.dockerRegistryUsername)
      extension.dockerRegistryPassword.convention(rootExtension.dockerRegistryPassword)
      extension.dockerRegistry.convention(rootExtension.dockerRegistry)

      repositories {
        mavenLocal()
        mavenCentral()
        google()
      }

      plugins.withType<JavaBasePlugin> {
        configure<JavaPluginExtension> {
          sourceCompatibility = rootExtension.javaVersion.get()
          targetCompatibility = rootExtension.javaVersion.get()
        }
      }
      plugins.withType<KotlinPlatformJvmPlugin> {
        configure<KotlinJvmProjectExtension> {
          target {
            compilations.all {
              kotlinOptions.jvmTarget = rootExtension.javaVersion.get().toString()
              kotlinOptions.javaParameters = true
            }
          }
        }
      }
      tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions {
          jvmTarget = rootExtension.javaVersion.get().toString()
          javaParameters = true
        }
      }

      tasks.register("resolveAndLockAll") {
        notCompatibleWithConfigurationCache("Filters configurations at execution time")
        doFirst {
          require(gradle.startParameter.isWriteDependencyLocks)
        }
        doLast {
          configurations.filter {
            // Add any custom filtering on the configurations to be resolved
            it.isCanBeResolved
          }.forEach(Configuration::resolve)
        }
      }

      afterEvaluate {
        group = rootExtension.group.get()
      }
    }
  }

  companion object {
    const val KOTLIN_CODE_STYLE = "kotlin.code.style"
    const val KOTLIN_CODE_STYLE_VALUE = "official"

    const val KOTLIN_JS_COMPILER = "kotlin.js.compiler"
    const val KOTLIN_JS_COMPILER_VALUE = "ir"
  }
}
