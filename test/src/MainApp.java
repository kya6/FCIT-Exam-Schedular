/*
Before running the MainApp, make sure you run the Server class first.

FCIT Exam Schedular By
Mohammad Alkhayat </>
Abdulrahman Barashid </>
Hattan Alzahrani </>

 */

import com.sun.javafx.menu.MenuItemBase;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.concurrent.Task;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class MainApp extends Application {

    private ScheduleTable scheduleTable;
    private CourseFileHandler fileHandler;

    @Override
    public void start(Stage primaryStage) {
        try {
            Socket socket = new Socket("localhost", 12345);
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());

            // Initialize components
            fileHandler = new CourseFileHandler();
            scheduleTable = new ScheduleTable();
            UserSchedule.initializeDatabase();

            // Image banner
            ImageView banner = new ImageView(new Image("file:banner.png"));
            banner.setFitHeight(200);
            banner.setPreserveRatio(false);
            banner.setFitWidth(1000);

            // Hardcoded course numbers for each course name
            Map<String, List<String>> courseNumbersMap = new HashMap<>();
            courseNumbersMap.put("CPCS", List.of("202", "203", "204", "211", "212", "214", "222", "223", "241", "301", "302", "324", "331", "351", "361", "371", "381", "391"));
            courseNumbersMap.put("CPIT", List.of("201", "210", "220", "240", "250", "251", "252", "260", "280", "285", "305", "330", "345", "370", "380", "405", "425", "435", "470"));
            courseNumbersMap.put("CPIS", List.of("210", "220", "222", "240", "250", "312", "334", "342", "351", "352", "354", "357", "358", "370", "380", "428", "434"));

            ComboBox<String> courseNameDropdown = new ComboBox<>();
            ;
            courseNameDropdown.getItems().addAll("CPCS", "CPIT", "CPIS");
            courseNameDropdown.setPromptText("Select Course Name");

            ComboBox<String> courseNumberDropdown = new ComboBox<>();
            courseNumberDropdown.setPromptText("Select Course Number");
            courseNumberDropdown.setEditable(true);

            // Update course numbers based on selected course name
            courseNameDropdown.setOnAction(event -> {
                String selectedCourseName = courseNameDropdown.getValue();
                if (selectedCourseName != null) {
                    List<String> courseNumbers = courseNumbersMap.getOrDefault(selectedCourseName, List.of());
                    courseNumberDropdown.getItems().setAll(courseNumbers);
                } else {
                    courseNumberDropdown.getItems().clear();
                }
            });

            // Create "Add Course" button
            Button addCourseButton = new Button("Add Course");
            addCourseButton.setOnAction(event -> {
                String courseName = courseNameDropdown.getValue();
                String courseNumber = courseNumberDropdown.getValue();

                if (courseName == null || courseNumber == null) {
                    showAlert(Alert.AlertType.WARNING, "Invalid Selection", "Please select both course name and course number.");
                    return;
                }

                Task<Course> addCourseTask = new Task<>() {
                    @Override
                    protected Course call() throws Exception {
                        return DatabaseHandler.getCourseFromExamSchedule(courseName, courseNumber);
                    }
                };

                addCourseTask.setOnSucceeded(e -> {
                    Course course = addCourseTask.getValue();
                    if (course != null) {
                        scheduleTable.addCourse(course);
                        UserSchedule.saveToUserSchedule(course); // Add to UserSchedule database
                        showAlert(Alert.AlertType.INFORMATION, "Course Added", "Course added to your schedule.");
                    } else {
                        showAlert(Alert.AlertType.WARNING, "Course Not Found", "The selected course was not found in the database.");
                    }
                });

                addCourseTask.setOnFailed(e -> {
                    Throwable exception = addCourseTask.getException();
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to add course: " + exception.getMessage());
                    exception.printStackTrace();
                });

                new Thread(addCourseTask).start();
            });

            Button saveCsvButton = new Button("Save as CSV");
            saveCsvButton.setOnAction(event -> {
                ObservableList<Course> allCourses = scheduleTable.getAllCourses();
                if (allCourses.isEmpty()) {
                    showAlert(Alert.AlertType.WARNING, "No Courses", "No courses are displayed in the schedule.");
                    return;
                }
                fileHandler.saveCoursesToCSV(allCourses);
                showAlert(Alert.AlertType.INFORMATION, "Export Successful", "All courses have been exported to CSV.");
            });


            Button saveIcsButton = new Button("Save as ICS");
            saveIcsButton.setOnAction(event -> {
                ObservableList<Course> allCourses = scheduleTable.getAllCourses();
                if (allCourses.isEmpty()) {
                    showAlert(Alert.AlertType.WARNING, "No Courses", "No courses are displayed in the schedule.");
                    return;
                }
                fileHandler.saveCoursesToICS(allCourses);
                showAlert(Alert.AlertType.INFORMATION, "Export Successful", "All courses have been exported to ICS.");
            });

            Button loadCoursesButton = new Button("Load Courses");
            loadCoursesButton.setOnAction(event -> {
                ObservableList<Course> userCourses = UserSchedule.getAllCourses();
                if (userCourses.isEmpty()) {
                    showAlert(Alert.AlertType.INFORMATION, "No Courses", "No courses found in your saved schedule.");
                } else {
                    scheduleTable.addCourses(userCourses);
                    showAlert(Alert.AlertType.INFORMATION, "Courses Loaded", "Courses loaded into your schedule.");
                }
            });

            // Input fields for adding courses manually :)
            TextField manualCourseName = new TextField();
            manualCourseName.setPromptText("Course Name");
            TextField manualCourseNumber = new TextField();
            manualCourseNumber.setPromptText("Course Number");
            TextField manualPlace = new TextField();
            manualPlace.setPromptText("Place");
            TextField manualStartTime = new TextField();
            manualStartTime.setPromptText("Start Time (HH:MM)");
            TextField manualEndTime = new TextField();
            manualEndTime.setPromptText("End Time (HH:MM)");
            ComboBox<String> manualDay = new ComboBox<>();
            manualDay.getItems().addAll("Sunday", "Monday", "Tuesday", "Wenesday", "Thursday", "Friday", "Saturday");
            manualDay.setPromptText("Day");
            TextField manualDate = new TextField();
            manualDate.setPromptText("Exam Date (YYYY-MM-DD)");

            Button manualAddButton = new Button("Add Manually");
            manualAddButton.setOnAction(event -> {
                String courseName = manualCourseName.getText();
                String courseNumber = manualCourseNumber.getText();
                String place = manualPlace.getText();
                String startTime = manualStartTime.getText();
                String endTime = manualEndTime.getText();
                String day = manualDay.getValue();
                String date = manualDate.getText();

                if (courseName.isEmpty() || !courseName.matches("[a-zA-Z]+")) {
                    showAlert(Alert.AlertType.WARNING, "Invalid Input", "Course Name must be only characters.");
                    return;
                }
                if (courseNumber.isEmpty() || !courseNumber.matches("\\d+")) {
                    showAlert(Alert.AlertType.WARNING, "Invalid Input", "Course Number must be only digits.");
                    return;
                }
                if (place.isEmpty()) {
                    showAlert(Alert.AlertType.WARNING, "Invalid Input", "Place cannot be empty.");
                    return;
                }
                if (startTime.isEmpty() || !startTime.matches("\\d{2}:\\d{2}")) {
                    showAlert(Alert.AlertType.WARNING, "Invalid Input", "Start Time must be in HH:MM format.");
                    return;
                }
                if (endTime.isEmpty() || !endTime.matches("\\d{2}:\\d{2}")) {
                    showAlert(Alert.AlertType.WARNING, "Invalid Input", "End Time must be in HH:MM format.");
                    return;
                }
                if (day == null) {
                    showAlert(Alert.AlertType.WARNING, "Invalid Input", "Please select a valid Day.");
                    return;
                }
                if (date.isEmpty() || !date.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    showAlert(Alert.AlertType.WARNING, "Invalid Input", "Date must be in YYYY-MM-DD format.");
                    return;
                }

                try {
                    LocalDate fixedDate = LocalDate.parse(date);

                    // Create and save the course
                    Course course = new Course(courseName, courseNumber, startTime, endTime, day, place, fixedDate);
                    scheduleTable.addCourse(course);
                    UserSchedule.saveToUserSchedule(course);
                    showAlert(Alert.AlertType.INFORMATION, "Course Added", "Course added manually to your schedule.");
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Input", "An error occurred while processing your input. Please check the values and try again.");
                }
            });

            // Clear all button logic
            Button clearAllButton = new Button("Clear Table");
            clearAllButton.setOnAction(event -> {
                scheduleTable.clearAllCourses();
                UserSchedule.clearAllCourses();
                showAlert(Alert.AlertType.INFORMATION, "Clear Table", "All courses have been removed!");
            });

            // Layout :]
            VBox dropdownPanel = new VBox(10, new Label("Course Name:"), courseNameDropdown,
                    new Label("Course Number:"), courseNumberDropdown, addCourseButton);

            HBox timeFields = new HBox(10, manualStartTime, manualEndTime);
            HBox courseFields = new HBox(10, manualCourseName, manualCourseNumber);
            HBox dateFields = new HBox(30, manualDay, manualDate);

            VBox manualPanel = new VBox(10, new Label("Add Course Manually:"), courseFields,
                    manualPlace, timeFields, dateFields, manualAddButton);

            VBox leftPanel = new VBox(20, dropdownPanel, manualPanel);
            leftPanel.setPadding(new Insets(10));
            leftPanel.setPrefWidth(300);

            HBox buttonsPanel = new HBox(10, saveCsvButton, saveIcsButton, loadCoursesButton, clearAllButton);
            buttonsPanel.setPadding(new Insets(10));

            BorderPane root = new BorderPane();
            root.setTop(banner);
            root.setLeft(leftPanel);
            root.setBottom(buttonsPanel);
            root.setCenter(scheduleTable.getTableView());

            Scene scene = new Scene(root, 1000, 750);
            primaryStage.setScene(scene);
            primaryStage.setTitle("FCIT Exam Schedular");
            primaryStage.show();

        } catch (Exception e) {
            System.err.println("Failed to start the application: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
