# Medical Adherence website: Netlify deployment

The source website is in `MedicalAdherence_Integrated/Website`. It is PHP-rendered but uses Firebase Authentication and Cloud Firestore in the browser. Netlify does not run ordinary PHP server pages, so this repository includes a generated static export in `MedicalAdherence_Integrated/Website/netlify`.

## Connect the repository

1. Push this repository to GitHub.
2. In Netlify, choose **Add new site → Import an existing project**.
3. Select the GitHub repository and use the repository root as the base directory.
4. Leave the build command empty.
5. Netlify will read `netlify.toml` and publish `MedicalAdherence_Integrated/Website/netlify`.
6. Deploy the site.

The committed `netlify` directory means the first deployment works without a PHP build environment. If the PHP templates change, regenerate the export locally with:

```bash
bash scripts/build-netlify-static.sh
```

Commit the regenerated `MedicalAdherence_Integrated/Website/netlify` directory together with the source changes.

## Firebase setup after deployment

In Firebase Console for project `medical-adherence-android-app`:

- Add the Netlify hostname under **Authentication → Settings → Authorized domains**.
- Confirm Email/Password and Google sign-in are enabled.
- Deploy and review the repository's Firestore rules before testing.
- Test with two separate accounts to confirm that each account can only access its own data.

The Firebase browser configuration in `assets/js/firebase-sync.js` is a public web-app configuration, not an Admin SDK secret. Do not add service-account credentials to the repository or Netlify environment.

## Important scope

Netlify serves the static HTML export and browser Firebase integration. It does not execute the original `.php` files or provide PHP/MySQL hosting. The original PHP templates remain in the repository as the source used to generate the export.
