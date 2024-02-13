package io.github.pshevche.equinox;

import io.soabase.recordbuilder.core.RecordBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

class EquinoxSimulator {

    record OsgiBundle(
        String name,
        String rawContent
    ) {
    }

    @RecordBuilder
    record EquinoxWorkspace(
        String rawConfigContent,
        String rawBundlesInfoContent,
        List<OsgiBundle> frameworkBundles,
        List<OsgiBundle> externalBundles
    ) {

        void printSummary() {
            System.out.println("=== config.ini ===");
            System.out.println(rawConfigContent);
            System.out.println("=== config.ini ===\n");

            System.out.println("=== bundles.info ===");
            System.out.println(rawBundlesInfoContent);
            System.out.println("=== bundles.info ===\n");

            System.out.println("=== OSGi framework ===");
            frameworkBundles.forEach(System.out::println);
            System.out.println("=== OSGi framework ===\n");

            System.out.println("=== External bundles ===");
            externalBundles.forEach(System.out::println);
            System.out.println("=== External bundles ===");
        }
    }

    static EquinoxWorkspace createWorkspace(Path configFile) throws IOException {
        List<String> configFileContent = Files.readAllLines(configFile);
        Path bundlesInfoPath = getBundlesInfoPath(configFileContent);
        return EquinoxSimulatorEquinoxWorkspaceBuilder.builder()
            .rawConfigContent(String.join("\n", configFileContent))
            .rawBundlesInfoContent(readContent(bundlesInfoPath.toString()))
            .frameworkBundles(loadFrameworkBundles(configFileContent))
            .externalBundles(loadExternalBundles(bundlesInfoPath))
            .build();
    }

    private static Path getBundlesInfoPath(List<String> configIniContent) {
        return configIniContent.stream()
            .filter(it -> it.startsWith("org.eclipse.equinox.simpleconfigurator.configUrl="))
            .map(it -> {
                int pathStart = it.indexOf("/");
                return Paths.get(it.substring(pathStart));
            })
            .findFirst()
            .orElseThrow();
    }

    private static List<OsgiBundle> loadFrameworkBundles(List<String> configFileContent) throws IOException {
        List<OsgiBundle> bundles = new ArrayList<>();
        for (String line : configFileContent) {
            if (line.startsWith("osgi.bundles=")) {
                int pathStart = line.indexOf("file:/");
                int pathEnd = line.indexOf("@");
                bundles.add(new OsgiBundle("osgi.bundles", readContent(line.substring(pathStart + 5, pathEnd))));
            } else if (line.startsWith("osgi.framework=")) {
                int pathStart = line.indexOf("file:/");
                bundles.add(new OsgiBundle("osgi.framework", readContent(line.substring(pathStart + 5))));
            }
        }

        return bundles;
    }

    private static List<OsgiBundle> loadExternalBundles(Path bundlesInfoPath) throws IOException {
        return Files.readAllLines(bundlesInfoPath).stream()
            .map(bundle -> {
                String[] parts = bundle.split(",");
                String bundlePath = parts[2].substring(parts[2].indexOf("/"));
                try {
                    return new OsgiBundle(parts[0], readContent(bundlePath));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            })
            .toList();
    }

    private static String readContent(String path) throws IOException {
        return String.join("\n", Files.readAllLines(Paths.get(path)));
    }
}
