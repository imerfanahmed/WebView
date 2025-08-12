package com.dataenvelope.edueasyadminportal;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.GeolocationPermissions;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.Manifest;

import androidx.core.content.ContextCompat;
import android.content.pm.PackageManager;
import androidx.core.graphics.ColorUtils;

public class CustomWebChromeClient extends WebChromeClient {
    private Activity activity;
    private View customView;
    private WebChromeClient.CustomViewCallback customViewCallback;
    private int originalOrientation;
    private int originalSystemUiVisibility;
    private FrameLayout fullscreenContainer;
    private ValueCallback<Uri[]> filePathCallback;
    private PermissionRequest mPermissionRequest;
    private int defaultStatusBarColor;

    public CustomWebChromeClient(Activity activity) {
        this.activity = activity;
        this.defaultStatusBarColor = activity.getWindow().getStatusBarColor();
    }

    public ValueCallback<Uri[]> getFilePathCallback() {
        return filePathCallback;
    }

    public void setFilePathCallback(ValueCallback<Uri[]> filePathCallback) {
        this.filePathCallback = filePathCallback;
    }

    // Todo : (Restricted)
    @Override
    public void onShowCustomView(View view, CustomViewCallback callback) {


        customView = view;
        originalSystemUiVisibility = activity.getWindow().getDecorView().getSystemUiVisibility();
        originalOrientation = activity.getRequestedOrientation();
        customViewCallback = callback;

        // Create a new FrameLayout container for the custom view
        fullscreenContainer = new FrameLayout(activity);
        fullscreenContainer.setBackgroundColor(activity.getResources().getColor(android.R.color.black));

        // Add the custom view to the fullscreen container
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        fullscreenContainer.addView(customView, params);

        // Add the fullscreen container to the window
        FrameLayout decor = (FrameLayout) activity.getWindow().getDecorView();
        decor.addView(fullscreenContainer, params);

        // Set the proper flags to show the view in fullscreen mode
        activity.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

        // Set both status bar and navigation bar to transparent in fullscreen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().setStatusBarColor(Color.TRANSPARENT);
            activity.getWindow().setNavigationBarColor(Color.TRANSPARENT);
        }

        // Force landscape orientation for video
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    @Override
    public void onHideCustomView() {
        if (customView == null) return;

        // Remove the custom view
        FrameLayout decor = (FrameLayout) activity.getWindow().getDecorView();
        decor.removeView(fullscreenContainer);
        fullscreenContainer = null;
        customView = null;

        // Restore the system UI visibility
        activity.getWindow().getDecorView().setSystemUiVisibility(originalSystemUiVisibility);

        // Restore the original orientation
        activity.setRequestedOrientation(originalOrientation);

        // Restore the status bar color
        restoreDefaultStatusBarColor();

        // Call the custom view callback
        if (customViewCallback != null) {
            customViewCallback.onCustomViewHidden();
            customViewCallback = null;
        }
    }

    // Todo : (Restricted)
    @Override
    public void onPermissionRequest(PermissionRequest request) {
        mPermissionRequest = request;
        String[] requestedResources = request.getResources();

        activity.runOnUiThread(() -> {
            // Check if camera or microphone permissions are needed
            boolean needsCameraPermission = false;
            boolean needsMicrophonePermission = false;

            for (String r : requestedResources) {
                if (r.equals(PermissionRequest.RESOURCE_VIDEO_CAPTURE)) {
                    needsCameraPermission = true;
                }
                if (r.equals(PermissionRequest.RESOURCE_AUDIO_CAPTURE)) {
                    needsMicrophonePermission = true;
                }
            }

            // Check if we have all required permissions
            boolean hasAllPermissions = true;

            if (needsCameraPermission && ContextCompat.checkSelfPermission(activity,
                    Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                hasAllPermissions = false;
            }

            if (needsMicrophonePermission && ContextCompat.checkSelfPermission(activity,
                    Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                hasAllPermissions = false;
            }

            if (hasAllPermissions) {
                request.grant(requestedResources);
            } else {
                // Request permissions through MainActivity
                if (activity instanceof MainActivity) {
                    ((MainActivity) activity).requestMediaPermissions(needsCameraPermission,
                            needsMicrophonePermission);
                } else {
                    request.deny();
                }
            }
        });
    }

    public void grantMediaPermissions() {
        if (mPermissionRequest != null) {
            mPermissionRequest.grant(mPermissionRequest.getResources());
        }
    }

    public void denyMediaPermissions() {
        if (mPermissionRequest != null) {
            mPermissionRequest.deny();
        }
    }

    // Todo : (Restricted)
    @Override
    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePath,
                                   FileChooserParams fileChooserParams) {
        if (filePathCallback != null) {
            filePathCallback.onReceiveValue(null);
        }
        filePathCallback = filePath;
        return false;
    }

    @Override
    public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
        callback.invoke(origin, true, false);
    }

