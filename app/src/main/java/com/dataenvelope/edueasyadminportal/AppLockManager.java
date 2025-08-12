package com.dataenvelope.edueasyadminportal;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class AppLockManager {

    private static final String TAG = AppLockManager.class.getSimpleName();
    private static final String CONFIG_URL = "https://muhitasraf.github.io/webview_control.json";
    private static final String PREFS_NAME = "AppLockPrefs";
    private static final String APP_ID = "com.bouopentv.webview";  // Change this per app

    public interface LockCallback {
        void onLocked(String message, String redirectUrl);
        void onUnlocked();
    }

    // ... existing code ...

    public static void checkLockStatus(Context context, LockCallback callback) {
        if(!isFrequentlyChecked(context)){
            return;
        }
        new Thread(() -> {
            try {
                JSONObject jsonResponse = getJsonObject();
                Log.d(TAG, "checkLockStatus: "+jsonResponse.has(APP_ID));
                if (jsonResponse.has(APP_ID)) {
                    JSONObject appConfig = jsonResponse.getJSONObject(APP_ID);
                    Log.d(TAG, "checkLockStatus: "+appConfig);
                    boolean isLocked = appConfig.optBoolean("is_locked", false);
                    boolean isFrequentlyChecked = appConfig.optBoolean("frequently_check", false);
                    boolean isForceClose = appConfig.optBoolean("is_force_closed", false);
                    String redirectUrl = appConfig.optString("redirect_url", "");
                    String action = appConfig.optString("action", "");

                    // Get message object
                    JSONObject messageObj = appConfig.optJSONObject("message");
                    String title = messageObj != null ? messageObj.optString("titile", "Warning") : "Warning";
                    String body = messageObj != null ? messageObj.optString("body", "App is locked") : "App is locked";
                    String posButton = messageObj != null ? messageObj.optString("pos_btn", "OK") : "OK";
                    String negButton = messageObj != null ? messageObj.optString("neg_btn", "Cancel") : "Cancel";

                    saveFrequentlyCheckStatus(context, isFrequentlyChecked);
                    saveLockStatus(context, isLocked);

                    if (isLocked) {
                        callback.onLocked(body, redirectUrl);
                        new Handler(context.getMainLooper()).post(() -> {
                            new MaterialAlertDialogBuilder(context)
                                    .setTitle(title)
                                    .setMessage(body)
                                    .setPositiveButton(posButton, (dialog, which) -> {
                                        // Handle force close if enabled
                                        if (isForceClose) {
                                            System.exit(0);
                                        }
                                    })
                                    .setNegativeButton(negButton, (dialog, which) -> {
                                        if (isForceClose) {
                                            System.exit(0);
                                        }
                                        dialog.dismiss();
                                    })
                                    .setCancelable(false)
                                    .show();
                        });
                    } else {
                        callback.onUnlocked();
                    }
                }
            } catch (Exception e) {
                Log.e("AppLockManager", "Error fetching lock status", e);
            }
        }).start();
    }

// ... existing code ...

    @NonNull
    private static JSONObject getJsonObject() throws IOException, JSONException {
        URL url = new URL(CONFIG_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        JSONObject jsonResponse = new JSONObject(response.toString());
        return jsonResponse;
    }

    private static void saveLockStatus(Context context, boolean isLocked) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean("isLocked", isLocked).apply();
    }

    private static void saveFrequentlyCheckStatus(Context context, boolean status) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean("isFrequentlyChecked", status).apply();
    }

    public static boolean isAppLocked(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean("isLocked", false);
    }

    public static boolean isFrequentlyChecked(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean("isFrequentlyChecked", true);
    }
}
