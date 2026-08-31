# MediTrack Pro

A personal health management Android application built with Kotlin. MediTrack Pro helps users track medications, manage health records, monitor refill schedules, and coordinate with caregivers — all from a single, privacy-focused mobile app.

---

## Features

**Medication Management**
- Add, view, and delete medications with dose, schedule, and refill tracking
- Today, All Medications, and Refill tabs for organized viewing
- Refill progress bars showing days remaining per medication

**Health Records**
- Log lab results, prescriptions, doctor visits, and vaccinations
- View record counts by category
- Long-press to delete individual records

**Home Dashboard**
- Live clock and personalized greeting
- Dose progress tracker
- Upcoming dose alert card

**Profile and Health Info**
- Store blood type, height, weight, DOB, gender, and medical conditions
- Auto-calculated BMI from height and weight inputs
- Dynamic condition badges on profile screen

**Caregiver Support**
- Add a caregiver with name, relation, and phone number
- Toggle missed-dose alerts, daily summaries, and emergency notifications

**Authentication**
- Firebase email and password sign in and sign up
- Password reset via email
- Optional biometric authentication on app launch

**Appearance and Customization**
- Dark and light theme toggle
- Five accent color options applied across the entire app
- Three text size levels via font scale

**Privacy and Security**
- Biometric lock toggle
- Clear all local data option
- Delete Firebase account with re-authentication

**Emergency Contacts**
- Store two emergency contacts with name, relation, and phone number

**Notifications**
- Toggle preferences for dose reminders, missed doses, refill alerts, caregiver alerts, and sound

---

## Tech Stack

- **Language**: Kotlin
- **Architecture**: MVVM with LiveData and ViewModel
- **Database**: Room (local SQLite)
- **Authentication**: Firebase Auth
- **UI**: Material Components, CardView, ConstraintLayout
- **Navigation**: Jetpack Navigation Component with Bottom Navigation
- **Biometric**: AndroidX Biometric library
- **Background Tasks**: WorkManager
- **Theming**: AppCompatDelegate DayNight, custom BaseActivity for font scaling

---

## Project Structure

```
com.example.meditrackpro
├── MainActivity.kt               — Navigation host, shared ViewModel
├── BaseActivity.kt               — Font scale and theme base class
├── AppThemeHelper.kt             — Accent color, theme, and font scale utilities
├── LoginActivity.kt              — Firebase sign in, biometric check
├── SignUpActivity.kt             — Firebase account creation
├── HomeFragment.kt               — Dashboard with doses and vitals
├── MedicationsFragment.kt        — Tabbed medication list
├── HealthFragment.kt             — Health records list and counters
├── ProfileFragment.kt            — User profile, health info, settings
├── AddMedicineActivity.kt        — Add new medication form
├── AddHealthRecordActivity.kt    — Add new health record form
├── EditHealthInfoActivity.kt     — Edit health info with BMI calculation
├── CaregiverSettingsActivity.kt  — Caregiver details and alert toggles
├── NotificationSettingsActivity  — Notification preference toggles
├── PrivacySettingsActivity.kt    — Biometric, data clear, account delete
├── EmergencyContactsActivity.kt  — Two emergency contact slots
├── AppAppearanceActivity.kt      — Theme, accent color, text size
├── Medicine.kt                   — Room entity for medications
├── MedicineDao.kt                — DAO for medication queries
├── MedicineDatabase.kt           — Room database definition
├── MedicineViewModel.kt          — ViewModel for medication data
├── HealthRecord.kt               — Room entity for health records
├── HealthRecordDao.kt            — DAO for health record queries
└── HealthViewModel.kt            — ViewModel for health record data
```

---

## Setup

1. Clone the repository
2. Open in Android Studio (Hedgehog or later recommended)
3. Connect your Firebase project and place your `google-services.json` in the `app/` directory
4. Enable Email and Password authentication in your Firebase console
5. Sync Gradle and run on a device or emulator running API 26 or higher

---

## Requirements

- Android API 26 (Android 8.0) minimum
- Firebase project with Email and Password auth enabled
- Google Services JSON file

---

## Permissions

- Internet access for Firebase authentication
- Post notifications for dose reminders
- Exact alarm scheduling for timed reminders
- Camera for future medicine scanner feature
- Body sensors and activity recognition for future vitals integration

---

## Roadmap

- Real-time dose reminder notifications via WorkManager scheduling
- Medicine barcode and label scanner using CameraX and ML Kit
- Medical report upload and PDF parsing
- Adherence percentage tracking with mark-as-taken functionality
- Full light theme styling across all screens

---

## Author

Ritarshi Roy
