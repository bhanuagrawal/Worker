package com.example.timer.sservices;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.MutableLiveData;

import com.example.timer.LiveDataHelper;
import com.example.timer.MainActivity;
import com.example.timer.R;

import static com.example.timer.MainActivity.TIMER_CHANNEL_ID;

public class TimerService extends Service {

    private static final int NF_ID = 1;
    private int counter;
    private NotificationManager mNotificationManager;
    MutableLiveData<Integer> timerLiveData;

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationManager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        timerLiveData = LiveDataHelper.getTimerLiveData();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {



        Notification notification = getNotification("0");

        startForeground(NF_ID, notification);


        new Thread(new Runnable() {
            @Override
            public void run() {

                try {

                    while (counter < 100){
                        Thread.sleep(1000);
                        counter++;
                        timerLiveData.postValue(counter);
                        mNotificationManager.notify(NF_ID, getNotification(String.valueOf(counter)));
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


                stopForeground(true);
                stopSelf(startId);
            }
        }).start();

        return START_STICKY;
    }

    private Notification getNotification(String value) {

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0, notificationIntent, 0);
        return new NotificationCompat.Builder(this, TIMER_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_timer)
                .setContentTitle("Wait till 100")
                .setContentText(value)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
