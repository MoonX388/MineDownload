# 🎮 MineDownload

**APK Extractor & Uploader** - Extract installed APKs and upload to VPS for testing purposes.

---

## ✨ Features

- 🔐 **Google Account Authentication** - Uses device Google account (no Web Client ID needed for now)
- 📦 **APK Extraction** - Extract APK from installed apps on device
- ☁️ **VPS Upload** - Upload extracted APK to your VPS server
- 🎨 **Modern UI** - Beautiful Material Design with smooth animations
- 🚀 **Background Processing** - Downloads run in background service

---

## 🚀 Quick Start

### 1. Install App
```bash
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

### 2. Prepare Device
- Make sure you have a Google account logged in on device
- Install the target app (e.g., Minecraft) that you want to extract

### 3. Use App
1. Open **MineDownload** app
2. Click **Sign In with Google** (will use device account)
3. Enter **Version Code** (e.g., 1013)
4. Click **Add to Queue**
5. APK will be extracted and uploaded to VPS

---

## ⚙️ Configuration

### VPS Upload URL
Edit `app/src/main/java/com/moonx/app/relay/RelayUploader.kt`:
```kotlin
private const val VPS_UPLOAD_URL = "https://your-vps.example.com/api/upload_stream"
```

### Authorization Token
Edit `app/src/main/java/com/moonx/app/relay/RelayUploader.kt`:
```kotlin
.addHeader("Authorization", "Bearer YOUR_APP_UPLOAD_TOKEN")
```

### Target Package
Edit `app/src/main/java/com/moonx/app/ui/MainActivity.kt`:
```kotlin
"com.mojang.minecraftpe",  // Change to your target package
```

---

## 📱 UI Features

### Animations
- ✨ **Card Entrance** - Smooth slide-in animation on app start
- 🎯 **Button Click** - Scale animation for tactile feedback
- ✅ **Success Pulse** - Card elevation pulse on success
- ❌ **Error Shake** - Shake animation on error
- 🎨 **Material Design** - Modern cards with shadows and colors

### Color Scheme
- **Primary (Green)**: `#4CAF50` - Header card
- **Google Blue**: `#4285F4` - Login button
- **Orange**: `#FF9800` - Action button
- **Gray**: `#F5F5F5` - Status card background

---

## 🔧 How It Works

### Flow
```
1. User clicks "Sign In with Google"
   ↓
2. App gets token from device Google account
   ↓
3. User enters version code and clicks "Add to Queue"
   ↓
4. RelayDownloadService starts in background
   ↓
5. PlayStoreDownloader extracts APK from installed app
   ↓
6. RelayUploader streams APK to VPS
   ↓
7. Done! ✅
```

### Components
- **MainActivity** - Main UI with animations
- **GoogleAuth** - Device account authentication
- **PlayStoreDownloader** - Extract APK from installed apps
- **RelayUploader** - Upload APK stream to VPS
- **RelayDownloadService** - Background service for processing

---

## ⚠️ Important Notes

### Current Status
- ✅ **UI & Animations** - Working perfectly
- ✅ **APK Extraction** - Working (from installed apps)
- ⚠️ **Google Auth** - Needs Google Cloud Console setup for full functionality
- ⚠️ **Play Store API** - Not implemented (requires Google Console + risky)

### Google Cloud Console Setup (Optional)
If you want full Google OAuth functionality:
1. Go to [Google Cloud Console](https://console.cloud.google.com)
2. Create new project
3. Enable "Google Play Android Developer API"
4. Create OAuth 2.0 credentials
5. Add package name and SHA-1 fingerprint
6. Update `GoogleAuth.kt` with proper scope

**Note**: This is optional. App works without it for basic APK extraction.

---

## 🎯 Use Cases

### ✅ Legal Use
- Testing your own app versions
- Backup APKs you own
- Internal development testing
- Educational purposes

### ❌ Illegal Use
- Distributing pirated APKs
- Bypassing paid apps
- Violating developer copyrights
- Commercial use without permission

---

## 📝 Logs

Monitor app activity:
```bash
adb logcat | grep -E "(MainActivity|GoogleAuth|PlayStoreDownloader|RelayUploader|RelayDownloadService)"
```

Expected output:
```
D/MainActivity: Queued version code: 1013
D/RelayDownloadService: Start relay job for com.mojang.minecraftpe vc=1013
D/PlayStoreDownloader: Extracting APK from installed app: com.mojang.minecraftpe
D/PlayStoreDownloader: Found APK at: /data/app/.../base.apk
D/PlayStoreDownloader: Opening stream for APK: base.apk (XXX bytes)
D/RelayUploader: Uploading stream to VPS...
D/RelayDownloadService: Upload finished for com.mojang.minecraftpe @1013
```

---

## 🐛 Troubleshooting

### "UnregisteredOnApiConsole" Error
**Cause**: OAuth scope not registered in Google Cloud Console  
**Solution**: Either setup Google Console OR ignore (app will still work for APK extraction)

### "Package not installed" Error
**Cause**: Target app not installed on device  
**Solution**: Install the app first before trying to extract

### "Failed to upload" Error
**Cause**: VPS URL or token incorrect  
**Solution**: Check `RelayUploader.kt` configuration

---

## 📚 Tech Stack

- **Language**: Kotlin
- **UI**: Material Design Components, CardView, ConstraintLayout
- **Animations**: ObjectAnimator, View Animations
- **Networking**: OkHttp, Retrofit
- **Security**: EncryptedSharedPreferences
- **Background**: Coroutines, Services
- **Auth**: Google Play Services Auth

---

## 📄 License

This project is for **educational and testing purposes only**.  
Use responsibly and respect app developers' rights.

---

## 🤝 Contributing

Feel free to improve the UI, add features, or fix bugs!

---

**Made with ❤️ for testing purposes**
