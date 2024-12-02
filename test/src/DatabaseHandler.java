import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDate;
import java.time.chrono.HijrahDate;
import java.time.format.DateTimeFormatter;


public class DatabaseHandler {

    private static final String DATABASE_URL = "jdbc:sqlite:examScheduleDB.sqlite";
    private static final String TABLE_NAME = "ExamSchedule";

    public static void initializeDatabase() {
        String createTableSQL = """
        CREATE TABLE IF NOT EXISTS ExamSchedule (
            CourseName TEXT NOT NULL,
            CourseNumber TEXT NOT NULL,
            Place TEXT NOT NULL,
            StartTime TEXT NOT NULL,
            EndTime TEXT NOT NULL,
            Day TEXT NOT NULL,
            ExamDate TEXT NOT NULL
        );
        """;

        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement stmt = conn.prepareStatement(createTableSQL)) {
            stmt.execute();
        } catch (Exception e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Inserts data into the database
    public static void saveToDatabase(String courseName, String courseNumber, String place,
                                      String startTime, String endTime, String day, String examDate) {
        String insertSQL = """
    INSERT INTO ExamSchedule (CourseName, CourseNumber, Place, StartTime, EndTime, Day, ExamDate)
    VALUES (?, ?, ?, ?, ?, ?, ?);
    """;

        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement stmt = conn.prepareStatement(insertSQL)) {

            // Convert Hijri to Gregorian
            String convertedDate = convertHijriToGregorian(examDate, "dd/MM/yyyy", "yyyy-MM-dd");
            if (convertedDate == null) {
                System.err.println("Skipping invalid date: " + examDate);
                return; // Skip invalid date
            }

            stmt.setString(1, courseName);
            stmt.setString(2, courseNumber);
            stmt.setString(3, place);
            stmt.setString(4, startTime);
            stmt.setString(5, endTime);
            stmt.setString(6, day);
            stmt.setString(7, convertedDate);

            int rowsInserted = stmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("Error saving data: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public static Course getCourseFromExamSchedule(String courseName, String courseNumber) {
        String query = """
        SELECT CourseName, CourseNumber, Place, StartTime, EndTime, Day, ExamDate
        FROM ExamSchedule
        WHERE CourseName = ? AND CourseNumber = ?;
    """;

        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, courseName);
            stmt.setString(2, courseNumber);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                // Extract course details
                String name = rs.getString("CourseName");
                String number = rs.getString("CourseNumber");
                String place = rs.getString("Place");
                String startTime = rs.getString("StartTime");
                String endTime = rs.getString("EndTime");
                String day = rs.getString("Day");
                String examDate = rs.getString("ExamDate");
                LocalDate dateFormat = LocalDate.parse(examDate);
                return new Course(name, number, startTime, endTime, day, place, dateFormat);
            }
        } catch (Exception e) {
            System.err.println("Error retrieving course from database: " + e.getMessage());
            e.printStackTrace();
        }

        // Return null if the course is not found
        return null;
    }

    public static void clear() {
        String deleteSQL = "DELETE FROM ExamSchedule";

        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement stmt = conn.prepareStatement(deleteSQL)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static String convertHijriToGregorian(String hijriDate, String inputPattern, String outputPattern) {
        try {
            // Split the Hijri date into day, month, and year
            String[] dateParts = hijriDate.split("/");
            if (dateParts.length != 3) {
                throw new IllegalArgumentException("Invalid Hijri date format: " + hijriDate);
            }

            int day = Integer.parseInt(dateParts[0]);
            int month = Integer.parseInt(dateParts[1]);
            int year = Integer.parseInt(dateParts[2]);

            // Create a HijrahDate instance
            HijrahDate hijrahDate = HijrahDate.of(year, month, day);

            // Convert to Gregorian date
            LocalDate gregorianDate = LocalDate.from(hijrahDate);

            // Format Gregorian date to desired output format
            DateTimeFormatter gregorianFormatter = DateTimeFormatter.ofPattern(outputPattern);

            return gregorianDate.format(gregorianFormatter);
        } catch (Exception e) {
            System.err.println("Error converting Hijri to Gregorian: " + e.getMessage());
            return null; // Return null for invalid input
        }
    }

}
