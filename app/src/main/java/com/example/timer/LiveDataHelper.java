package com.example.timer;

import androidx.lifecycle.MutableLiveData;

public class LiveDataHelper {

    public enum Timer_STATUS {
        STARTED, PAUSED, NOT_STARTED
    }

    public enum SERVICE_STATUS {
        STARTED, STOPPED
    }

    static MutableLiveData<SERVICE_STATUS> serviceStatus;

    public static MutableLiveData<SERVICE_STATUS> getServiceStatus() {
        if(serviceStatus == null){
            serviceStatus = new MutableLiveData<>();
            serviceStatus.postValue(SERVICE_STATUS.STOPPED);
        }
        return serviceStatus;
    }

    static MutableLiveData<Timer_STATUS> timerStatus;

    public static MutableLiveData<Timer_STATUS> getTimerStatus() {
        if(timerStatus == null){
            timerStatus = new MutableLiveData<>();
            timerStatus.postValue(Timer_STATUS.NOT_STARTED);
        }
        return timerStatus;
    }

    static MutableLiveData<Integer> timerLiveData;

    public static MutableLiveData<Integer> getTimerLiveData() {
        if(timerLiveData == null){
            timerLiveData = new MutableLiveData<>();
            timerLiveData.postValue(0);
        }
        return timerLiveData;
    }

    public static void setTimerLiveData(MutableLiveData<Integer> timerLiveData) {
        LiveDataHelper.timerLiveData = timerLiveData;
    }
}
