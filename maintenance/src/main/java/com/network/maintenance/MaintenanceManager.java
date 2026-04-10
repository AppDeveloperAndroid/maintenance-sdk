package com.network.maintenance;

import android.app.Activity;
import android.app.Application;
import android.os.Handler;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

public class MaintenanceManager {
    private static Application appContext;
    private static FirebaseRemoteConfig secondaryRemoteConfig;
    private static final String SECONDARY_APP_NAME = "MAINTENANCE";
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

        appContext = application;

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

        initSecondaryFirebase(application);
        fetchConfig(appId);
    }

    public static void forceCheck() {
        checkMaintenance();
    }

    private static void fetchConfig(String appId) {

        if (secondaryRemoteConfig == null) {
            Log.e("Maintenance", "Secondary Firebase not initialized");
            return;
        }

        secondaryRemoteConfig.setConfigSettingsAsync(
                new FirebaseRemoteConfigSettings.Builder()
                        .setMinimumFetchIntervalInSeconds(60)
                        .build()
        );

        secondaryRemoteConfig.fetchAndActivate().addOnCompleteListener(task -> {

            Log.d("Maintenance", "Fetch success (SDK Firebase): " + task.isSuccessful());

            if (task.isSuccessful()) {

                String json = secondaryRemoteConfig.getString("maintenance_config");
                Log.d("Maintenance", "SDK JSON: " + json);

                currentConfig = MaintenanceParser.getConfig(json, appId);

                if (currentConfig != null) {
                    Log.d("Maintenance", "Parsed Config:");
                    Log.d("Maintenance", "isActive: " + currentConfig.isActive);
                    Log.d("Maintenance", "startTime: " + currentConfig.startTime);
                    Log.d("Maintenance", "endTime: " + currentConfig.endTime);
                    Log.d("Maintenance", "message: " + currentConfig.message);
                    Log.d("Maintenance", "title: " + currentConfig.title);
                }

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

        if (secondaryRemoteConfig == null) return;

        secondaryRemoteConfig.fetchAndActivate().addOnCompleteListener(task -> {

            if (task.isSuccessful()) {

                String json = secondaryRemoteConfig.getString("maintenance_config");
                Log.d("Maintenance", "Updated SDK JSON: " + json);

                currentConfig = MaintenanceParser.getConfig(json, appId);

                if (currentConfig != null) {
                    Log.d("Maintenance", "Updated Parsed Config:");
                    Log.d("Maintenance", "isActive: " + currentConfig.isActive);
                    Log.d("Maintenance", "startTime: " + currentConfig.startTime);
                    Log.d("Maintenance", "endTime: " + currentConfig.endTime);
                    Log.d("Maintenance", "message: " + currentConfig.message);
                    Log.d("Maintenance", "title: " + currentConfig.title);
                }

//                checkMaintenance();
            }
        });
    }

    private static void checkMaintenance() {

        if (currentConfig == null) return;

        long now = System.currentTimeMillis();

        MaintenanceState newState;

        Log.d("Maintenance", "now=" + now + " start=" + currentConfig.startTime + " end=" + currentConfig.endTime);

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

    private static void initSecondaryFirebase(Application application) {
        try {
            FirebaseApp secondaryApp;

            for (FirebaseApp app : FirebaseApp.getApps(application)) {
                if (SECONDARY_APP_NAME.equals(app.getName())) {
                    secondaryApp = app;
                    secondaryRemoteConfig = FirebaseRemoteConfig.getInstance(secondaryApp);
                    return;
                }
            }

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setApplicationId("1:662701941845:android:e08c059b3b35b9d0f5a0dc")
                    .setApiKey("AIzaSyDpnQVOr2qVC5gUIVNhqqZskK4WUO1QzDA")
                    .setProjectId("maintenance-control-system")
                    .build();

            secondaryApp = FirebaseApp.initializeApp(application, options, SECONDARY_APP_NAME);
            secondaryRemoteConfig = FirebaseRemoteConfig.getInstance(secondaryApp);

        } catch (Exception e) {
            Log.e("Maintenance", "Secondary Firebase init failed", e);
        }
    }
}