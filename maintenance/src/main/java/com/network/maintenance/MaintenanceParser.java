package com.network.maintenance;

import org.json.JSONObject;

public class MaintenanceParser {

    public static MaintenanceModel getConfig(String json, String appId) {
        try {
            JSONObject root = new JSONObject(json);

            JSONObject global = root.optJSONObject("global");
            JSONObject apps = root.optJSONObject("apps");
            JSONObject app = apps != null ? apps.optJSONObject(appId) : null;

            // Priority: app > global
            JSONObject selected = null;

            if (app != null && app.optBoolean("isActive")) {
                selected = app;
            } else if (global != null && global.optBoolean("isActive")) {
                selected = global;
            }

            MaintenanceModel model = new MaintenanceModel();

            if (selected != null) {
                model.isActive = selected.optBoolean("isActive");
                model.startTime = selected.optLong("startTime");
                model.endTime = selected.optLong("endTime");
                model.title = selected.optString("title");
                model.message = selected.optString("message");
            } else {
                model.isActive = false;
                model.startTime = 0;
                model.endTime = 0;
                model.title = "";
                model.message = "";
            }

            return model;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}