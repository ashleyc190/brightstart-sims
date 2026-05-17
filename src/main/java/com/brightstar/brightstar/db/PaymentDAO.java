package com.brightstar.brightstar.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaymentDAO {

    public record Payment(int id, int studentId, String studentName,
                          String studentNo, double amount, String date,
                          String method, String reference) {}

    // ── Record a new payment ──
    public static void insertPayment(int studentId, double amount,
                                     String date, String method,
                                     String reference) throws SQLException {
        String sql = """
            INSERT INTO payments (student_id, amount, payment_date, method, reference_number)
            VALUES (?, ?, ?, ?, ?)
        """;
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1,    studentId);
            ps.setDouble(2, amount);
            ps.setString(3, date);
            ps.setString(4, method);
            ps.setString(5, reference);
            ps.executeUpdate();
        }
    }

    // ── Get all payments for a student ──
    public static List<Payment> getPaymentsForStudent(int studentId) throws SQLException {
        List<Payment> list = new ArrayList<>();
        String sql = """
            SELECT p.id, p.student_id, s.first_name || ' ' || s.last_name as student_name,
                   s.student_no, p.amount, p.payment_date, p.method, p.reference_number
            FROM payments p
            JOIN students s ON p.student_id = s.id
            WHERE p.student_id = ?
            ORDER BY p.payment_date DESC
        """;
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    // ── Get outstanding balance for a student ──
    public static double getOutstandingBalance(int studentId) throws SQLException {
        // Monthly fee hardcoded at R2500 — replace with a fees table later
        double monthlyFee = 2500.0;
        String sql = "SELECT SUM(amount) as total FROM payments WHERE student_id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            double paid = rs.getDouble("total");
            double outstanding = monthlyFee - paid;
            return Math.max(outstanding, 0);
        }
    }

    // ── Get total outstanding fees across all students ──
    public static double getTotalOutstanding() throws SQLException {
        double monthlyFee = 2500.0;
        String countSql   = "SELECT COUNT(*) as count FROM students WHERE enrolled = 1";
        String paidSql    = "SELECT SUM(amount) as total FROM payments";

        try (Statement stmt = DatabaseManager.getConnection().createStatement()) {
            ResultSet rs1    = stmt.executeQuery(countSql);
            int studentCount = rs1.getInt("count");

            ResultSet rs2  = stmt.executeQuery(paidSql);
            double paid    = rs2.getDouble("total");

            double outstanding = (studentCount * monthlyFee) - paid;
            return Math.max(outstanding, 0);
        }
    }

    // ── Generate a reference number ──
    public static String generateReference(String method, String date) {
        String prefix = switch (method) {
            case "Credit Card" -> "CC";
            case "EFT"         -> "EFT";
            case "Cash"        -> "CASH";
            default            -> "REF";
        };
        return prefix + "-" + date + "-" + String.format("%03d",
                (int)(Math.random() * 999) + 1);
    }

    private static Payment mapRow(ResultSet rs) throws SQLException {
        return new Payment(
                rs.getInt("id"),
                rs.getInt("student_id"),
                rs.getString("student_name"),
                rs.getString("student_no"),
                rs.getDouble("amount"),
                rs.getString("payment_date"),
                rs.getString("method"),
                rs.getString("reference_number")
        );
    }
}