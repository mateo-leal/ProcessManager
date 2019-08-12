package com.mateolegi.despliegues_audiencias.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProcessManagerTest {

    @Test
    void test_setValue_when_Platform_not_initialized() {
        ProcessManager.setValue("Test");
    }

    @Test
    void test_startAndWait_when_constructor_empty() {
        assertThrows(IllegalArgumentException.class, ProcessManager::new);
    }
}