module com.mateolegi {
    requires javafx.controls;
    requires javafx.fxml;
    requires slf4j.api;
    requires org.apache.commons.io;
    requires org.apache.commons.collections4;
    requires jsch;
    requires gson;
    requires ant;
    requires org.eclipse.jgit;
    requires commons.cli;
    requires org.jetbrains.annotations;
    requires java.net.http;
    requires java.sql;

    opens com.mateolegi.despliegues_audiencias to javafx.fxml;
    opens com.mateolegi.despliegues_audiencias.util to gson;
    exports com.mateolegi.despliegues_audiencias;
}