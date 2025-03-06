package Attempt1;

import java.util.*;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

public class Teacher {  
    private static final String DB_URL = "jdbc:sqlite:C:\\Users\\User\\eclipse-workspace\\ProjectSW\\SQLite database\\testDB.db";
    
    //Teacher creating new course and quiz 
    public static List<String[]> gatherQuizQuestions(Scanner sc) {
        List<String[]> questions = new ArrayList<>();
        
        while (true) {
            System.out.print("Enter question (or type 'done' to finish): ");
            String question = sc.nextLine();
            if (question.equalsIgnoreCase("done")) {
                break;
            }
            String[] options = new String[7];
            options[0] = question;

            System.out.print("Enter option 1: ");
            options[1] = sc.nextLine();

            System.out.print("Enter option 2: ");
            options[2] = sc.nextLine();

            System.out.print("Enter option 3: ");
            options[3] = sc.nextLine();

            System.out.print("Enter option 4: ");
            options[4] = sc.nextLine();

            System.out.print("Enter correct option (1-4): ");
            options[5] = sc.nextLine();

            System.out.print("Enter weight of the question: ");
            options[6] = sc.nextLine();

            questions.add(options);
        }
        return questions;
    }
    
    //Write quiz with new course that teacher added to database in subjects table and new table for new course
    public static void writeQuizToDatabase(String subject, List<String[]> questions) {
        String subjectTable = subject.replaceAll("\\s+", "_"); 

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            conn.setAutoCommit(false);

            //Insert the new subject into the Subjects table
            String insertSubjectSQL = "INSERT OR IGNORE INTO Subjects (name) VALUES (?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertSubjectSQL)) {
                pstmt.setString(1, subject);
                pstmt.executeUpdate();
            }

            //To ensure all quizzes of each new course will step by step input to subjects table in to the database in this format
            String createTableSQL = "CREATE TABLE IF NOT EXISTS \"" + subjectTable + "\" ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "question TEXT NOT NULL, "
                    + "option1 TEXT NOT NULL, "
                    + "option2 TEXT NOT NULL, "
                    + "option3 TEXT NOT NULL, "
                    + "option4 TEXT NOT NULL, "
                    + "correct_option TEXT NOT NULL, "
                    + "weight INTEGER NOT NULL)";
            //Creates a Statement object to execute the SQL query on the database automatically 
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(createTableSQL);
            }

            // Insert the quiz questions into the subject's table
            String insertQuestionSQL = "INSERT INTO \"" + subjectTable + "\" (question, option1, option2, option3, option4, correct_option, weight) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertQuestionSQL)) {
                for (String[] question : questions) {
                    pstmt.setString(1, question[0]);
                    pstmt.setString(2, question[1]);
                    pstmt.setString(3, question[2]);
                    pstmt.setString(4, question[3]);
                    pstmt.setString(5, question[4]);
                    pstmt.setString(6, question[5]);
                    pstmt.setInt(7, Integer.parseInt(question[6]));
                    pstmt.executeUpdate();
                }
            }
            conn.commit();  
            System.out.println("Quiz saved successfully in the database.");
        } catch (SQLException e) {
            System.out.println("Error writing to database: " + e.getMessage());
        }
    }
    
    // Method to read quiz from database in the subjects table
    static List<String[]> fetchQuizFromDatabase(String subject) {
        List<String[]> quizQuestions = new ArrayList<>();
        String subjectTable = subject.replaceAll("\\s+", "_");
        String query = "SELECT question, option1, option2, option3, option4, correct_option, weight FROM \"" + subjectTable + "\"";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                quizQuestions.add(new String[]{
                    rs.getString("question"),
                    rs.getString("option1"),
                    rs.getString("option2"),
                    rs.getString("option3"),
                    rs.getString("option4"),
                    rs.getString("correct_option"),
                    rs.getString("weight")
                });
            }
        } catch (SQLException e) {
            System.out.println("Error fetching quiz from database: " + e.getMessage());
        }
        return quizQuestions;
    }
    
    // Method to list available Subjects from database
    public static List<String> fetchSubjectsFromDatabase() {
    	List<String> subjects = new ArrayList<>();
        String query = "SELECT name FROM Subjects";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                subjects.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            System.out.println("Error fetching subjects from database: " + e.getMessage());
        }
        return subjects;
    }
    
    public static int takeQuiz(Scanner sc, Student student, String subject, List<String[]> quizQuestions) {
        List<String> studentAnswers = new ArrayList<>();
        int totalScore = 0;
        int totalWeight = 0;  

        for (String[] question : quizQuestions) {
            try {
                int weight = Integer.parseInt(question[6]); 
                totalWeight += weight;

                System.out.println("\n" + question[0]); 
                for (int i = 1; i <= 4; i++) {
                    System.out.println(i + ". " + question[i]); 
                }

                System.out.print("Enter your choice (1-4) or 0 to quit: ");
                String userChoice = sc.nextLine();

                if (userChoice.equals("0")) {
                    System.out.println("Quiz exited. Your progress is saved.");
                    break;
                }

                studentAnswers.add(userChoice);

                if (userChoice.equals(question[5])) {
                    totalScore += weight;  
                    System.out.println("Correct!");
                } else {
                    System.out.println("Incorrect. Correct answer: " + question[5]);
                }
                System.out.println();
            } catch (NumberFormatException e) {
                System.out.println("Error: Invalid question format. Weight must be a number.");
            }
        }

        // Save the quiz history
        StudentManagement.saveQuizHistoryToDatabase(student.getUsername(), subject, quizQuestions, studentAnswers, totalScore);

        System.out.println("----------------------------");
        System.out.println("Total Score: " + totalScore + "/" + totalWeight);

        return totalScore;  
    }
}
