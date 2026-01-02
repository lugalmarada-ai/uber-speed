# Uber Speed Barinas - Android App

Native Android Application for Uber Speed Barinas, built with Kotlin and MVVM architecture.

## 📱 Features

- **Authentication**: Login and Registration with email/password.
- **Home Dashboard**: Interactive map with Google Maps integration.
- **Service Booking**: Request Taxi or Delivery services.
- **Trip History**: View past trips and details.
- **User Profile**: View user information.

## 🛠 Tech Stack

- **Language**: Kotlin
- **Architecture**: MVVM (Model-View-ViewModel)
- **Networking**: Retrofit + Gson
- **Async**: Coroutines & Flow
- **Navigation**: Navigation Component (Single Activity)
- **UI**: Material Design, XML Layouts
- **Maps**: Google Maps SDK

## 🚀 Getting Started

### Prerequisites

- Android Studio Iguana or later
- JDK 17
- Android SDK 34

### Configuration

1.  **Clone the repository**:
    ```bash
    git clone https://github.com/your-username/uber-speed-android.git
    cd uber-speed-android
    ```

2.  **Environment Variables**:
    Create a `local.properties` file in the root directory (if not exists) and add your API keys:
    ```properties
    sdk.dir=/path/to/android/sdk
    MAPS_API_KEY=YOUR_GOOGLE_MAPS_API_KEY
    ```

    > **Note**: The backend URL is configured in `com.uberspeed.client.utils.Constants`. Update it to point to your backend server.

3.  **Build & Run**:
    - Open the project in Android Studio.
    - Sync Gradle Project.
    - Select an emulator or connected device.
    - Click **Run 'app'**.

## 📁 Project Structure

```
uber-speed-android/
├── app/
│   ├── src/main/java/com/uberspeed/client/
│   │   ├── data/           # Repositories, Models, API
│   │   ├── ui/             # Activities, Fragments, ViewModels
│   │   ├── utils/          # Constants, Helper classes
│   │   └── UberSpeedApp.kt # Application class
│   └── src/main/res/       # Layouts, Values, Navigation
└── build.gradle            # Project build config
```

## ⚠️ Notes

- The app uses a mock backend URL (`https://api.uber-speed.xyz/v1/`). Ensure your backend is running and accessible.
- Google Maps requires a valid API Key with **Maps SDK for Android** enabled.
