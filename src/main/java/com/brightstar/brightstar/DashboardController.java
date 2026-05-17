package com.brightstar.brightstar;

import com.brightstar.brightstar.db.ReportDAO;
import com.brightstar.brightstar.db.StudentDAO;
import com.brightstar.brightstar.util.Navigator;
import com.brightstar.brightstar.util.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class DashboardController {

    @FXML private Label dateLabel;
    @FXML private Label greetingLabel;
    @FXML private Label statStudents;
    @FXML private Label statPresent;
    @FXML private Label statFees;
    @FXML private Label statNotifications;
    @FXML private Label avatarLabel;
    @FXML private Label footerLabel;

    @FXML
    public void initialize() {
        // Avatar + greeting from session
        String name = SessionManager.getName();
        avatarLabel.setText(name.substring(0, 1).toUpperCase());
        footerLabel.setText("Logged in as " + name);

        // Date
        dateLabel.setText(LocalDate.now()
                .format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));

        // Time-aware greeting
        int hour = LocalTime.now().getHour();
        if      (hour < 12) greetingLabel.setText("Good morning 👋");
        else if (hour < 17) greetingLabel.setText("Good afternoon 👋");
        else                greetingLabel.setText("Good evening 👋");

        // Load live stats
        loadStats();
    }

    private void loadStats() {
        try {
            ReportDAO.ReportSummary summary = ReportDAO.getSummary();
            statStudents.setText(String.valueOf(summary.totalStudents()));
            statFees.setText(String.format("R %.0f", summary.outstandingFees()));
            statNotifications.setText(String.valueOf(summary.pendingNotifications()));

            // Present today — attendance rate as a percentage
            statPresent.setText(String.format("%.0f%%", summary.attendanceRate()));

        } catch (SQLException e) {
            statStudents.setText("—");
            statPresent.setText("—");
            statFees.setText("—");
            statNotifications.setText("—");
            e.printStackTrace();
        }
    }

    // ── Navigation ──
    @FXML
    public void handleNewStudent(ActionEvent e) throws IOException {
        Navigator.navigateTo(e, "new-student-view.fxml");
    }

    @FXML
    public void handleAttendance(ActionEvent e) throws IOException {
        Navigator.navigateTo(e, "attendance-view.fxml");
    }

    @FXML
    public void handlePayments(ActionEvent e) throws IOException {
        Navigator.navigateTo(e, "payment-view.fxml");
    }

    @FXML
    public void handleReports(ActionEvent e) throws IOException {
        Navigator.navigateTo(e, "reports-view.fxml");
    }

    @FXML
    public void handleNotifications(ActionEvent e) throws IOException {
        Navigator.navigateTo(e, "notifications-view.fxml");
    }
}