package com.example.teamworkchat;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.ProgressBar;

public class MainActivity extends AppCompatActivity {

    public static final String channelID = "Silent_Knight";
    public static final String channelName = "Sabik Rahat";
    public static final String channelDescription = "Android Studio Notification Pop-up.";

    private ProgressBar progressBar;
    private int progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = findViewById(R.id.mainActivityProgressBarID);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription(channelDescription);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                doProgress();
            }
        });

        thread.start();
    }

    private void doProgress() {

        for (progress = 0; progress <= 90; progress += 30) {
            try {
                Thread.sleep(1000);
                progressBar.setProgress(progress);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        finish();
        Intent intent = new Intent(getApplicationContext(), SigninActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void alertMessege() {
        AlertDialog.Builder alertdialogbuilder = new AlertDialog.Builder(MainActivity.this);

        //for setting title
        alertdialogbuilder.setTitle(R.string.alertTitle);

        //for setting message
        alertdialogbuilder.setMessage(R.string.alertMessege);

        //for setting icon
        alertdialogbuilder.setIcon(R.drawable.alert);

        //for setting buttons
        alertdialogbuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();   //for cancel the dialog
                finish();         //for exit the application
            }
        });

        alertdialogbuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertdialogbuilder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertdialogbuilder.setCancelable(false);    //for not cancel the alert dialog for unexpected click.
        AlertDialog alertdialog = alertdialogbuilder.create();
        alertdialog.show();
    }

    @Override
    public void onBackPressed() {
        alertMessege();
    }
}