# Tugas Besar Android IF3210 Pengembangan Aplikasi Berbasis Platform

## Table of Contents

1. [About Bondoman](#about-bondoman)
2. [Libraries](#libraries)
3. [Screenshots](#screenshots)
4. [OWASP](#owasp)
5. [Accessibility Testing](#accessibility-testing)
6. [Credits](#credits)

## About Bondoman

Bondoman is a user-friendly money management app designed specifically for Android devices. Bondoman helps you take control of your finances effortlessly by providing essential tools to manage your transactions efficiently.

Bondoman is compatible with Android devices running on API 29 (Android 10) up to API 34 (Android 14)

<b>Bondoman Features:</b>

1. <b>Login</b>: Securely log in to your Bondoman account to access all features.
2. <b>Home Dashboard</b>: Get a comprehensive overview of your finances at a glance, including your budget summaries and recent transactions.
3. <b>Transaction List</b>: View list of your transactions, categorized by date.
4. <b>Add and Edit Transactions</b>: Easily add new transactions and edit existing ones.
5. <b>Scan Receipts</b>: Capture receipts directly from your device's camera or select from gallery to add new transaction.
6. <b>Save Transaction List to Excel</b>: Export your transaction data to Excel format for further analysis or record-keeping.
7. <b>Send Transaction List File to Email</b>: Share your transaction files via email with ease, allowing for seamless collaboration or backup.
8. <b>Twibbon Integration</b>: Add a personal touch to your transactions by overlaying Twibbon frames, making them more visually appealing and shareable on social media.
9. <b>Randomize Transaction</b>: Adding new transaction by randomization.

## Libraries

1. AndroidX Core KTX: `androidx.core:core-ktx:1.7.0`
2. AndroidX AppCompat: `androidx.appcompat:appcompat:1.3.0`
3. Material Components for Android: `com.google.android.material:material:1.3.0`
4. AndroidX ConstraintLayout: `androidx.constraintlayout:constraintlayout:2.1.4`
5. AndroidX Room (Runtime, Common, KTX):
   - `androidx.room:room-runtime:2.5.0`
   - `androidx.room:room-common:2.5.0`
   - `androidx.room:room-ktx:2.5.0`
6. CameraView: `androidx.camera:camera-view:1.3.2`
7. Google AR Sceneform (Filament): `com.google.ar.sceneform:filament-android:1.17.1`
8. AndroidX Lifecycle (LiveData, ViewModel):
   - `androidx.lifecycle:lifecycle-livedata-ktx:2.4.0`
   - `androidx.lifecycle:lifecycle-viewmodel-ktx:2.4.0`
9. Retrofit: `com.squareup.retrofit2:retrofit:2.9.0`
10. Retrofit Gson Converter: `com.squareup.retrofit2:converter-gson:2.9.0`
11. OkHttp Logging Interceptor: `com.squareup.okhttp3:logging-interceptor:4.9.3`
12. MPAndroidChart: `com.github.PhilJay:MPAndroidChart:v3.1.0`
13. AndroidX Preference: `androidx.preference:preference-ktx:1.2.0`
14. AndroidX DataStore: `androidx.datastore:datastore-core:1.0.0`
15. Google Places SDK: `com.google.android.libraries.places:places:3.3.0`
16. Apache POI (Java Excel API):
    - `org.apache.poi:poi:5.2.2`
    - `org.apache.poi:poi-ooxml:5.2.2`
17. JUnit: `junit:junit:4.13.2`
18. AndroidX Test (JUnit): `androidx.test.ext:junit:1.1.3`
19. Espresso Core: `androidx.test.espresso:espresso-core:3.4.0`
20. CameraX (Camera Camera2, Camera Lifecycle):
    - `androidx.camera:camera-camera2:1.4.0-alpha04`
    - `androidx.camera:camera-lifecycle:1.4.0-alpha04`

## Screenshots

<div style="display: flex; justify-content: center; flex-wrap: wrap;gap: 2rem;">
    <img src="screenshot/1_login.png" style="max-width: 300px;">
    <img src="screenshot/2_login_input.png" style="max-width: 300px;">
    <img src="screenshot/3_home.png" style="max-width: 300px;">
    <img src="screenshot/4_transaction_list.png" style="max-width: 300px;">
    <img src="screenshot/5_detail_transaction.png" style="max-width: 300px;">
    <img src="screenshot/6_add_transaction.jpg" style="max-width: 300px;">
    <img src="screenshot/7_add_transaction_location.png" style="max-width: 300px;">
    <img src="screenshot/8_edit_transaction.jpg" style="max-width: 300px;">
    <img src="screenshot/9_scan_options.png" style="max-width: 300px;">
    <img src="screenshot/10_scan_camera.png" style="max-width: 300px;">
    <img src="screenshot/12_scan_select_picture.png" style="max-width: 300px;">
    <img src="screenshot/13_scan_result.png" style="max-width: 300px;">
    <img src="screenshot/14_chart.png" style="max-width: 300px;">
    <img src="screenshot/15_settings.png" style="max-width: 300px;">
    <img src="screenshot/16_save_transaction_list.png" style="max-width: 300px;">
    <img src="screenshot/18_send_transaction_list.png" style="max-width: 300px;">
    <img src="screenshot/19_send_transaction_list_email.png" style="max-width: 300px;">
    <img src="screenshot/20_randomize_transaction.png" style="max-width: 300px;">
    <img src="screenshot/21_twibbon.png" style="max-width: 300px;">
    <img src="screenshot/22_twibbon_save.png" style="max-width: 300px;">
</div>

## OWASP

### M4: Validasi Input/Output yang Tidak Cukup

#### Validasi Input Form Login

- Validasi input diimplementasikan pada form login untuk memastikan hanya input yang valid yang diterima.
- Aturan validasi meliputi pemeriksaan untuk kolom yang kosong, format email yang benar, dan format password yang benar untuk menghindari injection.

```
    // Username validation check
    private fun isUserNameValid(username: String): Boolean {
        val maxLength = 50
        val expectedEmailPattern = "^\\w+@std\\.stei\\.itb\\.ac\\.id$".toRegex()
        return expectedEmailPattern.matches(username) && username.length <= maxLength
    }

    // Password validation check
    private fun isPasswordValid(password: String): Boolean {
        val minLength = 6
        val maxLength = 40
        return password.length in minLength..maxLength
    }
```

#### Validasi Input pada Tambah dan Edit Transaksi

- Validasi input diberlakukan baik untuk menambahkan maupun mengedit transaksi.
- Kolom seperti judul and nominal divalidasi untuk mencegah injection.
- Pesan validasi yang tepat ditampilkan kepada pengguna untuk memandu mereka dalam memberikan input yang benar.

```
    private fun validateTitle(title: String): Boolean {
        val maxLength = 50
        return title.isNotEmpty() && title.length <= maxLength && !containsSpecialCharacters(title)
    }

    private fun validateAmount(amountText: String): Boolean {
        val amount = amountText.toIntOrNull() ?: 0
        val minAmount = 1
        val maxAmount = 100000000000
        return amount in minAmount..maxAmount
    }
```

### M8: Konfigurasi Keamanan yang Tidak Tepat

#### Konfigurasi Kunci API Google Maps

- Aplikasi mengelola kunci API secara aman, termasuk kunci API Google Maps.
- Kunci API disimpan dengan aman dalam file `apikey.properties` dan diakses melalui kelas `BuildConfig`.
- Pendekatan ini memastikan bahwa informasi sensitif seperti kunci API tidak terpapar langsung dalam kode sumber.

```
    // apikey.properties
    API_KEY=AIzaSyBuyaXa4Ism_7xVMqZNLeTCmChMEsX8gKY
    ENCRYPTION_KEY=V6B9D+GQITf/WKjSxMcSjQ==
```

```
    // Load API key from apikey.properties file
    val apikeyPropsFile = rootProject.file("apikey.properties")
    if (apikeyPropsFile.exists()) {
        val apikeyProps = Properties()
        apikeyProps.load(apikeyPropsFile.inputStream())
        // Access API key
        buildConfigField("String", "API_KEY", "\"${apikeyProps.getProperty("API_KEY")}\"")
        buildConfigField("String", "ENCRYPTION_KEY", "\"${apikeyProps.getProperty("ENCRYPTION_KEY")}\"")
    } else {
        // Set API key to empty string if apikey.properties doesn't exist
        buildConfigField("String", "API_KEY", "\"\"")
        buildConfigField("String", "ENCRYPTION_KEY", "\"\"")
    }
```

```
    // Initialize Places SDK
    Places.initialize(applicationContext, BuildConfig.API_KEY)
```

### M9: Penyimpanan Data yang Tidak Aman

#### Enkripsi Token JWT

- Token JWT, yang digunakan untuk tujuan otentikasi, disimpan dengan aman dengan mengenkripsinya.
- Enkripsi memastikan bahwa bahkan jika token diakses oleh pihak yang tidak berwenang, mereka tetap tidak terbaca dan tidak dapat digunakan.

```
    // Login
    val result = loginRepository.login(username, password)

    if (result is Result.Success) {
        val token = result.data
        val encryptedToken = encryptToken(token)

        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putString("username", username)
        editor.putString("token", encryptedToken)
        editor.apply()
        _loginResult.value = LoginResult(success = LoggedInUserView(displayName = username))
    } else if (result is Result.Error) {
        _loginResult.value = LoginResult(error = R.string.login_failed)
    }
```

```
    // Encryption
    private fun encryptToken(token: String): String? {
        try {
            val keySpec = SecretKeySpec(ENCRYPTION_KEY.toByteArray(), "AES")
            val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
            val initVectorBytes = Base64.decode(sharedPreferences.getString("init_vector", ""), Base64.DEFAULT)
            val ivParameterSpec = IvParameterSpec(initVectorBytes)
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivParameterSpec)
            val encryptedBytes = cipher.doFinal(token.toByteArray())
            return Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
```

```
    // Decrypt token using AES in CBC mode
    private fun decryptToken(encryptedToken: String): String? {
        try {
            val keySpec = SecretKeySpec(ENCRYPTION_KEY.toByteArray(), "AES")
            val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
            val initVectorBytes = Base64.decode(sharedPreferences.getString("init_vector", ""), Base64.DEFAULT)
            val ivParameterSpec = IvParameterSpec(initVectorBytes)
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivParameterSpec)
            val decryptedBytes = cipher.doFinal(Base64.decode(encryptedToken, Base64.DEFAULT))
            return String(decryptedBytes)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
```

## Accessibility Testing

Notes: Suggestions have been fixed and can be seen on [screenshot section](#screenshots)<br>
Other suggestions contain suggestions that can not be fixed, such as suggestions for Google plugins and navigation bar<br>

<div style="display: flex; justify-content: center; flex-wrap: wrap;gap: 2rem;">
    <img src="screenshot/accessibility_testing/2_home_see_all_button.png" style="max-width: 300px;">
    <img src="screenshot/accessibility_testing/8_transaction_edit_button.png" style="max-width: 300px;">
    <img src="screenshot/accessibility_testing/9_transaction_text_field.png" style="max-width: 300px;">
    <img src="screenshot/accessibility_testing/12_transaction_save_button.png" style="max-width: 300px;">
    <img src="screenshot/accessibility_testing/13_chart_container.png" style="max-width: 300px;">
    <img src="screenshot/accessibility_testing/14_chart_insight_text.png" style="max-width: 300px;">
    <img src="screenshot/accessibility_testing/16_twibbon_retake_button.png" style="max-width: 300px;">
</div>

## Credits

| NIM      | Full Name                 | Responsibilities                                                                                                                | Working Hours                                                                          |
| -------- | ------------------------- | ------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------- |
| 13521059 | Arleen Chrysantha Gunardi | Login <br> Save and Send Transaction list <br> Transaction <br> Accessibility Testing <br> Intro <br> Settings                  | 8 hours <br> 8 hours <br> 4 hours <br> 4 hours <br> 4 hours <br> 4 hours               |
| 13521124 | Michael Jonathan Halim    | Header dan Navbar <br> Logout <br> Transactions <br> Background Service <br> Network Sensing <br> Broadcast Receiver <br> OWASP | 4 hours <br> 4 hours <br> 12 hours <br> 4 hours <br> 4 hours <br> 4 hours <br> 8 hours |
| 13521142 | Enrique Alifio Ditya      | Transactions <br> Scan <br> Graf <br> Twibbon <br> Home <br> Save and Send Transaction list                                     | 4 hours <br> 8 hours <br> 8 hours <br> 4 hours <br> 4 hours <br> 4 hours               |

# Total Working Hours

| NIM      | Full Name                 | Total Working Hours |
| -------- | ------------------------- | ------------------- |
| 13521059 | Arleen Chrysantha Gunardi | 32 hours            |
| 13521124 | Michael Jonathan Halim    | 40 hours            |
| 13521142 | Enrique Alifio Ditya      | 32 hours            |
