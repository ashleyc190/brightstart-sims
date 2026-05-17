package com.brightstar.brightstar.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {

    public record Student(int id, String studentNo, String firstName,
                          String lastName, String dob, String gender,
                          String enrolmentDate, String className,
                          String guardianName, String relationship,
                          String phone, String email) {}

    // ── Generate next student number e.g. S001, S002 ──
    public static String generateStudentNo() throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM students";
        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            int count = rs.getInt("count") + 1;
            return String.format("S%03d", count);
        }
    }

    // ── Insert new student ──
    public static void insertStudent(String studentNo, String firstName,
                                     String lastName,  String dob,
                                     String gender,    String enrolmentDate,
                                     int classId,      String guardianName,
                                     String relationship, String phone,
                                     String email) throws SQLException {
        String sql = """
            INSERT INTO students (student_no, first_name, last_name, dob, gender,
                                  enrolment_date, class_id, guardian_name,
                                  relationship, phone, email)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1,  studentNo);
            ps.setString(2,  firstName);
            ps.setString(3,  lastName);
            ps.setString(4,  dob);
            ps.setString(5,  gender);
            ps.setString(6,  enrolmentDate);
            ps.setInt(7,     classId);
            ps.setString(8,  guardianName);
            ps.setString(9,  relationship);
            ps.setString(10, phone);
            ps.setString(11, email);
            ps.executeUpdate();
        }
    }

    // ── Get all enrolled students ──
    public static List<Student> getAllStudents() throws SQLException {
        List<Student> list = new ArrayList<>();
        String sql = """
            SELECT s.id, s.student_no, s.first_name, s.last_name, s.dob,
                   s.gender, s.enrolment_date, c.name as class_name,
                   s.guardian_name, s.relationship, s.phone, s.email
            FROM students s
            LEFT JOIN classes c ON s.class_id = c.id
            WHERE s.enrolled = 1
            ORDER BY s.first_name
        """;
        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    // ── Get students by class ──
    public static List<Student> getStudentsByClass(int classId) throws SQLException {
        List<Student> list = new ArrayList<>();
        String sql = """
            SELECT s.id, s.student_no, s.first_name, s.last_name, s.dob,
                   s.gender, s.enrolment_date, c.name as class_name,
                   s.guardian_name, s.relationship, s.phone, s.email
            FROM students s
            LEFT JOIN classes c ON s.class_id = c.id
            WHERE s.class_id = ? AND s.enrolled = 1
            ORDER BY s.first_name
        """;
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, classId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    // ── Get total student count ──
    public static int getTotalCount() throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM students WHERE enrolled = 1";
        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.getInt("count");
        }
    }

    // ── Get all class names for dropdowns ──
    public static List<String> getClassNames() throws SQLException {
        List<String> list = new ArrayList<>();
        String sql = "SELECT name FROM classes ORDER BY name";
        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(rs.getString("name"));
        }
        return list;
    }

    // ── Get class id by name ──
    public static int getClassIdByName(String name) throws SQLException {
        String sql = "SELECT id FROM classes WHERE name = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id");
        }
        return -1;
    }

    // ── Get class name by id ──
    public static String getClassNameById(int classId) throws SQLException {
        String sql = "SELECT name FROM classes WHERE id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, classId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("name");
        }
        return "";
    }

    // ── Map ResultSet row to Student record ──
    private static Student mapRow(ResultSet rs) throws SQLException {
        return new Student(
                rs.getInt("id"),
                rs.getString("student_no"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("dob"),
                rs.getString("gender"),
                rs.getString("enrolment_date"),
                rs.getString("class_name"),
                rs.getString("guardian_name"),
                rs.getString("relationship"),
                rs.getString("phone"),
                rs.getString("email")
        );
    }
}