# Xoboard

<p align="center">
  <strong>A customizable Android keyboard with clipboard history, text styles and emoji.</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/platform-Android-3DDC84?logo=android&logoColor=white&style=for-the-badge" alt="Android" />
  <img src="https://img.shields.io/badge/language-Java-ED8B00?logo=openjdk&logoColor=white&style=for-the-badge" alt="Java" />
  <img src="https://img.shields.io/badge/min%20SDK-26-2E7D32?style=for-the-badge" alt="Minimum SDK 26" />
  <img src="https://img.shields.io/badge/target%20SDK-35-1565C0?style=for-the-badge" alt="Target SDK 35" />
</p>

Xoboard is a native Android input method editor (IME). It provides a familiar
on-screen keyboard plus clipboard history, Unicode text styling, emoji and
settings for appearance and typing behavior. The Android app runs on devices;
the desktop operating systems in this guide are development environments for
editing, building and installing it.

## Features

- **Keyboard:** QWERTY letters, number and symbol layouts, shift/caps lock,
  backspace, space, enter and configurable key behavior.
- **Clipboard history:** Quickly insert recent copied text, remove individual
  entries, clear history and set a maximum number of saved entries. A limit of
  `0` keeps history unlimited. Individual entries are capped at 4,000
  characters.
- **Text styles:** Convert text into Unicode variants such as bold, italic,
  script, Fraktur, small caps, superscript, subscript and decorated text.
- **Emoji:** Browse emoji by category and insert them into the current text
  field.
- **Appearance:** System, light, dark and custom themes, with editable
  background, key, text and accent colors.
- **Typing preferences:** Toggle haptic feedback, sentence capitalization and
  double-space period insertion.

## Project Requirements

| Component               | Version or requirement                  |
| ----------------------- | --------------------------------------- |
| Android Gradle Plugin   | `9.4.0`                                 |
| Gradle                  | `9.6.0`, pinned by the included Wrapper |
| Java                    | JDK `17`                                |
| Android compile SDK     | API `35`                                |
| Android Build Tools     | `36.0.0`                                |
| Minimum Android version | Android `8.0`, API `26`                 |
| App ID                  | `com.xodobyte.xoboard`                  |

These versions match the project's Gradle configuration and Android's AGP
compatibility information. Android Studio is optional: the command-line SDK and
a physical Android device are enough to build and install Xoboard. Use an
emulator only if you want to test without a phone.

## Development Setup

Install JDK 17 and the Android SDK Platform `35`, Platform Tools and Build Tools
`36.0.0`. You can install these through Android Studio's SDK Manager or use the
command-line tools. The SDK Manager command `sdkmanager` is still usable but is
deprecated by Google in favor of the newer Android CLI; these instructions use
it because it is available in the command-line tools and matches the setup this
project was built with.

### Arch Linux with Hyprland

This is the command-line setup used by the project maintainer. Run commands as
your normal user except where `sudo` is shown.

Install Java 17 and utilities:

```bash
sudo pacman -S --needed jdk17-openjdk unzip curl
```

```bash
sudo archlinux-java set java-17-openjdk
```

Verify Java:

```bash
java -version
```

```bash
javac -version
```

```bash
archlinux-java status
```

