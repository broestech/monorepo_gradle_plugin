plugins {
  `kotlin-dsl`
  `maven-publish`
  signing
}

if (version == "unspecified") {
  version = "0.0.0-SNAPSHOT"
}
group = "com.broeskamp.monorepo.gradle.plugin"

val kotlinVersion: String by project
val quarkusVersion: String by project

dependencies {
  implementation("com.github.node-gradle:gradle-node-plugin:3.5.0")
  implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
  implementation("org.jetbrains.kotlin:kotlin-allopen:$kotlinVersion")
  implementation("io.quarkus:gradle-application-plugin:$quarkusVersion")
  implementation("org.jetbrains.gradle:package-search-gradle-plugins:1.5.2")
  implementation("me.qoomon:gradle-git-versioning-plugin:6.4.2")
  implementation("com.android.tools.build:gradle:7.4.2")
}

gradlePlugin {
  plugins {
    register("broestech-base") {
      id = "$group.base"
      implementationClass = "com.broeskamp.monorepo.gradle.plugin.base.BroestechBasePlugin"
    }
    register("broestech-vue-app-plugin") {
      id = "$group.vue-app"
      implementationClass = "com.broeskamp.monorepo.gradle.plugin.web.VueTsWebAppPlugin"
    }
    register("broestech-multiplatform") {
      id = "$group.multiplatform"
      implementationClass = "com.broeskamp.monorepo.gradle.plugin.mmp.MultiplatformPlugin"
    }
    register("broestech-quarkus") {
      id = "$group.quarkus"
      implementationClass = "com.broeskamp.monorepo.gradle.plugin.quarkus.QuarkusPlugin"
    }
    register("broestech-multiplatform-mobile-lib") {
      id = "$group.multiplatform-mobile-lib"
      implementationClass = "com.broeskamp.monorepo.gradle.plugin.mmp.MobileMultiplatformLibraryPlugin"
    }
    register("broestech-kotlin-android") {
      id = "$group.kotlin-android"
      implementationClass = "com.broeskamp.monorepo.gradle.plugin.kotlin.AndroidKotlinPlugin"
    }
  }
}

java {
  withJavadocJar()
  withSourcesJar()
}

tasks.withType<Javadoc> {
  (options as StandardJavadocDocletOptions).apply {
    addBooleanOption("html5", true)
  }
}

signing {
  val signingKey: String? by project
  val signingPassword: String? by project
  useInMemoryPgpKeys(signingKey, signingPassword)
  sign(publishing.publications)
}

publishing {
  publications.withType<MavenPublication> {
    pom {
      description.set("Gradle plugin configurations for mono-repositories with Kotlin, Quarkus, Vue & TypeScript.")
      url.set("https://github.com/broestech/monorepo_gradle_plugin")
      name.set("Broeskamp plugin")
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

afterEvaluate {
  publishing {
    publications.withType<MavenPublication> {
      pom {
        description.set("Gradle plugin configurations for mono-repositories with Kotlin, Quarkus, Vue & TypeScript.")
        name.set("Broeskamp plugin")
      }
    }
  }
}

gradle.taskGraph.whenReady {
  if (hasTask(":publishToMavenLocal")) {
    println("disable signing")
    tasks.withType<Sign>().configureEach {
      onlyIf("local publish") { false }
    }
  }
}
