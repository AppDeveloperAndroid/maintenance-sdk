package com.network.maintenance;

import android.app.Activity;
import android.app.Application;
import android.os.Handler;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

public class MaintenanceManager {
    private static boolean isCheckingStarted = false;
//    private static MaintenanceListener listener;
    private static Activity currentActivity;

    /*public static void setListener(MaintenanceListener l) {
        listener = l;
        if (currentConfig != null) {
            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                updateUI(currentState);
            });
        }
    }*/
    private static final long CHECK_INTERVAL = 15000;
    private static final long ONE_DAY = 24 * 60 * 60 * 1000;

    private static MaintenanceModel currentConfig;
    private static MaintenanceState currentState = MaintenanceState.NONE;

//    private static Handler handler = new Handler();
    private static Handler handler = new Handler(android.os.Looper.getMainLooper());

    public static void init(Application application, String appId) {

        // Track current activity
        application.registerActivityLifecycleCallbacks(new SimpleActivityLifecycle() {
            @Override
            public void onActivityResumed(Activity activity) {
                currentActivity = activity;
            }

            @Override
            public void onActivityPaused(Activity activity) {
                if (currentActivity == activity) {
                    currentActivity = null;
                }
            }
        });

        fetchConfig(appId);
    }

    public static void forceCheck() {
        checkMaintenance();
    }

    private static void fetchConfig(String appId) {

        FirebaseRemoteConfig remoteConfig;

        try {
            remoteConfig = FirebaseRemoteConfig.getInstance();
        } catch (Exception e) {
            Log.e("Maintenance", "RemoteConfig init failed", e);
            return;
        }

        remoteConfig.setConfigSettingsAsync(
                new FirebaseRemoteConfigSettings.Builder()
                        .setMinimumFetchIntervalInSeconds(60)
                        .build()
        );

        FirebaseRemoteConfig finalRemoteConfig = remoteConfig;
        Log.d("FirebaseCheck", "App: " + FirebaseApp.getInstance().getName());
        remoteConfig.fetchAndActivate().addOnCompleteListener(task -> {
            Log.d("Maintenance", "Fetch success: " + task.isSuccessful());

            if (task.isSuccessful()) {

                String json = finalRemoteConfig.getString("maintenance_config");
                Log.d("kkkkkkkkkkkkkk", "JSON: " + json);
                currentConfig = MaintenanceParser.getConfig(json, appId);

                if (currentConfig != null) {
                    Log.d("kkkkkkkkkkkkkk", "Parsed Config:");
                    Log.d("kkkkkkkkkkkkkk", "isActive: " + currentConfig.isActive);
                    Log.d("kkkkkkkkkkkkkk", "startTime: " + currentConfig.startTime);
                    Log.d("kkkkkkkkkkkkkk", "endTime: " + currentConfig.endTime);
                    Log.d("kkkkkkkkkkkkkk", "message: " + currentConfig.message);
                    Log.d("kkkkkkkkkkkkkk", "title: " + currentConfig.title);
                } else {
                    Log.d("kkkkkkkkkkkkkk", "Config is NULL");
                }
//                startChecking(appId);
                if (!isCheckingStarted) {
                    isCheckingStarted = true;
                    startChecking(appId);
                }
            }
        });
    }

    private static long lastFetchTime = 0;

    private static void startChecking(String appId) {

        handler.post(new Runnable() {
            @Override
            public void run() {

                long now = System.currentTimeMillis();


                if (now - lastFetchTime >= 20000) {
                    fetchLatestConfig(appId);
                    lastFetchTime = now;
                }

                checkMaintenance();

                handler.postDelayed(this, CHECK_INTERVAL);
            }
        });
    }

    private static void fetchLatestConfig(String appId) {

        FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();

        remoteConfig.fetchAndActivate().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                String json = remoteConfig.getString("maintenance_config");
                Log.d("Maintenance", "Updated JSON: " + json);

                currentConfig = MaintenanceParser.getConfig(json, appId);

                if (currentConfig != null) {
                    Log.d("kkkkkkkkkkkkkk", "1 Parsed Config:");
                    Log.d("kkkkkkkkkkkkkk", "1 isActive: " + currentConfig.isActive);
                    Log.d("kkkkkkkkkkkkkk", "1 startTime: " + currentConfig.startTime);
                    Log.d("kkkkkkkkkkkkkk", "1 endTime: " + currentConfig.endTime);
                    Log.d("kkkkkkkkkkkkkk", "1 message: " + currentConfig.message);
                    Log.d("kkkkkkkkkkkkkk", "1 title: " + currentConfig.title);
                } else {
                    Log.d("kkkkkkkkkkkkkk", "1 Config is NULL");
                }

                checkMaintenance();
            }
        });
    }

    private static void checkMaintenance() {

        if (currentConfig == null) return;

        long now = System.currentTimeMillis();

        MaintenanceState newState;

        Log.d("kkkkkkkkkkkkk TIME_DEBUG", "now=" + now + " start=" + currentConfig.startTime + " end=" + currentConfig.endTime);

        if (!currentConfig.isActive) {
            newState = MaintenanceState.NONE;

        } else if (now >= currentConfig.startTime && now <= currentConfig.endTime) {
            newState = MaintenanceState.BLOCKING;

        } else if (now < currentConfig.startTime &&
                now >= (currentConfig.startTime - ONE_DAY)) {
            newState = MaintenanceState.WARNING;

        } else {
            newState = MaintenanceState.NONE;
        }

        if (newState != currentState) {
            Log.d("STATE_CHANGE", currentState + " → " + newState);
            currentState = newState;
            updateUI(newState);
        } else {
            updateUI(newState);
        }
    }

    private static void updateUI(MaintenanceState state) {

        if (currentActivity == null) return;

        String message = currentConfig != null ? currentConfig.message : "";
        String title = currentConfig != null ? currentConfig.title : "";
        MaintenanceUI.handleUI(currentActivity, state, title, message, currentConfig.startTime);
    }
}