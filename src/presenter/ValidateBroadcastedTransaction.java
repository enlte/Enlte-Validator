/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package presenter;

import enltevalidator.EnlteValidator;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import utils.Utilities;

/**
 *
 * @author Administrator
 */
public class ValidateBroadcastedTransaction {

    private final String filePath;
    private final String mUserId;
    private final String mWalletId;

    public ValidateBroadcastedTransaction(String userId, String walletId, String dbPath) {
        this.mUserId = userId;
        this.mWalletId = walletId;
        this.filePath = dbPath;
    }

    /**
     * get new block chain record from server.
     *
     */
    public void hitBlockChain() {
        try {
            //System.out.println("Checking for new updates... ");
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost postRequest = new HttpPost("http://enlte.com/transaction_blockchain/broadcast_hash");
            //System.out.println("URL:-http://enlte.com/transaction_blockchain/broadcast_hash");
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
            System.out.println("response:-" + result.trim());
            System.out.println("\n \n");
            handleResult(result);
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
    private void handleResult(String result) {
        if (result != null && result.length() > 0) {
            try {
                JSONObject jSONObject = new JSONObject(result);
                String index = "", previous_hash = "", data_hash = "", time_stamp = "", amount = "0";
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
                if (jSONObject.has("amount")) {
                    amount = jSONObject.getString("amount");
                }
                String content = "";
                try {
                    content = utils.Utilities.readFile(filePath);
                    System.out.println("=================" + filePath);
                } catch (IOException ex) {
                    Logger.getLogger(EnlteValidator.class.getName()).log(Level.SEVERE, null, ex);
                }
                if (content == null || content.length() == 0 || !content.contains(result)) {
                    //voteForHash(data_hash, previous_hash, time_stamp, index, userId);
                    //update vote status to db to restrick the further vote.
                    utils.Utilities.writeToFile(filePath, content, result);
                }
                checkBroadcastedHash(data_hash, index);
            } catch (JSONException ex) {
                Logger.getLogger(EnlteValidator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void checkBroadcastedHash(String saved_hash, String index) {
        //To change body of generated methods, choose Tools | Templates.
        try {
            //System.out.println("Checking for new updates... ");
            DefaultHttpClient httpClient = new DefaultHttpClient();
            String url = "http://enlte.com/transaction_blockchain/broadcast_client_hash";
            HttpPost postRequest = new HttpPost(url);
            //System.out.println(url);
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
            System.out.println("response:-" + result.trim());
            if (result != null && result.trim().length() > 0 && !result.equals("null")) {
                handleValidateResult(result.trim(), savedIndex + "", saved_hash);
            }
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(EnlteValidator.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(EnlteValidator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    double amountSum;

    private void handleValidateResult(String result, String savedIndex, String savedHash) {
        if (result != null && result.length() > 0) {
            try {
                String mapleStr = "";
                amountSum = 0;
                JSONObject jSONObject = null, comparedJson = null;
                try {
                    comparedJson = new JSONObject();
                    JSONArray jSONArray = new JSONArray(result);
                    if (jSONArray != null && jSONArray.length() > 0) {
                        //jSONObject = jSONArray.getJSONObject(0);                        
                        mapleStr = processJsonArry(jSONArray);
                        JSONArray jarray = jSONArray.getJSONArray(jSONArray.length() - 1);
                        jSONObject = jarray.getJSONObject(0);
                    }
                } catch (JSONException e) {
                    jSONObject = new JSONObject(result);
                }
                String index = "", time_stamp = "";

                if (jSONObject.has("index")) {
                    index = jSONObject.getString("index");
                }
                if (jSONObject.has("index_h")) {
                    index = jSONObject.getString("index_h");
                }
                if (jSONObject.has("timestamp")) {
                    time_stamp = jSONObject.getString("timestamp");
                }
                /*String  previous_hash = "", data_hash = "",  amount = "0";
                
                if (jSONObject.has("previous_hash")) {
                    previous_hash = jSONObject.getString("previous_hash");
                }
                if (jSONObject.has("data_hash")) {
                    data_hash = jSONObject.getString("data_hash");
                }
                if (jSONObject.has("time_stamp")) {
                    time_stamp = jSONObject.getString("time_stamp");
                }

                if (jSONObject.has("amt")) {
                    amount = jSONObject.getString("amt");
                    if (amount == null || amount.equals("") || amount.equals("null")) {
                        amount = "0";
                    }
                }*/

                String maple_dhash = Utilities.sha256Hex(mapleStr);

                comparedJson.put("index", index);
                comparedJson.put("previous_hash", savedHash);
                comparedJson.put("data_hash", maple_dhash);
                comparedJson.put("time_stamp", time_stamp);
                comparedJson.put("amount", amountSum + "");
                result = comparedJson.toString();
                String content = "";
                try {
                    content = utils.Utilities.readFile(filePath);
                } catch (IOException ex) {
                    Logger.getLogger(EnlteValidator.class.getName()).log(Level.SEVERE, null, ex);
                }
                if (!content.contains(result)) {
                    //if (savedIndex.equals(index) && savedHash.equals(previous_hash)) {
                    //update vote status to db to restrick the further vote.
                    voteForHash(maple_dhash, savedHash, time_stamp, amountSum + "", index, "1");
                    utils.Utilities.writeToFile(filePath, content, result);
                    //} else {
                    // voteForHash(data_hash, previous_hash, time_stamp, amount, index, "0");
                    //}
                }
                //System.out.println(" \n Wait for next voting... " );
                //checkBroadcastedHash(data_hash,userId, index);
            } catch (JSONException ex) {
                Logger.getLogger(EnlteValidator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private String processJsonArry(JSONArray jsonArray) {

        String result = "";
        //boolean islastItem = false;
        for (int i = 0; i < jsonArray.length(); i++) {
            String dhash = "";
//            if (jsonArray.length() - 1 == i) {
//                islastItem = true;
//            }
            if (i == 0) {
                try {
                    dhash = processJson(jsonArray.getJSONObject(0));
                } catch (JSONException e) {
                    //e.printStackTrace();
                    try {
                        JSONArray jsonArray2 = jsonArray.getJSONArray(i);
                        dhash = processJson(jsonArray2.getJSONObject(0));
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
                }
            } else {
                try {
                    JSONArray jsonArray2 = jsonArray.getJSONArray(i);
                    dhash = processJson(jsonArray2.getJSONObject(0));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            result = result + dhash;
        }
        return result;
        //voteForLike();
    }

    private String processJson(JSONObject jsonObject) {
        String dHash = "";//, previousHash = "";
        //String index = "", timeStamp = "";
        String amount = "0";
        try {
            if (jsonObject.has("data_hash")) {
                dHash = jsonObject.getString("data_hash");
            }
            //if (jsonObject.has("index")) {
            //index = jsonObject.getString("index");
            /*if (firstPreviousHash.equals("")) {
                    firstIndex = index;
                }*/
            //}
            //if (jsonObject.has("index_h")) {
            //index = jsonObject.getString("index_h");
            /*if (firstPreviousHash.equals("")) {
                    firstIndex = index;
                }*/
            //}
            //if (jsonObject.has("previous_hash")) {
            //previousHash = jsonObject.getString("previous_hash");
            /*if (firstPreviousHash.equals("")) {
                    firstPreviousHash = previousHash;
                }*/
            //}

            /*if (jsonObject.has("time_stamp")) {
                timeStamp = jsonObject.getString("time_stamp");
            }
            if (jsonObject.has("timestamp")) {
                timeStamp = jsonObject.getString("timestamp");
            }*/
            if (jsonObject.has("amt")) {
                amount = jsonObject.getString("amt");
                if (amount == null || amount.equals("") || amount.equals("null")) {
                    amount = "0";
                }
                try {
                    amountSum = amountSum + Double.parseDouble(amount);
                    System.out.println(amountSum);
                } catch (Exception e) {
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return dHash;
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
    private void voteForHash(String dhash, String phash, String timestamp, String amount, String index, String status) throws JSONException {
        try {
            System.out.println("Doing Vote for sha: " + dhash);
            DefaultHttpClient httpClient = new DefaultHttpClient();

            HttpPost postRequest = new HttpPost("http://enlte.com/transaction_blockchain/vote_hash");
            //System.out.println("http://enlte.com/transaction_blockchain/vote_hash");
            /*JSONObject params = new JSONObject();
            params.put("dhash", dhash);
            params.put("phash", phash);
            params.put("time_stamp", timestamp);
            params.put("status", status);
            params.put("index", index);
            params.put("amt", amount);
            params.put("user_id", mUserId);
            params.put("wallet_id", mWalletId);
            System.out.println("Doing Vote for new block: " + params.toString());
            StringEntity input = new StringEntity(params.toString());
            input.setContentType("application/json");*/

            // postRequest.setEntity(input);
            List<NameValuePair> params = new ArrayList<NameValuePair>(8);
            params.add(new BasicNameValuePair("dhash", dhash));
            params.add(new BasicNameValuePair("phash", phash));
            params.add(new BasicNameValuePair("time_stamp", timestamp));
            params.add(new BasicNameValuePair("status", status));
            params.add(new BasicNameValuePair("index", index));
            params.add(new BasicNameValuePair("amt", amount));
            params.add(new BasicNameValuePair("user_id", mUserId));
             params.add(new BasicNameValuePair("wallet_id", mWalletId));
            postRequest.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            
            HttpResponse response = httpClient.execute(postRequest);

            if (response.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + response.getStatusLine().getStatusCode());
            }
            String result = EntityUtils.toString(response.getEntity());
            System.out.println("Voting Done: " + result.trim());
            //handleResult(result);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(EnlteValidator.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(EnlteValidator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
