import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.serialization") version "1.6.10"
    id("com.github.johnrengelman.shadow").version("7.1.2")
    id("com.google.devtools.ksp").version("1.6.10-1.0.3")
    id("org.flywaydb.flyway") version "8.5.1"
    id("nu.studer.jooq") version "7.1.1"
}

group = "org.snd"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("ch.qos.logback:logback-core:1.2.11")
    implementation("ch.qos.logback:logback-classic:1.2.11")
    implementation("org.slf4j:slf4j-api:1.7.36")
    implementation("io.github.microutils:kotlin-logging-jvm:2.1.21")

    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("com.squareup.okhttp3:okhttp-sse:4.9.3")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")

    implementation("com.squareup.moshi:moshi:1.13.0")
    ksp("com.squareup.moshi:moshi-kotlin-codegen:1.13.0")

    implementation("io.javalin:javalin:4.4.0")

    implementation("org.apache.commons:commons-text:1.9")
    implementation("com.charleskorn.kaml:kaml:0.43.0")
    implementation("io.github.resilience4j:resilience4j-ratelimiter:1.7.1")
    implementation("io.github.resilience4j:resilience4j-retry:1.7.1")
    implementation("org.jsoup:jsoup:1.14.3")

    implementation("org.flywaydb:flyway-core:8.5.4")
    implementation("org.jooq:jooq:3.16.5")
    implementation("org.xerial:sqlite-jdbc:3.36.0.3")
    jooqGenerator("org.xerial:sqlite-jdbc:3.36.0.3")
    implementation("com.zaxxer:HikariCP:5.0.1")
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs += listOf(
            "-Xopt-in=kotlin.ExperimentalStdlibApi"
        )
    }
}

tasks {
    shadowJar {
        manifest {
            attributes(Pair("Main-Class", "org.snd.ApplicationKt"))
        }
    }
}

sourceSets {
    // add a flyway sourceSet
    val flyway by creating {
        compileClasspath += sourceSets.main.get().compileClasspath
        runtimeClasspath += sourceSets.main.get().runtimeClasspath
    }
    // main sourceSet depends on the output of flyway sourceSet
    main {
        output.dir(flyway.output)
    }
}

val dbSqlite = mapOf(
    "url" to "jdbc:sqlite:${project.buildDir}/generated/flyway/database.sqlite"
)
val migrationDirsSqlite = listOf(
    "$projectDir/src/flyway/resources/db/migration/sqlite",
)
flyway {
    url = dbSqlite["url"]
    locations = arrayOf("classpath:db/migration/sqlite")
}
tasks.flywayMigrate {
    // in order to include the Java migrations, flywayClasses must be run before flywayMigrate
    dependsOn("flywayClasses")
    migrationDirsSqlite.forEach { inputs.dir(it) }
    outputs.dir("${project.buildDir}/generated/flyway")
    doFirst {
        delete(outputs.files)
        mkdir("${project.buildDir}/generated/flyway")
    }
    mixed = true
}

jooq {
    version.set("3.16.5")
    configurations {
        create("main") {
            jooqConfiguration.apply {
                logging = org.jooq.meta.jaxb.Logging.WARN
                jdbc.apply {
                    driver = "org.sqlite.JDBC"
                    url = dbSqlite["url"]
                }
                generator.apply {
                    database.apply {
                        name = "org.jooq.meta.sqlite.SQLiteDatabase"
                    }
                    target.apply {
                        packageName = "org.snd.jooq"
                    }
                }
            }
        }
    }
}

tasks.named<nu.studer.gradle.jooq.JooqGenerate>("generateJooq") {
    migrationDirsSqlite.forEach { inputs.dir(it) }
    allInputsDeclared.set(true)
    dependsOn("flywayMigrate")
}
