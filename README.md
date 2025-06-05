# Lost and Found App

A mobile application to report, search, and manage lost and found items with geolocation, image uploads, and admin tools.

[![Watch the demo](demo/demo-thumbnail.png)](demo/demo.mp4)

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

4. **Run the Project:**

- Select a connected device or emulator.
- Click **Run > Run 'app'**.
---
