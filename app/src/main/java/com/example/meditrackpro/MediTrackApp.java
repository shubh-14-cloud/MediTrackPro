package com.example.meditrackpro;

import android.app.Application;

import timber.log.Timber;

public class MediTrackApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Set the saved light/dark choice once for the whole process, before any screen opens.
        AppThemeHelper.applyTheme(this);
        NotificationHelper.createChannels(this);
        CaregiverAlarmScheduler.scheduleDailySummary(this);
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }
}
