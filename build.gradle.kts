plugins {
  id("java-library")
  id("maven-publish")
  id("signing")
}

group = "ac.intave"
version = providers.gradleProperty("releaseVersion").getOrElse("0.0.1-SNAPSHOT")

repositories {
  mavenCentral()
}

dependencies {
  testImplementation(platform("org.junit:junit-bom:5.10.0"))
  testImplementation("org.junit.jupiter:junit-jupiter")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
  implementation("com.google.code.gson:gson:2.10.1")
  implementation("com.github.luben:zstd-jni:1.5.7-10")
}

tasks.test {
  useJUnitPlatform()
}

tasks.withType<JavaCompile>().configureEach {
  options.release.set(17)
}

java {
  withSourcesJar()
  withJavadocJar()
}

publishing {
  publications {
    create<MavenPublication>("mavenJava") {
      from(components["java"])

      artifactId = "samples"

      pom {
        name.set("Intave Samples")
        description.set("A library for serializing and deserializing Minecraft user events in a binary format.")
        url.set("https://github.com/intave/samples")

        licenses {
          license {
            name.set("PolyForm Perimeter License 1.0.0")
            url.set("https://polyformproject.org/licenses/perimeter/1.0.0/")
            distribution.set("repo")
          }
        }

        developers {
          developer {
            id.set("Jpx3")
            name.set("Richard Strunk")
            organization.set("Intave")
            organizationUrl.set("https://intave.ac")
          }
        }

        scm {
          connection.set(
            "scm:git:git://github.com/intave/samples.git"
          )
          developerConnection.set(
            "scm:git:ssh://github.com/intave/samples.git"
          )
          url.set("https://github.com/intave/samples")
        }
      }
    }
  }

  repositories {
    maven {
      name = "central"
      url = uri("https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/")
      credentials {
        username = providers.gradleProperty("centralUsername").orNull
        password = providers.gradleProperty("centralPassword").orNull
      }
    }
  }
}

signing {
  isRequired = !version.toString().endsWith("-SNAPSHOT")
  useInMemoryPgpKeys(
    providers.gradleProperty("signingKey").orNull,
    providers.gradleProperty("signingPassword").orNull
  )
  sign(publishing.publications["mavenJava"])
}
