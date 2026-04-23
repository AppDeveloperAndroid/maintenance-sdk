package com.network.maintenance;

import org.json.JSONObject;

public class MaintenanceParser {

    public static MaintenanceModel getConfig(String json, String appId) {
        try {
            JSONObject root = new JSONObject(json);

            JSONObject apps = root.optJSONObject("apps");
            JSONObject app = apps != null ? apps.optJSONObject(appId) : null;

            MaintenanceModel model = new MaintenanceModel();

            // ✅ Always read app config (NOT dependent on isActive)
            if (app != null) {

                // Maintenance
                model.isActive = app.optBoolean("isActive");
                model.startTime = app.optLong("startTime");
                model.endTime = app.optLong("endTime");
                model.title = app.optString("title");
                model.message = app.optString("message");

                // ✅ UPDATE PARSING (independent)
                if (app.has("update")) {

                    JSONObject updateObj = app.getJSONObject("update");

                    UpdateModel update = new UpdateModel();
                    update.isActive = updateObj.optBoolean("isActive");
                    update.minVersion = updateObj.optInt("minVersion");
                    update.title = updateObj.optString("title");
                    update.message = updateObj.optString("message");
                    update.navigateUrl = updateObj.optString("navigateUrl");

                    model.update = update;

                    android.util.Log.d("MaintenanceSDK", "Update parsed ✅");
                    android.util.Log.d("MaintenanceSDK", "Update isActive: " + update.isActive);
                    android.util.Log.d("MaintenanceSDK", "Update minVersion: " + update.minVersion);

                } else {
                    android.util.Log.e("MaintenanceSDK", "No update object found ❌");
                }

            } else {
                model.isActive = false;
            }

            return model;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}