package com.vogella.android.smag_btp;

import android.app.Activity;
import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Bundle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.pushwoosh.BasePushMessageReceiver;
import com.pushwoosh.BaseRegistrationReceiver;
import com.pushwoosh.PushManager;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;

public class MainActivity extends Activity{
    private Context context;

    private static String URL = "https://cloud.arest.io/pranav123";

    private static final String TEMP = "temperature";
    private static final String HUMID = "moisture";
    private static final String INTRU = "intrusion";

    String RESULT[] = new String[3];

    private SampleSQLiteDBHelper db;

    TextView humidTV, tempTV, intTV;
    EditText lowerThresh, higherThresh;
    Button btnAddData, btnViewAll, btnAddLowerThreshold, btnAddHigherThreshold;
    String lowerString, higherString;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnAddHigherThreshold = (Button)findViewById(R.id.AddHigherThresholdBtn);
        btnAddLowerThreshold = (Button)findViewById(R.id.AddLowerThresholdBtn);
        humidTV = (TextView) findViewById(R.id.humid);
        intTV = (TextView) findViewById(R.id.intru);
        tempTV = (TextView) findViewById(R.id.temp);
        db = new SampleSQLiteDBHelper(this);
        btnAddData = (Button) findViewById(R.id.AddDataBtn);
        btnViewAll = (Button) findViewById(R.id.ViewDataBtn);


        btnAddLowerThreshold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lowerThresh = (EditText) findViewById(R.id.editLowerThreshold);

                if(RESULT[1]==null){
                    Log.d("RESULT","NULL");
                    return;
                }

                lowerString = lowerThresh.getText().toString();

                if(Double.parseDouble(RESULT[1])<Double.parseDouble(lowerString)){
                    String message = "Temperature = " + RESULT[0] + "\n" + "Moisture = " + RESULT[1] + "\n" + "Intrusion = " + RESULT[2] + "\n" + "Increase Water Content";
                    SendSMS msg = new SendSMS();
                    msg.sendSms(message);
                    sendSMS("9013105404", message);
                }

                else{
                    String message = "Temperature = " + RESULT[0] + "\n" + "Moisture = " + RESULT[1] + "\n" + "Intrusion = " + RESULT[2];
                    SendSMS msg = new SendSMS();
                    msg.sendSms(message);
                    sendSMS("9013105404", message);
                }


            }
        });
        btnAddHigherThreshold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                higherThresh = (EditText) findViewById(R.id.editHigherThreshold);
                higherString = higherThresh.getText().toString();

                if(Double.parseDouble(RESULT[1])>Double.parseDouble(higherString)){
                    String message = "Temperature = " + RESULT[0] + "\n" + "Moisture = " + RESULT[1] + "\n" + "Intrusion = " + RESULT[2] + "\n" + "Decrease Water Content";
                    SendSMS msg = new SendSMS();
                    msg.sendSms(message);
                    sendSMS("9013105404", message);
                }

                else{
                    String message = "Temperature = " + RESULT[0] + "\n" + "Moisture = " + RESULT[1] + "\n" + "Intrusion = " + RESULT[2];
                    SendSMS msg = new SendSMS();
                    msg.sendSms(message);
                    sendSMS("9013105404", message);
                }


            }
        });



        new ProgressTask().execute();


        //Pushwoosh Begin Register receivers for push notifications
        registerReceivers();
        //Create and start push manager
        PushManager pushManager = PushManager.getInstance(this);//Start push manager, this will count app open for Pushwoosh stats as well
        try {
            pushManager.onStartup(this);
        } catch (Exception e) {
//push notifications are not available or AndroidManifest.xml is not configured properly
        }
//Register for push!
        pushManager.registerForPushNotifications();
        checkMessage(getIntent());
//PushwooshEnd in onCreate
    }


