package com.brightstar.brightstar.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {

    public record Notification(int id, String recipient, String subject,
                               String message, String status, String sentDate) {}

    // ── Insert notification ──
    public static void insertNotification(String recipient, String subject,
                                          String message) throws SQLException {
        String sql = """
            INSERT INTO notifications (recipient, subject, message, status, sent_date)
            VALUES (?, ?, ?, 'Sent', date('now'))
        """;
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, recipient);
            ps.setString(2, subject);
            ps.setString(3, message);
            ps.executeUpdate();
        }
    }

    // ── Get all notifications ──
    public static List<Notification> getAll() throws SQLException {
        List<Notification> list = new ArrayList<>();
        String sql = """
            SELECT id, recipient, subject, message, status, sent_date
            FROM notifications
            ORDER BY id DESC
        """;
        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Notification(
                        rs.getInt("id"),
                        rs.getString("recipient"),
                        rs.getString("subject"),
                        rs.getString("message"),
                        rs.getString("status"),
                        rs.getString("sent_date")
                ));
            }
        }
        return list;
    }

    // ── Get pending count ──
    public static int getPendingCount() throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM notifications WHERE status = 'Pending'";
        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.getInt("count");
        }
    }
}