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

tasks.named<Test>("test") {
    useJUnitPlatform()
    distribution {
        enabled = true
        requirements.addAll("demo")
    }
}
