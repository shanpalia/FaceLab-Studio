# FaceLab Studio

**By PaliaAPK HUB · Developer by ShanPalia**

Android application: `com.shanpalia.facelab`

## Release status

- App UI and media pickers: implemented.
- Cloudflare R2 upload API: deployed, but AI video face-swap provider adapter is **not connected**.
- Do not advertise this build as a working face-swap release. It is a preview/beta build until an authorized AI provider is integrated and end-to-end tested.
- The app currently reports when processing is unavailable; it must not claim that a video was generated.

## Build a signed APK with Codemagic

1. Connect this GitHub repository to Codemagic.
2. Create an Android signing keystore in Codemagic and save it under the reference **facelab_keystore**.
3. Start workflow **android-release-apk**.
4. Download the generated APK from the successful build's Artifacts section.
5. Install and test it on a real Android device before publishing.

Required signing reference: `facelab_keystore`.

## PaliaAPK HUB release

The website reads the update manifest at:

`https://raw.githubusercontent.com/shanpalia/WebsitePaliaAPK_V.2/main/facelab-studio.json`

After a signed APK has been built, uploaded to a stable HTTPS download location, and tested, update that manifest's `apk_url` to the direct APK download URL. Until then it intentionally remains empty.

## Important before public release

- Integrate and test a compatible AI provider; the current Worker does not perform face swapping.
- Add a public privacy policy and retention/deletion behavior for uploaded media before enabling uploads for public users.
- Test install, update detection, download, processing, error handling, and uninstall on Android 8.0+.
- Only process faces and videos the user owns or has permission to edit.
