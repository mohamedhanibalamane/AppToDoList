package model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    // Vérifier si un utilisateur existe déjà
    public static boolean userExists(String email) throws SQLException {
        String query = "SELECT * FROM userstab WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }
    
    // Ajouter un nouvel utilisateur
    public static void addUser(String fullName, String email, String password) throws SQLException {
        String query = "INSERT INTO userstab (full_name, email, password) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, fullName);
            stmt.setString(2, email);
            stmt.setString(3, password);
            stmt.executeUpdate();
        }
    }
    
    // Vérifier les informations de connexion
    public static boolean validateLogin(String email, String password) throws SQLException {
        String query = "SELECT * FROM userstab WHERE email = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            stmt.setString(2, password);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }
    
    // Récupérer le nom complet d'un utilisateur
    public static String getUserFullName(String email) throws SQLException {
        String query = "SELECT full_name FROM userstab WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("full_name");
                }
            }
        }
        return null;
    }
}