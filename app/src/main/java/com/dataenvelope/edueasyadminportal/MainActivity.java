package com.dataenvelope.edueasyadminportal;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.webkit.GeolocationPermissions;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;
import android.app.DownloadManager;
import android.content.Context;
import android.webkit.CookieManager;
import android.webkit.URLUtil;
import android.webkit.DownloadListener;
import android.location.LocationManager;
import androidx.appcompat.app.AlertDialog;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private WebView webView;
    private CustomWebChromeClient webChromeClient;
    private PermissionManager permissionManager;
    private ValueCallback<Uri[]> pendingFilePathCallback;
    private static final String[] LOCATION_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    private static final int MEDIA_PERMISSION_REQUEST_CODE = 1003;
    private boolean pendingCameraPermission = false;
    private boolean pendingMicrophonePermission = false;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1004;
    private GeolocationPermissions.Callback geolocationCallback;
    private String geolocationOrigin;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private float startY;
    private boolean isScrolling = false;

    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                    permissions -> {
                        boolean allGranted = true;
                        for (Boolean isGranted : permissions.values()) {
                            if (!isGranted) {
                                allGranted = false;
                                break;
                            }
                        }
                        if (allGranted && pendingFilePathCallback != null) {
                            openFilePicker();
                        } else {
                            if (pendingFilePathCallback != null) {
                                pendingFilePathCallback.onReceiveValue(null);
                                pendingFilePathCallback = null;
                            }
                            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                        }
                    });

    private final ActivityResultLauncher<Intent> fileChooserLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Uri[] results = null;
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        if (data.getClipData() != null) {
                            // Handle multiple files
                            int count = data.getClipData().getItemCount();
                            results = new Uri[count];
                            for (int i = 0; i < count; i++) {
                                results[i] = data.getClipData().getItemAt(i).getUri();
                            }
                        } else if (data.getData() != null) {
                            // Handle single file
                            results = new Uri[]{data.getData()};
                        }
                    }
                }
                if (pendingFilePathCallback != null) {
                    pendingFilePathCallback.onReceiveValue(results);
                    pendingFilePathCallback = null;
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = findViewById(R.id.progressBar);
        swipeRefreshLayout = findViewById(R.id.swipeRefresh);
        
        permissionManager = new PermissionManager(this);
        setupWebView();
        setupSwipeRefresh();
        AppLockManager.checkLockStatus(this, new AppLockManager.LockCallback() {
            @Override
            public void onLocked(String message, String redirectUrl) {

            }

            @Override
            public void onUnlocked() {

            }
        });
//        webView.loadUrl("file:///android_asset/test.html");
        webView.loadUrl("https://edueasy.jabalunnur.com");
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            webView.reload();
        });

        // Set the colors for the refresh animation
        swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
        );

        // Handle touch events to prevent refresh while scrolling webpage
        webView.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startY = event.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float y = event.getY();
                    float deltaY = y - startY;
                    
                    // Check if user is scrolling the webpage
                    if (Math.abs(deltaY) > 30) { // threshold for scrolling detection
                        isScrolling = true;
                    }
                    
                    // Disable SwipeRefreshLayout if webpage can scroll up or user is scrolling
                    if (webView.getScrollY() > 0 || isScrolling) {
                        swipeRefreshLayout.setEnabled(false);
                    } else {
                        swipeRefreshLayout.setEnabled(true);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    isScrolling = false;
                    startY = 0;
                    // Re-enable SwipeRefreshLayout after touch ends
                    swipeRefreshLayout.setEnabled(true);
                    break;
            }
            return false;
        });
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        webView = findViewById(R.id.webView);
        webView.setWebViewClient(new CustomWebViewClient(this, webView) {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(0);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        webChromeClient = new CustomWebChromeClient(this) {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                progressBar.setProgress(newProgress);
            }

            // Todo : (Restricted)

            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback,
                                           FileChooserParams fileChooserParams) {
                if (pendingFilePathCallback != null) {
                    pendingFilePathCallback.onReceiveValue(null);
                }
                pendingFilePathCallback = filePathCallback;
                checkAndRequestStoragePermission();
                return true;
            }

            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                geolocationOrigin = origin;
                geolocationCallback = callback;
                checkAndRequestLocationPermission();
            }
        };
        webView.setWebChromeClient(webChromeClient);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setGeolocationEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        // Caching
