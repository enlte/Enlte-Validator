/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package enltevalidator;

import presenter.ValidateBroadcastedPost;
import presenter.ValidateBroadcastedTransaction;
import java.awt.GraphicsEnvironment;
import java.io.Console;
import java.io.IOException;
import java.util.Date;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

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
            time.schedule(st, 0, 20000); // Create Repetitively task for every 1 secs
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
        private final ValidateBroadcastedPost postValidator;
        private final ValidateBroadcastedTransaction transactionValidator;

        private ScheduledTask(String userId) {
            mUserId = userId; //To change body of generated methods, choose Tools | Templates.
            postValidator =   new ValidateBroadcastedPost(mUserId, filePath);
            transactionValidator = new ValidateBroadcastedTransaction(mUserId, filePath);
        }

        public void run() {
            now = new Date(); // initialize date
            System.out.println("Time is :" + now); // Display current time
            //hitBlockChain(mUserId);
            postValidator.hitBlockChain();
            transactionValidator.hitBlockChain();
        }
    }

}
