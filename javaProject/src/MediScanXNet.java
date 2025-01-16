import org.json.JSONException;
import org.json.JSONObject;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MediScanXNet extends Thread {
    private static Connection DbCon;

    public MediScanXNet() throws SQLException {
        DbCon = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/MediScanX", "root", "Changeme@123456");
    }

    public void run() {
        try {
            while (true) {
                predictImage();
                Thread.sleep(1000 * 10);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void predictImage() {
        String urlString = "http://127.0.0.1:5000/predict";
        //to get max ID to start with
        String countOfRow = "SELECT * FROM patient_diagnosis";
        String update_patient_diagnosis_stat = "UPDATE patient_diagnosis SET diagnosis = ?, accuracy = ? WHERE id = ?";

        //make object to connect to the database
        try (PreparedStatement get_rowsStat = DbCon.prepareStatement(countOfRow);
             PreparedStatement update_patient_diagnosis = DbCon.prepareStatement(update_patient_diagnosis_stat)) {
            ResultSet rows = get_rowsStat.executeQuery();
            while (rows.next()) {
                int row = rows.getInt("id");
                String imagePath = rows.getString("image_path");
                JSONObject requestBody = new JSONObject();
                requestBody.put("image_path", imagePath);
                String jsonPayload = requestBody.toString();

                //create object URL and send the request
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                try (OutputStream outputStream = connection.getOutputStream()) {
                    outputStream.write(jsonPayload.getBytes());
                    outputStream.flush();
                }

                //if there is response from the server
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    //BufferedReader to accept larger chunks of data
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                        StringBuilder responseBuilder = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            responseBuilder.append(line);
                        }
                        JSONObject jsonResponse = new JSONObject(responseBuilder.toString());
                        //take from the server
                        String predictionResult = jsonResponse.getString("result");
                        double probability = jsonResponse.getDouble("probability");

                        update_patient_diagnosis.setString(1, predictionResult);
                        update_patient_diagnosis.setDouble(2, probability);
                        update_patient_diagnosis.setInt(3, row);
                        update_patient_diagnosis.executeUpdate();
                    }
                } else {
                    System.out.println("Failed : HTTP error code : " + responseCode);
                }
                connection.disconnect();
            }
        } catch (SQLException | IOException | JSONException e) {
            e.printStackTrace();
        }
    }
}