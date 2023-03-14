package de.broestech.monorepo.gradle.plugin.quarkus

import de.broestech.monorepo.gradle.plugin.base.BroestechBaseExtension
import io.quarkus.gradle.QuarkusPlugin
import io.quarkus.gradle.extension.QuarkusPluginExtension
import io.quarkus.gradle.tasks.QuarkusBuild
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Delete
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.internal.os.OperatingSystem
import org.gradle.kotlin.dsl.*
import org.jetbrains.gradle.plugins.docker.DockerExtension
import org.jetbrains.gradle.plugins.docker.DockerImage
import org.jetbrains.gradle.plugins.docker.DockerPlugin
import org.jetbrains.kotlin.allopen.gradle.AllOpenExtension
import org.jetbrains.kotlin.allopen.gradle.AllOpenGradleSubplugin
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper

class QuarkusPlugin : Plugin<Project> {

  override fun apply(project: Project): Unit = project.run {

    val extension = project.extensions.create<QuarkusExtension>("broesQuarkus")

    apply<KotlinPluginWrapper>()
    apply<AllOpenGradleSubplugin>()
    apply<QuarkusPlugin>()
    apply<DockerPlugin>()

    configure<AllOpenExtension> {
      annotation("javax.ws.rs.Path")
      annotation("javax.enterprise.context.ApplicationScoped")
      annotation("io.quarkus.test.junit.QuarkusTest")
      annotation("javax.persistence.Entity")
    }

    val implementation: Configuration = configurations["implementation"]
    dependencies {
      implementation(enforcedPlatform("io.quarkus.platform:quarkus-bom:2.16.2.Final"))
    }

    val webArchives = configurations.register("webArchives") {
      isCanBeConsumed = false
      isCanBeResolved = true
    }

    val extractWeb = tasks.register<Copy>("extractWeb") {
      inputs.files(webArchives)
      webArchives.get().files.forEach {
        from(zipTree(it!!))
      }
      into(layout.projectDirectory.dir("src/main/resources/META-INF/resources/"))
    }
    tasks.named("processResources").configure {
      dependsOn(extractWeb)
    }
    tasks.named("processResources").configure {
      dependsOn(extractWeb)
    }
    tasks.named<Delete>("clean").configure {
      delete(layout.projectDirectory.dir("src/main/resources/META-INF/resources"))
    }

    val appName = "${rootProject.name.replace("_", "-")}${project.path.replace(":", "-")}-app"
    val dockerTaskName = "docker${appName.toCamelCase().capitalized()}Build"
    tasks.register("assembleDocker") {
      dependsOn(dockerTaskName)
    }
    val quarkusBuild =
      tasks.named<QuarkusBuild>(QuarkusPlugin.QUARKUS_BUILD_TASK_NAME)

    configure<QuarkusPluginExtension> {
      finalName.set(appName)
    }

    val baseExtension = the<BroestechBaseExtension>()

    configure<DockerExtension> {
      registries {
        create("aws-registry") {
          username = baseExtension.dockerRegistryUsername.get()
          password = baseExtension.dockerRegistryPassword.get()
          url = baseExtension.dockerRegistry.get()
          imageNamePrefix = baseExtension.dockerRegistry.get()
        }
      }
    }

    configure<DockerExtension> {
      @Suppress("UNCHECKED_CAST")
      (extensions["images"] as NamedDomainObjectContainer<DockerImage>)
        .register(appName) {
          files {
            from(extension.dockerfile) {
              rename { "Dockerfile" }
            }
            from(provider {
              quarkusBuild.get().nativeRunner
            })
          }
          taskProviders {
            dockerPrepareTaskProvider {
              dependsOn(quarkusBuild)
            }
          }
        }
    }

    gradle.taskGraph.whenReady {
      project.extra.set(
        QuarkusPlugin.QUARKUS_PACKAGE_TYPE,
        "native"
      )
      if (hasTask("$path:$dockerTaskName")
        && OperatingSystem.current().familyName != "linux"
      ) {
        println("")
        println("Enable Quarkus Container Build")
        println("")

        project.extra.set(
          "quarkus.native.container-build",
          "true"
        )
        project.extra.set(
          "quarkus.native.container-runtime",
          "docker"
        )
        project.extra.set(
          "quarkus.native.native-image-xmx",
          extension.quarkusBuilderXmx
        )
        project.extra.set(
          "quarkus.native.builder-image",
          extension.quarkusBuilderImage
        )
      }
    }
  }

  private fun String.toCamelCase() =
    replace(Regex("[^a-zA-Z\\d](\\w)")) { it.value.last().uppercaseChar().toString() }
}