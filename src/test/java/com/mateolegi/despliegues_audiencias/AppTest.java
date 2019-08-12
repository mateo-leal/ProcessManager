package com.mateolegi.despliegues_audiencias;

import javafx.application.Platform;
import org.junit.jupiter.api.Test;

class AppTest {

    @Test
    void main() {
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                Platform.exit();
            } catch (InterruptedException ignored) { }
        }).start();
        App.main(null);
    }
}