# MediTrack Pro

An Android app, written in Java, that helps people stay on top of their medication, keeps their health records in one place, and automatically keeps a family caregiver informed. Patient data stays on the device and sensitive settings are encrypted.

---

## Features

**Medication Management**
- Add, edit and delete medicines with a dose and one or more daily times, chosen with a time picker
- Today, All Medications and Refill tabs
- Refill progress bars showing days of supply left

**Dose Reminders**
- Exact-time notifications for every scheduled dose, rescheduled automatically each day
- Snooze reminders (10 minutes, 30 minutes or 1 hour)
- Reminders are re-armed after a device reboot

**Adherence Tracking**
- Home dashboard with a live clock, personalised greeting and daily dose list
- One tap marks a dose as taken (and toggles it back)
- Live "taken / total" progress bar and adherence percentage

**Health Records**
- Log lab results, prescriptions, doctor visits and vaccinations
- Record counts by category, a detail screen where status and notes can be edited, and long-press to delete
- Scan a prescription or report with the camera: ML Kit reads the text on the device and pre-fills the new record
- Captured images are deleted automatically after 30 days

**Caregiver Alerts (SMS)**
- Save a caregiver's name, relation and phone number
- Missed-dose alert 30 minutes after a dose if it has not been marked as taken
- Daily summary text at 9 PM, plus an emergency alert if no doses were taken that day
- "Send test SMS" button to confirm it works

**Location Sharing**
- Optional: when turned on, caregiver SMS alerts include a Google Maps link to the patient's location and how recent it is
- "My location" screen shows the patient on a Google Map (dark map style in dark mode) with a button to send the location to the caregiver right now
- The caregiver does not need the app; the link opens in any maps app or browser
- Alerts sent while the app is closed use the last known location; granting "Allow all the time" keeps it fresh

**Profile and Health Info**
- Blood type, height, weight, date of birth, gender and medical conditions
- BMI calculated automatically, with validation on height and weight
- Condition badges on the profile screen, and an editable display name

**Authentication and Privacy**
- Firebase email and password sign in, sign up and password reset
- Optional biometric lock on app launch
- Preferences are stored in AES-256 `EncryptedSharedPreferences`
- Clear local data, and delete the Firebase account with re-authentication

**Appearance**
- A plain, professional interface with one accent colour, in dark (default) or light mode, or follow the system setting
- Three text sizes through a font scale
- Every colour comes from one palette (`values/colors.xml` and `values-night/colors.xml`), so text and buttons stay readable in both themes

**Emergency Contacts and Notification Settings**
- Two emergency contacts with name, relation and phone number
- Toggles for dose reminders, missed doses, refills, caregiver alerts and sound

---

## Tech Stack

- **Language**: Java 11 (with core library desugaring for `java.time`)
- **Architecture**: MVVM with a repository layer, `ViewModel` and `LiveData`
- **Database**: Room (SQLite), schema version 5 with explicit migrations
- **Background work**: `AlarmManager` exact alarms with broadcast receivers; DAO calls run on a shared `ExecutorService`
- **Authentication**: Firebase Auth
- **Camera and OCR**: CameraX and ML Kit on-device text recognition
- **Location**: Fused Location Provider and the Google Maps SDK (Play services)
- **Security**: AndroidX Security Crypto (`EncryptedSharedPreferences`) and AndroidX Biometric
- **UI**: Material Components, CardView, ConstraintLayout, RecyclerView with `ListAdapter`, Navigation Component with bottom navigation
- **Logging**: Timber

---

## Project Structure

