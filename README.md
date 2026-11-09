# Medical Adherence Android App

The Android Studio project is in [`MedicalAdherence_Android_Safety_Changes/`](MedicalAdherence_Android_Safety_Changes/). Open that directory in Android Studio, sync Gradle, and run the app on an emulator or device.

The project includes medication autocomplete using NLM RxNorm, DailyMed image lookup, FDA openFDA safety-label information, medication search, and a private Possible Overdose report stored in Firestore under each signed-in user.

## GitHub Actions

`.github/workflows/android.yml` runs automatically on pushes and pull requests targeting `main`. It installs Java 17 and Android SDK 35, runs the unit tests, builds the debug APK, and uploads the APK and test reports as workflow artifacts. It can also be started manually from the **Actions** tab with **Run workflow**.

The workflow expects `MedicalAdherence_Android_Safety_Changes/app/google-services.json` to be present, as it is in this repository. Review Firebase security rules before production use.
