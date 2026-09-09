# Release Signing Architecture & Key Custody

This document defines the production signing configuration, environment variable requirements, and Play App Signing workflows for NEXIQ (`REL-002`).

---

## 1. Security Architecture Principles

1. **Zero Secret Footprint in Git**: Upload keystores (`*.jks`, `*.keystore`), store passwords, key aliases, and key passwords must never be committed to Git or pushed to any remote repository.
2. **Environment-Driven Configuration**: Gradle parses signing credentials exclusively from environment variables or a secure, unversioned local file outside the repository.
3. **Graceful Build Fallback**: In the absence of production signing credentials, release builds fall back to debug signing or skip signing, ensuring that developer builds and CI test pipelines do not fail.

---

## 2. Play App Signing Architecture

NEXIQ uses **Google Play App Signing**:
- **App Signing Key**: Generated and securely held in Google Cloud KMS by Google Play. Used by Google to sign the final optimized APKs distributed to end users.
- **Upload Key**: Generated and held by the release engineer / repository maintainer. Used exclusively to sign the Android App Bundle (`.aab`) before uploading it to the Google Play Developer Console.

---

## 3. Generating the Upload Key

To generate a new, secure upload keystore locally:

```bash
keytool -genkeypair \
  -v \
  -keystore nexiq-upload-keystore.jks \
  -alias nexiq-upload \
  -keyalg RSA \
  -keysize 4096 \
  -validity 10000 \
  -storetype PKCS12
```

> [!CAUTION]
> Store `nexiq-upload-keystore.jks` in a secure, encrypted password manager or offline HSM. **Do not** place it in the project working tree.

---

## 4. Environment Variables Specification

The Gradle build script (`app/build.gradle.kts`) reads the following environment variables:

| Environment Variable | Description | Example |
| :--- | :--- | :--- |
| `KEYSTORE_PATH` | Absolute filesystem path to the upload keystore file | `/var/secrets/nexiq-upload.jks` |
| `KEYSTORE_PASSWORD` | Password protecting the keystore file | *(Secret)* |
| `KEY_ALIAS` | Alias name of the upload key inside the keystore | `nexiq-upload` |
| `KEY_PASSWORD` | Password protecting the specific key alias | *(Secret)* |

### Local Release Build (Dry-Run)
To run a signed release build locally:

```bash
# In Bash:
export KEYSTORE_PATH="/path/outside/repo/nexiq-upload.jks"
export KEYSTORE_PASSWORD="your-keystore-password"
export KEY_ALIAS="nexiq-upload"
export KEY_PASSWORD="your-key-password"

./gradlew bundleRelease
```

---

## 5. CI / CD Configuration (GitHub Actions)

When configuring automated release builds in GitHub Actions:
1. Store the base64-encoded keystore in GitHub Secrets: `KEYSTORE_BASE64`.
2. Store passwords as encrypted repository secrets: `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`.
3. In the CI runner workflow:
   ```yaml
   - name: Decode Upload Keystore
     run: |
       echo "${{ secrets.KEYSTORE_BASE64 }}" | base64 --decode > /tmp/upload-keystore.jks
     env:
       KEYSTORE_BASE64: ${{ secrets.KEYSTORE_BASE64 }}

   - name: Build Release Bundle
     run: ./gradlew bundleRelease
     env:
       KEYSTORE_PATH: /tmp/upload-keystore.jks
       KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
       KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
       KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}

   - name: Secure Cleanup
     run: rm -f /tmp/upload-keystore.jks
   ```

---

## 6. Key Recovery & Rotation

If the upload key is ever lost or compromised:
1. Log in to **Google Play Console** $\to$ Select **NEXIQ**.
2. Navigate to **Release $\to$ Setup $\to$ App integrity $\to$ App Signing tab**.
3. Request an **Upload key reset**. Google Support will verify developer identity and register the public certificate of the replacement upload key.
