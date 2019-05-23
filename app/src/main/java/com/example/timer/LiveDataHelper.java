package com.example.timer;

import androidx.lifecycle.MutableLiveData;

public class LiveDataHelper {
    static MutableLiveData<Integer> timerLiveData;

    public static MutableLiveData<Integer> getTimerLiveData() {
        if(timerLiveData == null){
            timerLiveData = new MutableLiveData<>();
            timerLiveData.postValue(-1);
        }
        return timerLiveData;
    }

    public static void setTimerLiveData(MutableLiveData<Integer> timerLiveData) {
        LiveDataHelper.timerLiveData = timerLiveData;
    }
}
