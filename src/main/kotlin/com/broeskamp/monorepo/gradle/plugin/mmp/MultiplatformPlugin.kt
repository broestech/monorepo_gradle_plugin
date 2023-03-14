package com.broeskamp.monorepo.gradle.plugin.mmp

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper
import org.jetbrains.kotlin.gradle.targets.js.testing.karma.KotlinKarma
import org.jetbrains.kotlin.gradle.targets.jvm.tasks.KotlinJvmTest

class MultiplatformPlugin : Plugin<Project> {
  override fun apply(project: Project): Unit = project.run {

    apply<KotlinMultiplatformPluginWrapper>()

    val typescriptD = configurations.register("typescriptD") {
      isCanBeConsumed = true
      isCanBeResolved = false
    }

    configure<KotlinMultiplatformExtension> {
      jvm {
        // needed for quarkus plugin to work!!!docker
        withJava()
        testRuns["test"].executionTask.configure(KotlinJvmTest::useJUnitPlatform)
      }
      js {
        browser {
          commonWebpackConfig {
            sourceMaps = false
          }
          testTask {
            useKarma(KotlinKarma::useChromeHeadless)
          }
        }
        binaries.executable()
        compilations.all {
          compileTaskProvider.configure {
            kotlinOptions.apply {
              noStdlib = true
              sourceMap = false
            }
          }
        }
      }

      targets.all {
        compilations.all {
          compileTaskProvider.configure {
            kotlinOptions.freeCompilerArgs += listOf("-opt-in=kotlin.js.ExperimentalJsExport")
          }
        }
      }

      sourceSets {
        @Suppress("UNUSED_VARIABLE")
        val commonTest by getting {
          dependencies {
            implementation(kotlin("test"))
          }
        }
      }
    }

    val ignore = listOf("jsNpm", "jsTestNpm")
    configurations.all {
      if (ignore.contains(name)) {
        resolutionStrategy.deactivateDependencyLocking()
      }
    }

    artifacts {
      add(typescriptD.name, tasks.named("jsBrowserDistribution"))
    }
  }
}
