import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class ScheduleTable {

    private final TableView<Course> tableView;
    private final ObservableList<Course> courses;

    public ScheduleTable() {
        tableView = new TableView<>();
        courses = FXCollections.observableArrayList();

        // Define table columns
        TableColumn<Course, String> nameColumn = new TableColumn<>("Course Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Course, String> numberColumn = new TableColumn<>("Number");
        numberColumn.setCellValueFactory(new PropertyValueFactory<>("number"));

        TableColumn<Course, String> placeColumn = new TableColumn<>("Place");
        placeColumn.setCellValueFactory(new PropertyValueFactory<>("place"));

        TableColumn<Course, String> startTimeColumn = new TableColumn<>("Start Time");
        startTimeColumn.setCellValueFactory(new PropertyValueFactory<>("startTime"));

        TableColumn<Course, String> endTimeColumn = new TableColumn<>("End Time");
        endTimeColumn.setCellValueFactory(new PropertyValueFactory<>("endTime"));

        TableColumn<Course, String> dayColumn = new TableColumn<>("Day");
        dayColumn.setCellValueFactory(new PropertyValueFactory<>("day"));

        TableColumn<Course, String> examDateColumn = new TableColumn<>("Exam Date");
        examDateColumn.setCellValueFactory(new PropertyValueFactory<>("examDate"));

        // Delete Button Column
        TableColumn<Course, Void> deleteColumn = new TableColumn<>("Delete");
        deleteColumn.setCellFactory(col -> new TableCell<>() {
            private final Button deleteButton = new Button("Delete");

            {
                deleteButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");
                deleteButton.setOnAction(e -> {
                    Course course = getTableView().getItems().get(getIndex());
                    courses.remove(course); // Remove course from table
                    UserSchedule.deleteFromUserSchedule(course); // Remove course from UserSchedule database
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteButton);
                }
            }
        });

        // Add columns to the table
        tableView.getColumns().addAll(nameColumn, numberColumn, placeColumn, startTimeColumn, endTimeColumn, dayColumn, examDateColumn, deleteColumn);

        // Bind the data to the table
        tableView.setItems(courses);
    }

    public TableView<Course> getTableView() {
        return tableView;
    }

    public void addCourse(Course course) {
        courses.add(course);
    }

    public void addCourses(ObservableList<Course> courseList) {
        courses.addAll(courseList);
    }

    public void removeCourse(Course course) {
        courses.remove(course);
    }

    public ObservableList<Course> getAllCourses() {
        return FXCollections.unmodifiableObservableList(courses);
    }

    public void clearAllCourses() {
        courses.clear();
    }

}
