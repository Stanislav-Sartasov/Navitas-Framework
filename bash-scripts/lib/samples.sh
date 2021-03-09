#!/bin/bash

. ./functions.sh

#It is necessary to turn off all the sparing functions of the phone,
#except for the tested one, turn on the airplane mode and send the
#entire application to sleep mode

#This command may not work on some devices. You should check it in advance
adb shell dumpsys battery unplug

adb shell dumpsys battery set ac 0
adb shell dumpsys battery set usb 0
adb shell dumpsys battery set wireless 0
adb shell dumpsys battery set status 0

#adb shell svc wifi enable
#adb shell svc bluetooth enable

adb shell input keyevent 26

echo "Wait 30 seconds to avoid Wi-Fi or Bluetooth scans or other negative impacts on testing, \
it's also time to make some final changes before the test"
sleep 30

bluetooth_common 30 0.2 -s
bluetooth_common 30 1 -s

echo "Testing completed"

#This commands may not work on some devices. You should check it in advance
adb shell input keyevent 26

#adb shell svc wifi disable
#adb shell svc bluetooth disable

adb shell dumpsys battery set ac 1
adb shell dumpsys battery set usb 1
adb shell dumpsys battery set wireless 1
adb shell dumpsys battery set status 1