Download Google's command-line tools into the standard SDK layout. This is the
archive revision used for the recorded setup; for a different/current revision,
download the Linux command-line tools archive from the
[official Android Studio downloads page](https://developer.android.com/studio#command-line-tools-only).

```bash
mkdir -p "$HOME/Android/Sdk/cmdline-tools"
```

```bash
cd "$HOME/Android/Sdk/cmdline-tools"
```

```bash
curl -fLO https://dl.google.com/android/repository/commandlinetools-linux-15859902_latest.zip
```

```bash
unzip -q commandlinetools-linux-15859902_latest.zip
```

```bash
mv cmdline-tools latest
```

```bash
rm commandlinetools-linux-15859902_latest.zip
```

Add the SDK tools to Bash's environment. These commands append the settings to
`~/.bashrc` and load them into the current terminal:

```bash
printf '%s\n' 'export ANDROID_HOME="$HOME/Android/Sdk"' >> "$HOME/.bashrc"
```

```bash
printf '%s\n' 'export PATH="$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/build-tools/36.0.0"' >> "$HOME/.bashrc"
```

```bash
source "$HOME/.bashrc"
```

For Zsh, use `~/.zshrc` instead. For Fish, configure `ANDROID_HOME` and `PATH`
using Fish's universal-variable syntax or its configuration file.

Accept the Android SDK licenses when prompted, then install the packages this
project uses:

```bash
sdkmanager --licenses
```

```bash
sdkmanager "platform-tools" "platforms;android-35" "build-tools;36.0.0"
```

Check the installation:

```bash
sdkmanager --list
```

```bash
adb version
```

```bash
apksigner --version
```

For USB debugging on Linux, install the Android udev rules appropriate for your
distribution. On Arch, one common AUR option is `android-udev` (or
`android-udev-git`); then reload the rules and reconnect the device:

```bash
sudo udevadm control --reload-rules
```

```bash
sudo udevadm trigger
```

Android Studio is optional and can be installed from the
[official Android Studio page](https://developer.android.com/studio) or an Arch
package source you trust. On Hyprland, if the Java UI has window-management or
rendering issues, try the XWayland Qt backend when starting an emulator:

```bash
QT_QPA_PLATFORM=xcb emulator -avd <avd-name>
```

The exact behavior depends on your graphics drivers, emulator image and
Android Studio distribution. A physical phone avoids emulator setup and is
usually the simplest way to test an IME.

### Debian, Ubuntu, Fedora and Other Linux

Use the JDK 17 package from your distribution. For example, on Debian/Ubuntu:

```bash
sudo apt update
```

```bash
sudo apt install openjdk-17-jdk unzip curl
```

On Fedora:

```bash
sudo dnf install java-17-openjdk-devel unzip curl
```

Other distributions provide an equivalent OpenJDK 17 development package.
Verify the JDK with these commands:

```bash
java -version
```

```bash
javac -version
```

Install the Android SDK using Android Studio or Google's
[command-line tools](https://developer.android.com/studio/command-line). Follow
the SDK directory, `cmdline-tools/latest` layout, environment variable and
`sdkmanager` package commands in the Arch section, substituting your preferred
SDK location. Linux users who deploy to a phone over USB may need their
distribution's Android udev rules; do not run Gradle or ADB as root to work
around missing device permissions.

### Windows 10 and Windows 11

Use a 64-bit JDK 17 and Android SDK. Android Studio's Windows installer is an
official `.exe` and is the easiest way to install the IDE and SDK: download it
from [developer.android.com/studio](https://developer.android.com/studio), run
the installer, then follow its Setup Wizard. In **SDK Manager**, install
Android SDK Platform `35`, Android SDK Build-Tools `36.0.0` and Android SDK
Platform-Tools.

For a JDK, install Eclipse Temurin 17 from its official
[Windows installer page](https://adoptium.net/installation/windows/) or use
Windows Package Manager (`winget`) in an elevated or regular terminal:

```powershell
winget install EclipseAdoptium.Temurin.17.JDK
```

Close and reopen the terminal after installation, then check `java -version`
and `javac -version`. If Android Studio uses a different JDK, set its Gradle
JDK to the installed JDK 17 in the IDE settings.

The usual command-line SDK location is
`%LOCALAPPDATA%\Android\Sdk`. If you installed the SDK elsewhere, use that
location instead. To use the command-line tools, download the Windows
command-line tools archive from Google's
[official downloads page](https://developer.android.com/studio#command-line-tools-only),
extract it so the files are under
`%ANDROID_HOME%\cmdline-tools\latest\bin` and set `ANDROID_HOME` to the SDK
directory. The extraction layout should resemble:

```text
C:\Users\you\AppData\Local\Android\Sdk\
  cmdline-tools\
    latest\
      bin\sdkmanager.bat
  platform-tools\
  platforms\
  build-tools\
```

You can set `ANDROID_HOME` and add the command-line tools and platform tools to
`Path` in **Start > Edit the system environment variables > Environment
Variables**. To set them only for the current PowerShell session:

```powershell
$env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
```

```powershell
$env:Path += ";$env:ANDROID_HOME\cmdline-tools\latest\bin;$env:ANDROID_HOME\platform-tools;$env:ANDROID_HOME\build-tools\36.0.0"
```

Then accept licenses and install the required packages:

```powershell
sdkmanager.bat --licenses
```

```powershell
sdkmanager.bat "platform-tools" "platforms;android-35" "build-tools;36.0.0"
```

In **Command Prompt (CMD)**, the equivalent current-session environment setup
and SDK commands are:

```bat
set ANDROID_HOME=%LOCALAPPDATA%\Android\Sdk
```

```bat
set PATH=%PATH%;%ANDROID_HOME%\cmdline-tools\latest\bin;%ANDROID_HOME%\platform-tools;%ANDROID_HOME%\build-tools\36.0.0
```

```bat
call "%ANDROID_HOME%\cmdline-tools\latest\bin\sdkmanager.bat" --licenses
```

```bat
call "%ANDROID_HOME%\cmdline-tools\latest\bin\sdkmanager.bat" "platform-tools" "platforms;android-35" "build-tools;36.0.0"
```

The `set` commands only affect that CMD window. Set persistent variables using
the Windows Environment Variables dialog. For USB deployment, install the
appropriate OEM USB driver if Windows does not recognize the phone; Google USB
Driver is available through SDK Manager for supported Google devices.

### macOS

Install Android Studio using the official macOS `.dmg` from
[developer.android.com/studio](https://developer.android.com/studio), then
complete the Setup Wizard and use **Tools > SDK Manager** to install Android SDK
Platform `35`, Build-Tools `36.0.0` and Platform-Tools. Android Studio is
optional; the official command-line tools also work on macOS.

Install a JDK 17 distribution such as Eclipse Temurin using the official
[macOS installer](https://adoptium.net/installation/macOS/) or a trusted package
manager. With Homebrew already installed, for example:

```bash
brew install --cask temurin@17
```

Check the JDK installation:

```bash
java -version
```

```bash
javac -version
```

The common SDK directory is `$HOME/Library/Android/sdk`. For command-line
tools, download the macOS archive from Google's
[official downloads page](https://developer.android.com/studio#command-line-tools-only)
and arrange its contents under
`$ANDROID_HOME/cmdline-tools/latest/`. Then configure the shell (the example
uses Zsh, the default shell on current macOS releases):

```bash
printf '%s\n' 'export ANDROID_HOME="$HOME/Library/Android/sdk"' >> "$HOME/.zshrc"
```

```bash
printf '%s\n' 'export PATH="$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/build-tools/36.0.0"' >> "$HOME/.zshrc"
```

```bash
source "$HOME/.zshrc"
```

Install packages and accept the relevant licenses:

```bash
sdkmanager --licenses
```

```bash
sdkmanager "platform-tools" "platforms;android-35" "build-tools;36.0.0"
```

USB debugging does not require Linux udev rules on macOS.

## Get the Project Ready

Clone or extract the project, then open a terminal in its root directory (the
directory containing `gradlew` and `settings.gradle`). Gradle needs the Android
SDK location. Android Studio usually creates `local.properties` automatically
once the SDK is configured. Otherwise create this untracked, machine-specific
file in the project root using the actual SDK path on your computer:

```properties
sdk.dir=/absolute/path/to/Android/Sdk
```

Common examples:

```properties
# Linux
sdk.dir=/home/your-user/Android/Sdk

# macOS
sdk.dir=/Users/your-user/Library/Android/sdk

# Windows (forward slashes are accepted)
sdk.dir=C:/Users/your-user/AppData/Local/Android/Sdk
```

Do not commit `local.properties`. The repository includes `gradlew`,
`gradlew.bat` and the Gradle Wrapper configuration for Gradle `9.6.0`, so a
separate system Gradle installation is not normally needed. The first Wrapper
run downloads Gradle from `services.gradle.org`; the build also downloads the
Android Gradle Plugin from Google's Maven repository, so network access is
required on first setup.

## Build and Install

Run these commands from the project root. On Linux and macOS:

```bash
./gradlew --version
```

```bash
./gradlew assembleDebug
```

On Windows PowerShell or CMD:

```powershell
.\gradlew.bat --version
```

```powershell
.\gradlew.bat assembleDebug
```

To build the configured release APK and copy it to the distribution folder:

```bash
./gradlew xoboardApk
```

Windows:

```powershell
.\gradlew.bat xoboardApk
```

The custom `xoboardApk` task depends on `assembleRelease` and copies the result
to `app/build/dist/Xoboard.apk`. A regular release build is at
`app/build/outputs/apk/release/app-release.apk`. Debug builds are at
`app/build/outputs/apk/debug/app-debug.apk`.

Useful additional checks:

```bash
./gradlew lint
```

```bash
./gradlew test
```

On Windows PowerShell or CMD:

```powershell
.\gradlew.bat lint
```

```powershell
.\gradlew.bat test
```

There are currently no checked-in unit or instrumentation test sources.

To install a debug build over USB, enable Developer options and USB debugging
on the Android device, connect it, approve the debugging prompt, then run:

```bash
adb devices
```

```bash
./gradlew installDebug
```

The Windows equivalent is:

```powershell
adb devices
```

```powershell
.\gradlew.bat installDebug
```

To install the release APK produced by `xoboardApk`, first enable Developer
options and USB debugging on the Android device, connect it and approve the
RSA authorization prompt. Confirm ADB lists the device as `device`:

```bash
adb devices
```

From the project root, install or update Xoboard with:

```bash
adb install -r app/build/dist/Xoboard.apk
```

In Windows PowerShell or CMD, run the same relative-path command after building
the release APK:

```powershell
adb install -r app/build/dist/Xoboard.apk
```

You can also transfer the APK to the phone and open it there. Android may ask
you to allow app installation from the file manager or browser used to open
the APK. To install through ADB directly:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Enable and Use the Keyboard

After installing Xoboard, Android requires you to enable it as an input method
and explicitly choose it:

1. Open Xoboard from the launcher and tap **Enable keyboard**.
2. In Android's keyboard or input-method settings, enable **Xoboard**. Android
   will show the system warning for enabling a third-party keyboard; review it
   and continue only if you trust the installed build.
3. Return to Xoboard and tap **Choose active keyboard**, then select Xoboard.
4. Open any text field to use the keyboard. Its toolbar switches to clipboard
   history and text styles; the emoji control is available from the keyboard.
5. Open Xoboard's settings page to adjust themes, colors, clipboard retention
   and typing behavior.

Settings labels and navigation differ slightly by Android version and device
manufacturer. The keyboard can also be selected from Android's input-method
picker while a text field is focused.

## Privacy Notes

Xoboard has no account or server integration and requests the Android vibration
permission. Clipboard history and preferences are stored locally in the app's
private data. The keyboard reads clipboard text through Android's clipboard
service and saves eligible text in its history; clear the history in settings
or uninstall the app to remove local app data. As with any third-party
keyboard, only enable and use it if you are comfortable entering text with
that keyboard. Android displays its own system warning before enabling an
input method.

## Release Signing and Private Files

The release build reads `keystore.properties` from the project root. If that
file exists, it must contain the release keystore path, store password, key
alias and key password. The project checkout used by the maintainer has a
release keystore configured locally; signing credentials and the keystore are
excluded from version control. If no `keystore.properties` is present, Gradle
uses the Android debug key to sign the release variant, which is not suitable
for publishing an update under a stable signing identity.

Keep these files private and out of commits:

- `keystore.properties`, which can contain passwords and signing configuration.
- The release keystore file (`.keystore`, `.jks`, `.p12` or `.pfx`).
- `local.properties`, which contains a local SDK path.
- APKs and other generated build outputs.

Back up the release key securely. An app update must use the same signing key
as the installed release. Do not copy passwords into documentation, command
history, issue reports or public repositories.

Verify a signed APK with Android Build Tools:

```bash
apksigner verify --print-certs app/build/outputs/apk/release/app-release.apk
```

## Project Structure

| Path                                                                   | Purpose                                                              |
| ---------------------------------------------------------------------- | -------------------------------------------------------------------- |
| `app/src/main/java/com/xodobyte/xoboard/XoboardService.java`           | Input method UI, key handling, clipboard, text-style and emoji modes |
| `app/src/main/java/com/xodobyte/xoboard/SettingsActivity.java`         | Keyboard setup links and appearance/behavior preferences             |
| `app/src/main/java/com/xodobyte/xoboard/ClipStore.java`                | Local clipboard history storage and retention limit                  |
| `app/src/main/java/com/xodobyte/xoboard/FontStyles.java`               | Unicode text-style transformations                                   |
| `app/src/main/java/com/xodobyte/xoboard/EmojiData.java`                | Emoji categories and code-point data                                 |
| `app/src/main/java/com/xodobyte/xoboard/Theme.java` and `XoPrefs.java` | Theme resolution and preference keys                                 |
| `app/src/main/res/xml/method.xml`                                      | Android input-method metadata                                        |
| `app/src/main/AndroidManifest.xml`                                     | App, settings activity, IME service and permission declarations      |
| `app/src/main/res/drawable/`                                           | Keyboard and launcher vector drawables                               |
| `app/build.gradle`                                                     | Android app configuration, release signing and `xoboardApk` task     |
| `gradle/wrapper/`                                                      | Gradle Wrapper version and launcher JAR                              |

## Troubleshooting

### Gradle cannot find the Android SDK

Check that `local.properties` exists in the project root and that `sdk.dir`
points to the SDK directory itself. Confirm that Android SDK Platform `35`,
Build-Tools `36.0.0` and Platform-Tools are installed in that SDK.

### The build uses the wrong Java version

This project requires JDK 17. Check `java -version` and `javac -version`. In
Android Studio, also check the Gradle JDK setting; it can differ from the JDK
selected by your terminal's `JAVA_HOME` or `PATH`.

### `adb` or `apksigner` is not found

Confirm Platform-Tools and Build-Tools `36.0.0` are installed. Add the SDK's
`platform-tools` and `build-tools/36.0.0` directories to `PATH` or run the
tools by their full paths. On Windows, the executable names are `adb.exe` and
`apksigner.bat`.

### The phone is missing from `adb devices`

Use a data-capable USB cable, enable USB debugging, approve the on-device RSA
prompt and check `adb devices` again. On Linux, check the distribution's udev
rules. On Windows, install the appropriate device manufacturer's USB driver
when needed. On macOS, reconnect the device and authorize the debugging prompt.

### Xoboard does not appear in the keyboard picker

Confirm that the APK installed successfully, open the Xoboard settings app and
use **Enable keyboard**. Android lists it only after the input method has been
enabled in system settings. Select it separately using **Choose active
keyboard** or Android's input-method picker.

## References

- [Android Studio downloads and installers](https://developer.android.com/studio)
- [Android command-line tools](https://developer.android.com/studio/command-line)
- [`sdkmanager` documentation and deprecation notice](https://developer.android.com/tools/sdkmanager)
- [Android Gradle Plugin 9.4.0 compatibility](https://developer.android.com/build/releases/gradle-plugin)
- [Gradle Wrapper documentation](https://docs.gradle.org/current/userguide/gradle_wrapper.html)
- [Eclipse Temurin JDK installation guides](https://adoptium.net/installation/)
- [Microsoft: Get started with Java on Windows](https://learn.microsoft.com/en-us/windows/dev-environment/java/)
- [Arch Linux Android development wiki](https://wiki.archlinux.org/title/Android)
- [Arch Linux Java wiki](https://wiki.archlinux.org/title/Java)
- [Project license](LICENSE)
