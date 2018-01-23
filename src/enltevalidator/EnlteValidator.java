/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package enltevalidator;

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
 * @author Administrator this class is used to get latest hash from server &
 * compare data with local database to vote for hash.
 */
public class EnlteValidator {

    private static String filePath;

    /**
     * this method is main launcher of application which will open terminal
     * automatically.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        // TODO code application logic here
        Console console = System.console();
        if (console == null && !GraphicsEnvironment.isHeadless()) {
            //Sample.class.getProtectionDomain().getCodeSource().getLocation().toString()
            String filename = EnlteValidator.class.getProtectionDomain().getCodeSource().getLocation().toString().substring(6);
            filename = filename.replace("build/classes/", "");
//            if (!filename.contains("dist/EnlteValidator.jar")) {
//                filename = filename + "dist/EnlteValidator.jar";
//            }
            System.out.println("sample.Sample.main()" + filename);
            Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start", "cmd", "/k", "java -jar \"" + filename + "\""});
        } else {
            Scanner scanner = new Scanner(System.in);
            filePath = EnlteValidator.class.getProtectionDomain().getCodeSource().getLocation().toString();
            filePath = filePath.substring(6);
            //System.out.println("filePath:-after editing-"+filePath);
            System.out.println("Please enter your user id");
            String userId = scanner.next();
            System.out.println("Please enter file path to store data locally.");
            filePath = scanner.next();
            if (filePath.contains("/")) {
                String[] array = filePath.split("/");
                String lastItem = "";
                for (int i = 0; i < array.length; i++) {
                    lastItem = array[i];
                }
                filePath = filePath.replace(lastItem, "");
            }
            System.out.println("Your local DB Path-" + filePath);
            System.out.println("Your app is now listening to ongoing precess...");
            Timer time = new Timer(); // Instantiate Timer Object
            ScheduledTask st = new ScheduledTask(userId); // Instantiate SheduledTask class
            time.schedule(st, 0, 10000); // Create Repetitively task for every 1 secs
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
        File file = new File(filePath + "/db.txt");
        if (!file.exists()) {
            file.createNewFile();
            //System.out.println("New File Created.");
        }
        BufferedReader br = new BufferedReader(new FileReader(filePath + "/db.txt"));
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
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath + "/db.txt"))) {

            //String content = "Enlte\n";
            bw.append(content + " " + result);

            // no need to close it.
            //bw.close();
            System.out.println("File updated.");

        } catch (IOException e) {
            e.printStackTrace();
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

        public void run() {
            now = new Date(); // initialize date
            System.out.println("Time is :" + now); // Display current time
            hitBlockChain(mUserId);
        }
    }

    /**
     * get new block chain record from server.
     *
     * @param userId
     */
    private static void hitBlockChain(String userId) {
        try {
            System.out.println("Checking for new updates... ");
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost postRequest = new HttpPost(
                    "http://enlte.com/blockchain/broadcast_hash");
            //System.out.println("http://enlte.com/blockchain/broadcast_client_hash");
            JSONObject json = new JSONObject();
            StringEntity input = new StringEntity(json.toString());
            input.setContentType("application/json");
            postRequest.setEntity(input);

            HttpResponse response = httpClient.execute(postRequest);

            if (response.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + response.getStatusLine().getStatusCode());
            }
            String result = EntityUtils.toString(response.getEntity());
            System.out.println("response:-" + result);
            handleResult(result, userId);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(EnlteValidator.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(EnlteValidator.class.getName()).log(Level.SEVERE, null, ex);
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
                String index = "", previous_hash = "", data_hash = "", time_stamp = "";
                if (jSONObject.has("index")) {
                    index = jSONObject.getString("index");
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
                    Logger.getLogger(EnlteValidator.class.getName()).log(Level.SEVERE, null, ex);
                }
                if (content == null || content.length() == 0 || !content.contains(result)) {
                    //voteForHash(data_hash, previous_hash, time_stamp, index, userId);
                    //update vote status to db to restrick the further vote.
                    writeToFile(content, result);
                }
                checkBroadcastedHash(data_hash, userId, index);
            } catch (JSONException ex) {
                Logger.getLogger(EnlteValidator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private static void checkBroadcastedHash(String saved_hash, String userId, String index) {
        //To change body of generated methods, choose Tools | Templates.
        try {
            //System.out.println("Checking for new updates... ");
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost postRequest = new HttpPost(
                    "http://enlte.com/blockchain/broadcast_client_hash");
            System.out.println("http://enlte.com/blockchain/broadcast_client_hash");
            JSONObject json = new JSONObject();
            StringEntity input = new StringEntity(json.toString());
            input.setContentType("application/json");
            postRequest.setEntity(input);

            HttpResponse response = httpClient.execute(postRequest);

            if (response.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + response.getStatusLine().getStatusCode());
            }
            String result = EntityUtils.toString(response.getEntity());

            int savedIndex = Integer.parseInt(index) + 1;
            System.out.println("response:-" + result);
            handleValidateResult(result, userId, savedIndex + "", saved_hash);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(EnlteValidator.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(EnlteValidator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void handleValidateResult(String result, String userId, String savedIndex, String savedHash) {
        if (result != null && result.length() > 0) {
            try {
                JSONObject jSONObject = new JSONObject(result);
                String index = "", previous_hash = "", data_hash = "", time_stamp = "";
                if (jSONObject.has("index")) {
                    index = jSONObject.getString("index");
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

                if (savedIndex.equals(index) && savedHash.equals(previous_hash)) {

                    //update vote status to db to restrick the further vote.
                    String content = "";
                    try {
                        content = readFile();
                    } catch (IOException ex) {
                        Logger.getLogger(EnlteValidator.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    if (!content.contains(result)) {
                        voteForHash(data_hash, previous_hash, time_stamp, index, userId);
                        writeToFile(content, result);
                    }
                }
                //checkBroadcastedHash(data_hash,userId, index);
            } catch (JSONException ex) {
                Logger.getLogger(EnlteValidator.class.getName()).log(Level.SEVERE, null, ex);
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
    private static void voteForHash(String dhash, String phash, String timestamp, String index, String userId) throws JSONException {
        try {
            System.out.println("Doing Vote for sha: " + dhash);
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost postRequest = new HttpPost(
                    "http://enlte.com/blockchain/vote_hash");
            //System.out.println("http://enlte.com/blockchain/vote_hash");
            JSONObject params = new JSONObject();
            params.put("dhash", dhash);
            params.put("phash", phash);
            params.put("time_stamp", timestamp);
            params.put("status", "1");
            params.put("index", index);
            params.put("user_id", userId);
            StringEntity input = new StringEntity(params.toString());
            input.setContentType("application/json");
            postRequest.setEntity(input);

            HttpResponse response = httpClient.execute(postRequest);

            if (response.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + response.getStatusLine().getStatusCode());
            }
            String result = EntityUtils.toString(response.getEntity());
            System.out.println("Voting Done: " + result);
            //handleResult(result);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(EnlteValidator.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(EnlteValidator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
