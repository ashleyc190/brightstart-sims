package com.brightstar.brightstar;

import com.brightstar.brightstar.db.UserDAO;
import com.brightstar.brightstar.util.Navigator;
import com.brightstar.brightstar.util.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.sql.SQLException;

public class HelloController {

    @FXML private TextField usernameField;
    @FXML private TextField passwordField;
    @FXML private Label errorLabel;

    @FXML
    private void handleLogin(ActionEvent event) throws IOException, SQLException {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter your username and password");
            return;
        }

        UserDAO.User user = UserDAO.login(username, password);

        if (user == null) {
            errorLabel.setText("Invalid username or password.");
        }

        SessionManager.setCurrentUser(user);
        Navigator.navigateTo(event, "dashboard-view.fxml");
    }

}