//        webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        settings.setJavaScriptEnabled(true);
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);

        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        // Todo : (Restricted)
        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition,
                                      String mimeType, long contentLength) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                    if (!permissionManager.checkAndRequestStoragePermissions()) {
                        Toast.makeText(MainActivity.this,
                            "Storage permission required for downloading files",
                            Toast.LENGTH_LONG).show();
                        return;
                    }
                }

                try {
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));

                    String fileName = URLUtil.guessFileName(url, contentDisposition, mimeType);

                    request.setDescription("Downloading file...");
                    request.setTitle(fileName);
                    request.setNotificationVisibility(
                            DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

                    String cookies = CookieManager.getInstance().getCookie(url);
                    if (cookies != null) {
                        request.addRequestHeader("cookie", cookies);
                    }

                    request.addRequestHeader("User-Agent", userAgent);

                    DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                    if (dm != null) {
                        dm.enqueue(request);
                        Toast.makeText(MainActivity.this,
                            "Downloading file...",
                            Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this,
                            "Cannot access download service",
                            Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this,
                        "Error starting download: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
                }
            }
        });

        // Set default location permission to denied
        GeolocationPermissions.getInstance().clearAll();
    }

    private void checkLocationPermissions() {
        if (!hasLocationPermissions()) {
            requestPermissionLauncher.launch(LOCATION_PERMISSIONS);
        }
    }

    private boolean hasLocationPermissions() {
        for (String permission : LOCATION_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) 
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void checkAndRequestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            openFilePicker(); // Android 13+ doesn't need storage permission for file picker
        } else {
            if (permissionManager.checkAndRequestStoragePermissions()) {
                openFilePicker();
            }
        }
    }

    private void openFilePicker() {
        try {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            fileChooserLauncher.launch(Intent.createChooser(intent, "Choose File"));
        } catch (Exception e) {
            Toast.makeText(this, "Cannot open file picker", Toast.LENGTH_SHORT).show();
            if (pendingFilePathCallback != null) {
                pendingFilePathCallback.onReceiveValue(null);
                pendingFilePathCallback = null;
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                checkLocationServicesEnabled();
            } else {
                Toast.makeText(this, "Location permission is required for this feature", 
                        Toast.LENGTH_LONG).show();
                if (geolocationCallback != null) {
                    geolocationCallback.invoke(geolocationOrigin, false, false);
                    geolocationCallback = null;
                    geolocationOrigin = null;
                }
            }
        } else if (requestCode == MEDIA_PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                webChromeClient.grantMediaPermissions();
            } else {
                webChromeClient.denyMediaPermissions();
                // Show a message about which permissions were denied
                StringBuilder message = new StringBuilder("Required permissions denied: ");
                if (pendingCameraPermission) {
                    message.append("Camera ");
                }
                if (pendingMicrophonePermission) {
                    message.append("Microphone");
                }
                Toast.makeText(this, message.toString(), Toast.LENGTH_LONG).show();
            }

            // Reset pending flags
            pendingCameraPermission = false;
            pendingMicrophonePermission = false;
        } else if (requestCode == PermissionManager.STORAGE_PERMISSION_REQUEST_CODE) {
            permissionManager.handlePermissionResult(requestCode, permissions, grantResults,
                    new PermissionManager.PermissionCallback() {
                        @Override
                        public void onPermissionGranted() {
                            openFilePicker();
                        }

                        @Override
                        public void onPermissionDenied() {
                            if (pendingFilePathCallback != null) {
                                pendingFilePathCallback.onReceiveValue(null);
                                pendingFilePathCallback = null;
                            }
                            Toast.makeText(MainActivity.this, 
                                    "Storage permission required for file operations", 
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

    public void requestMediaPermissions(boolean needsCamera, boolean needsMicrophone) {
        List<String> permissions = new ArrayList<>();
        
        if (needsCamera) {
            pendingCameraPermission = true;
            permissions.add(Manifest.permission.CAMERA);
        }
        
        if (needsMicrophone) {
            pendingMicrophonePermission = true;
            permissions.add(Manifest.permission.RECORD_AUDIO);
        }

        if (!permissions.isEmpty()) {
            ActivityCompat.requestPermissions(this, 
                    permissions.toArray(new String[0]), 
                    MEDIA_PERMISSION_REQUEST_CODE);
        }
    }

    private void checkAndRequestLocationPermission() {
        if (hasLocationPermissions()) {
            checkLocationServicesEnabled();
        } else {
            ActivityCompat.requestPermissions(this,
                    LOCATION_PERMISSIONS,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void checkLocationServicesEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gpsEnabled = false;
        boolean networkEnabled = false;

        try {
            gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {}

        try {
            networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {}

        if (!gpsEnabled && !networkEnabled) {
            // Show location settings dialog
            new AlertDialog.Builder(this)
                    .setMessage("Location services are disabled. Would you like to enable them?")
                    .setPositiveButton("Settings", (dialogInterface, i) -> {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    })
                    .setNegativeButton("Cancel", (dialogInterface, i) -> {
                        if (geolocationCallback != null) {
                            geolocationCallback.invoke(geolocationOrigin, false, false);
                            geolocationCallback = null;
                            geolocationOrigin = null;
                        }
                    })
                    .show();
        } else {
            if (geolocationCallback != null) {
                geolocationCallback.invoke(geolocationOrigin, true, false);
                geolocationCallback = null;
                geolocationOrigin = null;
            }
        }
    }
}