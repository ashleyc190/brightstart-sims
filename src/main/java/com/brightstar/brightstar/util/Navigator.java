package com.brightstar.brightstar.util;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;

public class Navigator {

    public static void navigateTo(ActionEvent event, String fxmlFile) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                Navigator.class.getResource("/com/brightstar/brightstar/" + fxmlFile)
        );

        Parent root = loader.load();

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root, screenBounds.getWidth(), screenBounds.getHeight()));
        stage.show();
        stage.setMaximized(true);
    }

}
