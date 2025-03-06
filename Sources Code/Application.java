package Attempt1;

import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class Application {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("\n--- Welcome to Course Management ---");
            System.out.println("1. Teacher Login");
            System.out.println("2. Student Login");
            System.out.println("3. Exit");
            System.out.print("Enter your choice: ");
            int userType = 0;
            try {
                userType = sc.nextInt();
                sc.nextLine();
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number.");
                sc.nextLine();
                continue;
            }
            switch (userType) {
                case 1:
                    teacherMenu(sc);
                    break;
                case 2:
                    studentMenu(sc);
                    break;
                case 3:
                    System.out.println("Exiting...");
                    sc.close();
                    return;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }
    //Option1 Teacher Login
    private static void teacherMenu(Scanner sc) {
        while (true) {
            System.out.println("\n--- Teacher Menu ---");
            System.out.println("1. List Courses");
            System.out.println("2. Create a New Course & Quiz");
            System.out.println("3. View all student score in each subject");
            System.out.println("4. Back");
            System.out.print("Enter your choice: ");
            int choice = 0;
            try {
                choice = sc.nextInt();
                sc.nextLine();
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number.");
                sc.nextLine();
                continue;
            }
            switch (choice) {
            	//List courses available from database subjects table
                case 1:
                	List<String> courses = Teacher.fetchSubjectsFromDatabase();
                    if (courses.isEmpty()) {
                        System.out.println("No courses available.");
                    } else {
                    	System.out.println("----------------------------");
                        System.out.println("Available Courses: "); //will read from database StudentList table
                        for (String course : courses) {
                            System.out.println(course); 
                        }
                        System.out.println("----------------------------");
                    }
                    break;
                //Teacher create a new course & quiz than will update to database immediately  
                case 2:
                    System.out.print("Enter subject name for new course: ");
                    String newSubject = sc.nextLine();
                    //Call gatherQuizQuestions method from Teacher class for creating course and quiz processing
                    List<String[]> quizQuestions = Teacher.gatherQuizQuestions(sc);
                    
                    if (!quizQuestions.isEmpty()) {
                        //Write quiz to database after collecting questions
                        Teacher.writeQuizToDatabase(newSubject, quizQuestions);
                        
                        List<String> updatedCourses = Teacher.fetchSubjectsFromDatabase();
                        System.out.println("New course added successfully! Here is the updated list of courses:");
                        for (String course : updatedCourses) {
                            System.out.println(course);  // Display the updated list
                        }
                    } else {
                        System.out.println("No quiz questions entered.");
                    }
                    break;
                case 3:
                    Student.viewAllStudentScores();
                    break;
                case 4:
                    return;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }
    //Option2 Student Login
    private static void studentMenu(Scanner sc) {
        System.out.print("Enter username: ");
        String username = sc.nextLine();
        System.out.print("Enter password: ");
        String password = sc.nextLine();

        Student student = Student.authenticateStudent(username, password); 
        if (student == null) {
            System.out.println("Invalid credentials. Try again.");
            return;
        }
        System.out.println("Login Successful! Welcome, " + student.getUsername());
        
        while (true) {
            System.out.println("\n--- Student Menu ---");
            System.out.println("1. Take a Quiz");
            System.out.println("2. View Quiz History");
            System.out.println("3. Back");
            System.out.print("Enter your choice (1-3): ");
            
            int choice = 0; 
            try {
                choice = sc.nextInt();
                sc.nextLine();  
            } catch (InputMismatchException e) {
                System.out.println("Invalid choice. Please enter a number.");
                sc.nextLine(); 
                continue;  
            }

            switch (choice) {
            	//Take a Quiz
                case 1:
                	List<String> courses = Teacher.fetchSubjectsFromDatabase();
                    if (courses.isEmpty()) {
                        System.out.println("No courses available.");
                        break;
                    }
                    System.out.println("----------------------------");
                    System.out.println("Available Courses:");
                    for (int i = 0; i < courses.size(); i++) {
                        System.out.println((i + 1) + ". " + courses.get(i));
                    }
                    System.out.println("----------------------------");

                    System.out.print("Enter the number of the subject you want to take a quiz on: ");
                    int subjectChoice = sc.nextInt();
                    sc.nextLine();

                    if (subjectChoice < 1 || subjectChoice > courses.size()) {
                        System.out.println("Invalid choice. Try again.");
                        break;
                    }

                    String selectedSubject = courses.get(subjectChoice - 1);
                    System.out.println("You selected: " + selectedSubject);
                    
                    //Verifying if student took a quiz in each course already
                    if (StudentManagement.hasTakenQuiz(student.getUsername(), selectedSubject)) {
                        System.out.println("You have already taken the quiz for " + selectedSubject + ". You can only view your history.");
                    } else {
                        // Fetch quiz questions from the database
                        List<String[]> quizQuestions = Teacher.fetchQuizFromDatabase(selectedSubject);
                        if (!quizQuestions.isEmpty()) {
                            Teacher.takeQuiz(sc, student, selectedSubject, quizQuestions);
                        } else {
                            System.out.println("No quiz found for this subject.");
                        }
                    }
                    break;
                //View Quiz History
                case 2:
                    // Prompt student to choose the subject for quiz history
                    List<String> availableSubjects = Teacher.fetchSubjectsFromDatabase();
                    if (availableSubjects.isEmpty()) {
                        System.out.println("No courses available.");
                        break;
                    }

                    System.out.println("----------------------------");
                    System.out.println("Available Courses:");
                    for (int i = 0; i < availableSubjects.size(); i++) {
                        System.out.println((i + 1) + ". " + availableSubjects.get(i));
                    }
                    System.out.println("----------------------------");

                    System.out.print("Enter the number of the subject you want to view quiz history for: ");
                    int historySubjectChoice = sc.nextInt();
                    sc.nextLine();

                    if (historySubjectChoice < 1 || historySubjectChoice > availableSubjects.size()) {
                        System.out.println("Invalid choice. Try again.");
                        break;
                    }

                    String selectedHistorySubject = availableSubjects.get(historySubjectChoice - 1);
                    System.out.println("You selected: " + selectedHistorySubject);

                    // Call viewQuizHistory with the student's username and the selected subject
                    StudentManagement.viewQuizHistory(username, selectedHistorySubject);
                    break;
                case 3:
                    return; 

                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }


}