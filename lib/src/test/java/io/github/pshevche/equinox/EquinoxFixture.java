package io.github.pshevche.equinox;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

class EquinoxFixture {

    static String createWorkspace(Path configFile) throws IOException {
        List<String> workspaceConfig = new ArrayList<>();

        List<String> configIniContent = Files.readAllLines(configFile);

        workspaceConfig.add("=== config.ini ===");
        workspaceConfig.addAll(configIniContent);
        workspaceConfig.add("=== config.ini ===\n");

        workspaceConfig.add("=== OSGi framework bundles ===");
        printOSGiFrameworkBundles(configIniContent, workspaceConfig);
        workspaceConfig.add("=== OSGi framework bundles ===\n");

        Path bundlesInfoPath = getBundlesInfoPath(configIniContent);
        List<String> bundlesInfoContent = Files.readAllLines(bundlesInfoPath);
        workspaceConfig.add("=== bundles.info ===");
        workspaceConfig.addAll(bundlesInfoContent);
        workspaceConfig.add("=== bundles.info ===\n");

        workspaceConfig.add("=== External bundles ===");
        printOSGiExternalBundles(bundlesInfoContent, workspaceConfig);
        workspaceConfig.add("=== External bundles ===");

        return String.join("\n", workspaceConfig);
    }

    private static void printOSGiExternalBundles(List<String> bundlesInfoContent, List<String> workspaceConfig) throws IOException {
        for (String bundle : bundlesInfoContent) {
            String[] parts = bundle.split(",");
            workspaceConfig.add("=== " + parts[0] + " ===");
            String bundlePath = parts[2].substring(parts[2].indexOf("/"));
            workspaceConfig.addAll(Files.readAllLines(Paths.get(bundlePath)));
            workspaceConfig.add("=== " + parts[0] + " ===");
        }
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

    private static void printOSGiFrameworkBundles(List<String> configIniContent, List<String> workspaceConfig) throws IOException {
        for (String line : configIniContent) {
            if (line.startsWith("osgi.bundles=")) {
                workspaceConfig.add("=== osgi.bundles value ===");
                int pathStart = line.indexOf("file:/");
                int pathEnd = line.indexOf("@");
                workspaceConfig.addAll(Files.readAllLines(Paths.get(line.substring(pathStart + 5, pathEnd))));
                workspaceConfig.add("=== osgi.bundles value ===");
            } else if (line.startsWith("osgi.framework=")) {
                workspaceConfig.add("=== osgi.framework value ===");
                int pathStart = line.indexOf("/");
                workspaceConfig.addAll(Files.readAllLines(Paths.get(line.substring(pathStart))));
                workspaceConfig.add("=== osgi.framework value ===");
            }
        }
    }
}
