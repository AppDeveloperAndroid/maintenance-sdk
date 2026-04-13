package com.network.maintenance;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.material.card.MaterialCardView;

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
                MaterialCardView btnOk = view.findViewById(R.id.btnOk);

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
                }
            }

        } else if (state == MaintenanceState.BLOCKING) {

            if (blockingDialog != null && blockingDialog.isShowing()) return;

            View view = activity.getLayoutInflater().inflate(R.layout.alert_dialog, null);
            ImageView ivIcon = view.findViewById(R.id.ivIcon);
            TextView tvTitle = view.findViewById(R.id.tvTitle);
            TextView tvDesc = view.findViewById(R.id.tvDesc);
            MaterialCardView btnOk = view.findViewById(R.id.btnOk);

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
}