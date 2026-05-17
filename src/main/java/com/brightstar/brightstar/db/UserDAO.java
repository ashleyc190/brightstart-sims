package com.brightstar.brightstar.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    public record User(int id, String username, String role, String name) {}

    public static User login(String username, String password) throws SQLException {
        String sql = "SELECT id, username, role, name FROM users " + "Where username = ? AND password = ?";

        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("role"),
                        rs.getString("name")
                );
            }
        }

        return null;
    }

}
