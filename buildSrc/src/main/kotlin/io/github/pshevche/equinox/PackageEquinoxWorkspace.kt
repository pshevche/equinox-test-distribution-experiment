package io.github.pshevche.equinox

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import kotlin.io.path.appendText
import kotlin.io.path.exists

abstract class PackageEquinoxWorkspace : DefaultTask() {

    @get:InputFile
    abstract val equinoxConfigFile: RegularFileProperty

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    private lateinit var packagedConfigDir: Path
    private lateinit var packagedFrameworkBundlesDir: Path
    private lateinit var packagedExternalBundlesDir: Path

    @TaskAction
    fun packageWorkspace() {
        setupPackagedWorkspaceFolders()
        val packagedConfigFile = packagedConfigDir.resolve("config.ini")
        packagedConfigFile.toFile().writeText("")
        equinoxConfigFile.get().asFile.forEachLine {
            if (it.startsWith("osgi.bundles=")) {
                val newOsgiBundlesValue = packageOsgiBundles(it)
                packagedConfigFile.appendText("${newOsgiBundlesValue}\n")
            } else if (it.startsWith("osgi.framework=")) {
                val newOsgiFrameworkValue = packageOsgiFramework(it)
                packagedConfigFile.appendText("${newOsgiFrameworkValue}\n")
            } else if (it.startsWith("org.eclipse.equinox.simpleconfigurator.configUrl=")) {
                val newEquinoxConfigurationUrl = packageEquinoxConfiguration(it)
                packagedConfigFile.appendText("${newEquinoxConfigurationUrl}\n")
            } else {
                packagedConfigFile.appendText("${it}\n")
            }
        }
    }

    private fun setupPackagedWorkspaceFolders() {
        packagedConfigDir = outputDirectory.get().asFile.toPath().resolve("equinox-configuration")
        packagedFrameworkBundlesDir = outputDirectory.get().asFile.toPath().resolve("equinox-framework")
        packagedExternalBundlesDir = outputDirectory.get().asFile.toPath().resolve("external-osgi-bundles")

        if (!packagedConfigDir.exists()) {
            Files.createDirectory(packagedConfigDir)
        }
        if (!packagedFrameworkBundlesDir.exists()) {
            Files.createDirectory(packagedFrameworkBundlesDir)
        }
        if (!packagedExternalBundlesDir.exists()) {
            Files.createDirectory(packagedExternalBundlesDir)
        }
    }

    private fun packageEquinoxConfiguration(equinoxConfiguratorUrl: String): String {
        val bundlesInfoPathStart = equinoxConfiguratorUrl.indexOf("file:")
        val bundlesInfoPath = equinoxConfiguratorUrl.substring(bundlesInfoPathStart + 5)
        val bundlesInfoFileName = bundlesInfoPath.substring(bundlesInfoPath.lastIndexOf("/") + 1)
        val bundlesInfoNewPath =
            packagedConfigDir.resolve("org.eclipse.equinox.simpleconfigurator").resolve(bundlesInfoFileName)
        if (!bundlesInfoNewPath.parent.exists()) {
            Files.createDirectories(bundlesInfoNewPath.parent)
        }

        bundlesInfoNewPath.toFile().writeText("")
        File(bundlesInfoPath).forEachLine {
            val parts = it.split(",")
            val pathStart = parts[2].indexOf("file:")
            val path = parts[2].substring(pathStart + 5)
            val fileName = path.substring(path.lastIndexOf("/") + 1)
            val newFilePath = packagedExternalBundlesDir.resolve(fileName)
            Files.copy(Paths.get(path), newFilePath, StandardCopyOption.REPLACE_EXISTING)
            bundlesInfoNewPath.toFile().appendText("${it.replace(path, newFilePath.toString())}\n")
        }

        return equinoxConfiguratorUrl.replace(bundlesInfoPath, bundlesInfoNewPath.toString())
    }

    private fun packageOsgiFramework(osgiFrameworkProp: String): String {
        val pathStart = osgiFrameworkProp.indexOf("file:")
        val path = osgiFrameworkProp.substring(pathStart + 5)
        val fileName = path.substring(path.lastIndexOf("/") + 1)
        val newFilePath = packagedFrameworkBundlesDir.resolve(fileName)
        Files.copy(Paths.get(path), newFilePath, StandardCopyOption.REPLACE_EXISTING)
        return osgiFrameworkProp.replace(path, newFilePath.toString())
    }

    private fun packageOsgiBundles(osgiBundlesProp: String): String {
        val pathStart = osgiBundlesProp.indexOf("file:")
        val pathEnd = osgiBundlesProp.indexOf("@")
        val path = osgiBundlesProp.substring(pathStart + 5, pathEnd)
        val fileName = path.substring(path.lastIndexOf("/") + 1)
        val newFilePath = packagedFrameworkBundlesDir.resolve(fileName)
        Files.copy(Paths.get(path), newFilePath, StandardCopyOption.REPLACE_EXISTING)
        return osgiBundlesProp.replace(path, newFilePath.toString())
    }
}