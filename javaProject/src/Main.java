import org.json.JSONException;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.*;

public class Main {

    public static void main(String[] args)throws Exception {
        String url = "jdbc:mysql://127.0.0.1:3306/MediScanX";
        String username = "root";
        String password = "Changeme@123456";
        MediScanXDB database = new MediScanXDB(url, username, password, null);
        MediScanXNet network = new MediScanXNet();
        Thread dataThread = new Thread(database);
        network.start();
        dataThread.start();

    }

}






