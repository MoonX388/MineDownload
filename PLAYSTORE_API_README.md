# Play Store Internal API Implementation

## ⚠️ DISCLAIMER

**PERINGATAN PENTING:**

1. **Melanggar Terms of Service Google** - Menggunakan undocumented Play Store API melanggar ToS
2. **Risiko Ban Akun** - Akun Google Anda bisa di-ban permanen
3. **Tidak Didukung Resmi** - API ini tidak didokumentasikan dan bisa berubah kapan saja
4. **Gray Area Legal** - Status legal tidak jelas, gunakan dengan risiko sendiri
5. **Hanya untuk Testing** - Jangan gunakan untuk production atau komersial

**GUNAKAN AKUN TESTING, BUKAN AKUN UTAMA!**

---

## 🎯 Cara Kerja

### Flow Lengkap

```
1. User Login (ambil token dari akun Google device)
   ↓
2. User input version code dan klik "Add to Queue"
   ↓
3. RelayDownloadService dimulai
   ↓
4. PlayStoreDownloader mencoba download dari Play Store:
   a. Checkin ke Google Play Services (dapatkan Android ID)
   b. Get app details dari Play Store
   c. Purchase/acquire app (untuk free apps)
   d. Download APK stream
   ↓
5. Jika gagal, fallback ke ekstrak APK yang terinstall
   ↓
6. Upload APK stream ke VPS
   ↓
7. Selesai
```

### Komponen Utama

#### 1. `PlayStoreApiClient.kt`
Client untuk mengakses Play Store Internal API:
- `performCheckin()` - Checkin ke Play Services, dapatkan Android ID
- `getAppDetails()` - Ambil info aplikasi dari Play Store
- `purchaseApp()` - Acquire/purchase aplikasi (free apps)
- `downloadApk()` - Download APK stream dari Play Store
- `downloadApkFullFlow()` - Full flow dari checkin sampai download

#### 2. `PlayStoreDownloader.kt`
Wrapper yang mencoba download dari Play Store, fallback ke ekstrak installed:
- `openDownloadStream()` - Main method, coba Play Store dulu
- `extractInstalledApk()` - Fallback: ekstrak dari yang terinstall

#### 3. `GoogleAuth.kt`
Autentikasi menggunakan akun Google device:
- `getTokenFromFirstAccount()` - Ambil token dari akun pertama
- `getDeviceGoogleAccounts()` - List semua akun Google di device

---

## 🚀 Cara Menggunakan

### 1. Setup
```bash
# Build aplikasi
./gradlew assembleDebug

# Install ke device
adb install app/build/outputs/apk/debug/app-debug.apk
```

### 2. Persiapan
- Pastikan device punya akun Google yang login
- **GUNAKAN AKUN TESTING**, bukan akun utama
- Untuk testing, install aplikasi target (contoh: Minecraft)

### 3. Jalankan Aplikasi
1. Buka aplikasi
2. Klik **Login** - akan ambil token dari akun Google device
3. Input **version code** (contoh: 1013 untuk Minecraft)
4. Klik **Add to Queue**
5. Cek logcat untuk melihat progress

### 4. Monitor Log
```bash
adb logcat | grep -E "(PlayStoreApiClient|PlayStoreDownloader|RelayDownloadService)"
```

Expected logs:
```
D/PlayStoreDownloader: Attempting to download from Play Store API...
D/PlayStoreApiClient: Performing Play Store checkin...
D/PlayStoreApiClient: Checkin successful
D/PlayStoreApiClient: Fetching app details for: com.mojang.minecraftpe
D/PlayStoreApiClient: App details fetched successfully
D/PlayStoreApiClient: Acquiring app: com.mojang.minecraftpe (version: 1013)
D/PlayStoreApiClient: App acquired successfully
D/PlayStoreApiClient: Downloading APK: com.mojang.minecraftpe (version: 1013)
D/PlayStoreApiClient: APK download started (XXX bytes)
D/PlayStoreDownloader: Successfully downloaded from Play Store
D/RelayUploader: Uploading stream to VPS...
D/RelayDownloadService: Upload finished for com.mojang.minecraftpe @1013
```

---

## ⚙️ Konfigurasi

### VPS Upload URL
Edit `RelayUploader.kt`:
```kotlin
private const val VPS_UPLOAD_URL = "https://your-vps.example.com/api/upload_stream"
```

