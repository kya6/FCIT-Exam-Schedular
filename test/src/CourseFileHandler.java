import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class CourseFileHandler {

    private static final String CSV_FILE_PATH = "exam_schedule.csv";
    private static final String ICS_FILE_PATH = "exam_schedule.ics";

    // Saves selected courses to a CSV file
    public void saveCoursesToCSV(ObservableList<Course> courses) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CSV_FILE_PATH))) {
            writer.write("Course Name,Course Number,Exam Date,Start Time,End Time,Day,Place"); // Header
            writer.newLine();
            for (Course course : courses) {
                String line = String.format("%s,%s,%s,%s,%s,%s,%s",
                        escapeSpecialCharacters(course.getName()),
                        escapeSpecialCharacters(course.getNumber()),
                        escapeSpecialCharacters(course.getExamDate().toString()),
                        escapeSpecialCharacters(course.getStartTime()),
                        escapeSpecialCharacters(course.getEndTime()),
                        escapeSpecialCharacters(course.getDay()),
                        escapeSpecialCharacters(course.getPlace()));
                writer.write(line);
                writer.newLine();
            }
            System.out.println("Courses successfully saved to " + CSV_FILE_PATH);
        } catch (IOException e) {
            System.err.println("Error saving courses to file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Saves selected courses to an iCalendar (.ics) file
    public void saveCoursesToICS(ObservableList<Course> courses) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ICS_FILE_PATH))) {
            writer.write("BEGIN:VCALENDAR");
            writer.newLine();
            writer.write("VERSION:2.0");
            writer.newLine();
            writer.write("PRODID:-//YourOrganization//ExamSchedule//EN");
            writer.newLine();

            for (Course course : courses) {
                writer.write("BEGIN:VEVENT");
                writer.newLine();
                writer.write("SUMMARY:" + escapeSpecialCharacters(course.getName() + "-" + course.getNumber() + " Exam"));
                writer.newLine();
                writer.write("DESCRIPTION:FCIT Exam Schedular by Mohammad Alkhayat & Abdulrahman Barashid & Hattan Alzahrani");
                writer.newLine();
                writer.write("LOCATION:" + escapeSpecialCharacters(course.getPlace()));
                writer.newLine();
                writer.write("DTSTART:" + formatICSDateTime(course.getExamDate(), course.getStartTime()));
                writer.newLine();
                writer.write("DTEND:" + formatICSDateTime(course.getExamDate(), course.getEndTime()));
                writer.newLine();
                writer.write("END:VEVENT");
                writer.newLine();
            }

            writer.write("END:VCALENDAR");
            System.out.println("Courses successfully saved to " + ICS_FILE_PATH);
        } catch (IOException e) {
            System.err.println("Error saving courses to ICS file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Escapes special characters for CSV or ICS
    private String escapeSpecialCharacters(String data) {
        if (data == null) return "";
        String escapedData = data;
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            data = data.replace("\"", "\"\"");
            escapedData = "\"" + data + "\"";
        }
        return escapedData;
    }

    // Formats date and time into the iCalendar format (e.g., 20240101T090000)
    private String formatICSDateTime(LocalDate date, String time) {
        try {
            LocalTime parsedTime = LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"));
            return date.format(DateTimeFormatter.ofPattern("yyyyMMdd")) +
                    "T" + parsedTime.format(DateTimeFormatter.ofPattern("HHmmss"));
        } catch (Exception e) {
            System.err.println("Error formatting date and time: " + e.getMessage());
            e.printStackTrace();
            return "";
        }
    }
}
