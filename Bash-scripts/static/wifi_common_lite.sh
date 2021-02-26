#!/bin/bash

#First you need to turn on airplane mode.

adb shell dumpsys battery unplug

#This command may not work on some devices. You should check it in advance.
adb shell svc wifi enable

adb shell input keyevent 26

echo "Wait 30 seconds to avoid Wi-Fi scans or other negative impacts on testing"
sleep 30

echo Wi-Fi disregarded test with a sampling rate of 0.1 second \(10 hz\)
adb shell dumpsys batterystats --reset

for ((var = 1; var <= 600; var++))
do
	
	echo >> wifi.txt
	echo -n "$(echo "$var * .1" | bc)" >> wifi.txt
	
	adb shell dumpsys batterystats > batterystats.txt
	
	echo -n "$(grep -inr "WiFi Battery drain:" ./batterystats.txt | cut -d ":" -f2- |
	cut -d ":" -f2 | cut -d "m" -f1)" >> wifi.txt
	
	sleep .1
	
done

echo Wi-Fi test with a sampling rate of 0.25 second \(4 hz\)
adb shell dumpsys batterystats --reset

for ((var = 1; var <= 240; var++))
do
	
	echo >> wifi.txt
	echo -n "$(echo "$var * .25" | bc)" >> wifi.txt
	
	adb shell dumpsys batterystats > batterystats.txt
	
	echo -n "$(grep -inr "WiFi Battery drain:" ./batterystats.txt | cut -d ":" -f2- |
	cut -d ":" -f2 | cut -d "m" -f1)" >> wifi.txt
	
	sleep .25
	
done

echo Wi-Fi test with a sampling rate of 0.5 second \(2 hz\)
adb shell dumpsys batterystats --reset

for ((var = 1; var <= 120; var++))
do
	
	echo >> wifi.txt
	echo -n "$(echo "$var * .5" | bc)" >> wifi.txt
	
	adb shell dumpsys batterystats > batterystats.txt
	
	echo -n "$(grep -inr "WiFi Battery drain:" ./batterystats.txt | cut -d ":" -f2- |
	cut -d ":" -f2 | cut -d "m" -f1)" >> wifi.txt
	
	sleep .5
	
done

echo Wi-Fi test with a sampling rate of 1 second \(1 hz\)
adb shell dumpsys batterystats --reset

for ((var = 1; var <= 60; var++))
do

	echo >> wifi.txt
	echo -n "$(echo "$var * 1" | bc)" >> wifi.txt
	
	adb shell dumpsys batterystats > batterystats.txt
	
	echo -n "$(grep -inr "WiFi Battery drain:" ./batterystats.txt | cut -d ":" -f2- |
	cut -d ":" -f2 | cut -d "m" -f1)" >> wifi.txt
	
	sleep 1
	
done

echo Wi-Fi test with a sampling rate of 2 second \(0.5 hz\)
adb shell dumpsys batterystats --reset

for ((var = 1; var <= 30; var++))
do

	echo >> wifi.txt
	echo -n "$(echo "$var * 2" | bc)" >> wifi.txt
	
	adb shell dumpsys batterystats > batterystats.txt
	
	echo -n "$(grep -inr "WiFi Battery drain:" ./batterystats.txt | cut -d ":" -f2- |
	cut -d ":" -f2 | cut -d "m" -f1)" >> wifi.txt
	
	sleep 2
	
done

echo Wi-Fi test with a sampling rate of 10 second \(0.1 hz\)
adb shell dumpsys batterystats --reset

for ((var = 1; var <= 6; var++))
do

	echo >> wifi.txt
	echo -n "$(echo "$var * 10" | bc)" >> wifi.txt
	
	adb shell dumpsys batterystats > batterystats.txt
	
	echo -n "$(grep -inr "WiFi Battery drain:" ./batterystats.txt | cut -d ":" -f2- |
	cut -d ":" -f2 | cut -d "m" -f1)" >> wifi.txt
	
	sleep 10
	
done

echo "Testing completed"

adb shell input keyevent 26

#This command may not work on some devices. You should check it in advance.
adb shell svc wifi disable

adb shell dumpsys battery set ac 1
