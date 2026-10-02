# 💜 Purple Rooms — Verified Room & PG Booking Android App

**Purple Rooms** is a modern, production-grade Android application built with **Jetpack Compose**, **Kotlin Coroutines & Flow**, **Room Database**, and **Material Design 3**. It connects students, working professionals, and property owners directly with zero brokerage.

---

## 🚀 Key Features

### 📍 Production Location Management System
- **Location Selector on Home Screen:** 📍 `[Selected Location] ▼` (e.g., *Vadodara, Gujarat*, *Alkapuri, Vadodara*).
- **Choose Your Location Screen:**
  - 📍 **Use My Current Location:** One-tap GPS auto-detection with runtime permissions & reverse geocoding.
  - 🔎 **Real-time Autocomplete Search:** Instant geocoding & Places search across all cities, localities, areas, and pincodes (e.g., *Vadodara*, *Bettiah*, *Patna*, *Ahmedabad*, *Mumbai*, *Delhi*).
  - 🕒 **Recent Locations:** Persisted in local Room database with instant click-to-select and clear history.
  - 🏙️ **Popular Cities:** Quick selection grid for major student and job hubs.
  - 🛡️ **Robust Error & Permission Handling:** Graceful fallback for GPS disabled, permission denied, or offline modes.
- **Interactive Map Screen:**
  - Interactive panning, zooming, and area exploration.
  - Custom Purple Rooms branded property price markers (e.g. `🏠 ₹6,500`).
  - **"Search this area"** floating button with dynamic bounding-box recalculation.
  - Property preview card on marker tap with instant **"View Property"** navigation.
- **Proximity Distance Engine:** Accurate Haversine distance calculations in kilometers (`0.8 km away`) displayed on property cards and search results.

---

### 🔐 Authentication & Role Security
- **Role-Based Routing:** Seamless switching and secure separation between **Student** and **Owner** experiences.
- **Mobile OTP & Email Auth:** Secure login, 6-digit OTP verification, profile completion, and password reset flows.
- **Data Persistence:** Session and user data securely managed with Room Database.

---

### 🏠 Property Discovery & Booking
- **Property Types:** Single Rooms, PGs, Hostels, Apartments, and Luxury Villas.
- **Zero Brokerage Booking Flow:** Room option selection, move-in date picker, duration selector, and payment options (Online & Pay at Property / COD).
- **Direct Owner Chat:** Real-time messaging with owners and instant responses.
- **Owner Dashboard & Wizard:** Step-by-step listing creation, analytics, and inquiry management.

---

## 🛠️ Tech Stack & Architecture

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose (100% Declarative UI)
- **Design System:** Material Design 3 (M3)
- **Architecture:** MVVM (Model-View-ViewModel) + Clean Architecture
- **Local Persistence:** Room Database + KSP
- **Async & Reactive:** Kotlin Coroutines & StateFlow
- **Testing:** JUnit & Robolectric for JVM unit/CUJ testing

---

## 📦 How to Upload to GitHub

You can export or push this project to GitHub in two easy ways:

### Option 1: AI Studio GitHub Integration (Easiest)
1. Open the project in **Google AI Studio Build**.
2. Click the **Settings / Export** menu in the top-right header.
3. Select **Push to GitHub** or **Create GitHub Repository**.
4. Authorize your GitHub account and select your repository name.

### Option 2: Export as ZIP and Push via Git CLI
1. Download the project ZIP from the AI Studio menu.
2. Unzip the project folder on your computer.
3. Open a terminal in the project directory and run:
   ```bash
   git init
   git add .
   git commit -m "Initial commit: Purple Rooms with Production Location System"
   git branch -M main
   git remote add origin https://github.com/<your-username>/purple-rooms.git
   git push -u origin main
   ```

---

## 📱 Building & Running in Android Studio

1. Open **Android Studio** (Hedgehog or newer recommended).
2. Select **Open** and choose this project directory.
3. Allow Gradle to sync dependencies.
4. Select an emulator or physical Android device (API 26+) and click **Run (Shift + F10)**.
