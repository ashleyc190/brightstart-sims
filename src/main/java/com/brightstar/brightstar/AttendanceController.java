package com.brightstar.brightstar;

import com.brightstar.brightstar.db.AttendanceDAO;
import com.brightstar.brightstar.db.StudentDAO;
import com.brightstar.brightstar.util.Navigator;
import com.brightstar.brightstar.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttendanceController {

    @FXML private ComboBox<String> classCombo;
    @FXML private TextField        dateField;
    @FXML private Label            presentCount;
    @FXML private Label            absentCount;
    @FXML private Label            lateCount;
    @FXML private Label            avatarLabel;
    @FXML private Label            statusLabel;
    @FXML private FlowPane         studentGrid;

    // Tracks current status selection per student id
    private final Map<Integer, String>  statusMap   = new HashMap<>();
    // Tracks the three buttons per student id
    private final Map<Integer, Button[]> buttonMap  = new HashMap<>();

    private List<StudentDAO.Student> currentStudents;
    private int currentClassId = -1;

    @FXML
    public void initialize() throws SQLException {
        // Avatar
        String name = SessionManager.getName();
        avatarLabel.setText(name.substring(0, 1).toUpperCase());

        // Date defaults to today
        dateField.setText(
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        );

        // Load classes
        classCombo.setItems(FXCollections.observableArrayList(
                StudentDAO.getClassNames()
        ));

        // Auto-load when class changes
        classCombo.setOnAction(e -> {
            try { loadStudents(); } catch (SQLException ex) { ex.printStackTrace(); }
        });
    }

    // ── Load students for selected class ──
    private void loadStudents() throws SQLException {
        String className = classCombo.getValue();
        if (className == null) return;

        currentClassId  = StudentDAO.getClassIdByName(className);
        currentStudents = StudentDAO.getStudentsByClass(currentClassId);
        String date     = dateField.getText().trim();

        // Load any existing attendance for this date
        List<AttendanceDAO.AttendanceRecord> existing =
                AttendanceDAO.getAttendanceForDate(currentClassId, date);

        Map<Integer, String> existingStatus = new HashMap<>();
        for (AttendanceDAO.AttendanceRecord r : existing) {
            existingStatus.put(r.studentId(), r.status());
        }

        statusMap.clear();
        buttonMap.clear();
        studentGrid.getChildren().clear();

        for (StudentDAO.Student student : currentStudents) {
            String status = existingStatus.getOrDefault(student.id(), "Present");
            statusMap.put(student.id(), status);
            studentGrid.getChildren().add(buildStudentCard(student, status));
        }

        updateSummary();
    }

    // ── Build a student card dynamically ──
    private HBox buildStudentCard(StudentDAO.Student student, String initialStatus) {
        HBox card = new HBox(14);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPrefWidth(460);
        card.setStyle("""
            -fx-background-color:white;
            -fx-background-radius:8;
            -fx-effect:dropshadow(gaussian,rgba(0,0,0,0.08),6,0,0,2);
            -fx-border-color:#eeeeee;
            -fx-border-radius:8;
            -fx-border-width:1;
        """);
        card.setPadding(new Insets(16));

        // Avatar circle
        StackPane avatar = new StackPane();
        avatar.setPrefSize(52, 52);
        avatar.setMaxSize(52, 52);
        avatar.setStyle("-fx-background-color:#f5c99a;-fx-background-radius:50;");
        String initials = student.firstName().substring(0, 1).toUpperCase()
                + student.lastName().substring(0, 1).toUpperCase();
        Label initialsLabel = new Label(initials);
        initialsLabel.setStyle("-fx-text-fill:#8a5000;-fx-font-weight:bold;-fx-font-size:14px;");
        avatar.getChildren().add(initialsLabel);

        // Info
        VBox info = new VBox(6);
        info.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label nameLabel = new Label(student.firstName() + " " + student.lastName());
        nameLabel.setStyle("-fx-font-size:15px;-fx-font-weight:bold;-fx-font-family:'Georgia';-fx-text-fill:#1a1a1a;");

        Label subLabel = new Label(student.className() + "  ·  " + student.studentNo());
        subLabel.setStyle("-fx-font-size:11px;-fx-text-fill:#999;-fx-font-family:'Georgia';");

        // Status buttons
        HBox buttons = new HBox(6);
        Button btnPresent = buildStatusButton("Present", "#4caf50", student.id());
        Button btnAbsent  = buildStatusButton("Absent",  "#e57373", student.id());
        Button btnLate    = buildStatusButton("Late",    "#ffb74d", student.id());
        buttons.getChildren().addAll(btnPresent, btnAbsent, btnLate);
        buttonMap.put(student.id(), new Button[]{btnPresent, btnAbsent, btnLate});

        info.getChildren().addAll(nameLabel, subLabel, buttons);
        card.getChildren().addAll(avatar, info);

        // Apply initial status highlight
        applyStatus(student.id(), initialStatus);

        return card;
    }

    // ── Build a single status button ──
    private Button buildStatusButton(String status, String colour, int studentId) {
        Button btn = new Button(status);
        btn.setStyle(inactiveStyle());
        btn.setCursor(javafx.scene.Cursor.HAND);
        btn.setPadding(new Insets(4, 10, 4, 10));
        btn.setOnAction(e -> {
            statusMap.put(studentId, status);
            applyStatus(studentId, status);
            updateSummary();
        });
        return btn;
    }

    // ── Highlight the active button, grey out others ──
    private void applyStatus(int studentId, String status) {
        Button[] buttons = buttonMap.get(studentId);
        if (buttons == null) return;

        String[] colours = {"#4caf50", "#e57373", "#ffb74d"};
        String[] labels  = {"Present", "Absent", "Late"};

        for (int i = 0; i < buttons.length; i++) {
            if (labels[i].equals(status)) {
                buttons[i].setStyle(activeStyle(colours[i]));
            } else {
                buttons[i].setStyle(inactiveStyle());
            }
        }
    }

    private String activeStyle(String colour) {
        return "-fx-background-color:" + colour + ";-fx-background-radius:4;"
                + "-fx-text-fill:white;-fx-font-size:11px;-fx-font-weight:bold;";
    }

    private String inactiveStyle() {
        return "-fx-background-color:#f5f5f5;-fx-background-radius:4;"
                + "-fx-text-fill:#999;-fx-border-color:#ddd;-fx-border-radius:4;"
                + "-fx-border-width:1;-fx-font-size:11px;";
    }

    // ── Update summary badge counts ──
    private void updateSummary() {
        long present = statusMap.values().stream().filter(s -> s.equals("Present")).count();
        long absent  = statusMap.values().stream().filter(s -> s.equals("Absent")).count();
        long late    = statusMap.values().stream().filter(s -> s.equals("Late")).count();
        presentCount.setText("Present: " + present);
        absentCount.setText("Absent: "  + absent);
        lateCount.setText("Late: "      + late);
    }

    // ── Mark all present ──
    @FXML
    private void handleMarkAllPresent() {
        if (currentStudents == null) return;
        for (StudentDAO.Student s : currentStudents) {
            statusMap.put(s.id(), "Present");
            applyStatus(s.id(), "Present");
        }
        updateSummary();
    }

    // ── Filter cards by status ──
    @FXML
    private void handleFilterAll()     { showAllCards(); }
    @FXML
    private void handleFilterPresent() { filterCards("Present"); }
    @FXML
    private void handleFilterAbsent()  { filterCards("Absent"); }
    @FXML
    private void handleFilterLate()    { filterCards("Late"); }

    private void showAllCards() {
        studentGrid.getChildren().forEach(node -> node.setVisible(true));
    }

    private void filterCards(String status) {
        if (currentStudents == null) return;
        for (int i = 0; i < currentStudents.size(); i++) {
            int studentId = currentStudents.get(i).id();
            boolean match = status.equals(statusMap.get(studentId));
            studentGrid.getChildren().get(i).setVisible(match);
        }
    }

    // ── Save as draft ──
    @FXML
    private void handleSaveDraft(ActionEvent event) {
        saveAttendance(false);
        statusLabel.setText("✓ Draft saved");
        statusLabel.setStyle("-fx-text-fill:#2e8b2e;-fx-font-size:12px;-fx-font-family:'Georgia';");
    }

    // ── Submit attendance ──
    @FXML
    private void handleSubmit(ActionEvent event) {
        if (statusMap.isEmpty()) {
            statusLabel.setText("No students loaded.");
            return;
        }
        saveAttendance(true);
        statusLabel.setText("✓ Attendance submitted");
        statusLabel.setStyle("-fx-text-fill:#2e8b2e;-fx-font-size:12px;-fx-font-family:'Georgia';");
    }

    private void saveAttendance(boolean submitted) {
        if (currentStudents == null || currentClassId == -1) return;
        String date = dateField.getText().trim();
        try {
            for (StudentDAO.Student student : currentStudents) {
                String status = statusMap.getOrDefault(student.id(), "Present");
                AttendanceDAO.saveAttendance(
                        student.id(), currentClassId, date, status, submitted
                );
            }
        } catch (SQLException e) {
            statusLabel.setText("Error saving: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack(ActionEvent event) throws IOException {
        Navigator.navigateTo(event, "dashboard-view.fxml");
    }
}