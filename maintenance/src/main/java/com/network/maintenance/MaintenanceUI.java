package com.network.maintenance;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;

public class MaintenanceUI {
    private static AlertDialog blockingDialog;

    public static void handleUI(Activity activity, MaintenanceState state,  String title, String message, long startTime) {

        if (activity == null || activity.isFinishing()) return;

        if (state == MaintenanceState.WARNING) {

            if (shouldShowWarning(activity, startTime)) {
                new AlertDialog.Builder(activity)
                        .setTitle(title)
                        .setMessage(message)
                        .setPositiveButton("OK", null)
                        .show();
            }

        } else if (state == MaintenanceState.BLOCKING) {

            if (blockingDialog != null && blockingDialog.isShowing()) return;

            blockingDialog = new AlertDialog.Builder(activity)
                    .setTitle(title)
                    .setMessage(message)
                    .setCancelable(false)
                    .create();

            blockingDialog.show();

        } else if (state == MaintenanceState.NONE) {

            if (blockingDialog != null && blockingDialog.isShowing()) {
                blockingDialog.dismiss();
            }
        }
    }

    private static boolean shouldShowWarning(Context context, long startTime) {

        SharedPreferences prefs = context.getSharedPreferences("maintenance", Context.MODE_PRIVATE);

        long lastShownFor = prefs.getLong("last_warning_for", -1);

        if (lastShownFor == startTime) {
            return false;
        }

        prefs.edit().putLong("last_warning_for", startTime).apply();

        return true;
    }
}