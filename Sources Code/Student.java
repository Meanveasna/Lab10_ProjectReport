package Attempt1;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Student {
    private int id;
    private String name;
    private String password;
    private String gender;
    private static final String DB_URL = "jdbc:sqlite:C:\\Users\\User\\eclipse-workspace\\ProjectSW\\SQLite database\\testDB.db";
    
    public Student(int id, String name, String password, String gender) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.gender = gender;
    }
    
    public int getId() {
        return id;
    }

    public String getUsername() {
        return name;
    }
    
    public String getPassword() {
        return password;
    }
    
    public String getGender() {
        return gender;
    }

    public static Student authenticateStudent(String username, String password) {
        String query = "SELECT * FROM StudentsList WHERE username = ? AND password = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
            PreparedStatement pstmt = conn.prepareStatement(query)) {
             
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) { 
                return new Student(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("gender")
                );
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
        return null; 
    }
    // Method to fetch and display quiz history for the student
    public static void displayQuizHistory(String studentUsername, String selectedCourse) {
        String query = "SELECT question, options, student_answer, correct_option FROM QuizHistory WHERE username = ? AND subject = ? ORDER BY date_taken DESC";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, studentUsername);
            stmt.setString(2, selectedCourse);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("No quiz history found for " + selectedCourse + ".");
                    return; 
                }

                do {
                    System.out.println("Question: " + rs.getString("question"));

                    String[] options = rs.getString("options").split(",");
                    System.out.println("Options:");
                    for (int i = 0; i < options.length; i++) {
                        System.out.println((i + 1) + ". " + options[i]);
                    }

                    System.out.println("Your answer: " + rs.getString("student_answer"));
                    System.out.println("Correct answer: " + rs.getString("correct_option"));
                    System.out.println("-----------------------------------");
                } while (rs.next());

            }
        } catch (SQLException e) {
            System.out.println("Error fetching quiz history: " + e.getMessage());
        }
    }
    
    public static void viewAllStudentScores() {
        String query = "SELECT username, subject, total_score, date_taken FROM TotalQuizScore " +
                       "ORDER BY total_score DESC"; 

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            System.out.println("\n--- Student Scores (High to Low) ---");
            System.out.printf("%-15s %-30s %-10s %-15s\n", "Username", "Subject", "Score", "Date Taken");
            System.out.println("-----------------------------------------------------------------------");

            boolean hasRecords = false;
            while (rs.next()) {
                hasRecords = true;
                String username = rs.getString("username");
                String subject = rs.getString("subject");
                int totalScore = rs.getInt("total_score");
                String dateTaken = rs.getString("date_taken");

                System.out.printf("%-15s %-30s %-10d %-15s\n", username, subject, totalScore, dateTaken);
            }

            if (!hasRecords) {
                System.out.println("No quiz scores available.");
            }

        } catch (SQLException e) {
            System.out.println("Error fetching student scores: " + e.getMessage());
        }
    }



}