```
com.example.meditrackpro
├── MediTrackApp                  Application class: notification channels, daily summary alarm, Timber
├── MainActivity                  Navigation host with bottom nav, shared MedicineViewModel, onboarding dialog
├── BaseActivity                  Applies the saved font scale to every screen
│
├── Authentication
│   ├── LoginActivity             Sign in, password reset, biometric prompt
│   └── SignUpActivity            Account creation
│
├── Screens
│   ├── HomeFragment              Dashboard: clock, dose list, adherence, snooze
│   ├── MedicationsFragment       Today / All / Refill tabs
│   ├── HealthFragment            Health records list and category counts
│   ├── ProfileFragment           Profile, health info, caregiver card, settings links
│   ├── AddMedicineActivity       Add or edit a medicine
│   ├── AddHealthRecordActivity   Add a record, with optional scanned photo and OCR text
│   ├── HealthRecordDetailActivity  View and edit a record
│   ├── CameraActivity            CameraX capture, preview and OCR
│   ├── EditHealthInfoActivity    Blood type, height, weight, DOB, BMI
│   ├── CaregiverSettingsActivity Caregiver details, alert toggles, test SMS
│   ├── LocationActivity          Map of the patient's location, send to caregiver
│   ├── EmergencyContactsActivity
│   ├── NotificationSettingsActivity
│   ├── PrivacySettingsActivity   Biometric lock, clear data, delete account
│   └── AppAppearanceActivity     Theme and text size
│
├── Data
│   ├── Medicine, HealthRecord, DoseLog          Room entities
│   ├── MedicineDao, HealthRecordDao, DoseLogDao
│   ├── MedicineDatabase                          Room database and migrations
│   ├── MedicineRepository, HealthRepository
│   └── MedicineViewModel, HealthViewModel
│
├── Alarms and Alerts
│   ├── AlarmScheduler            Schedules, cancels and reschedules dose alarms and snoozes
│   ├── MedicationAlarmReceiver   Shows the dose notification
│   ├── BootReceiver              Re-arms alarms after reboot
│   ├── CaregiverAlarmScheduler   Missed-dose check and daily summary alarms
│   ├── MissedDoseCheckReceiver   Sends the missed-dose SMS
│   ├── DailySummaryReceiver      Sends the daily summary and emergency SMS
│   ├── CaregiverSmsSender        SMS sending and phone number normalisation
│   └── NotificationHelper        Notification channels
│
└── Helpers
    ├── LocationHelper            Finds, caches and formats the patient's location for alerts
    ├── UiHelper                  Selected / unselected styling for chips and cards
    ├── OcrHelper                 ML Kit text extraction
    ├── SecurePrefs               Opens an encrypted preferences file
    ├── PrefsConstants            Preference file names
    ├── AppThemeHelper            Theme mode and font scale
    └── MedicineAdapter, RefillAdapter, HealthRecordAdapter
```

---

## Setup

1. Clone the repository.
2. Open it in Android Studio (a recent stable release; the project uses AGP 8.11).
3. Add a Google Maps API key to `local.properties`, which is git-ignored (the free Maps Demo Key from mapsplatform.google.com/maps-demo-key works, no billing account needed):
   ```
   MAPS_API_KEY=your_key_here
   ```
   Without a key the app still builds and runs; the location screen shows a notice instead of the map, and alerts still carry the location link.
4. Place your own `google-services.json` in the `app/` directory (the one in the repo belongs to the original Firebase project).
5. Enable Email/Password sign-in in the Firebase console.
6. Sync Gradle and run on a device or emulator running Android 7.0 (API 24) or higher.

SMS alerts need a device with a SIM. On an emulator, the caregiver screen still saves, but texts will not be delivered.

---

## Requirements

- Android 7.0 (API 24) minimum, built against API 36
- A Firebase project with Email/Password authentication enabled

---

## Permissions

| Permission | Used for |
|---|---|
| `INTERNET` | Firebase authentication |
| `POST_NOTIFICATIONS` | Dose reminders |
| `SCHEDULE_EXACT_ALARM`, `USE_EXACT_ALARM` | Reminders at the exact scheduled time |
| `RECEIVE_BOOT_COMPLETED` | Re-arming reminders after a reboot |
| `CAMERA` | Scanning prescriptions and reports |
| `USE_BIOMETRIC` | Optional biometric app lock |
| `SEND_SMS` | Caregiver alerts |
| `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION` | Showing and sharing the patient's location |
| `ACCESS_BACKGROUND_LOCATION` | Optional: a fresh location in alerts sent while the app is closed |
| `BODY_SENSORS`, `ACTIVITY_RECOGNITION` | Declared for future vitals integration, not used yet |

---

## Roadmap

- Live location for caregivers in their own app (today they receive a map link by SMS)
- Cloud backup and cross-device sync (the Cloud backup toggle is a placeholder)
- Wearable integration for vitals
- Multi-language support
- Drug-interaction and allergy warnings

---

## Authors

- Shubhankar Mohan Sharma
- Shreyashi Srivastava
- Khushi Shetty
