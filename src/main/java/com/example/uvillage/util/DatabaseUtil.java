package com.example.uvillage.util;

public class DatabaseUtil {
    public static boolean authenticateUser(String username, String password) {
        return "admin".equals(username) && "admin".equals(password);
    }

}
