package com.example.meditrackpro;

import android.app.Application;

import timber.log.Timber;

public class MediTrackApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        NotificationHelper.createChannels(this);
        CaregiverAlarmScheduler.scheduleDailySummary(this);
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }
}
