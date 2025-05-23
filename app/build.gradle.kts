plugins {
    application
    java
}

group = "com.superwatch"
version = "0.1.5"

repositories {
    mavenCentral()
    maven {
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }
}

dependencies {
    // Dépendances de l'application
    implementation("org.spigotmc:spigot-api:1.21.1-R0.1-SNAPSHOT")
    implementation("com.googlecode.json-simple:json-simple:1.1.1")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")

    // Utilisation de JUnit Jupiter (JUnit 5) pour les tests
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))  // conforme à la version du POM Maven
    }
}

application {
    mainClass.set("com.superwatch.App")  // à modifier si la classe principale est différente
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}



tasks.register<Copy>("copyVersionFile") {
    from("/app/src/main/resources/version.properties")
    into("$buildDir/resources/main")
    doFirst {
        val propertiesFile = File("/app/src/main/resources/version.properties")
        propertiesFile.writeText("version=${project.version}")
    }
}

tasks.named("processResources") {
    dependsOn("copyVersionFile")
}





tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    archiveFileName.set("SuperWatch-v" + project.version + ".jar")
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    manifest {
        attributes["Main-Class"] = "com.superwatch.App"
        attributes["Implementation-Version"] = project.version
        attributes["Plugin-Version"] = project.version
        attributes["Plugin-ID"] = "SuperWatch"
        attributes["Plugin-Description"] = "My Minecraft Plugin"
        attributes["Plugin-Main"] = "com.superwatch.App"
    }
}

tasks.register("shadeJar", Jar::class) {
    archiveClassifier.set("all")
    from(sourceSets.main.get().output)
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}
