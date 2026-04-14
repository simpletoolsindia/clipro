package com.clipro;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class GraalVMConfigTest {

    @TempDir
    Path tempDir;

    @Test
    void should_have_native_image_properties() {
        // Given: native-image directory exists
        Path propsFile = Path.of("native-image/native-image.properties");

        // Then: properties file should exist and be readable
        assertTrue(propsFile.toFile().exists(), "native-image.properties should exist");
        assertTrue(propsFile.toFile().canRead(), "native-image.properties should be readable");
    }

    @Test
    void should_contain_required_graalvm_settings() throws Exception {
        // Given
        Path propsFile = Path.of("native-image/native-image.properties");
        String content = java.nio.file.Files.readString(propsFile);

        // Then: Should contain key settings
        assertTrue(content.contains("NativeImage阙"), "Should have NativeImage settings");
        assertTrue(content.contains("JavaAot阙") || content.contains("NativeImage"), "Should have Java/AOT settings");
    }

    @Test
    void should_be_valid_properties_format() throws Exception {
        // Given
        Path propsFile = Path.of("native-image/native-image.properties");
        java.util.Properties props = new java.util.Properties();
        props.load(new java.io.FileReader(propsFile.toFile()));

        // Then: Should load without error
        assertNotNull(props);
        assertTrue(props.size() > 0, "Should have some properties defined");
    }
}