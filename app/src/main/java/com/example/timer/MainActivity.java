package com.example.timer;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.timer.sservices.TimerService;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    public static final String TIMER_CHANNEL_ID = "timer";
    @BindView(R.id.timer)
    TextView timerTV;

    @BindView(R.id.pause)
    Button pauseBtn;

    @BindView(R.id.resume)
    Button resumeBtn;

    @BindView(R.id.stop)
    Button stopBtn;

    @BindView(R.id.start)
    Button startBtn;



    MutableLiveData<Integer> timerLiveData;
    private TimerService mService;
    private boolean mBound = false;
    private Intent timerServiceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        createNotificationChannel();

        timerServiceIntent = new Intent(this, TimerService.class);



        timerLiveData = LiveDataHelper.getTimerLiveData();
        timerLiveData.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                timerTV.setText(String.valueOf(integer));
            }
        });


        LiveDataHelper.getServiceStatus().observe(this, new Observer<LiveDataHelper.SERVICE_STATUS>() {
            @Override
            public void onChanged(LiveDataHelper.SERVICE_STATUS service_status) {
                if(service_status != LiveDataHelper.SERVICE_STATUS.STARTED){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(timerServiceIntent);
                    }
                    else{
                        startService(timerServiceIntent);
                    }
                }
            }
        });




        LiveDataHelper.getTimerStatus().observe(this, new Observer<LiveDataHelper.Timer_STATUS>() {
            @Override
            public void onChanged(LiveDataHelper.Timer_STATUS timer_status) {
                if(timer_status == LiveDataHelper.Timer_STATUS.NOT_STARTED){
                    startBtn.setVisibility(View.VISIBLE);
                    stopBtn.setVisibility(View.GONE);
                    pauseBtn.setVisibility(View.GONE);
                    resumeBtn.setVisibility(View.GONE);
                }
                else if(timer_status == LiveDataHelper.Timer_STATUS.STARTED){
                    startBtn.setVisibility(View.GONE);
                    stopBtn.setVisibility(View.VISIBLE);
                    pauseBtn.setVisibility(View.VISIBLE); //VISIBLE
                    resumeBtn.setVisibility(View.GONE);
                }
                else if(timer_status == LiveDataHelper.Timer_STATUS.PAUSED){
                    startBtn.setVisibility(View.GONE);
                    stopBtn.setVisibility(View.VISIBLE);
                    pauseBtn.setVisibility(View.GONE);
                    resumeBtn.setVisibility(View.VISIBLE); //VISIBLE
                }
            }
        });


        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mService.startTimer();
            }
        });

        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mService.stopTimer();
            }
        });

        pauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mService.pauseTimer();
            }
        });

        resumeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mService.resumeTimer();
            }
        });
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            TimerService.ServiceBinder binder = (TimerService.ServiceBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
        }
    };

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(TIMER_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindService(timerServiceIntent, serviceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(serviceConnection);
        mBound = false;
    }
}
