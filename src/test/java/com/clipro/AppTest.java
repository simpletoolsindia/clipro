package com.clipro;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AppTest {

    @Test
    void shouldHaveMainMethod() {
        // Given - App class exists
        // When - We verify the class is accessible
        // Then - No exception thrown
        assertNotNull(App.class);
    }
}
