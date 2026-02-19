import org.gradle.api.plugins.antlr.AntlrTask

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

tasks.register<AntlrTask>("generateCompilerGrammar") {
    source = fileTree("src/compiler") {
        include("**/*.g4")
    }
    dependsOn("cleanCompilerGenerated")
    outputDirectory = file("gen/compiler")
    arguments = listOf("-visitor", "-listener", "-package", "compiler", "-Xexact-output-dir")
    maxHeapSize = "64m"
}

tasks.register<AntlrTask>("generateSvmGrammar") {
    source = fileTree("src/svm") {
        include("**/*.g4")
    }
    dependsOn("cleanSvmGenerated")
    outputDirectory = file("gen/svm")
    arguments = listOf("-visitor", "-listener", "-package", "svm", "-Xexact-output-dir")
    maxHeapSize = "64m"
}

tasks.compileJava {
    dependsOn("generateCompilerGrammar", "generateSvmGrammar")
}

tasks.register<Delete>("cleanCompilerGenerated") {
    delete(file("gen/compiler"))
}

tasks.register<Delete>("cleanSvmGenerated") {
    delete(file("gen/svm"))
}

tasks.clean {
    dependsOn("cleanCompilerGenerated", "cleanSvmGenerated")
    delete(file("gen"))
}

tasks.build {
    dependsOn("spotlessApply")
}

