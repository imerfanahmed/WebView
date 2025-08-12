# Advanced Android WebView

A feature-rich Android WebView implementation with modern UI/UX features, comprehensive permission handling, and adaptive theming.

## Features

### 🎨 Dynamic Theming
- Adaptive status bar and navigation bar colors based on webpage theme
- Automatic light/dark icon switching based on background color
- Smooth color transitions
- Fallback to dominant webpage color when theme-color is not available

### 📱 Media Handling
- Full-screen video support with proper orientation handling
- Camera access for photo/video capture
- Microphone access for audio recording
- File upload support (single/multiple files)
- File download manager with progress tracking

### 📍 Location Services
- Geolocation support with permission handling
- Location accuracy settings
- Proper permission request flow

### 🔒 Advanced Permission Management
- Runtime permission handling for:
  - Storage (adaptive for Android 13+)
  - Camera
  - Microphone
  - Location
  - File access
- Proper permission state management
- User-friendly permission request dialogs

### 💫 UI/UX Features
- Pull-to-refresh functionality
- Horizontal progress loader
- Smooth fullscreen transitions
- Edge-to-edge content display
- Proper handling of system UI visibility
- Immersive mode for fullscreen content

### 🔧 Technical Features
- Support for modern Android versions (API 21+)
- Proper memory management
- Error handling and recovery
- State preservation
- SSL/TLS handling
- Custom WebView client implementation

## Setup

### Prerequisites
- Android Studio Arctic Fox or newer
- Minimum SDK: API 21 (Android 5.0)
- Target SDK: API 33 (Android 13)

### Dependencies
Add these dependencies to your app's build.gradle:
```gradle
dependencies {
    implementation 'androidx.webkit:webkit:1.6.1'
    implementation 'androidx.swiperefreshlayout:swiperefreshlayout:1.1.0'
    implementation 'androidx.core:core-ktx:1.9.0'
    implementation 'androidx.appcompat:appcompat:1.6.1'
}
```

### Permissions
Add these permissions to your AndroidManifest.xml:
```xml
<!-- Internet -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

<!-- Storage (Android 12 and below) -->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" android:maxSdkVersion="32" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" android:maxSdkVersion="32" />

<!-- Storage (Android 13+) -->
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
<uses-permission android:name="android.permission.READ_MEDIA_VIDEO" />
<uses-permission android:name="android.permission.READ_MEDIA_AUDIO" />

<!-- Camera & Microphone -->
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />

<!-- Location -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```

## Usage

### Basic Implementation
```java
webView.setWebViewClient(new CustomWebViewClient(this, webView));
webView.setWebChromeClient(new CustomWebChromeClient(this));

WebSettings settings = webView.getSettings();
settings.setJavaScriptEnabled(true);
settings.setDomStorageEnabled(true);
settings.setGeolocationEnabled(true);
```

### Handle Permissions
```java
@Override
public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    // Handle permission results using PermissionManager
}
```

## TODO

### High Priority
- [ ] Implement offline caching support
- [ ] Add dark mode support with proper WebView handling
- [ ] Implement custom error pages
- [ ] Add download progress tracking UI
- [ ] Implement certificate pinning for security

### Medium Priority
- [ ] Add support for custom user agents
- [ ] Implement JavaScript interfaces for native features
- [ ] Add support for push notifications
- [ ] Implement form data autofill
- [ ] Add screenshot/print functionality

### Low Priority
- [ ] Add support for custom protocols
- [ ] Implement ad-blocking capabilities
- [ ] Add support for multiple windows
- [ ] Implement custom context menus
- [ ] Add support for WebRTC

## Contributing
Contributions are welcome! Please feel free to submit a Pull Request.

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments
- Android WebView documentation
- Material Design guidelines
- AndroidX libraries
- Open source community

## Contact
For any queries or suggestions, please open an issue in the repository. 