    @Override
    public void onReceivedTitle(WebView view, String title) {
        super.onReceivedTitle(view, title);
        // Try to get theme color from webpage
        String javascript = "javascript: (function() { " +
                "var metaTag = document.querySelector('meta[name=\"theme-color\"]'); " +
                "return metaTag ? metaTag.content : ''; " +
                "})()";
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            view.evaluateJavascript(javascript, value -> {
                String color = value.replaceAll("\"", "");
                if (!color.isEmpty()) {
                    try {
                        updateStatusBarColor(Color.parseColor(color));
                    } catch (IllegalArgumentException e) {
                        // Invalid color format, use dominant color from webpage
                        getDominantColor(view);
                    }
                } else {
                    // No theme-color meta tag, use dominant color
                    getDominantColor(view);
                }
            });
        }
    }

    private void getDominantColor(final WebView view) {
        view.post(() -> {
            view.setDrawingCacheEnabled(true);
            view.buildDrawingCache();
            try {
                // Get the top portion of the webpage
                android.graphics.Bitmap bitmap = view.getDrawingCache(true);
                if (bitmap != null) {
                    // Sample colors from the top of the page
                    int topPixelColor = bitmap.getPixel(bitmap.getWidth() / 2, 0);
                    updateStatusBarColor(topPixelColor);
                    view.destroyDrawingCache();
                } else {
                    restoreDefaultStatusBarColor();
                }
            } catch (Exception e) {
                restoreDefaultStatusBarColor();
            }
            view.setDrawingCacheEnabled(false);
        });
    }

    private void updateStatusBarColor(int color) {
        if (activity != null && !activity.isFinishing()) {
            Window window = activity.getWindow();
            if (window != null) {
                // Make status bar and nav bar color slightly darker for better contrast
                float[] hsl = new float[3];
                ColorUtils.colorToHSL(color, hsl);
                // Darken the color
                hsl[2] = Math.max(0f, hsl[2] - 0.1f);
                int darkColor = ColorUtils.HSLToColor(hsl);

                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(darkColor);
                
                // Set navigation bar color (Android 5.0 and above)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    window.setNavigationBarColor(darkColor);
                }

                // Set status bar and navigation bar icons to light or dark based on color brightness
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    View decorView = window.getDecorView();
                    int flags = decorView.getSystemUiVisibility();
                    if (ColorUtils.calculateLuminance(color) > 0.5) {
                        // Light color - use dark icons
                        flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                        }
                    } else {
                        // Dark color - use light icons
                        flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            flags &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                        }
                    }
                    decorView.setSystemUiVisibility(flags);
                }
            }
        }
    }

    private void restoreDefaultStatusBarColor() {
        if (activity != null && !activity.isFinishing()) {
            Window window = activity.getWindow();
            if (window != null) {
                window.setStatusBarColor(defaultStatusBarColor);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    window.setNavigationBarColor(defaultStatusBarColor);
                }
                
                // Restore system UI flags
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    View decorView = window.getDecorView();
                    int flags = decorView.getSystemUiVisibility();
                    if (ColorUtils.calculateLuminance(defaultStatusBarColor) > 0.5) {
                        flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                        }
                    } else {
                        flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            flags &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                        }
                    }
                    decorView.setSystemUiVisibility(flags);
                }
            }
        }
    }
} 