# PrimeiraApp Firebase setup

The Android app uses Firebase Authentication with email/password and Cloud
Firestore for user profiles, shifts, and shift signups. Do not commit
`app/google-services.json`; it is intentionally ignored because it contains
project-specific configuration.

## Initial Firebase Console setup

1. Create or select a Firebase project.
2. Add an Android app with package name `com.example.primeiraapp`. Download
   `google-services.json` into `app/google-services.json` locally.
3. In **Authentication > Sign-in method**, enable **Email/Password**.
4. In **Firestore Database**, create the database in the required region.
   Do not leave production data on the permissive test-mode rules.
5. Create the initial shift documents under `shifts`. Each shift should include
   `date`, `startTime`, `endTime`, `title`, `description` (optional), and
   `status`; use a status other than `cancelled` for shifts visible to users.
6. Deploy the rules from the repository root:

   ```bash
   firebase login
   firebase use <your-firebase-project-id>
   firebase deploy --only firestore:rules
   ```

   The Firebase CLI will use `firebase.json` and `firestore.rules`.

## Admin shift management

Shift writes are restricted to authenticated users with the Firebase Auth
custom claim `admin: true`. Set that claim from a trusted server or the
Firebase Admin SDK only; never put service-account credentials in this
repository or the Android app. After setting the claim, sign out and back in
to refresh the user's ID token.

The app reads only non-cancelled shifts and users can read, create, and delete
only their own document at
`shifts/{shiftId}/signups/{their-auth-uid}`. Signup documents must contain
matching `userId` and `shiftId` values and a server timestamp `createdAt`.
