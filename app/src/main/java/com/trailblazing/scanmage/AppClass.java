package com.trailblazing.scanmage;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.google.firebase.analytics.FirebaseAnalytics;
//import com.google.firebase.crashlytics.FirebaseCrashlytics;

public class AppClass extends Application {
    private static SharedPreferences sharedPreferences;

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseAnalytics.getInstance(this);
//        FirebaseCrashlytics.getInstance();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (sharedPreferences.getString("storage_location", "").isEmpty()) {
            sharedPreferences.edit().putString("storage_location", getExternalFilesDir("videos").toString());
        }
    }

    public static SharedPreferences getSP() {
        return sharedPreferences;
    }
}
