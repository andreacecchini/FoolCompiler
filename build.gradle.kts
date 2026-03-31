import org.gradle.api.plugins.antlr.AntlrTask

data class GrammarConfig(
    val name: String
) {
    val sourceDir: String
        get() = name
    val generatedDir: String
        get() = name
    val pkg: String
        get() = name
    private val capitalizedName: String
        get() = name.replaceFirstChar { c -> c.uppercaseChar() }
    val generateTaskName: String
        get() = "generate${capitalizedName}Grammar"
    val cleanTaskName: String
        get() = "clean${capitalizedName}Generated"
}

plugins {
    java
    application
    antlr
    id("com.diffplug.spotless") version "6.25.0"
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(24))
    }
}

application {
    mainClass.set("compiler.Test")
}

spotless {
    java {
        target("src/**/*.java")
        // .aosp() for 4-spaces indentation
        googleJavaFormat("1.19.2").aosp()
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }
}

dependencies {
    antlr("org.antlr:antlr4:4.13.2")
    implementation("org.antlr:antlr4-runtime:4.13.2")
}

sourceSets {
    main {
        java {
            setSrcDirs(listOf("src", "gen"))
        }
    }
}

tasks.named<AntlrTask>("generateGrammarSource") {
    enabled = false
}

val grammarConfigs = listOf(
    GrammarConfig("compiler"),
    GrammarConfig("svm"),
    GrammarConfig("visualsvm")
)

val generateTaskNames = mutableListOf<String>()
val cleanTaskNames = mutableListOf<String>()

for (grammar in grammarConfigs) {
    generateTaskNames.add(grammar.generateTaskName)
    cleanTaskNames.add(grammar.cleanTaskName)

    tasks.register<Delete>(grammar.cleanTaskName) {
        delete(file("gen/${grammar.generatedDir}"))
    }

    tasks.register<AntlrTask>(grammar.generateTaskName) {
        source = fileTree("src/${grammar.sourceDir}") {
            include("**/*.g4")
        }
        dependsOn(grammar.cleanTaskName)
        outputDirectory = file("gen/${grammar.generatedDir}")
        arguments = listOf("-visitor", "-listener", "-package", grammar.pkg, "-Xexact-output-dir")
        maxHeapSize = "64m"
    }
}

tasks.compileJava {
    dependsOn(*generateTaskNames.toTypedArray())
}

tasks.clean {
    dependsOn(*cleanTaskNames.toTypedArray())
    delete(file("gen"))
}

tasks.register<JavaExec>("debug") {
    group = "application"
    description = "Run compiler and virtual machine in debug mode (visualsvm)."
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("compiler.Test")
    args("debug")
}

tasks.build {
    dependsOn("spotlessApply")
}

