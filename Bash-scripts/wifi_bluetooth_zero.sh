#!/bin/bash

#First you need to turn on airplane mode.

adb shell dumpsys battery unplug

#This commands may not work on some devices. You should check it in advance.
adb shell svc wifi disable
adb shell svc bluetooth disable

adb shell input keyevent 26

echo "Wait 30 seconds to avoid some negative impacts on testing"
sleep 30

echo Wi-Fi and Bluetooth test in the off state
adb shell dumpsys batterystats --reset

	sleep 30

	adb shell dumpsys batterystats > batterystats.txt

	echo -n "$(grep -inr "WiFi Battery drain:" ./batterystats.txt | cut -d ":" -f2- |
	cut -d ":" -f2 | cut -d "m" -f1)" >> wifi_zero.txt

	echo -n "$(grep -inr "Bluetooth:" ./batterystats.txt | cut -d ":" -f2- | cut -d ":" -f2 |
	cut -d "b" -f2- | cut -d "b" -f2 | cut -d "t" -f2- | cut -d "t" -f2 | cut -d "=" -f2- |
	cut -d "=" -f2 | cut -d " " -f1)">> bluetooth_zero.txt

echo "Testing completed"

#This commands may not work on some devices. You should check it in advance.
adb shell svc wifi enable
adb shell svc bluetooth enable

adb shell dumpsys battery set ac 1
