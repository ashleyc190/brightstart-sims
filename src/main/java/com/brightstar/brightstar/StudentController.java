package com.brightstar.brightstar;

import com.brightstar.brightstar.db.StudentDAO;
import com.brightstar.brightstar.util.Navigator;
import com.brightstar.brightstar.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class StudentController {

    @FXML private TextField        firstNameField;
    @FXML private TextField        lastNameField;
    @FXML private TextField        dobField;
    @FXML private ComboBox<String> genderCombo;
    @FXML private TextField        enrolmentDateField;
    @FXML private ComboBox<String> classCombo;
    @FXML private TextField        guardianNameField;
    @FXML private TextField        relationshipField;
    @FXML private TextField        phoneField;
    @FXML private TextField        emailField;
    @FXML private Label            errorLabel;
    @FXML private Label            avatarLabel;

    @FXML
    public void initialize() throws SQLException {
        // Set avatar from session
        String name = SessionManager.getName();
        avatarLabel.setText(name.substring(0, 1).toUpperCase());

        // Gender options
        genderCombo.setItems(FXCollections.observableArrayList(
                "Female", "Male", "Other"
        ));
        genderCombo.setValue("Female");

        // Load classes from DB
        classCombo.setItems(FXCollections.observableArrayList(
                StudentDAO.getClassNames()
        ));

        // Default enrolment date to today
        enrolmentDateField.setText(
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        );
    }

    @FXML
    private void handleSave(ActionEvent event) {
        String firstName     = firstNameField.getText().trim();
        String lastName      = lastNameField.getText().trim();
        String dob           = dobField.getText().trim();
        String gender        = genderCombo.getValue();
        String enrolmentDate = enrolmentDateField.getText().trim();
        String className     = classCombo.getValue();
        String guardianName  = guardianNameField.getText().trim();
        String relationship  = relationshipField.getText().trim();
        String phone         = phoneField.getText().trim();
        String email         = emailField.getText().trim();

        // Validate
        if (firstName.isEmpty() || lastName.isEmpty()) {
            errorLabel.setText("First and last name are required.");
            return;
        }
        if (className == null) {
            errorLabel.setText("Please select a class.");
            return;
        }
        if (guardianName.isEmpty() || phone.isEmpty()) {
            errorLabel.setText("Guardian name and phone number are required.");
            return;
        }

        try {
            String studentNo = StudentDAO.generateStudentNo();
            int classId      = StudentDAO.getClassIdByName(className);

            StudentDAO.insertStudent(
                    studentNo, firstName, lastName, dob, gender,
                    enrolmentDate, classId, guardianName,
                    relationship, phone, email
            );

            showSuccess();
            clearForm();

        } catch (SQLException e) {
            errorLabel.setText("Error saving student: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) throws IOException {
        Navigator.navigateTo(event, "dashboard-view.fxml");
    }

    private void showSuccess() {
        errorLabel.setStyle("-fx-text-fill:#2e8b2e;-fx-font-size:12px;-fx-font-family:'Georgia';");
        errorLabel.setText("✓ Student registered successfully!");
    }

    private void clearForm() {
        firstNameField.clear();
        lastNameField.clear();
        dobField.clear();
        genderCombo.setValue("Female");
        enrolmentDateField.setText(
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        );
        classCombo.setValue(null);
        guardianNameField.clear();
        relationshipField.clear();
        phoneField.clear();
        emailField.clear();
    }
}