package Attempt1;

import java.util.*;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class StudentManagement {
    private static final String DB_URL = "jdbc:sqlite:C:\\Users\\User\\eclipse-workspace\\ProjectSW\\SQLite database\\testDB.db";
    
    //Verifying method, 1 attempt for each student to do quiz in each course
    public static boolean hasTakenQuiz(String studentUsername, String subject) {
        String query = "SELECT * FROM QuizHistory WHERE username = ? AND subject = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, studentUsername);
            stmt.setString(2, subject);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.out.println("Error checking quiz history: " + e.getMessage());
        }
        return false;
    }

    public static void saveQuizHistoryToDatabase(String studentUsername, String subject, List<String[]> quizQuestions, List<String> studentAnswers, int totalScore) {
        String query = "INSERT INTO QuizHistory (username, subject, question, options, student_answer, correct_option, score, date_taken) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String totalScoreQuery = "INSERT OR REPLACE INTO TotalQuizScore (username, subject, total_score, date_taken) VALUES (?, ?, ?, ?)";  

        if (quizQuestions.size() != studentAnswers.size()) {
            System.out.println("Warning: Mismatch in size between quiz questions and student answers. Saving only answered questions...");
        }
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DB_URL); 
            conn.setAutoCommit(false);  

            try (PreparedStatement insertStmt = conn.prepareStatement(query);
                 PreparedStatement totalScoreStmt = conn.prepareStatement(totalScoreQuery)) {

                for (int i = 0; i < quizQuestions.size(); i++) {
                    String studentAnswer = studentAnswers.size() > i ? studentAnswers.get(i) : ""; // i is current question
                    if (!studentAnswer.isEmpty()) { 
                        String[] questionData = quizQuestions.get(i);
                        String question = questionData[0];  
                        String options = String.join(", ", Arrays.copyOfRange(questionData, 1, 5)); // Extract the answer options from the questionData array
                        String correctAnswer = questionData[5]; 

                        int score = 0; 
                        if (studentAnswer.equals(correctAnswer)) {
                            try {
                                score = Integer.parseInt(questionData[6]); 
                            } catch (NumberFormatException e) {
                                System.out.println("Invalid score format in quiz data: " + e.getMessage());
                            }
                        }

                        insertStmt.setString(1, studentUsername);
                        insertStmt.setString(2, subject);
                        insertStmt.setString(3, question);
                        insertStmt.setString(4, options);
                        insertStmt.setString(5, studentAnswer);
                        insertStmt.setString(6, correctAnswer);
                        insertStmt.setInt(7, score);
                        insertStmt.setString(8, LocalDate.now().toString()); 

                        insertStmt.addBatch();
                    }
                }

                totalScoreStmt.setString(1, studentUsername);
                totalScoreStmt.setString(2, subject);
                totalScoreStmt.setInt(3, totalScore);
                totalScoreStmt.setString(4, LocalDate.now().toString());  

                insertStmt.executeBatch();  
                totalScoreStmt.executeUpdate(); 

                conn.commit();  
            } catch (SQLException e) {
                System.out.println("Error saving quiz history to database: " + e.getMessage());
                if (conn != null) {
                    try {
                        conn.rollback(); 
                    } catch (SQLException rollbackEx) {
                        System.out.println("Error during rollback: " + rollbackEx.getMessage());
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error establishing database connection: " + e.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.close();  
                } catch (SQLException e) {
                    System.out.println("Error closing connection: " + e.getMessage());
                }
            }
        }
    }

    public static void viewQuizHistory(String studentUsername, String subject) {
        String query = "SELECT * FROM QuizHistory WHERE username = ? AND subject = ? ORDER BY date_taken DESC";  

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, studentUsername);  
            stmt.setString(2, subject);  

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Display quiz history
                    System.out.println("Quiz History for " + subject + ":");
                    do {
                    	System.out.println();
                        String question = rs.getString("question");
                        String options = rs.getString("options");
                        String studentAnswer = rs.getString("student_answer");
                        String correctAnswer = rs.getString("correct_option");

                        System.out.println("Question: " + question);
                        System.out.println("Options: " + options);
                        System.out.println("Your Answer: " + studentAnswer);
                        System.out.println("Correct Answer: " + correctAnswer);
                        System.out.println();
                    } while (rs.next());  
                } else {
                    System.out.println("No history for this subject.");
                }
            }

        } catch (SQLException e) {
            System.out.println("Error fetching quiz history: " + e.getMessage());
        }
    }
}