//Registration receiver
    BroadcastReceiver mBroadcastReceiver = new BaseRegistrationReceiver() {
        @Override
        public void onRegisterActionReceive(Context context, Intent intent) {
            checkMessage(intent);
        }
    };

    //Push message receiver
    private BroadcastReceiver mReceiver = new BasePushMessageReceiver() {
        @Override
        protected void onMessageReceive(Intent intent) {
//JSON_DATA_KEY contains JSON payload of push notification.
            showMessage("push message is " + intent.getExtras().getString(JSON_DATA_KEY));
        }
    };

    //Registration of the receivers
    public void registerReceivers() {
        IntentFilter intentFilter = new IntentFilter(getPackageName() + ".action.PUSH_MESSAGE_RECEIVE");
        registerReceiver(mReceiver, intentFilter, getPackageName() + ".permission.C2D_MESSAGE", null);
        registerReceiver(mBroadcastReceiver, new IntentFilter(getPackageName() + "." + PushManager.REGISTER_BROAD_CAST_ACTION));
    }

    public void unregisterReceivers() {
//Unregister receivers on pause
        try {
            unregisterReceiver(mReceiver);
        } catch (Exception e) {
// pass.
        }try {
            unregisterReceiver(mBroadcastReceiver);
        } catch (Exception e) {
//pass through
        }
    }

    @Override
    public void onPause() {
        super.onPause();
//Unregister receivers on pause
        unregisterReceivers();
    }

    @Override
    protected void onResume() {
        super.onResume();
//Re-register receivers on resume
        registerReceivers();
    }

    private void checkMessage(Intent intent) {
        if (null != intent) {
            if (intent.hasExtra(PushManager.PUSH_RECEIVE_EVENT)) {
                showMessage("push message is " + intent.getExtras().getString(PushManager.PUSH_RECEIVE_EVENT));}
            else if (intent.hasExtra(PushManager.REGISTER_EVENT)) {
                showMessage("register");
            }
            else if (intent.hasExtra(PushManager.UNREGISTER_EVENT)) {
                showMessage("unregister");
            }
            else if (intent.hasExtra(PushManager.REGISTER_ERROR_EVENT)) {
                showMessage("register error");
            }
            else if(intent.hasExtra(PushManager.UNREGISTER_ERROR_EVENT)) {
                showMessage("unregister error");
            }
            resetIntentValues();
        }
    }

    private void resetIntentValues() {
        Intent mainAppIntent = getIntent();
        if (mainAppIntent != null) {
            if (mainAppIntent.hasExtra(PushManager.PUSH_RECEIVE_EVENT)) {
                mainAppIntent.removeExtra(PushManager.PUSH_RECEIVE_EVENT);}
            else if (mainAppIntent.hasExtra(PushManager.REGISTER_EVENT)) {
                mainAppIntent.removeExtra(PushManager.REGISTER_EVENT);
            }
            else if (mainAppIntent.hasExtra(PushManager.UNREGISTER_EVENT)) {
                mainAppIntent.removeExtra(PushManager.UNREGISTER_EVENT);
            }
            else if (mainAppIntent.hasExtra(PushManager.REGISTER_ERROR_EVENT)) {
                mainAppIntent.removeExtra(PushManager.REGISTER_ERROR_EVENT);
            }
            else if (mainAppIntent.hasExtra(PushManager.UNREGISTER_ERROR_EVENT)) {
                mainAppIntent.removeExtra(PushManager.UNREGISTER_ERROR_EVENT);
            }
            setIntent(mainAppIntent);
        }
    }

    private void showMessage(String message) {
        Log.i("AndroidBash",message);
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        setIntent(intent);checkMessage(intent);
    }

 public void addData(final Double temp, final Double moist, final Integer intru) {
        btnAddData.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean isInserted = db.insertData(temp, moist, intru);
                    if(isInserted)
                        Toast.makeText(MainActivity.this,"Inserted Successfully",Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(MainActivity.this,"Could Not Insert",Toast.LENGTH_LONG).show();
                }
            }
        );
    }

    public void viewAll() {
        btnViewAll.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Cursor res = db.getAllData();
                    if(res.getCount()==0) {
                        showMessageData("Error","Nothing Found");
                        return;
                    }
                    StringBuffer buffer = new StringBuffer();
                    while(res.moveToNext()) {
                        buffer.append("Id = " + res.getString(0) + "\n");
                        buffer.append("Date = " +  res.getString(1) + "\n");
                        buffer.append("Temperature = " + res.getString(2) + "\n");
                        buffer.append("Moisture = " + res.getString(3) + "\n");
                        buffer.append("Intrusion = " + res.getString(4) + "\n");

                    }
                    showMessageData("Data", buffer.toString());
                }
            }
        );
    }

    public void showMessageData(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }

    public void sendSMS(String phoneNo, String msg) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, msg, null, null);
            /*Toast.makeText(getApplicationContext(), "Message Sent",
                    Toast.LENGTH_LONG).show();*/
        } catch (Exception ex) {
            /*Toast.makeText(getApplicationContext(),ex.getMessage().toString(),
                    Toast.LENGTH_LONG).show();*/

            ex.printStackTrace();
        }
    }

    private class ProgressTask extends AsyncTask<String, Void, String[]> {


        private Context context;

        protected void onPreExecute() {

        }

        @Override
        protected void onPostExecute(final String result[]) {
            RESULT = result;
            humidTV.setText("Humidity = " + result[1]);
            intTV.setText("Intrusion = "  + result[2]);
            tempTV.setText("Temperature = "  + result[0]);
        }

        protected String[] doInBackground(final String... args) {


            StringBuilder sb = new StringBuilder();

            java.net.URL url;
            HttpURLConnection urlConnection = null;
            try {
                url = new URL(URL);

                urlConnection = (HttpURLConnection) url
                        .openConnection();

                InputStream in = urlConnection.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String line = null;

                try{
                    while((line = br.readLine()) != null){
                        sb.append(line + "\n");
                    }
                }catch (IOException io){
                    io.printStackTrace();
                }
                finally {
                    try{
                        in.close();
                    }catch (Exception e){

                    }
                }


            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }

            JSONObject object;
            String[] result = new String[3];
            Double temp = 0.0, humid = 0.0;
            Integer intru = 0;
            try {
                object = new JSONObject(sb.toString());
                JSONObject object2 = object.getJSONObject("variables");
                temp = object2.getDouble("temperature");
                humid = object2.getDouble("moisture");
                intru = object2.getInt("intrusion");

            }catch (Exception e){
                e.printStackTrace();
            }

            result[0] = String.valueOf(temp);
            result[1] = String.valueOf(humid);
            result[2] = String.valueOf(intru);

            if(result[2].equals("1")) {
                String message = "Temperature = " + result[0] + "\n" + "Moisture = " + result[1] + "\n" + "Intrusion = " + result[2] + "\n" + "Intrusion in Farm";
                SendSMS msg = new SendSMS();
                msg.sendSms(message);
                sendSMS("9013105404", message);
            }
            addData(temp,humid,intru);
            viewAll();

            return result;

        }
    }
}

