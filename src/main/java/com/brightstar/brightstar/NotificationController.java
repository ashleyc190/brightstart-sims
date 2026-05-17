package com.brightstar.brightstar;

import com.brightstar.brightstar.db.NotificationDAO;
import com.brightstar.brightstar.util.Navigator;
import com.brightstar.brightstar.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class NotificationController {

    @FXML private Label            avatarLabel;
    @FXML private Label            dateLabel;
    @FXML private ComboBox<String> recipientCombo;
    @FXML private ComboBox<String> subjectCombo;
    @FXML private TextArea         messageArea;
    @FXML private Label            feedbackLabel;
    @FXML private VBox             historyTable;

    @FXML
    public void initialize() throws SQLException {
        // Avatar
        String name = SessionManager.getName();
        avatarLabel.setText(name.substring(0, 1).toUpperCase());

        // Recipient options
        recipientCombo.setItems(FXCollections.observableArrayList(
                "Parent", "Teacher", "All"
        ));
        recipientCombo.setValue("Parent");

        // Subject options
        subjectCombo.setItems(FXCollections.observableArrayList(
                "Fee Reminder", "Attendance", "Payment Due", "General Notice"
        ));
        subjectCombo.setValue("Fee Reminder");

        // Load history
        loadHistory();
    }

    // ── Load notification history ──
    private void loadHistory() throws SQLException {
        historyTable.getChildren().clear();

        List<NotificationDAO.Notification> notifications = NotificationDAO.getAll();

        if (notifications.isEmpty()) {
            Label empty = new Label("No notifications sent yet.");
            empty.setStyle("-fx-text-fill:#999;-fx-font-size:12px;" +
                    "-fx-font-family:'Georgia';-fx-padding:8 0 0 0;");
            historyTable.getChildren().add(empty);
            return;
        }

        boolean alt = false;
        for (NotificationDAO.Notification n : notifications) {
            historyTable.getChildren().add(buildHistoryRow(n, alt));
            alt = !alt;
        }
    }

    // ── Build a history row ──
    private HBox buildHistoryRow(NotificationDAO.Notification n, boolean alt) {
        HBox row = new HBox();
        row.setPrefHeight(38);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color:" + (alt ? "#fafafa" : "white") + ";"
                + "-fx-border-color:rgba(255,255,255,0.20);-fx-border-width:1 0 0 0;");
        row.setPadding(new Insets(0, 10, 0, 10));

        Label recipientLabel = new Label(n.recipient());
        Label subjectLabel   = new Label(n.subject());
        Label statusLabel    = new Label(n.status());

        recipientLabel.setPrefWidth(160);
        subjectLabel.setPrefWidth(200);
        statusLabel.setPrefWidth(120);

        String baseStyle = "-fx-font-size:13px;-fx-font-family:'Georgia';-fx-text-fill:white;";
        recipientLabel.setStyle(baseStyle);
        subjectLabel.setStyle(baseStyle);

        // Colour-code status
        if ("Sent".equals(n.status())) {
            statusLabel.setStyle(baseStyle + "-fx-font-style:normal;");
        } else {
            statusLabel.setStyle(baseStyle + "-fx-font-style:italic;-fx-text-fill:rgba(255,255,255,0.70);");
        }

        row.getChildren().addAll(recipientLabel, subjectLabel, statusLabel);
        return row;
    }

    // ── Send notification ──
    @FXML
    private void handleSend(ActionEvent event) {
        String recipient = recipientCombo.getValue();
        String subject   = subjectCombo.getValue();
        String message   = messageArea.getText().trim();

        if (message.isEmpty()) {
            showFeedback("Please enter a message before sending.", false);
            return;
        }

        try {
            NotificationDAO.insertNotification(recipient, subject, message);
            messageArea.clear();
            loadHistory();
            showFeedback("✓ Notification sent to " + recipient + ".", true);
        } catch (SQLException e) {
            showFeedback("Error sending notification: " + e.getMessage(), false);
        }
    }

    private void showFeedback(String message, boolean success) {
        feedbackLabel.setText(message);
        feedbackLabel.setStyle(
                "-fx-font-size:12px;-fx-font-family:'Georgia';" +
                        (success ? "-fx-text-fill:#2e8b2e;" : "-fx-text-fill:#e57373;")
        );
    }

    @FXML
    private void handleBack(ActionEvent event) throws IOException {
        Navigator.navigateTo(event, "dashboard-view.fxml");
    }
}