[![CodeFactor](https://www.codefactor.io/repository/github/stanislav-sartasov/navitas-framework/badge)](https://www.codefactor.io/repository/github/stanislav-sartasov/navitas-framework)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
# Navitas-Framework
An open-source power profiling framework for Android

## Architectural component
The Navitas Framework is architecturally composed of 3 parts:
* NaviProf is a gradle plugin responsible for profiling, that is, obtaining information about the power consumption of a device, as well as instructing tests and converting the received data into a JSON file.
* Navitas Plugin is a plugin for Android Studio that is responsible for configuring tests that run on the device, profiling the device using NaviProf, parsing the received data presented in JSON format, analyzing the collected data and visualizing the profiling results. The plugin is written using the MVVM pattern and also based on the [Intellij SDK](https://plugins.jetbrains.com/docs/intellij/welcome.html).
* NaviTests is a project containing two android applications required to demonstrate how Navitas Framework works:
  * Navi Tests is an android application that demonstrates work on a specific set of tests.
  * Navi Constants is an android application containing tests needed to determine the device's power consumption constants and then generate the corresponding device power profile, that is, the power_profile.xml file.
 
 ## Installation and start-up instructions
 * Clone the repo
 * In the Navitas Plugin project, you need to add a local.properties file containing the path to the Android SDK, as well as to the Android Studio.
 * When starting the NaviTests project, change the JDK in Android Studio to the one that corresponds to the JDK of the NaviProf project and Navitas Plugin.
 
 ## Work with Docker (Demo)
 * Connect mobile device to computer
 * In terminal run ``` $ adb usb ``` and enable usb debugging on phone
 * Run ``` $ adb tcpip 5555 ```
 * After run ``` $ adb connect {device IP-address}:5555 ``` and check connection ``` $ adb devices ```
 * Run dockerfile  ``` $ docker run --privileged  -v /home/rinatisk/.android:/root/.android -v /dev/bus/usb:/dev/bus/usb -t -i --device=/dev/ttyUSB0 ```