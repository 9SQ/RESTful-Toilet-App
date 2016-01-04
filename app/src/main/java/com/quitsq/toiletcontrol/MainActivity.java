package com.quitsq.toiletcontrol;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.squareup.okhttp.Headers;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final String TOILET_API_URL = "http://192.168.1.160/washlet";
    private static NotificationManager mNotificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNotificationManager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
        mNotificationManager.cancelAll();
        sendNotification();

        registerReceiver(onNotifyButtonPushed, new IntentFilter("toilet"));

        Button buttonRear = (Button) findViewById(R.id.button_rear);
        Button buttonSoft = (Button) findViewById(R.id.button_soft);
        Button buttonBidet = (Button) findViewById(R.id.button_bidet);
        Button buttonStop = (Button) findViewById(R.id.button_stop);
        Button buttonBig = (Button) findViewById(R.id.button_big);
        Button buttonSmall = (Button) findViewById(R.id.button_small);

        buttonRear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                httpRequest("POST", "rear");
            }
        });

        buttonSoft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                httpRequest("POST", "soft");
            }
        });

        buttonBidet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                httpRequest("POST", "bidet");
            }
        });

        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                httpRequest("POST", "stop");
            }
        });

        buttonBig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                httpRequest("DELETE", "big");
            }
        });

        buttonSmall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                httpRequest("DELETE", "small");
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mNotificationManager.cancelAll();
    }

    BroadcastReceiver onNotifyButtonPushed = (new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            int type = intent.getIntExtra("type", 0);
            if (action.equals("toilet")) {
                switch (type) {
                    case 1:
                        httpRequest("POST", "rear");
                        break;
                    case 2:
                        httpRequest("POST", "stop");
                        break;
                    case 3:
                        httpRequest("DELETE", "big");
                        break;
                    default:
                        break;
                }
            }
        }
    });

    private void sendNotification() {

        Intent intent1 = new Intent();
        intent1.setAction("toilet");
        intent1.putExtra("type", 1);
        PendingIntent pendingIntent1 = PendingIntent.getBroadcast(getApplicationContext(), 1001, intent1, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intent2 = new Intent();
        intent2.setAction("toilet");
        intent2.putExtra("type", 2);
        PendingIntent pendingIntent2 = PendingIntent.getBroadcast(getApplicationContext(), 1002, intent2, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intent3 = new Intent();
        intent3.setAction("toilet");
        intent3.putExtra("type", 3);
        PendingIntent pendingIntent3 = PendingIntent.getBroadcast(getApplicationContext(), 1003, intent3, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.icon_toilet)
                .setContentTitle("RESTful Toilet")
                .setContentText("ウォシュレット リモコン")
                .addAction(R.mipmap.action_rear, "洗浄", pendingIntent1)
                .addAction(R.mipmap.action_stop, "停止", pendingIntent2)
                .addAction(R.mipmap.action_flow_big, "流す", pendingIntent3)
                .setAutoCancel(false)
                .setOngoing(false);
        mNotificationManager.notify(0, builder.build());
    }

    private void httpRequest(final String method, final String command) {

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String result = null;
                Request request = null;

                RequestBody requestBody = new MultipartBuilder("BOUNDARY")
                        .type(MultipartBuilder.FORM)
                        .addPart(
                                Headers.of("Content-Disposition", "form-data; name=\"c\""),
                                RequestBody.create(MediaType.parse("text/plain; charset=utf-8"), command)
                        )
                        .build();

                if (method.equals("POST")) {
                    request = new Request.Builder()
                            .url(TOILET_API_URL)
                            .post(requestBody)
                            .build();
                } else if (method.equals("DELETE")) {
                    request = new Request.Builder()
                            .url(TOILET_API_URL)
                            .delete(requestBody)
                            .build();
                } else {
                    return "method not found";
                }

                OkHttpClient client = new OkHttpClient();

                try {
                    Response response = client.newCall(request).execute();
                    result = response.body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return result;
            }

            @Override
            protected void onPostExecute(String result) {
                Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();
            }
        }.execute();
    }

}
