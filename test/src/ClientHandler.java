import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientHandler implements Runnable {

    private Socket clientSocket;
    private static final String GOOGLE_SHEET_URL = "https://docs.google.com/spreadsheets/d/e/2PACX-1vRWCO0H4t-Ibac_Os8rlkadtoV-y-_wGKpCWDGUFEyS-EmHY-zXXRSJoRrtmlOhPsaXSC_e_1wTZ6tB/pubhtml?gid=307207568&single=true";

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            // Fetch course data from Google Sheet
            fetchAndSaveGoogleSheetData();

            try {
                // Read course name and course number from the client
                ObjectInputStream inputStream = new ObjectInputStream(clientSocket.getInputStream());
                ObjectOutputStream outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                String courseName = (String) inputStream.readObject();
                String courseNumber = (String) inputStream.readObject();

                System.out.println("Received request for Course: " + courseName + "-" + courseNumber);

                // Query the database
                Course course = DatabaseHandler.getCourseFromExamSchedule(courseName, courseNumber);

                // Send the response to the client
                outputStream.writeObject(course);
                outputStream.flush();
                System.out.println("Response sent to client.");
            } catch (EOFException e) {
            }
        } catch (Exception e) {
            System.err.println("Error processing client request: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void fetchAndSaveGoogleSheetData() {
        try {
            Document document = Jsoup.connect(GOOGLE_SHEET_URL).get();

            Elements rows = document.select("table tbody tr");

            String currentDay = null;
            String currentDate = null;

            for (Element row : rows) {
                Elements cells = row.select("td");


                // Check for day/date rows
                if (cells.size() == 1) {
                    String rowText = cells.get(0).text().trim();


                    // Regex for extracting day and date
                    String dayRegex = "(?i)(Sunday|Monday|Tuesday|Wenesday|Thursday|Friday|Saturday)";
                    String dateRegex = "\\d{2}/\\d{2}/\\d{4}";

                    // Extract day
                    Matcher dayMatcher = Pattern.compile(dayRegex).matcher(rowText);
                    if (dayMatcher.find()) {
                        currentDay = dayMatcher.group();
                    }

                    // Extract date
                    Matcher dateMatcher = Pattern.compile(dateRegex).matcher(rowText);
                    if (dateMatcher.find()) {
                        currentDate = dateMatcher.group();
                    }

                    if (currentDay != null && currentDate != null) {
                        continue; // Skip to the next row
                    }
                }

                if (cells.size() < 5 || cells.get(1).text().trim().equalsIgnoreCase("Course")) {
                    continue;
                }

                // Parse course rows if currentDay and currentDate are set
                if (currentDay != null && currentDate != null) {
                    try {
                        // Extract course details
                        String courseInfo = cells.get(1).text().trim();
                        String[] courseParts = courseInfo.split("-");
                        if (courseParts.length != 2) {
                            continue;
                        }

                        String courseName = courseParts[0].trim();
                        String courseNumber = courseParts[1].trim();
                        String place = cells.get(3).text().trim();
                        String timeRange = cells.get(4).text().trim();
                        String[] times = timeRange.split("-");
                        if (times.length != 2) {
                            System.err.println("Invalid time format: " + timeRange);
                            continue;
                        }
                        String startTime = times[0].trim();
                        String endTime = times[1].trim();

                        // Save to the database
                        DatabaseHandler.saveToDatabase(courseName, courseNumber, place, startTime, endTime, currentDay, currentDate);
                    } catch (Exception e) {
                        System.err.println("Error parsing course row: " + row.text());
                        e.printStackTrace();
                    }
                }

            }

        } catch (Exception e) {
            System.err.println("Error fetching or saving Google Sheet data: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
