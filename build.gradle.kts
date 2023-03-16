plugins {
  `kotlin-dsl`
  signing
  id("com.gradle.plugin-publish") version "1.1.0"
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
  website.set("https://github.com/broestech/monorepo_gradle_plugin")
  vcsUrl.set("https://github.com/broestech/monorepo_gradle_plugin.git")
  plugins {
    register("broestech-base") {
      id = "broestech-base"
      implementationClass = "com.broeskamp.monorepo.gradle.plugin.base.BroestechBasePlugin"
    }
    register("broestech-vue-app-plugin") {
      id = "broestech-vue-app"
      implementationClass = "com.broeskamp.monorepo.gradle.plugin.web.VueTsWebAppPlugin"
    }
    register("broestech-multiplatform") {
      id = "broestech-multiplatform"
      implementationClass = "com.broeskamp.monorepo.gradle.plugin.mmp.MultiplatformPlugin"
    }
    register("broestech-quarkus") {
      id = "broestech-quarkus"
      implementationClass = "com.broeskamp.monorepo.gradle.plugin.quarkus.QuarkusPlugin"
    }
    register("broestech-multiplatform-mobile-lib") {
      id = "broestech-multiplatform-mobile-lib"
      implementationClass = "com.broeskamp.monorepo.gradle.plugin.mmp.MobileMultiplatformLibraryPlugin"
    }
    register("broestech-kotlin-android") {
      id = "broestech-kotlin-android"
      implementationClass = "com.broeskamp.monorepo.gradle.plugin.kotlin.AndroidKotlinPlugin"
    }
  }
}

tasks.withType<Javadoc> {
  (options as StandardJavadocDocletOptions).apply {
    addBooleanOption("html5", true)
  }
}


publishing {
  repositories {
    mavenLocal()
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
