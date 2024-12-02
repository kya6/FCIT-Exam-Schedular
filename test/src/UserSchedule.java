import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class UserSchedule {

    private static final String DATABASE_URL = "jdbc:sqlite:userScheduleDB.sqlite";

    public static void initializeDatabase() {
        String createTableSQL = """
        CREATE TABLE IF NOT EXISTS UserSchedule (
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
            System.out.println("UserSchedule database initialized.");
        } catch (SQLException e) {
            System.err.println("Error initializing UserSchedule database: " + e.getMessage());
        }
    }

    public static void saveToUserSchedule(Course course) {
        String insertSQL = """
        INSERT INTO UserSchedule (CourseName, CourseNumber, Place, StartTime, EndTime, Day, ExamDate)
        VALUES (?, ?, ?, ?, ?, ?, ?);
        """;

        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement stmt = conn.prepareStatement(insertSQL)) {

            stmt.setString(1, course.getName());
            stmt.setString(2, course.getNumber());
            stmt.setString(3, course.getPlace());
            stmt.setString(4, course.getStartTime());
            stmt.setString(5, course.getEndTime());
            stmt.setString(6, course.getDay());
            stmt.setString(7, course.getExamDate().toString());

            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving course to UserSchedule: " + e.getMessage());
        }
    }

    public static ObservableList<Course> getAllCourses() {
        String query = "SELECT * FROM UserSchedule";
        ObservableList<Course> courses = FXCollections.observableArrayList();

        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String name = rs.getString("CourseName");
                String number = rs.getString("CourseNumber");
                String place = rs.getString("Place");
                String startTime = rs.getString("StartTime");
                String endTime = rs.getString("EndTime");
                String day = rs.getString("Day");
                LocalDate examDate = LocalDate.parse(rs.getString("ExamDate"), DateTimeFormatter.ISO_LOCAL_DATE);

                Course course = new Course(name, number, startTime, endTime, day, place, examDate);
                courses.add(course);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving courses from UserSchedule: " + e.getMessage());
        }

        return courses;
    }

    public static void deleteFromUserSchedule(Course course) {
        String deleteSQL = """
        DELETE FROM UserSchedule WHERE CourseName = ? AND CourseNumber = ?;
        """;

        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement stmt = conn.prepareStatement(deleteSQL)) {

            stmt.setString(1, course.getName());
            stmt.setString(2, course.getNumber());
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting course from UserSchedule: " + e.getMessage());
        }
    }

    public static void clearAllCourses() {
        String deleteSQL = "DELETE FROM UserSchedule";

        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement stmt = conn.prepareStatement(deleteSQL)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error clearing UserSchedule database: " + e.getMessage());
        }
    }

}
