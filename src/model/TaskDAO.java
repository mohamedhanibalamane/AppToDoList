package model;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TaskDAO {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    public static void addTask(String userEmail, String title, String paragraph, 
            LocalDateTime startDateTime, LocalDateTime endDateTime,
            String type, String priority) throws SQLException {
String query = "INSERT INTO taskstabl (user_email, title, paragraph, " +
     "start_date, start_time, end_date, end_time, type, priority, state) " +
     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

try (Connection conn = DatabaseConnection.getConnection();
PreparedStatement stmt = conn.prepareStatement(query)) {

stmt.setString(1, userEmail);
stmt.setString(2, title);
stmt.setString(3, paragraph);
stmt.setString(4, startDateTime.format(DATE_FORMATTER));
stmt.setString(5, startDateTime.format(TIME_FORMATTER));
stmt.setString(6, endDateTime.format(DATE_FORMATTER));
stmt.setString(7, endDateTime.format(TIME_FORMATTER));
stmt.setString(8, type);
stmt.setString(9, priority);
stmt.setString(10, determineTaskState(startDateTime, endDateTime));

stmt.executeUpdate();
}
}
    
    private static String determineTaskState(LocalDateTime start, LocalDateTime end) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(start)) {
            return "En Attente";
        } else if (now.isAfter(end)) {
            return "Échoué"; // Modifié pour correspondre à l'ENUM
        } else {
            return "En Cours";
        }
    }
    
    public static List<Task> getUserTasks(String userEmail) throws SQLException {
        List<Task> tasks = new ArrayList<>();
        String query = "SELECT title, paragraph, state, start_date, start_time, " +
                      "end_date, end_time, type, priority FROM taskstabl WHERE user_email = ? " +
                      "ORDER BY FIELD(state, 'En Cours', 'En Attente'), end_date, end_time";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, userEmail);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Task task = new Task(
                        rs.getString("title"),
                        rs.getString("paragraph"),
                        rs.getString("state"),
                        rs.getString("start_date"),
                        rs.getString("start_time").substring(0, 5),
                        rs.getString("end_date"),
                        rs.getString("end_time").substring(0, 5),
                        rs.getString("type"),
                        rs.getString("priority")
                    );
                    tasks.add(task);
                }
            }
        }
        return tasks;
    }
    
    public static void updateTask(String userEmail, Task oldTask, Task newTask) throws SQLException {
        deleteTask(userEmail, oldTask.getTitle(), oldTask.getStartDate(), oldTask.getStartTime());
        
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        
        LocalDateTime startDateTime = LocalDateTime.of(
            LocalDate.parse(newTask.getStartDate(), dateFormatter),
            LocalTime.parse(newTask.getStartTime(), timeFormatter)
        );
        
        LocalDateTime endDateTime = LocalDateTime.of(
            LocalDate.parse(newTask.getDueDate(), dateFormatter),
            LocalTime.parse(newTask.getDueTime(), timeFormatter)
        );
        
        addTask(userEmail, newTask.getTitle(), newTask.getParagraph(), 
               startDateTime, endDateTime, 
               newTask.getType(), newTask.getPriority());
    }
    
 // Méthode pour supprimer une tâche
    public static void deleteTask(String userEmail, String title, String startDate, String startTime) throws SQLException {
        String query = "DELETE FROM taskstabl WHERE user_email = ? AND title = ? " +
                      "AND start_date = ? AND start_time = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, userEmail);
            stmt.setString(2, title);
            stmt.setString(3, startDate);
            stmt.setString(4, startTime);
            
            stmt.executeUpdate();
        }
    }
    
 // Méthode pour archiver une tâche
    public static void archiveToHistory(String userEmail, Task task, boolean isCompleted) throws SQLException {
        String query = "INSERT INTO historytab (user_email, title, paragraph, state, " +
                     "start_date, start_time, end_date, end_time, type, priority, completed_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, userEmail);
            stmt.setString(2, task.getTitle());
            stmt.setString(3, task.getParagraph());
            stmt.setString(4, isCompleted ? "Terminer" : "Échoué");
            stmt.setString(5, task.getStartDate());
            stmt.setString(6, task.getStartTime());
            stmt.setString(7, task.getDueDate());
            stmt.setString(8, task.getDueTime());
            stmt.setString(9, task.getType());
            stmt.setString(10, task.getPriority());
            
            stmt.executeUpdate();
        }
    }
    
    public static int getTaskCount(String userEmail, String state, LocalDate date) throws SQLException {
        String query = "SELECT COUNT(*) as count FROM taskstabl WHERE user_email = ? AND state = ? " +
                      "AND end_date = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, userEmail);
            stmt.setString(2, state);
            stmt.setString(3, date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }
        }
        return 0;
    }
    
    public static int getHistoryCount(String userEmail, String state, LocalDate date) throws SQLException {
        String query = "SELECT COUNT(*) as count FROM historytab WHERE user_email = ? AND state = ? " +
                      "AND DATE(completed_at) = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, userEmail);
            stmt.setString(2, state);
            stmt.setString(3, date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }
        }
        return 0;
    }
    
    public static int getTotalTaskCount(String userEmail) throws SQLException {
        String query = "SELECT COUNT(*) as count FROM taskstabl WHERE user_email = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, userEmail);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }
        }
        return 0;
    }
    
    public static int getTotalHistoryCount(String userEmail, String state) throws SQLException {
        String query = "SELECT COUNT(*) as count FROM historytab WHERE user_email = ? AND state = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, userEmail);
            stmt.setString(2, state);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }
        }
        return 0;
    }
    
    public static String getMostFailedTaskTypeLastMonth(String userEmail) throws SQLException {
        String query = "SELECT type, COUNT(*) as count FROM historytab " +
                      "WHERE user_email = ? AND state = 'Échoué' " +
                      "AND MONTH(completed_at) = MONTH(CURRENT_DATE - INTERVAL 1 MONTH) " +
                      "AND YEAR(completed_at) = YEAR(CURRENT_DATE - INTERVAL 1 MONTH) " +
                      "GROUP BY type ORDER BY count DESC LIMIT 1";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, userEmail);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("type");
                }
            }
        }
        return "None";
    }

    public static List<Task> getUserHistory(String userEmail) throws SQLException {
        List<Task> history = new ArrayList<>();
        String query = "SELECT title, paragraph, state, start_date, start_time, " +
                     "end_date, end_time, type, priority FROM historytab " +
                     "WHERE user_email = ? ORDER BY completed_at DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, userEmail);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Task task = new Task(
                        rs.getString("title"),
                        rs.getString("paragraph"),
                        rs.getString("state"),
                        rs.getString("start_date"),
                        rs.getString("start_time").substring(0, 5),
                        rs.getString("end_date"),
                        rs.getString("end_time").substring(0, 5),
                        rs.getString("type"),
                        rs.getString("priority")
                    );
                    history.add(task);
                }
            }
        }
        return history;
    }

    public static int getTaskCountForMonth(String userEmail, int month, int year) throws SQLException {
        String query = "SELECT COUNT(*) as count FROM taskstabl WHERE user_email = ? " +
                      "AND MONTH(end_date) = ? AND YEAR(end_date) = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, userEmail);
            stmt.setInt(2, month);
            stmt.setInt(3, year);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }
        }
        return 0;
    }

    public static int getHistoryCountForMonth(String userEmail, String state, int month, int year) throws SQLException {
        String query = "SELECT COUNT(*) as count FROM historytab WHERE user_email = ? AND state = ? " +
                      "AND MONTH(completed_at) = ? AND YEAR(completed_at) = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, userEmail);
            stmt.setString(2, state);
            stmt.setInt(3, month);
            stmt.setInt(4, year);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }
        }
        return 0;
    }
    
    public static int getCurrentTasksCount(String userEmail) throws SQLException {
        String query = "SELECT COUNT(*) as count FROM taskstabl " +
                      "WHERE user_email = ? AND state = 'En Cours'";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, userEmail);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }
        }
        return 0;
    }
    
 // Dans TaskDAO.java

    public static int getTotalHistoryCount(String userEmail) throws SQLException {
        String query = "SELECT COUNT(*) as count FROM historytab WHERE user_email = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, userEmail);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }
        }
        return 0;
    }

    public static int getHistoryCountForMonth(String userEmail, int month, int year) throws SQLException {
        String query = "SELECT COUNT(*) as count FROM historytab WHERE user_email = ? " +
                      "AND MONTH(completed_at) = ? AND YEAR(completed_at) = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, userEmail);
            stmt.setInt(2, month);
            stmt.setInt(3, year);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }
        }
        return 0;
    }
    
}