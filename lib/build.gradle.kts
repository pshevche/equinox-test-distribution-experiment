import com.gradle.enterprise.gradleplugin.testdistribution.internal.TestDistributionExtensionInternal
import io.github.pshevche.equinox.PackageEquinoxWorkspace

plugins {
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    testAnnotationProcessor("io.soabase.record-builder:record-builder-processor:40")
    testCompileOnly("io.soabase.record-builder:record-builder-core:40")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("com.gradle:develocity-testing-annotations:2.0.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

val packageEquinoxWorkspace = tasks.register<PackageEquinoxWorkspace>("packageWorkspace") {
    equinoxConfigFile = file("../equinox-configuration/config.ini")
    outputDirectory = layout.buildDirectory.dir("equinoxWorkspace")
}

tasks.named<Test>("test") {
    useJUnitPlatform()

    val workspaceDirectory = packageEquinoxWorkspace.map { it.outputDirectory }
    inputs.files(workspaceDirectory)

    distribution {
        enabled = true
        requirements.addAll("demo")

        this as TestDistributionExtensionInternal
        processedResources {
            create("equinoxConfig") {
                files.from(workspaceDirectory.map { it.file("equinox-configuration/config.ini") })
            }
            create("equinoxBundlesInfo") {
                files.from(workspaceDirectory.map { it.file("equinox-configuration/org.eclipse.equinox.simpleconfigurator/bundles.info") })
            }
        }
    }
}
