# Firebase shifts setup

This project intentionally does not contain `app/google-services.json`,
Firebase service-account keys, or any other credentials. Keep those files
local or in a secret-managed deployment environment.

## Shift document schema

Create documents in the top-level `turnos` collection. The document ID is the
stable `shiftId` used by signup documents.

```json
{
  "DiaDaSemana": 1,
  "Inicio": "08:00",
  "Status": "open"
}
```

`DiaDaSemana` is a number from 1 (Monday) to 7 (Sunday), and `Inicio` must be
`HH:mm`. The Android client queries only shifts with `Status` equal to `open`;
cancelled shifts remain stored but are not shown in the schedule.

Signup documents are created by the app at
`turnos/{shiftId}/signups/{authUid}`:

```json
{
  "userId": "the-auth-uid",
  "shiftId": "the-shift-document-id",
  "createdAt": "Firestore server timestamp"
}
```

Do not manually enter a client timestamp for `createdAt`; the app writes a
server timestamp and the rules require it to match the request time.

## Admin claim

1. In **Authentication > Users**, copy the target user's UID.
2. From a trusted environment using the Firebase Admin SDK, set the custom
   claim `{ "admin": true }` for that UID. Never run Admin SDK code in the
   Android app and never commit its service-account key.
3. Have the user sign out and sign back in (or refresh the ID token) before
   testing admin writes. The claim is checked from `request.auth.token.admin`.
4. Deploy the rules from the repository root:

   ```bash
   firebase login
   firebase use <your-firebase-project-id>
   firebase deploy --only firestore:rules
   ```

## Signup verification

With a normal email/password account, verify that:

1. An open recurring shift can be read and appears for its weekday.
2. The app can create exactly one signup and then delete that same signup
   before the shift starts.
3. A second create attempt is rejected and an account cannot read, create, or
   delete another UID's signup.
4. A cancelled shift is not returned by the schedule and cannot receive a new
   signup.

With the admin account, verify that a shift with the documented shape can be
created, updated, and deleted. Invalid or incomplete shift documents should be
rejected by the deployed rules.
