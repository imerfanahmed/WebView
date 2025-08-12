package com.dataenvelope.edueasyadminportal;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionManager {
    private final Activity activity;
    public static final int STORAGE_PERMISSION_REQUEST_CODE = 1001;

    private static final String[] STORAGE_PERMISSIONS_BELOW_33 = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @SuppressLint("InlinedApi")
    private static final String[] STORAGE_PERMISSIONS_33_AND_ABOVE = new String[]{
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_AUDIO
    };

    public PermissionManager(Activity activity) {
        this.activity = activity;
    }

    public boolean checkAndRequestStoragePermissions() {
        if (hasStoragePermissions()) {
            return true;
        } else {
            requestStoragePermissions();
            return false;
        }
    }

    private boolean hasStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            for (String permission : STORAGE_PERMISSIONS_33_AND_ABOVE) {
                if (ContextCompat.checkSelfPermission(activity, permission) 
                        != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
            return true;
        } else {
            for (String permission : STORAGE_PERMISSIONS_BELOW_33) {
                if (ContextCompat.checkSelfPermission(activity, permission) 
                        != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
            return true;
        }
    }

    private void requestStoragePermissions() {
        String[] permissions = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                ? STORAGE_PERMISSIONS_33_AND_ABOVE
                : STORAGE_PERMISSIONS_BELOW_33;

        ActivityCompat.requestPermissions(
                activity,
                permissions,
                STORAGE_PERMISSION_REQUEST_CODE
        );
    }

    public interface PermissionCallback {
        void onPermissionGranted();
        void onPermissionDenied();
    }

    public void handlePermissionResult(int requestCode, String[] permissions,
                                     int[] grantResults, PermissionCallback callback) {
        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            if (grantResults.length > 0) {
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        allGranted = false;
                        break;
                    }
                }
            } else {
                allGranted = false;
            }

            if (allGranted) {
                callback.onPermissionGranted();
            } else {
                callback.onPermissionDenied();
            }
        }
    }
} 