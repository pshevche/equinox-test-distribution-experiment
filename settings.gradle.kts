plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.7.0"
    id("com.gradle.enterprise") version "3.16.2"
}

gradleEnterprise {
    server = providers.systemProperty("develocity.server").getOrElse("https://scans.gradle.com")
}

rootProject.name = "equinox-test-distribution-experiment"
include("lib")
