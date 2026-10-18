package com.torion.dao;

import com.torion.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDao {

    public UserDao() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            defaultAdminGenerator(conn);
        } catch (SQLException e) {
            System.err.println("Error seeding default admin: " + e.getMessage());
        }
    }

    public void defaultAdminGenerator(Connection conn) {
        String checkSql = "SELECT COUNT(*) FROM User WHERE role = 'ADMIN'";
        String insertSql = "INSERT INTO User(full_name, username, password_hash, role, status) VALUES ('adminuser', 'admin', 'admin123', 'ADMIN', 'APPROVED')";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(checkSql)) {
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.executeUpdate(insertSql);
                System.out.println("-> Default Admin created");
            }
        } catch (SQLException e) {
            System.err.println("Error seeding default admin: " + e.getMessage());
        }
    }

    public static boolean registerUser(String full_name, String username, String password_hash, String role) {

        String sql = "INSERT INTO User(full_name, username, password_hash, role, status) VALUES (?, ?, ?, ?, 'PENDING')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, full_name);
            pstmt.setString(2, username);
            pstmt.setString(3, password_hash);
            pstmt.setString(4, role.toUpperCase());
            pstmt.executeUpdate();
            System.out.println("-> Successful Registration, Waiting for Approval!");
            return true;

        } catch (SQLException e) {
            System.err.println("Failed: " + e.getMessage());
            return false;
        }
    }

    public User login(String username, String password_hash) {

        String sql = "SELECT user_id, full_name, username, password_hash, role, status FROM User WHERE username = ? AND password_hash = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password_hash);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String status = rs.getString("status");
                    if ("PENDING".equalsIgnoreCase(status)) {
                        System.out.println(" -> Account is pending, waiting for approval");
                        return null;
                    }
                    return new User(
                            rs.getInt("user_id"),
                            rs.getString("full_name"),
                            rs.getString("username"),
                            rs.getString("password_hash"),
                            rs.getString("role"),
                            status
                    );
                } else {
                    System.out.println(" -> Invalid username or password");
                }

            }

            } catch (Exception e) {
                System.err.println("Error during login: " + e.getMessage());
            }
            return null;
        }


        public List<User> getAllUser(){
            List<User> usersList = new ArrayList<>();
            String sql = "SELECT user_id, full_name, username, password_hash, role, status FROM User";

            try (Connection conn = DatabaseConnection.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)){

                while (rs.next()){
                    usersList.add(new User(
                            rs.getInt("user_id"),
                            rs.getString("full_name"),
                            rs.getString("username"),
                            rs.getString("password_hash"),
                            rs.getString("role"),
                            rs.getString("status")

                    ));
                }
            } catch (SQLException e) {
                System.err.println("Error fetching all users: " + e.getMessage());
            }
            return usersList;


        }

}


