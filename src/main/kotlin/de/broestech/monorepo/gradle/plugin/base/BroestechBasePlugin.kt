package de.broestech.monorepo.gradle.plugin.base

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
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.BufferedReader

class BroestechBasePlugin : Plugin<Project> {
  override fun apply(project: Project) = project.run {
    if (project != project.rootProject) {
      throw GradleException("BroestechBasePlugin (id: broestech-base) is only allowed @ root project")
    }

    apply<GitVersioningPlugin>()
    val rootExtension = extensions.create<BroestechBaseRootExtension>("broestechRoot")

    version = "0.0.0-SNAPSHOT"
    configure<GitVersioningPluginExtension> {
      apply {
        rev {
          describeTagPattern = "v(.+)"
          val process = ProcessBuilder("git", "describe", "--abbrev=0", "--tags")
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .start()
          val describeOutput =
            process.inputStream.bufferedReader().use(BufferedReader::readText).trim()
          version = (Regex(describeTagPattern).find(describeOutput)?.groupValues?.get(1)
            ?: "0.0.0") + "-\${commit.short}-SNAPSHOT"
        }
      }
    }

    allprojects {
      apply<BasePlugin>()

      val extension = extensions.create<BroestechBaseExtension>("broestech")
      extension.dockerRegistryUsername.convention(rootExtension.dockerRegistryUsername)
      extension.dockerRegistryPassword.convention(rootExtension.dockerRegistryPassword)
      extension.dockerRegistry.convention(rootExtension.dockerRegistry)

      repositories {
        mavenCentral()
        mavenLocal()
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

}
