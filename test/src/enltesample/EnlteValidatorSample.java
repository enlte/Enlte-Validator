/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package enltesample;

import java.awt.GraphicsEnvironment;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Console;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Administrator
 */
public class EnlteValidatorSample {

    private static String filePath;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        // TODO code application logic here
        Console console = System.console();
        if (console == null && !GraphicsEnvironment.isHeadless()) {
            //Sample.class.getProtectionDomain().getCodeSource().getLocation().toString()
            String filename = EnlteValidatorSample.class.getProtectionDomain().getCodeSource().getLocation().toString().substring(6);
            filename = filename.replace("build/classes/", "");
//            if (!filename.contains("dist/EnlteValidator.jar")) {
//                filename = filename + "dist/EnlteValidator.jar";
//            }
//37fa3882f58a932182a1c28150c5f17c25d1d2dd0fd00050cbc1c291bb23cfff
            System.out.println("sample.Sample.main()" + filename);
            Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start", "cmd", "/k", "java -jar \"" + filename + "\""});
        } else {
            Scanner scanner = new Scanner(System.in);
            // System.out.println("Please enter your user id");
            filePath = EnlteValidatorSample.class.getProtectionDomain().getCodeSource().getLocation().toString();
            // System.out.println("filePath:-"+filePath);
            filePath = filePath.substring(5);
             if (filePath.contains("/")) {
                String[] array = filePath.split("/");
                String lastItem = "";
                for (int i = 0; i < array.length; i++) {
                    lastItem = array[i];
                }
                filePath = filePath.replace(lastItem, "");
            }
            //System.out.println("filePath:-after editing-"+filePath);
            System.out.println("Please enter your user id");
            String userId = scanner.next();
            System.out.println("Please enter file path to store data locally.");
            filePath = scanner.next();
           
            System.out.println("Your local DB Path-" + filePath);
            System.out.println("Your app is now listening to ongoing precess...");
            Timer time = new Timer(); // Instantiate Timer Object
            ScheduledTask st = new ScheduledTask(userId); // Instantiate SheduledTask class
            time.schedule(st, 0, 5000); // Create Repetitively task for every 1 secs
        }
    }

    /**
     * this class is used to check new update from server after some interval.
     */
    // Create a class extends with TimerTask
    public static class ScheduledTask extends TimerTask {

        Date now; // to display current time
        // Add your task here
        private final String mUserId;

        private ScheduledTask(String userId) {
            mUserId = userId; //To change body of generated methods, choose Tools | Templates.
        }
        int counter = 0;

        public void run() {
            now = new Date(); // initialize date
            System.out.println("Time is :" + now); // Display current time
            counter++;
            hitBlockChain(mUserId);
            if (counter == 10) {
                System.out.println("Program is closed. This is sample project.");
                this.cancel();
                Scanner scanner = new Scanner(System.in);
                System.out.println("Please enter any two character to continue.");
                String userId = scanner.next();
                if (userId != null && userId.length() > 1) {
                    counter = 0;
                    Timer time = new Timer(); // Instantiate Timer Object
                    ScheduledTask st = new ScheduledTask(userId); // Instantiate SheduledTask class
                    time.schedule(st, 0, 5000);
                }
            }
        }
    }

    /**
     * get new block chain record from server.
     *
     * @param userId
     */
    private static void hitBlockChain(String userId) {
        try {

            String result = readBroadcastedFile();
            System.out.println("Broadcasted block-" + result);
            handleResult(result, userId);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(EnlteValidatorSample.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(EnlteValidatorSample.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * this method is used to parse block chain hash to compare locally.
     *
     * @param result
     * @param userId
     */
    private static void handleResult(String result, String userId) {
        if (result != null && result.length() > 0) {
            try {
                JSONObject jSONObject = new JSONObject(result);
                String previous_hash = "", data_hash = "", time_stamp = "";
                long index = 0;
                if (jSONObject.has("index")) {
                    index = jSONObject.getLong("index");
                }
                if (jSONObject.has("previous_hash")) {
                    previous_hash = jSONObject.getString("previous_hash");
                }
                if (jSONObject.has("data_hash")) {
                    data_hash = jSONObject.getString("data_hash");
                }
                if (jSONObject.has("time_stamp")) {
                    time_stamp = jSONObject.getString("time_stamp");
                }
                String content = "";
                try {
                    content = readFile();
                } catch (IOException ex) {
                    Logger.getLogger(EnlteValidatorSample.class.getName()).log(Level.SEVERE, null, ex);
                }
                if (content == null || content.length() == 0 || !content.contains(result)) {
                    writeToFile(content, result);
                    voteForHash(data_hash, previous_hash, time_stamp, index, userId);
                } else {
                    System.out.println("Already voted.");
                }
                System.out.println("Listening to new broacast block... ");
            } catch (JSONException ex) {
                Logger.getLogger(EnlteValidatorSample.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * this method is used to vote for hash, if it is exist in db file.
     *
     * @param dhash
     * @param phash
     * @param timestamp
     * @param index
     * @param userId
     * @throws JSONException
     */
    private static void voteForHash(String dhash, String phash, String timestamp, long index, String userId) throws JSONException {
        try {
            System.out.println("Voting status will broadcast to calculate max percentage.");

            System.out.println("Voting Done: ");
            //handleResult(result);
        } catch (Exception ex) {
            Logger.getLogger(EnlteValidatorSample.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * this file is used to read saved recode from db txt file.
     *
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    private static String readFile() throws FileNotFoundException, IOException {
        File file = new File(filePath + "db.txt");
        if (!file.exists()) {
            file.createNewFile();
            System.out.println("New File Created.");
        }
        BufferedReader br = new BufferedReader(new FileReader(filePath + "db.txt"));
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            String everything = sb.toString();
            return everything;
//            System.out.println("" + everything);
//            if (!everything.contains(content)) {
//                writeToFile(everything, content);
//            }
        } finally {
            br.close();
        }
    }

    /**
     * this file is used to read saved recode from db txt file.
     *
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    private static String readBroadcastedFile() throws FileNotFoundException, IOException {
        String home = System.getProperty("user.home");
        File file = new File(home + "/Downloads/" + "enlte-db.txt");
        if (!file.exists()) {
            file.createNewFile();
            //System.out.println("New File Created.");
        }
        BufferedReader br = new BufferedReader(new FileReader(file));
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            String everything = sb.toString();
            return everything;
//            System.out.println("" + everything);
//            if (!everything.contains(content)) {
//                writeToFile(everything, content);
//            }
        } finally {
            br.close();
        }
    }

    /**
     * this method is used to store new broadcasted hash from server.
     *
     * @param content
     * @param result
     */
    private static void writeToFile(String content, String result) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath + "db.txt"))) {

            //String content = "Enlte\n";
            bw.append(content + " " + result);

            // no need to close it.
            //bw.close();
            System.out.println("Found new block.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
