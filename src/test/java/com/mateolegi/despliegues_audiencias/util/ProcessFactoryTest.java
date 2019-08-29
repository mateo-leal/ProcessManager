package com.mateolegi.despliegues_audiencias.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProcessFactoryTest {

    @Test
    void test_setValue_when_Platform_not_initialized() {
        ProcessFactory.setValue("Test");
    }

    @Test
    void test_when_constructor_empty() {
        assertThrows(IllegalArgumentException.class, ProcessFactory::new);
    }
}