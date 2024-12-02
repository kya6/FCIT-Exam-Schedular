import java.io.Serializable;
import java.time.LocalDate;

public class Course implements Serializable {
    private String name;
    private String number;
    private String startTime;
    private String endTime;
    private String day;
    private String place;
    private LocalDate examDate;

    public Course(String name, String number, String startTime, String endTime, String day, String place, LocalDate examDate) {
        this.name = name;
        this.number = number;
        this.startTime = startTime;
        this.endTime = endTime;
        this.day = day;
        this.place = place;
        this.examDate = examDate;
    }

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getNumber() { return number; }
    public void setNumber(String number) { this.number = number; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getDay() { return day; }
    public void setDay(String day) { this.day = day; }

    public String getPlace() { return place; }
    public void setPlace(String place) { this.place = place; }

    public LocalDate getExamDate() { return examDate; }
    public void setExamDate(LocalDate examDate) { this.examDate = examDate; }
}
