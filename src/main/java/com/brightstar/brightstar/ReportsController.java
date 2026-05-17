package com.brightstar.brightstar;

import com.brightstar.brightstar.db.ReportDAO;
import com.brightstar.brightstar.util.Navigator;
import com.brightstar.brightstar.util.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReportsController {

    @FXML private Label dateLabel;
    @FXML private Label statStudents;
    @FXML private Label statAttendance;
    @FXML private Label statFees;
    @FXML private Label statNotifications;
    @FXML private VBox  reportTableBody;
    @FXML private Label avatarLabel;

    // Tracks which report is currently shown
    private String currentReport = "attendance";

    @FXML
    public void initialize() throws SQLException {
        // Avatar + date
        String name = SessionManager.getName();
        avatarLabel.setText(name.substring(0, 1).toUpperCase());
        dateLabel.setText(LocalDate.now()
                .format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));

        // Load summary stats
        loadSummary();

        // Default to attendance report
        loadAttendanceReport();
    }

    // ── Load stat cards ──
    private void loadSummary() throws SQLException {
        ReportDAO.ReportSummary summary = ReportDAO.getSummary();
        statStudents.setText(String.valueOf(summary.totalStudents()));
        statAttendance.setText(String.format("%.0f%%", summary.attendanceRate()));
        statFees.setText(String.format("R %.0f", summary.outstandingFees()));
        statNotifications.setText(String.valueOf(summary.pendingNotifications()));
    }

    // ── Weekly Attendance Report ──
    @FXML
    private void handleAttendanceReport(ActionEvent event) throws SQLException {
        currentReport = "attendance";
        loadAttendanceReport();
    }

    private void loadAttendanceReport() throws SQLException {
        reportTableBody.getChildren().clear();

        // Header row
        reportTableBody.getChildren().add(
                buildHeaderRow("Date", "Class", "Present", "Absent", "Late", "Total")
        );

        List<ReportDAO.AttendanceReport> rows = ReportDAO.getAttendanceReport();

        if (rows.isEmpty()) {
            reportTableBody.getChildren().add(buildEmptyRow("No submitted attendance records found."));
            return;
        }

        boolean alt = false;
        for (ReportDAO.AttendanceReport r : rows) {
            reportTableBody.getChildren().add(buildAttendanceRow(r, alt));
            alt = !alt;
        }
    }

    // ── Monthly Fee Report ──
    @FXML
    private void handleFeeReport(ActionEvent event) throws SQLException {
        currentReport = "fees";
        loadFeeReport();
    }

    private void loadFeeReport() throws SQLException {
        reportTableBody.getChildren().clear();

        // Header
        reportTableBody.getChildren().add(
                buildHeaderRow("Student", "No.", "Class", "Paid", "Outstanding", "")
        );

        List<ReportDAO.FeeReport> rows = ReportDAO.getFeeReport();

        if (rows.isEmpty()) {
            reportTableBody.getChildren().add(buildEmptyRow("No student fee records found."));
            return;
        }

        boolean alt = false;
        for (ReportDAO.FeeReport r : rows) {
            reportTableBody.getChildren().add(buildFeeRow(r, alt));
            alt = !alt;
        }
    }

    // ── Outstanding Balances Report ──
    @FXML
    private void handleOutstandingReport(ActionEvent event) throws SQLException {
        currentReport = "outstanding";
        reportTableBody.getChildren().clear();

        reportTableBody.getChildren().add(
                buildHeaderRow("Student", "No.", "Class", "Outstanding", "", "")
        );

        List<ReportDAO.FeeReport> rows = ReportDAO.getFeeReport();
        boolean alt = false;
        for (ReportDAO.FeeReport r : rows) {
            if (r.outstanding() > 0) {
                reportTableBody.getChildren().add(buildOutstandingRow(r, alt));
                alt = !alt;
            }
        }

        if (reportTableBody.getChildren().size() == 1) {
            reportTableBody.getChildren().add(buildEmptyRow("No outstanding balances."));
        }
    }

    // ── Row builders ──
    private HBox buildHeaderRow(String c1, String c2, String c3,
                                String c4, String c5, String c6) {
        HBox row = new HBox();
        row.setPrefHeight(36);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color:#c8900a;-fx-background-radius:4 4 0 0;");
        row.setPadding(new Insets(0, 10, 0, 10));
        row.getChildren().addAll(
                headerCell(c1, 180), headerCell(c2, 80),
                headerCell(c3, 100), headerCell(c4, 100),
                headerCell(c5, 100), headerCell(c6, 80)
        );
        return row;
    }

    private HBox buildAttendanceRow(ReportDAO.AttendanceReport r, boolean alt) {
        HBox row = buildDataRow(alt);
        row.getChildren().addAll(
                dataCell(r.date(),                  180),
                dataCell(r.className(),             80),
                dataCell(String.valueOf(r.present()), 100),
                dataCell(String.valueOf(r.absent()),  100),
                dataCell(String.valueOf(r.late()),    100),
                dataCell(String.valueOf(r.total()),   80)
        );
        return row;
    }

    private HBox buildFeeRow(ReportDAO.FeeReport r, boolean alt) {
        HBox row = buildDataRow(alt);
        row.getChildren().addAll(
                dataCell(r.studentName(),                      180),
                dataCell(r.studentNo(),                        80),
                dataCell(r.className() != null ? r.className() : "-", 100),
                dataCell(String.format("R %.0f", r.paid()),   100),
                dataCell(String.format("R %.0f", r.outstanding()), 100),
                dataCell("",                                   80)
        );
        return row;
    }

    private HBox buildOutstandingRow(ReportDAO.FeeReport r, boolean alt) {
        HBox row = buildDataRow(alt);
        Label outLabel = new Label(String.format("R %.0f", r.outstanding()));
        outLabel.setPrefWidth(100);
        outLabel.setStyle("-fx-font-size:13px;-fx-font-family:'Georgia';" +
                "-fx-text-fill:#e57373;-fx-font-weight:bold;");
        row.getChildren().addAll(
                dataCell(r.studentName(), 180),
                dataCell(r.studentNo(),   80),
                dataCell(r.className() != null ? r.className() : "-", 100),
                outLabel,
                dataCell("", 100),
                dataCell("", 80)
        );
        return row;
    }

    private HBox buildDataRow(boolean alt) {
        HBox row = new HBox();
        row.setPrefHeight(34);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color:" + (alt ? "#f9f9f9" : "white") + ";"
                + "-fx-border-color:#eeeeee;-fx-border-width:0 0 1 0;");
        row.setPadding(new Insets(0, 10, 0, 10));
        return row;
    }

    private Label headerCell(String text, double width) {
        Label l = new Label(text);
        l.setPrefWidth(width);
        l.setStyle("-fx-text-fill:white;-fx-font-weight:bold;-fx-font-size:13px;-fx-font-family:'Georgia';");
        return l;
    }

    private Label dataCell(String text, double width) {
        Label l = new Label(text);
        l.setPrefWidth(width);
        l.setStyle("-fx-font-size:13px;-fx-font-family:'Georgia';-fx-text-fill:#333;");
        return l;
    }

    private HBox buildEmptyRow(String message) {
        HBox row = new HBox();
        row.setPadding(new Insets(16));
        Label l = new Label(message);
        l.setStyle("-fx-text-fill:#999;-fx-font-size:12px;-fx-font-family:'Georgia';");
        row.getChildren().add(l);
        return row;
    }

    @FXML
    private void handleBack(ActionEvent event) throws IOException {
        Navigator.navigateTo(event, "dashboard-view.fxml");
    }
}