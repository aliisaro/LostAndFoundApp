# Lost and Found App

A mobile application to report, search, and manage lost and found items with geolocation, image uploads, and admin tools.

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
- Kotlin + Jetpack Compose
- Firebase Authentication & Firestore
- Firebase Storage for image uploads
- Google Maps SDK
- CameraX for capturing images

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

### Project Setup

### Prerequisites
- Android Studio (latest version)

1. **Clone the repository:**

```bash
git clone https://github.com/aliisaro/LostAndFoundApp.git
cd LostAndFoundApp
```

2. **Open the project in Android Studio:**

- Go to **File > Open** and select the `LostAndFoundApp` directory.

3. **Sync Gradle:**

- Go to **File > Sync Project with Gradle Files**.

### Google Maps API Key Setup

This project uses Google Maps services, so you need to add your own API key as follows:

1. Create a new Google Maps API key in the [Google Cloud Console](https://console.cloud.google.com/apis/credentials).

2. Create a file at `app/src/main/res/values/google_maps_api.xml` and add the following:

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="google_maps_key">YOUR_API_KEY_HERE</string>
</resources>
```

4. **Run the Project:**

- Select a connected device or emulator.
- Click **Run > Run 'app'**.
---
