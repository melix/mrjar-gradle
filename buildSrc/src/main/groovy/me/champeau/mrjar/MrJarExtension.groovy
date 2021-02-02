package me.champeau.mrjar

import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService

import javax.inject.Inject

abstract class MrJarExtension {
    private final JavaPluginExtension javaPluginExtension
    private final TaskContainer tasks
    private final SourceSetContainer sourceSets
    private final DependencyHandler dependencies
    private final ObjectFactory objects
    private final JavaToolchainService javaToolchains

    @Inject
    MrJarExtension(JavaPluginExtension javaPluginExtension,
                   SourceSetContainer sourceSets,
                   JavaToolchainService javaToolchains,
                   TaskContainer tasks,
                   DependencyHandler dependencies,
                   ObjectFactory objectFactory) {
        this.javaPluginExtension = javaPluginExtension
        this.sourceSets = sourceSets
        this.javaToolchains = javaToolchains
        this.tasks = tasks
        this.dependencies = dependencies
        this.objects = objectFactory
        defaultLanguageVersion(8)
    }

    void configureMrJar(int version, SourceSet languageSourceSet) {
        tasks.named('jar', Jar) {
            it.into("META-INF/versions/$version") {
                it.from languageSourceSet.output
            }
            it.manifest.attributes(
                    'Multi-Release': 'true',
                    'Main-Class': 'com.acme.JdkSpecific'
            )
        }
    }

    void addLanguageVersion(int version) {
        // First, let's create a source set for this language version
        def langSourceSet = sourceSets.create("java${version}") {
            it.java.srcDir("src/main/java${version}")
        }
        def testSourceSet = sourceSets.create("java${version}Test") {
            it.java.srcDir("src/test/java${version}")
        }
        def sharedTestSourceSet = sourceSets.test

        // This is only necessary because in real life, we have dependencies between classes
        // and what you're likely to want to do, is to provide a JDK 9 specific class, which depends on common
        // classes of the main source set. In other words, you want to override some specific classes, but they
        // still have dependencies onto other classes.
        // We want to avoid recompiling all those classes, so we're just saying that the Java 9 specific classes
        // "depend on" the main ones.
        def mainClasses = objects.fileCollection().from(sourceSets.main.output.classesDirs)
        dependencies."java${version}Implementation"(mainClasses)

        // then configure the compile task so that it uses the expected Gradle version
        def targetCompiler = javaToolchains.compilerFor {
            it.languageVersion = JavaLanguageVersion.of(version)
        }
        tasks.named("compileJava${version}Java") {
            it.javaCompiler = targetCompiler
        }

        // let's make sure to create a "test" task
        def targetLauncher = javaToolchains.launcherFor {
            it.languageVersion = JavaLanguageVersion.of(version)
        }

        // so here's the deal. MRjars are JARs! Which means that to execute tests, we need
        // the JAR on classpath, not just classes + resources as Gradle usually does
        def testClasspath = objects.fileCollection().from(tasks.named('jar', Jar))

        def testTask = tasks.register("testJava${version}", Test) {
            it.group = "verification"
            it.javaLauncher.set(targetLauncher)
            it.testClassesDirs = testSourceSet.output.classesDirs + sharedTestSourceSet.output.classesDirs
            testClasspath.from(
                    testSourceSet.runtimeClasspath,
                    sharedTestSourceSet.runtimeClasspath
            )
            it.classpath = testClasspath
        }

        tasks.named('check') {
            dependsOn(testTask)
        }

        configureMrJar(version, langSourceSet)
    }

    void defaultLanguageVersion(int version) {
        javaPluginExtension.toolchain.languageVersion.set(JavaLanguageVersion.of(version))
    }
}