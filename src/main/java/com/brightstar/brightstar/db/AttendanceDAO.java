package com.brightstar.brightstar.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AttendanceDAO {

    public record AttendanceRecord(int id, int studentId, String studentName,
                                   String studentNo, String status, boolean submitted) {}

    // ── Save or update a single attendance record ──
    public static void saveAttendance(int studentId, int classId,
                                      String date, String status,
                                      boolean submitted) throws SQLException {
        // Check if record already exists for this student/date
        String checkSql = """
            SELECT id FROM attendance
            WHERE student_id = ? AND date = ?
        """;
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(checkSql)) {
            ps.setInt(1, studentId);
            ps.setString(2, date);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                // Update existing
                String updateSql = """
                    UPDATE attendance SET status = ?, submitted = ?
                    WHERE student_id = ? AND date = ?
                """;
                try (PreparedStatement up = DatabaseManager.getConnection().prepareStatement(updateSql)) {
                    up.setString(1, status);
                    up.setInt(2, submitted ? 1 : 0);
                    up.setInt(3, studentId);
                    up.setString(4, date);
                    up.executeUpdate();
                }
            } else {
                // Insert new
                String insertSql = """
                    INSERT INTO attendance (student_id, class_id, date, status, submitted)
                    VALUES (?, ?, ?, ?, ?)
                """;
                try (PreparedStatement ip = DatabaseManager.getConnection().prepareStatement(insertSql)) {
                    ip.setInt(1, studentId);
                    ip.setInt(2, classId);
                    ip.setString(3, date);
                    ip.setString(4, status);
                    ip.setInt(5, submitted ? 1 : 0);
                    ip.executeUpdate();
                }
            }
        }
    }

    // ── Get attendance for a class on a specific date ──
    public static List<AttendanceRecord> getAttendanceForDate(int classId,
                                                              String date) throws SQLException {
        List<AttendanceRecord> list = new ArrayList<>();
        String sql = """
            SELECT a.id, a.student_id, s.first_name || ' ' || s.last_name as student_name,
                   s.student_no, a.status, a.submitted
            FROM attendance a
            JOIN students s ON a.student_id = s.id
            WHERE a.class_id = ? AND a.date = ?
            ORDER BY s.first_name
        """;
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, classId);
            ps.setString(2, date);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new AttendanceRecord(
                        rs.getInt("id"),
                        rs.getInt("student_id"),
                        rs.getString("student_name"),
                        rs.getString("student_no"),
                        rs.getString("status"),
                        rs.getInt("submitted") == 1
                ));
            }
        }
        return list;
    }

    // ── Summary counts for a class on a date ──
    public static int countByStatus(int classId, String date,
                                    String status) throws SQLException {
        String sql = """
            SELECT COUNT(*) as count FROM attendance
            WHERE class_id = ? AND date = ? AND status = ?
        """;
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, classId);
            ps.setString(2, date);
            ps.setString(3, status);
            ResultSet rs = ps.executeQuery();
            return rs.getInt("count");
        }
    }

    // ── Get attendance rate across all records ──
    public static double getAttendanceRate() throws SQLException {
        String sql = """
            SELECT
                COUNT(*) as total,
                SUM(CASE WHEN status = 'Present' THEN 1 ELSE 0 END) as present
            FROM attendance WHERE submitted = 1
        """;
        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            int total   = rs.getInt("total");
            int present = rs.getInt("present");
            if (total == 0) return 0;
            return (present * 100.0) / total;
        }
    }
}