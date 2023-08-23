plugins {
    id("com.diffplug.spotless") version libs.versions.spotless 
    id("com.revolut.jooq-docker") version libs.versions.jooq.docker.plugin 
    id("io.spring.dependency-management") version libs.versions.spring.dependency.management 
    id("jacoco")
    id("org.springframework.boot") version libs.versions.springboot 
    id("cat-fact-service.code-metrics")
    id("cat-fact-service.java-conventions")
    id("cat-fact-service.publishing-conventions")
    kotlin("jvm") version libs.versions.kotlin 
    kotlin("plugin.spring") version libs.versions.kotlin 
}

repositories {
    mavenLocal()
    mavenCentral()
    maven { url = uri("https://packages.confluent.io/maven/") }
    maven {
        url = uri("https://maven.pkg.github.com/ForkingGeniuses/cat-fact-service")
        credentials {
            username = findProperty("gpr.user")?.toString() ?: System.getenv("GITHUB_ACTOR")
            password = findProperty("gpr.key")?.toString() ?: System.getenv("GITHUB_TOKEN")
        }
    }
    maven {
        url = uri("https://maven.pkg.github.com/ForkingGeniuses/cat-fact-client")
        credentials {
            username = project.findProperty("gpr.user")?.toString() ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.key")?.toString() ?: System.getenv("PACKAGES_READ_TOKEN")
        }
    }
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.jvmTarget.get()))
        target { JavaLanguageVersion.of(libs.versions.jvmTarget.get()) }
    }
}

dependencies {

    implementation("com.yonatankarp:cat-fact-client:0.2.0")

    // Spring Boot
    implementation(libs.bundles.springboot.all)

    // Kotlin
    implementation(libs.bundles.kotlin.all)

    // Persistence
    runtimeOnly(libs.postgresql)
    jdbc(libs.postgresql) // Required to generate JOOQ models
    implementation(libs.bundles.persistence.support.all)

    // Documentation
    implementation(libs.springdoc.openapi.starter)

    // Tests
    testImplementation(libs.mockk.core)
    testImplementation(libs.mockk.spring)

    testImplementation(libs.testcontainers.jupiter)
    testImplementation(libs.testcontainers.postgres)
}

dependencyManagement {
    imports {
        mavenBom("org.testcontainers:testcontainers-bom:${libs.versions.testcontainers.get()}")
    }
}


tasks {
    getByName<Jar>("jar") {
        enabled = false
    }

    build {
        finalizedBy(spotlessApply)
    }

    withType<Test> {
        useJUnitPlatform()
        finalizedBy(spotlessApply)
        finalizedBy(jacocoTestReport)
    }

    jacoco {
        toolVersion = libs.versions.jacoco.get()
    }

    jacocoTestReport {
        reports {
            xml.required.set(true)
            html.required.set(true)
        }
    }
}

val tasksDependencies = mapOf(
    "spotlessKotlin" to listOf("compileKotlin", "compileTestKotlin", "test", "jacocoTestReport")
)

tasksDependencies.forEach { (taskName, dependencies) ->
    tasks.findByName(taskName)?.dependsOn(dependencies)
}
