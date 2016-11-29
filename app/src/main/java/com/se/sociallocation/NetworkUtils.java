package com.se.sociallocation;


import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Kris on 11/16/2016
 */

public class NetworkUtils extends AsyncTask<String, Void, String> {

    protected void onPreExecute() {}

    @Override
    protected String doInBackground(String... arg0) {

        try {
            URL url = new URL("https://fcm.googleapis.com/fcm/send");

            JSONObject postDataParams = new JSONObject();

            postDataParams.put("to", "ced9dMEVZhQ:APA91bEcdTxkFtXiSRVauV4SdM8ue00tDGgH_BmQS6ozl6-1aMcE1bclkgJviIFB2xOWIuPvT97HXTYToObkAFTNcUJcefxdFZvkkWfOzXAHHAXEgcUE2bM2bfooVdOWXi75X7ITFftp");
            postDataParams.put("data", new JSONObject().put("message", "hello"));

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "key=AAAAalMlgOg:APA91bErmq9v07J0dZdfXUPkOh4a4kbv-inDJan997N6-4PTieWpngOvb5frnRjmjAcrGG-Q2a211HrE885HqdMpJB0tRCayjSBs3N7ktPNzhj54hiM8-UdfzZijHmty2n6LLionV_FKKCupObtBrVliCo0VU89XdA");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(getPostDataString(postDataParams));

            writer.flush();
            writer.close();
            os.close();

            int responseCode=conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {

                BufferedReader in=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuffer sb = new StringBuffer("");
                String line="";

                while((line = in.readLine()) != null) {

                    sb.append(line);
                    break;
                }

                in.close();
                return sb.toString();

            }
            else {
                return "false : "+responseCode;
            }
        } catch (Exception e) {
            return "Exception: " + e.getMessage();
        }
    }

    private String getPostDataString(JSONObject params) throws Exception {

        StringBuilder result = new StringBuilder();
        boolean first = true;

        Iterator<String> itr = params.keys();

        while(itr.hasNext()){

            String key= itr.next();
            Object value = params.get(key);

            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(), "UTF-8"));

        }
        return result.toString();
    }


}
