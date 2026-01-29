# BoatMouse Speed Controller

A simple Java tool to quickly change Windows mouse speed, designed for Minecraft speedrunners who use ninjabrain for measurements. (I hate Windows settings. I find it easier to create a folder and open everything in it.)

## Features

- 🐢 **Slow mode:** Switch to a precise low speed for accurate measurements
- 🐇 **Fast mode:** Switch back to normal/high speed for standart if you use RawAccel
- ⚙️ **Customizable speeds:** Set your own slow/fast/startup speeds
- 🖤 **Dark theme:** Easy on the eyes
- 💾 **Persistent settings:** Remembers your preferences
- 🚀 **Startup speed:** Option to set specific speed on launch
- ⚡ **Toggle Mouse Acceleration:** New button to quickly enable/disable Windows mouse acceleration.

## Requirements

- Java 8 or higher installed

## How to Use

1. Download the latest release (or clone the repository)
2. Run the program: `java -jar BoatMouse.jar`
3. Click "Settings" to configure your preferred speeds
4. Use "🐢 Slow" and "🐇 Fast" buttons to switch between speeds instantly

## Building from Source

1. Clone or download this repository
2. Download required libraries:
   - [jna-5.13.0.jar]
   - [jna-platform-5.13.0.jar]
3. Place the JAR files in the same directory as `BoatMouse.java`
4. Use the provided `build-jar.bat` script or follow the manual compilation steps in the script.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
