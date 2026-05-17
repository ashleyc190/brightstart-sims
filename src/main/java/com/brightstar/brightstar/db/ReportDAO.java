package com.brightstar.brightstar.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReportDAO {

    public record ReportSummary(int totalStudents, double attendanceRate,
                                double outstandingFees, int pendingNotifications) {}

    public record AttendanceReport(String date, String className,
                                   int present, int absent, int late, int total) {}

    public record FeeReport(String studentName, String studentNo,
                            String className, double paid, double outstanding) {}

    // ── Dashboard summary ──
    public static ReportSummary getSummary() throws SQLException {
        int totalStudents        = StudentDAO.getTotalCount();
        double attendanceRate    = AttendanceDAO.getAttendanceRate();
        double outstandingFees   = PaymentDAO.getTotalOutstanding();
        int pendingNotifications = NotificationDAO.getPendingCount();

        return new ReportSummary(totalStudents, attendanceRate,
                outstandingFees, pendingNotifications);
    }

    // ── Attendance report grouped by date + class ──
    public static List<AttendanceReport> getAttendanceReport() throws SQLException {
        List<AttendanceReport> list = new ArrayList<>();
        String sql = """
            SELECT a.date, c.name as class_name,
                SUM(CASE WHEN a.status = 'Present' THEN 1 ELSE 0 END) as present,
                SUM(CASE WHEN a.status = 'Absent'  THEN 1 ELSE 0 END) as absent,
                SUM(CASE WHEN a.status = 'Late'    THEN 1 ELSE 0 END) as late,
                COUNT(*) as total
            FROM attendance a
            JOIN classes c ON a.class_id = c.id
            WHERE a.submitted = 1
            GROUP BY a.date, a.class_id
            ORDER BY a.date DESC
        """;
        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new AttendanceReport(
                        rs.getString("date"),
                        rs.getString("class_name"),
                        rs.getInt("present"),
                        rs.getInt("absent"),
                        rs.getInt("late"),
                        rs.getInt("total")
                ));
            }
        }
        return list;
    }

    // ── Fee report per student ──
    public static List<FeeReport> getFeeReport() throws SQLException {
        List<FeeReport> list = new ArrayList<>();
        double monthlyFee = 2500.0;
        String sql = """
            SELECT s.first_name || ' ' || s.last_name as student_name,
                   s.student_no, c.name as class_name,
                   COALESCE(SUM(p.amount), 0) as paid
            FROM students s
            LEFT JOIN classes c  ON s.class_id  = c.id
            LEFT JOIN payments p ON s.id = p.student_id
            WHERE s.enrolled = 1
            GROUP BY s.id
            ORDER BY s.first_name
        """;
        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                double paid        = rs.getDouble("paid");
                double outstanding = Math.max(monthlyFee - paid, 0);
                list.add(new FeeReport(
                        rs.getString("student_name"),
                        rs.getString("student_no"),
                        rs.getString("class_name"),
                        paid,
                        outstanding
                ));
            }
        }
        return list;
    }
}