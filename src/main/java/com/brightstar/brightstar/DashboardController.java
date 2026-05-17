package com.brightstar.brightstar;

import com.brightstar.brightstar.util.Navigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.io.IOException;

public class DashboardController {
    @FXML
    private Label dashboardText;

    @FXML void handleNewStudent(ActionEvent e) throws IOException {
        Navigator.navigateTo(e, "new-student-view.fxml");
    }

    @FXML void handleAttendance(ActionEvent e) throws IOException {
        Navigator.navigateTo(e, "attendance-view.fxml");
    }
}
