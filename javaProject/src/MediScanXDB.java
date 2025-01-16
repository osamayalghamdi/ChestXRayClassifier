import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import org.json.JSONException;

public class MediScanXDB implements Runnable {
	private static Connection connection;
	private MediScanXNet mediScanXNet;
	public MediScanXDB(String URL, String user, String pass, MediScanXNet medi) throws Exception {
		this.connection = DriverManager.getConnection(URL, user, pass);
		this.mediScanXNet = medi;
	}

	@Override
	public void run(){
		// the thread run method
		Scanner scanner = new Scanner(System.in);
		int choice;
		do {
		System.out.print("Choose an option:\n1. Login\n2. Sign up\n3. Exit\nEnter your choice: ");
		choice = scanner.nextInt();
		if (choice == 1) {
			// Login
			// Call the function to handle login
			try {
				login();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (choice == 2) {
			// Sign up
			System.out.print("Enter your username: ");
			String newUsername = scanner.next();
			// Check if the username already exists
			String checkUsernameSql = "SELECT * FROM patients WHERE username = ?";
			try (PreparedStatement checkUsernameStatement = connection.prepareStatement(checkUsernameSql)) {
				checkUsernameStatement.setString(1, newUsername);
				ResultSet resultSet = checkUsernameStatement.executeQuery();
				if (resultSet.next()) {
					System.out.println("Username already exists. Please choose a different one.");
					continue; // Continue the loop to allow the user to try signing up again
				}
			} catch (SQLException e) {
				e.printStackTrace(); // Handle the exception
				continue; // Continue the loop to allow the user to try signing up again
			}
			System.out.print("Enter your name: ");
			String name = scanner.next();
			System.out.print("Enter your age: ");
			int age = 0;
			try {
				age = scanner.nextInt();
			}catch (Exception e){
				System.out.println("Error enter int");
				break;
			}



			System.out.print("Enter your gender (Male/Female): ");
			String gender = scanner.next();
			try {
				// Call the function to handle sign up
				signUp(newUsername, name, age, gender);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (choice == 3) {
			System.out.println("Bye.");
			break;
		}
		else {
			System.out.println("Invalid choice. Please try again.");
		}
		}while(true );
		scanner.close();
	}
////////////////// Log in method //////////////////////////////////////////////////
    private void login() throws Exception {
		try {
			Scanner scanner = new Scanner(System.in);
			// Check if the user exists in the database
			System.out.print("Enter your username: ");
			String username = scanner.next();
			String sql = "SELECT * FROM patients WHERE username = ?";
			PreparedStatement statement = connection.prepareStatement(sql);
			statement.setString(1, username);
			ResultSet resultSet = statement.executeQuery();
			if (resultSet.next()) {
				// User exists
				System.out.println("User found. Proceeding with login...\n\n");
				int loginChoice;
				do {
				System.out.print("Please chose one of the services\n1. Create appointment\n2. Cheak the result of appointment\n3. Exit\n");
				loginChoice = scanner.nextInt();
				if(loginChoice == 1) {
				System.out.print("Enter the image path: ");
				String imagePath1 = scanner.next();
				createAppointment(username, imagePath1);
				}
				else if(loginChoice == 2) {
					//here the section of check result
					String reusltStat= "SELECT diagnosis, accuracy FROM patient_diagnosis WHERE username = ?";
					PreparedStatement reuslt = connection.prepareStatement(reusltStat);
					reuslt.setString(1, username);
					ResultSet WantedValues = reuslt.executeQuery();
					/* in the 'if' we shoow the user the result of ML if there is no result we need to tell him about it
					 the result we be updated every few minutes
					 */
					if(WantedValues.next()) {
					String diagnosis = WantedValues.getString("diagnosis");
					double accuracy = WantedValues.getFloat("accuracy");
///////////////////////////////////to show the result////////////////////////////////////////////
					if(!(diagnosis==null)) {
						
					System.out.println("diagnosis: "+diagnosis+"\naccuracy: "+accuracy+"\n");
					
					}
					else {
						
						System.out.println("The result is not out yet, please wait for at most 10 seconds");
						
					}
///////////////////////////////////////////////////////////////////////////////////////////
							}
					else {
						
						System.out.println("there is no appointment for this user at the moment");
					}
					WantedValues.close();
					reuslt.close();

				}
				else if (loginChoice == 3){
					break;
				}
				}while(true);
				
			} else {
				// User does not exist
				System.out.println("User not found in the database.");
			}
			// Close statement and result set
			statement.close();
			resultSet.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}
///////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////Sign Up method//////////////////////////////////////////////////////
    private void signUp(String newUsername, String name, int age, String gender)throws IOException{
        try {
            // Insert data into patients table
            String sql_patient = "INSERT INTO patients (username, Pname, age, gender) VALUES (?, ?, ?, ?)";
            PreparedStatement statement1 = connection.prepareStatement(sql_patient);
            statement1.setString(1, newUsername);
            statement1.setString(2, name);
            statement1.setInt(3, age);
            statement1.setString(4, gender);
            statement1.executeUpdate();
            System.out.println("\nSign up successful.\n");
            // Close statement
            statement1.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
/////////////////////////////////////////////////////////////////////////////////////////////////////////
    // this method for inserting into the patient_dianosis table
private static void createAppointment(String username, String imgPath) throws SQLException, Exception {
	// Retrieve the maximum ID from the patient_diagnosis table
	int maxId = 0;
	String getMaxIdSql = "SELECT MAX(id) FROM patient_diagnosis";
	try (PreparedStatement getMaxIdStatement = connection.prepareStatement(getMaxIdSql);
		 ResultSet resultSet = getMaxIdStatement.executeQuery()) {
		if (resultSet.next()) {
			maxId = resultSet.getInt(1);
		}
	}

	// Increment the maximum ID to get a unique ID for the new appointment
	int newId = maxId + 1;

	// SQL statements for insertion and update
	String sqlInsert = "INSERT INTO patient_diagnosis (id, username, image_path) VALUES (?, ?, ?)";
	String sqlUpdate = "UPDATE patient_diagnosis SET username = ?, image_path = ? WHERE id = ?";

	// Call the uploadImage method and get the new image path
	String newPath = uploadImage(imgPath, username, newId);

	try (PreparedStatement statementInsert = connection.prepareStatement(sqlInsert);
		 PreparedStatement statementUpdate = connection.prepareStatement(sqlUpdate)) {
		// Insert a new record (ID)
		statementInsert.setInt(1, newId);
		statementInsert.setString(2, username);
		statementInsert.setString(3, newPath);
		statementInsert.executeUpdate();

		// Update the record with the image path
		statementUpdate.setString(1, username);
		statementUpdate.setString(2, newPath);
		statementUpdate.setInt(3, newId);
		statementUpdate.executeUpdate();
	}
}

	private static String uploadImage(String imagePath, String username,int newId) throws IOException {
		String newPath = null;
		File imageFile = new File(imagePath);
		byte[] imageData = Files.readAllBytes(imageFile.toPath());
		//File destinationFile = new File("D:/IT/Smesters/th5_SMESTER/305/Medi/PROJECT_TEST_NEW/PROJECT_TEST/usersImages/" + "ID_"+ username +"_"+newId + ".jpeg");
		File destinationFile = new File("/usersImages/" + "ID_"+ username +"_"+newId + ".jpeg");

		destinationFile.getParentFile().mkdirs();
		try (FileOutputStream outputStream = new FileOutputStream(destinationFile)) {
			outputStream.write(imageData);
		}
		newPath = destinationFile.getAbsolutePath();
		System.out.println("Image uploaded successfully: " + newPath);
		return newPath;
	}
}