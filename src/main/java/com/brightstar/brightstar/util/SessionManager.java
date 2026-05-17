package com.brightstar.brightstar.util;

import com.brightstar.brightstar.db.UserDAO;

public class SessionManager {

    private static UserDAO.User currentUser;

    public static void setCurrentUser(UserDAO.User user) {
        currentUser = user;
    }

    public static UserDAO.User getCurrentUser() {
        return currentUser;
    }

    public static String getRole() {
        return currentUser != null ? currentUser.role() : "";
    }

    public static String getName() {
        return currentUser != null ? currentUser.name() : "";
    }

    public static boolean isAdmin() {
        return "admin".equals(getRole());
    }

    public static void logout() {
        currentUser = null;
    }

}