### Auth Token untuk VPS
Edit `RelayUploader.kt`:
```kotlin
.addHeader("Authorization", "Bearer YOUR_APP_UPLOAD_TOKEN")
```

---

## 🔧 Troubleshooting

### Error: "Checkin failed"
**Penyebab:** Play Store API menolak checkin request
**Solusi:**
- Pastikan device punya Google Play Services
- Coba gunakan akun Google yang berbeda
- Implementasi checkin perlu protobuf yang proper (simplified di kode ini)

### Error: "Failed to fetch app details"
**Penyebab:** Play Store API tidak mengembalikan app details
**Solusi:**
- Pastikan package name benar
- Pastikan app tersedia di Play Store untuk region device Anda
- Auth token mungkin tidak valid atau expired

### Error: "Failed to acquire app"
**Penyebab:** Tidak bisa purchase/acquire app
**Solusi:**
- Untuk paid apps, akun harus sudah beli app tersebut
- Untuk free apps, mungkin ada region restriction
- Coba install manual dari Play Store dulu

### Error: "Failed to download APK"
**Penyebab:** Play Store tidak memberikan download URL
**Solusi:**
- Fallback akan otomatis ke ekstrak installed APK
- Pastikan app sudah terinstall di device
- Version code yang diminta mungkin tidak tersedia

### Fallback ke Installed APK
Jika Play Store download gagal, sistem otomatis fallback:
```
W/PlayStoreDownloader: Play Store download failed: ..., falling back to installed APK
D/PlayStoreDownloader: Found APK at: /data/app/.../base.apk
D/PlayStoreDownloader: Opening stream for APK: base.apk (XXX bytes)
```

---

## 📝 Catatan Implementasi

### Simplified Implementation
Implementasi ini adalah **simplified version** dari Play Store API client:

1. **Protobuf tidak digunakan** - Play Store API menggunakan Protocol Buffers, tapi di sini menggunakan JSON simplified
2. **Device fingerprint hardcoded** - Menggunakan Pixel 6 fingerprint, seharusnya dynamic
3. **Android ID placeholder** - Checkin response perlu di-parse dari protobuf
4. **No encryption** - Play Store API menggunakan encryption untuk beberapa request
5. **No signature verification** - Play Store memverifikasi signature device

### Untuk Production
Jika Anda benar-benar ingin menggunakan ini (dengan risiko), perlu:

1. **Implementasi Protobuf** - Gunakan `protobuf-java` untuk encode/decode
2. **Dynamic Device Config** - Baca dari device, jangan hardcode
3. **Proper Checkin** - Implementasi full checkin protocol
4. **Token Refresh** - Handle token expiration dan refresh
5. **Error Handling** - Handle semua error case dari Play Store API
6. **Rate Limiting** - Jangan spam request, bisa kena ban
7. **Proxy/VPN** - Gunakan proxy untuk menghindari detection

### Library Alternatif
Jika mau lebih proper, gunakan library yang sudah ada:
- **gplaycli** (Python) - Command line tool untuk download dari Play Store
- **google-play-scraper** (Node.js) - Scraper untuk Play Store
- **play-store-api** (Java) - Unofficial Play Store API client

Tapi ingat, **semua melanggar ToS Google**.

---

## 🛡️ Legal & Ethical

### Kapan Boleh Digunakan?
- ✅ Testing internal aplikasi Anda sendiri
- ✅ Research & educational purposes
- ✅ Backup APK yang sudah Anda beli/install

### Kapan TIDAK Boleh?
- ❌ Distribusi APK bajakan
- ❌ Bypass paid apps
- ❌ Komersial tanpa izin
- ❌ Melanggar copyright developer

### Rekomendasi
Untuk testing versi lama APK, lebih baik:
1. **Simpan APK sendiri** - Backup setiap versi yang Anda install
2. **Gunakan APK archive legal** - APKMirror, APKPure (untuk free apps)
3. **Minta ke developer** - Jika untuk testing, minta versi lama langsung

---

## 📚 Referensi

- [Google Play Store ToS](https://play.google.com/about/developer-content-policy/)
- [gplaycli GitHub](https://github.com/matlink/gplaycli)
- [Protobuf Documentation](https://developers.google.com/protocol-buffers)

---

**Gunakan dengan bijak dan bertanggung jawab!**
