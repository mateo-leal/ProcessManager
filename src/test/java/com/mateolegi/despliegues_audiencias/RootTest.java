package com.mateolegi.despliegues_audiencias;

import com.mateolegi.despliegues.Root;
import org.junit.jupiter.api.Test;

class RootTest {

    @Test
    void test() {
        Root.get().manager().run();
    }

}