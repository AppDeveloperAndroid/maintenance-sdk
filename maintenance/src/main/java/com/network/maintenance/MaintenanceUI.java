package com.network.maintenance;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.cardview.widget.CardView;

public class MaintenanceUI {
    private static AlertDialog blockingDialog;

    public static void handleUI(Activity activity, MaintenanceState state,  String title, String message, long startTime) {

        if (activity == null || activity.isFinishing() || activity.isDestroyed()) return;

        if (state == MaintenanceState.WARNING) {

            if (shouldShowWarning(activity, startTime)) {
                View view = activity.getLayoutInflater().inflate(R.layout.alert_dialog, null);
                ImageView ivIcon = view.findViewById(R.id.ivIcon);
                TextView tvTitle = view.findViewById(R.id.tvTitle);
                TextView tvDesc = view.findViewById(R.id.tvDesc);
                CardView btnOk = view.findViewById(R.id.btnOk);

                tvTitle.setText(title);
                tvDesc.setText(message);
                ivIcon.setImageResource(R.drawable.ic_alert_yellow);

                AlertDialog dialog = new AlertDialog.Builder(activity)
                        .setView(view)
                        .setCancelable(true)
                        .create();

                btnOk.setVisibility(View.VISIBLE);
                btnOk.setOnClickListener(v -> dialog.dismiss());
                dialog.show();
                if (dialog.getWindow() != null) {
                    dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

                    int margin = 25; // dp
                    float density = activity.getResources().getDisplayMetrics().density;
                    int pxMargin = (int) (margin * density);

                    int width = activity.getResources().getDisplayMetrics().widthPixels - (pxMargin * 2);
                    dialog.getWindow().setLayout(width, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
                }
            }

        } else if (state == MaintenanceState.BLOCKING) {

            if (blockingDialog != null && blockingDialog.isShowing()) return;

            View view = activity.getLayoutInflater().inflate(R.layout.alert_dialog, null);
            ImageView ivIcon = view.findViewById(R.id.ivIcon);
            TextView tvTitle = view.findViewById(R.id.tvTitle);
            TextView tvDesc = view.findViewById(R.id.tvDesc);
            CardView btnOk = view.findViewById(R.id.btnOk);

            tvTitle.setText(title);
            tvDesc.setText(message);
            ivIcon.setImageResource(R.drawable.ic_alert_red);

            blockingDialog = new AlertDialog.Builder(activity)
                    .setView(view)
                    .setCancelable(false)
                    .create();

            btnOk.setVisibility(View.GONE);
            blockingDialog.show();
            if (blockingDialog.getWindow() != null) {
                blockingDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

                int margin = 25; // dp
                float density = activity.getResources().getDisplayMetrics().density;
                int pxMargin = (int) (margin * density);

                int width = activity.getResources().getDisplayMetrics().widthPixels - (pxMargin * 2);
                blockingDialog.getWindow().setLayout(width, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);

            }

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

    public static void showUpdateDialog(Activity activity, String title, String message, String url) {

        if (activity == null || activity.isFinishing() || activity.isDestroyed()) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Update", (d, w) -> openUrl(activity, url));


        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private static final String TAG = "MaintenanceSDK";

    private static void openUrl(Context context, String url) {

        try {
            Log.d(TAG, "Opening URL: " + url);

            if (url != null && url.contains("play.google.com")) {

                Uri uri = Uri.parse(url);
                String packageName = uri.getQueryParameter("id");

                Log.d(TAG, "Detected Play Store URL. Package: " + packageName);

                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=" + packageName));
                intent.setPackage("com.android.vending");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                context.startActivity(intent);

                Log.d(TAG, "Opened in Play Store app");

            } else {

                Log.d(TAG, "Opening in browser / external handler");

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                context.startActivity(intent);

                Log.d(TAG, "Opened URL successfully");

            }

        } catch (Exception e) {

            Log.e(TAG, "Failed to open via primary intent. Falling back to browser", e);

            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                context.startActivity(intent);

                Log.d(TAG, "Fallback: Opened URL in browser");

            } catch (Exception ex) {
                Log.e(TAG, "Fallback also failed. Cannot open URL", ex);
            }
        }
    }
}