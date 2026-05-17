package com.brightstar.brightstar;

import com.brightstar.brightstar.db.PaymentDAO;
import com.brightstar.brightstar.db.StudentDAO;
import com.brightstar.brightstar.util.Navigator;
import com.brightstar.brightstar.util.SessionManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PaymentController {

    @FXML private ComboBox<String> studentCombo;
    @FXML private Label            balanceLabel;
    @FXML private Label            statusLabel;
    @FXML private VBox             historyTable;
    @FXML private TextField        amountField;
    @FXML private TextField        paymentDateField;
    @FXML private ComboBox<String> methodCombo;
    @FXML private TextField        referenceField;
    @FXML private Label            avatarLabel;
    @FXML private Button           processButton;
    @FXML private StackPane        loadingOverlay;

    private List<StudentDAO.Student> allStudents;

    @FXML
    public void initialize() throws SQLException {
        // Avatar
        String name = SessionManager.getName();
        avatarLabel.setText(name.substring(0, 1).toUpperCase());

        // Payment date defaults to today
        paymentDateField.setText(
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        );

        // Payment methods
        methodCombo.setItems(FXCollections.observableArrayList(
                "Credit Card", "EFT", "Cash"
        ));
        methodCombo.setValue("Credit Card");

        // Auto-generate reference when method changes
        methodCombo.setOnAction(e -> generateReference());

        // Load students into dropdown
        allStudents = StudentDAO.getAllStudents();
        studentCombo.setItems(FXCollections.observableArrayList(
                allStudents.stream()
                        .map(s -> s.firstName() + " " + s.lastName()
                                + " (" + s.studentNo() + ")")
                        .toList()
        ));

        // Load student data when selection changes
        studentCombo.setOnAction(e -> {
            try { loadStudentData(); } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        generateReference();
    }

    // ── Load balance + payment history for selected student ──
    private void loadStudentData() throws SQLException {
        int idx = studentCombo.getSelectionModel().getSelectedIndex();
        if (idx < 0) return;

        StudentDAO.Student student = allStudents.get(idx);

        // Balance
        double outstanding = PaymentDAO.getOutstandingBalance(student.id());
        balanceLabel.setText(String.format("R %.2f", outstanding));
        statusLabel.setText(outstanding == 0 ? "Status: FULLY PAID" : "Status: OUTSTANDING");
        statusLabel.setStyle(outstanding == 0
                ? "-fx-text-fill:#2e8b2e;-fx-font-size:12px;-fx-font-weight:bold;-fx-font-family:'Georgia';"
                : "-fx-text-fill:#e57373;-fx-font-size:12px;-fx-font-weight:bold;-fx-font-family:'Georgia';");

        // Payment history
        loadPaymentHistory(student.id());

        // Pre-fill amount with outstanding
        if (outstanding > 0) {
            amountField.setText(String.format("%.2f", outstanding));
        }
    }

    // ── Build payment history rows dynamically ──
    private void loadPaymentHistory(int studentId) throws SQLException {
        historyTable.getChildren().clear();

        List<PaymentDAO.Payment> payments = PaymentDAO.getPaymentsForStudent(studentId);

        if (payments.isEmpty()) {
            Label empty = new Label("No payment history found.");
            empty.setStyle("-fx-text-fill:#999;-fx-font-size:12px;-fx-font-family:'Georgia';-fx-padding:8 0 0 0;");
            historyTable.getChildren().add(empty);
            return;
        }

        for (PaymentDAO.Payment p : payments) {
            HBox row = new HBox();
            row.setPrefHeight(34);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-border-color:#f0f0f0;-fx-border-width:0 0 1 0;");
            row.setPadding(new Insets(0, 10, 0, 10));

            Label dateLabel   = new Label(p.date());
            Label amountLabel = new Label(String.format("R %.0f", p.amount()));
            Label methodLabel = new Label(p.method());
            Label paidLabel   = new Label("Paid");

            dateLabel.setPrefWidth(110);
            amountLabel.setPrefWidth(80);
            methodLabel.setPrefWidth(80);
            paidLabel.setPrefWidth(60);

            String rowStyle = "-fx-font-size:13px;-fx-font-family:'Georgia';-fx-text-fill:#333;";
            dateLabel.setStyle(rowStyle);
            amountLabel.setStyle(rowStyle);
            methodLabel.setStyle(rowStyle);
            paidLabel.setStyle("-fx-font-size:13px;-fx-font-family:'Georgia';-fx-text-fill:#2e8b2e;-fx-font-weight:bold;");

            row.getChildren().addAll(dateLabel, amountLabel, methodLabel, paidLabel);
            historyTable.getChildren().add(row);
        }
    }

    // ── Generate reference number ──
    private void generateReference() {
        String method = methodCombo.getValue();
        String date   = paymentDateField.getText();
        if (method != null && date != null && !date.isEmpty()) {
            referenceField.setText(PaymentDAO.generateReference(method, date));
        }
    }

    // ── Process payment with loading animation ──
    @FXML
    private void handleProcessPayment(ActionEvent event) {
        int idx = studentCombo.getSelectionModel().getSelectedIndex();
        if (idx < 0) {
            showError("Please select a student.");
            return;
        }

        String amountText = amountField.getText().trim();
        if (amountText.isEmpty()) {
            showError("Please enter an amount.");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            showError("Invalid amount entered.");
            return;
        }

        StudentDAO.Student student = allStudents.get(idx);
        String date      = paymentDateField.getText().trim();
        String method    = methodCombo.getValue();
        String reference = referenceField.getText().trim();

        // Show loading overlay
        loadingOverlay.setVisible(true);
        processButton.setDisable(true);

        // Simulate gateway delay on background thread
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                Thread.sleep(2500); // simulate 2.5s gateway
                PaymentDAO.insertPayment(student.id(), amount, date, method, reference);
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            loadingOverlay.setVisible(false);
            processButton.setDisable(false);
            generateReference();
            try {
                loadStudentData(); // refresh balance + history
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            showSuccess("✓ Payment of R " + String.format("%.2f", amount) + " processed successfully.");
        });

        task.setOnFailed(e -> {
            loadingOverlay.setVisible(false);
            processButton.setDisable(false);
            showError("Payment failed: " + task.getException().getMessage());
        });

        new Thread(task).start();
    }

    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill:#e57373;-fx-font-size:12px;-fx-font-family:'Georgia';");
    }

    private void showSuccess(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill:#2e8b2e;-fx-font-size:12px;-fx-font-family:'Georgia';");
    }

    @FXML
    private void handleCancel(ActionEvent event) throws IOException {
        Navigator.navigateTo(event, "dashboard-view.fxml");
    }
}