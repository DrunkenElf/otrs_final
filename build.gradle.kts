import org.gradle.jvm.tasks.Jar

val ktorVersion: String by project
val kotlinVersion: String by project
val logbackVersion: String by project
val exposedVersion: String by project

plugins {
    application
    id("java-library")
    id("java")
    kotlin("jvm") version "1.5.30"
}

group "internship"
version "0.0.1"
project.version = version


application {
    //mainClassName = "io.ktor.server.netty.EngineMain"
    mainClass.set("io.ktor.server.netty.EngineMain")
    //applicationDefaultJvmArgs = listOf("-Dio.ktor.development=true")
}

repositories {
    mavenLocal()
    jcenter()
    mavenCentral()
    maven { url = uri("https://kotlin.bintray.com/ktor") }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-host-common:$ktorVersion")
    implementation("io.ktor:ktor-gson:$ktorVersion")

    implementation("org.jsoup:jsoup:1.13.1")

    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")



    implementation("org.apache.commons:commons-text:1.9")

    testImplementation("io.ktor:ktor-server-tests:$ktorVersion")
}

kotlin.sourceSets["main"].kotlin.srcDirs("src")

sourceSets["main"].resources.srcDirs("resources")


val fatJar = task("fatJar", type = Jar::class) {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    manifest {
        attributes["Implementation-Title"] = "Ktor - Vue Fat Jar"
        attributes["Implementation-Version"] = "1"
        attributes["Main-Class"] = "io.ktor.server.netty.EngineMain"
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    with(tasks.jar.get() as CopySpec)
}

val yarnBuild = task<Exec>("yarnBuild") {
    workingDir = file("src-vue")
    commandLine("yarn", "build")
}

val copyDistFolder = tasks.register<Copy>("copyDistFolder") {
    from(file("src-vue/dist"))
    into(file("resources/dist"))
}

var env = "production"

tasks.processResources {
    outputs.upToDateWhen { false }
    filesMatching("*.conf") {
        expand(
            "KTOR_ENV" to "production",
            //"KTOR_ENV" to "dev",
            "KTOR_PORT" to "81",
            "KTOR_MODULE" to "",
            "KTOR_AUTORELOAD" to "false",
            "KTOR_HOST" to "10.90.138.10"
        )
       /* when (env) {
            "development" -> {
                expand(
                    "KTOR_ENV" to "dev",
                    "KTOR_PORT" to "8081",
                    "KTOR_MODULE" to "build",
                    "KTOR_AUTORELOAD" to "true"
                )
            }
            "production" -> {
                expand(
                    "KTOR_ENV" to "production",
                    "KTOR_PORT" to "80",
                    "KTOR_MODULE" to "",
                    "KTOR_AUTORELOAD" to "false"
                )
            }
        }*/
    }
}

val setDev = tasks.register("setDev") {
    env = "development"
}

tasks {
    "run" {
        dependsOn(setDev)
    }
    "build" {
        dependsOn(fatJar)
        doLast {
            copy {
                delete("bundle")
                from(fatJar)
                into(file("bundle"))
            }
        }
    }
    "fatJar" {

        //dependsOn(copyDistFolder)
    }
    "copyDistFolder" {
        dependsOn(yarnBuild)
    }
}
