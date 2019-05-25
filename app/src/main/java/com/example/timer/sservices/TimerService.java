package com.example.timer.sservices;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.MutableLiveData;

import com.example.timer.LiveDataHelper;
import com.example.timer.MainActivity;
import com.example.timer.R;

import java.util.Stack;

import static com.example.timer.MainActivity.TIMER_CHANNEL_ID;

public class TimerService extends Service {

    private static final int NF_ID = 1;
    private static final int START_TIMER = 0;
    private static final int STOP_TIMER = 1;
    private static final int COUNTER_MAX = 20;
    private int counter;
    private NotificationManager mNotificationManager;
    MutableLiveData<Integer> timerLiveData;
    private Handler handler;
    private final ServiceBinder serviceBinder = new ServiceBinder();
    private Runnable runnable;
    private HandlerThread handlerThread;


    private final class ServiceHandler extends Handler {


        Stack<Message> s = new Stack<Message>();
        boolean is_paused = false;

        public synchronized void pause() {
            is_paused = true;
        }

        public synchronized void resume() {
            is_paused = false;
            while (!s.empty()) {
                sendMessageAtFrontOfQueue(s.pop());
            }
        }


        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case START_TIMER:


                    try {
                        while (counter < COUNTER_MAX){

                            if (is_paused) {
                                s.push(Message.obtain(msg));
                                return;
                            }else{
                                Thread.sleep(1000);
                                counter++;
                                timerLiveData.postValue(counter);
                                mNotificationManager.notify(NF_ID, getNotification(String.valueOf(counter)));
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    LiveDataHelper.getTimerStatus().postValue(LiveDataHelper.Timer_STATUS.NOT_STARTED);
                    stopForeground(true);
                    stopSelf();
                    break;
                case STOP_TIMER:
                    mNotificationManager.cancel(NF_ID);
                    counter = COUNTER_MAX+1;
                default:
                    super.handleMessage(msg);

            }

        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationManager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        timerLiveData = LiveDataHelper.getTimerLiveData();
        handlerThread = new HandlerThread("timer_thread");
        handlerThread.start();
        handler = new ServiceHandler(handlerThread.getLooper());

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = getNotification("0");
        startForeground(NF_ID, notification);
        LiveDataHelper.getServiceStatus().postValue(LiveDataHelper.SERVICE_STATUS.STARTED);
        return START_NOT_STICKY;
    }


    private Notification getNotification(String value) {

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0, notificationIntent, 0);
        return new NotificationCompat.Builder(this, TIMER_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_timer)
                .setContentTitle("Wait till " + String.valueOf(COUNTER_MAX))
                .setContentText(value)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
    }

    public void startTimer() {
        counter = 0;
        Message msg = handler.obtainMessage();
        msg.what = START_TIMER;
        handler.sendMessage(msg);
        LiveDataHelper.getTimerStatus().postValue(LiveDataHelper.Timer_STATUS.STARTED);
    }

    public void pauseTimer() {
        ((ServiceHandler) handler).pause();
        LiveDataHelper.getTimerStatus().postValue(LiveDataHelper.Timer_STATUS.PAUSED);

    }



    public void resumeTimer() {
        ServiceHandler s = new ServiceHandler(getMainLooper());
        ((ServiceHandler) handler).resume();

        LiveDataHelper.getTimerStatus().postValue(LiveDataHelper.Timer_STATUS.STARTED);

    }

    public void stopTimer() {
/*        ((ServiceHandler) handler).removeMessages(START_TIMER);
        LiveDataHelper.getTimerLiveData().postValue(0);
        LiveDataHelper.getTimerStatus().postValue(LiveDataHelper.Timer_STATUS.NOT_STARTED);*/

    }

    public class ServiceBinder extends Binder{

        public TimerService getService(){
            return TimerService.this;
        }

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return serviceBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LiveDataHelper.getServiceStatus().postValue(LiveDataHelper.SERVICE_STATUS.STOPPED);
    }
}
