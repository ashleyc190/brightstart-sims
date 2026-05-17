module com.brightstar.brightstar {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires java.desktop;
    requires java.sql;

    opens com.brightstar.brightstar to javafx.fxml;
    exports com.brightstar.brightstar;
}