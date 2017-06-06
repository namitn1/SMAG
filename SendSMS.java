package com.vogella.android.smag_btp;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by namit on 25/4/17.
 */

public class SendSMS {
    public void sendSms(String result) {
            // Construct data
            String user = "username=" + "namit.narang99@gmail.com";
            String hash = "&hash=" + "bb38d52e14fc59d6e44c66d787d4a9fc683ebe9dccb144fdbdcb7aabb887bfc4";
            String message = "&message=" + result;
            String sender = "&sender=" + "TXTLCL";
            String numbers = "&numbers=" + "919013477108";
            String apiKey = "apiKey=" + "EyUE2jstrmc-1aFkwKRBUB3lhWBWerivpH3pEl8L1o";

            String data = user + hash + numbers + message + sender;
            new ProgressTask(data).execute();

    }

    public class ProgressTask extends AsyncTask<String, Void, String> {


        private Context context;
        private String data;

        public ProgressTask(String data) {
            this.data = data;
        }

        protected void onPreExecute() {

        }

        @Override
        protected void onPostExecute(final String result) {
            Log.d("SMS Msg","Msg : " + result);
        }

        protected String doInBackground(final String... args) {

            try {
                // Send data
                HttpURLConnection conn = (HttpURLConnection) new URL("http://api.textlocal.in/send/?").openConnection();
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Length", Integer.toString(data.length()));
                conn.getOutputStream().write(data.getBytes("UTF-8"));
                final BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                final StringBuffer stringBuffer = new StringBuffer();
                String line;
                while ((line = rd.readLine()) != null) {
                    stringBuffer.append(line);
                }
                rd.close();

                return stringBuffer.toString();
            } catch (Exception e) {
                System.out.println("Error SMS " + e);
                return "Error " + e;
            }
        }
    }
}
