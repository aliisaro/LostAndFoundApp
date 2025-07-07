# Lost and Found App

A mobile application to report, search, and manage lost and found items with maps, geolocation, image uploads, and admin tools.

[Watch the Lost and Found App demo on YouTube](https://www.youtube.com/watch?v=7ez7mB4x1yM)

## Features
- User authentication via Firebase
- Report lost items with image and location
- Edit/delete own reports
- Search and filter lost items
- Mark items as found
- View lost items on a map
- Copy/paste items' GeoCode locations to map
- Multilingual support (English & Finnish)
- Admin panel for managing reports
- Expiry system for old lost items

## Tech Stack
-Language: Kotlin
-Frontend (UI): Jetpack Compose
-Backend: Firebase Authentication & Firestore
-Storage: Firebase Storage (image uploads)
-Maps & Location: Google Maps SDK & Geolocation API
-Camera: CameraX

## Screens
- Login / Register
- Home Screen
- Report Lost Item
- View Items on Map
- Search item reports
- Edit Report
- Profile
- Statistics
- Admin Panel

---

## Project Setup

### Prerequisites
- Android Studio
- A Firebase project
- A Google Cloud project with Maps API enabled

---

1. **Clone the repository:**

```bash
git clone https://github.com/aliisaro/LostAndFoundApp.git
cd LostAndFoundApp
```


2. **Open the project in Android Studio:**

- Go to **File > Open** and select the `LostAndFoundApp` directory.


3. **Sync Gradle:**

- Go to **File > Sync Project with Gradle Files**.


4. **Google Maps API Key Setup**

- Go to the [Google Cloud Console](https://console.cloud.google.com/apis/credentials).
- Create a new API key
- Create a file at:
    app/src/main/res/values/google_api_key.xml
- Add the following content:

```xml
<resources>
    <string name="google_api_key">YOUR_API_KEY_HERE</string>
</resources>
```


5. **Firebase Setup**

- Go to [Firebase Console](https://console.firebase.google.com/)
- Create a new project
- Add an Android app and download the `google-services.json` file
- Place it in the project at `app/google-services.json`
-  Enable the following Firebase services:
   - Authentication (Email/Password)
   - Firestore Database
   - Storage (for images)

     
6. **Run the Project:**

- Select a connected device or emulator.
- Click **Run > Run 'app'**.
---
