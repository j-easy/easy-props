package io.github.benas.easyproperties;

import org.junit.Test;

import static io.github.benas.easyproperties.Utils.extractPath;
import static org.assertj.core.api.Assertions.assertThat;

public class UtilsTest {

    @Test
    public void testExtractPath() {
        String path = "/path/to/file";
        String prefix = "file:";
        assertThat(extractPath(prefix + path)).isEqualTo(path);
    }
}