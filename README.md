# Sismon App (Sistem Monitoring Siswa)

Aplikasi Android untuk Sistem Monitoring Siswa (Sismon) yang digunakan oleh pihak sekolah (Guru, Guru Piket, Kesiswaan, BK, Satpam, Superadmin) dan Orang Tua untuk memantau kehadiran, perizinan, dan keterlambatan siswa secara real-time.

## Teknologi
- **Platform**: Android (Native Java)
- **Minimum SDK**: Android 8.1 / Oreo (API 27)
- **Target SDK**: Android 14 (API 34)
- **Networking**: Retrofit 2 & OkHttp 3
- **Layanan Cloud**: Firebase Cloud Messaging (FCM)

---

## Panduan Instalasi & Menjalankan Projek

### Prasyarat
- Android Studio (versi Ladybug / Hedgehog atau yang lebih baru)
- JDK 17 (Java Development Kit)
- Android SDK (API 34)
- Perangkat Android fisik atau Emulator Android (min. Android 8.1 / API 27)
- Server backend `server-absensi-digital` yang sudah berjalan

### Langkah Instalasi & Menjalankan
1. **Buka Projek di Android Studio**
   - Buka Android Studio.
   - Pilih menu **File > Open**, lalu arahkan ke folder `sismon-app`.
   - Tunggu hingga proses Gradle Sync selesai.

2. **Konfigurasi Alamat API Backend**
   Buka file `app/src/main/java/com/example/orion/utils/api/ApiClient.java`.
   Sesuaikan `base_url` dengan IP lokal komputer yang menjalankan server backend:
   ```java
   public static String base_url = "http://<IPv4>:8000/";
   ```
   *(Contoh: `http://192.168.1.10:8000/`)*

3. **Konfigurasi Firebase (google-services.json)**
   Pastikan file `google-services.json` sudah berada di dalam folder `app/`.

4. **Jalankan Aplikasi**
   - Hubungkan perangkat Android fisik (dengan USB Debugging aktif) atau jalankan Emulator.
   - Klik tombol **Run** (ikon segitiga hijau) atau tekan `Shift + F10` di Android Studio.
   - Alternatif build melalui terminal:
     ```bash
     ./gradlew assembleDebug
     ```
     *(Pada Windows: `.\gradlew.bat assembleDebug`)*