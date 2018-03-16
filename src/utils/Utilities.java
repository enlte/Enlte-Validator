/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;

/**
 *
 * @author Administrator
 */
public class Utilities {
     /**
     * this file is used to read saved recode from db txt file.
     *
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static String readFile(String filePath) throws FileNotFoundException, IOException {
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
    public static void writeToFile(String filePath, String content, String result) {
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
     * Returns a hexadecimal encoded SHA-256 hash for the input String.
     *
     * @param data
     * @return
     */
    public static String sha256Hex(String data) {
        String result = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes("UTF-8"));
            return bytesToHex(hash); // make it printable
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    /**
     * Use javax.xml.bind.DatatypeConverter class in JDK to convert byte array
     * to a hexadecimal string. Note that this generates hexadecimal in upper case.
     *
     * @param hash
     * @return
     */
    private static String bytesToHex(byte[] hash) {
        // Create Hex String
        StringBuilder hexString = new StringBuilder();
        for (byte aMessageDigest : hash) {
            String h = Integer.toHexString(0xFF & aMessageDigest);
            while (h.length() < 2)
                h = "0" + h;
            hexString.append(h);
        }
        return hexString.toString();
        //return DatatypeConverter.printHexBinary(hash);
    }
}
