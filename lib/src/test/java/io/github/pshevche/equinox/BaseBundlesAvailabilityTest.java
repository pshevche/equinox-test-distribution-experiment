package io.github.pshevche.equinox;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

abstract class BaseBundlesAvailabilityTest {

    static EquinoxSimulator.EquinoxWorkspace workspace;

    @BeforeAll
    static void createEquinoxWorkspace() throws IOException {
        workspace = EquinoxSimulator.createWorkspace(Paths.get("../equinox-configuration/config.ini"));
        workspace.printSummary();
    }

    @Test
    void validateOsgiFrameworkBundles() {
        assertEquals(2, workspace.frameworkBundles().size());
        assertEquals(
            new EquinoxSimulator.OsgiBundle("osgi.bundles", "OSGi's plugin to control the bundle list"),
            workspace.frameworkBundles().get(0)
        );
        assertEquals(
            new EquinoxSimulator.OsgiBundle("osgi.framework", "Core OSGi framework"),
            workspace.frameworkBundles().get(1)
        );
    }

    @Test
    void validateExternalBundles() {
        assertEquals(2, workspace.externalBundles().size());
        assertEquals(
            new EquinoxSimulator.OsgiBundle("hello-world", "Hello, World!"),
            workspace.externalBundles().get(0)
        );
        assertEquals(
            new EquinoxSimulator.OsgiBundle("bye-world", "Good bye, World!"),
            workspace.externalBundles().get(1)
        );
    }
}
