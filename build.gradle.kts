plugins {
  `kotlin-dsl`
  `maven-publish`
  signing
}

// require
repositories {
  mavenCentral()
  gradlePluginPortal()
  mavenLocal()
}

group = "de.broestech.monorepo.gradle.plugin"

val kotlinVersion: String by project
val quarkusVersion: String by project

dependencies {
  implementation("com.github.node-gradle:gradle-node-plugin:3.5.0")
  implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
  implementation("org.jetbrains.kotlin:kotlin-allopen:$kotlinVersion")
  implementation("io.quarkus:gradle-application-plugin:$quarkusVersion")
  implementation("org.jetbrains.gradle:package-search-gradle-plugins:1.5.2")
  implementation("me.qoomon:gradle-git-versioning-plugin:6.4.2")
}

gradlePlugin {
  plugins {
    register("broestech-base") {
      id = "broestech-base"
      implementationClass = "de.broestech.monorepo.gradle.plugin.base.BroestechBasePlugin"
    }
    register("broestech-vue-app-plugin") {
      id = "broestech-vue-app"
      implementationClass = "de.broestech.monorepo.gradle.plugin.web.VueTsWebAppPlugin"
    }
    register("broestech-multiplatform") {
      id = "broestech-multiplatform"
      implementationClass = "de.broestech.monorepo.gradle.plugin.mmp.MultiplatformPlugin"
    }
    register("broestech-quarkus") {
      id = "broestech-quarkus"
      implementationClass = "de.broestech.monorepo.gradle.plugin.quarkus.QuarkusPlugin"
    }
  }
}

publishing {
  publications {
    create<MavenPublication>("mavenJava") {
      artifactId = "monorepo_gradle_plugin"
      from(components["java"])
      pom {
        name.set(project.group.toString() + ":" + artifactId)
        description.set("Gradle plugin configurations for mono-repositories with Kotlin, Quarkus, Vue & TypeScript.")
        url.set("https://github.com/broestech/monorepo_gradle_plugin")
        packaging = "jar"
        licenses {
          license {
            name.set("MIT License")
            url.set("https://github.com/broestech/monorepo_gradle_plugin/blob/development/LICENSE.md")
            distribution.set("repository")
          }
        }
        scm {
          connection.set("scm:git:git://github.com/broestech/monorepo_gradle_plugin.git")
          url.set("https://github.com/broestech/monorepo_gradle_plugin")
          developerConnection.set("scm:git:ssh://github.com/broestech/monorepo_gradle_plugin.git")
        }
        developers {
          developer {
            name.set("Simon Gehring")
            email.set("gehring.simon@broeskamp.com")
            organization.set("Bröskamp Consulting GmbH")
            organizationUrl.set("https://broeskamp.com")
          }
          developer {
            name.set("Daniel Zwicker")
            email.set("zwicker.daniel@broeskamp.com")
            organization.set("Bröskamp Consulting GmbH")
            organizationUrl.set("https://broeskamp.com")
          }
          developer {
            name.set("Denis Neumann")
            email.set("neumann.denis@broeskamp.com")
            organization.set("Bröskamp Consulting GmbH")
            organizationUrl.set("https://broeskamp.com")
          }
        }
      }
    }
  }
  repositories {
    maven {
      name = "MavenCentral"
      credentials {
        username =
          System.getenv("OSSRH_USERNAME") ?: project.properties["ossrhUsername"] as String? ?: ""
        password =
          System.getenv("OSSRH_PASSWORD") ?: project.properties["ossrhPassword"] as String? ?: ""

        if (username!!.isEmpty()) {
          project.logger.error("Username for maven central is empty")
        }
        if (password!!.isEmpty()) {
          project.logger.error("Password for maven central is empty")
        }
      }
      url = if (project.version.toString().contains("SNAPSHOT"))
        uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
      else
        uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
    }
  }
}


signing {
  val signingKey: String? by project
  val signingPassword: String? by project
  useInMemoryPgpKeys(signingKey, signingPassword)
  sign(publishing.publications["mavenJava"])
}