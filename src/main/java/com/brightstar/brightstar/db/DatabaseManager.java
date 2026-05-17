package com.brightstar.brightstar.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private static final String DB_NAME = "brightstar.db";
    private static Connection connection;

    private static String getDbPath() {
        String home = System.getProperty("user.home");
        File dir = new File(home, "Brightstar");
        dir.mkdirs();
        return new File(dir, DB_NAME).getAbsolutePath();
    }

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection("jdbc:sqlite:" + getDbPath());
        }
        return connection;
    }

    public static void initialise() throws SQLException {
        try (Statement stmt = getConnection().createStatement()) {
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS users (
                    id       INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE NOT NULL,
                    password TEXT NOT NULL,
                    role     TEXT NOT NULL CHECK(role IN ('admin','teacher')),
                    name     TEXT NOT NULL
                )
            """);

            // Classes
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS classes (
                    id   INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT UNIQUE NOT NULL
                )
            """);

            // Students
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS students (
                    id               INTEGER PRIMARY KEY AUTOINCREMENT,
                    student_no       TEXT UNIQUE NOT NULL,
                    first_name       TEXT NOT NULL,
                    last_name        TEXT NOT NULL,
                    dob              TEXT,
                    gender           TEXT,
                    enrolment_date   TEXT,
                    class_id         INTEGER,
                    guardian_name    TEXT,
                    relationship     TEXT,
                    phone            TEXT,
                    email            TEXT,
                    enrolled         INTEGER DEFAULT 1,
                    FOREIGN KEY (class_id) REFERENCES classes(id)
                )
            """);

            // Attendance
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS attendance (
                    id         INTEGER PRIMARY KEY AUTOINCREMENT,
                    student_id INTEGER NOT NULL,
                    class_id   INTEGER NOT NULL,
                    date       TEXT NOT NULL,
                    status     TEXT NOT NULL CHECK(status IN ('Present','Absent','Late')),
                    submitted  INTEGER DEFAULT 0,
                    FOREIGN KEY (student_id) REFERENCES students(id),
                    FOREIGN KEY (class_id)   REFERENCES classes(id)
                )
            """);

            // Payments
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS payments (
                    id               INTEGER PRIMARY KEY AUTOINCREMENT,
                    student_id       INTEGER NOT NULL,
                    amount           REAL NOT NULL,
                    payment_date     TEXT NOT NULL,
                    method           TEXT,
                    reference_number TEXT,
                    FOREIGN KEY (student_id) REFERENCES students(id)
                )
            """);

            // Notifications
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS notifications (
                    id         INTEGER PRIMARY KEY AUTOINCREMENT,
                    recipient  TEXT NOT NULL,
                    subject    TEXT NOT NULL,
                    message    TEXT,
                    status     TEXT DEFAULT 'Pending',
                    sent_date  TEXT
                )
            """);

            seedUsers(stmt);
            seedClasses(stmt);

            System.out.println("Database initialised at: " + getDbPath());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void seedUsers(Statement stmt) throws SQLException {
        stmt.executeUpdate("""
            INSERT OR IGNORE INTO users (username, password, role, name)
            VALUES (
                   'admin', 'admin123', 'admin', 'Administrator'
            ),
                   (
                    'teacher', 'teacher123', 'teacher', 'Ms. Johnson'
                   )
        """);
    }

    private static void seedClasses(Statement stmt) throws SQLException {
        stmt.executeUpdate("""
            INSERT OR IGNORE INTO classes (name)
                   VALUES('Rainbow Class'),
                        ('Sunshine Class'),
                        ('Star Class')
        """);
    }

}
