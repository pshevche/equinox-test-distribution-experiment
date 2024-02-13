package io.github.pshevche.equinox;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.fail;

class DummyBundlesTest {

    @Test
    void validateEquinoxWorkspace() throws IOException {
        String workspaceConfig = EquinoxFixture.createWorkspace(Paths.get("../equinox-configuration/config.ini"));
        System.out.println(workspaceConfig);
        fail();
    }
}
