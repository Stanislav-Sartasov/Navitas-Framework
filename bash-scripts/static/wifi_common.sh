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

echo Wi-Fi test with a sampling rate of 0.75 second \(1.33 hz\)
adb shell dumpsys batterystats --reset

for ((var = 1; var <= 80; var++))
do

	echo >> wifi.txt
	echo -n "$(echo "$var * .75" | bc)" >> wifi.txt
	
	adb shell dumpsys batterystats > batterystats.txt
	
	echo -n "$(grep -inr "WiFi Battery drain:" ./batterystats.txt | cut -d ":" -f2- |
	cut -d ":" -f2 | cut -d "m" -f1)" >> wifi.txt
	
	sleep .75
	
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

echo Wi-Fi test with a sampling rate of 1.5 second \(0.66 hz\)
adb shell dumpsys batterystats --reset

for ((var = 1; var <= 40; var++))
do

	echo >> wifi.txt
	echo -n "$(echo "$var * 1.5" | bc)" >> wifi.txt
	
	adb shell dumpsys batterystats > batterystats.txt
	
	echo -n "$(grep -inr "WiFi Battery drain:" ./batterystats.txt | cut -d ":" -f2- |
	cut -d ":" -f2 | cut -d "m" -f1)" >> wifi.txt
	
	sleep 1.5
	
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

echo Wi-Fi test with a sampling rate of 2.5 second \(0.4 hz\)
adb shell dumpsys batterystats --reset

for ((var = 1; var <= 24; var++))
do

	echo >> wifi.txt
	echo -n "$(echo "$var * 2.5" | bc)" >> wifi.txt
	
	adb shell dumpsys batterystats > batterystats.txt
	
	echo -n "$(grep -inr "WiFi Battery drain:" ./batterystats.txt | cut -d ":" -f2- |
	cut -d ":" -f2 | cut -d "m" -f1)" >> wifi.txt
	
	sleep 2.5
	
done

echo Wi-Fi test with a sampling rate of 3 second \(0.33 hz\)
adb shell dumpsys batterystats --reset

for ((var = 1; var <= 20; var++))
do

	echo >> wifi.txt
	echo -n "$(echo "$var * 3" | bc)" >> wifi.txt

	adb shell dumpsys batterystats > batterystats.txt
	
	echo -n "$(grep -inr "WiFi Battery drain:" ./batterystats.txt | cut -d ":" -f2- |
	cut -d ":" -f2 | cut -d "m" -f1)" >> wifi.txt
	
	sleep 3
	
done

echo Wi-Fi test with a sampling rate of 3.5 second \(0.28 hz\)
adb shell dumpsys batterystats --reset

for ((var = 1; var <= 17; var++))
do

	echo >> wifi.txt
	echo -n "$(echo "$var * 3.5" | bc)" >> wifi.txt
	
	adb shell dumpsys batterystats > batterystats.txt
	
	echo -n "$(grep -inr "WiFi Battery drain:" ./batterystats.txt | cut -d ":" -f2- |
	cut -d ":" -f2 | cut -d "m" -f1)" >> wifi.txt
	
	sleep 3.5
	
done

echo Wi-Fi test with a sampling rate of 4 second \(0.25 hz\)
adb shell dumpsys batterystats --reset

for ((var = 1; var <= 15; var++))
do

	echo >> wifi.txt
	echo -n "$(echo "$var * 4" | bc)" >> wifi.txt
	
	adb shell dumpsys batterystats > batterystats.txt
	
	echo -n "$(grep -inr "WiFi Battery drain:" ./batterystats.txt | cut -d ":" -f2- |
	cut -d ":" -f2 | cut -d "m" -f1)" >> wifi.txt
	
	sleep 4
	
done

echo Wi-Fi test with a sampling rate of 4.5 second \(0.22 hz\)
adb shell dumpsys batterystats --reset

for ((var = 1; var <= 13; var++))
do

	echo >> wifi.txt
	echo -n "$(echo "$var * 4.5" | bc)" >> wifi.txt
	
	adb shell dumpsys batterystats > batterystats.txt
	
	echo -n "$(grep -inr "WiFi Battery drain:" ./batterystats.txt | cut -d ":" -f2- |
	cut -d ":" -f2 | cut -d "m" -f1)" >> wifi.txt
	
	sleep 4.5
	
done

echo Wi-Fi test with a sampling rate of 5 second \(0.2 hz\)
adb shell dumpsys batterystats --reset

for ((var = 1; var <= 12; var++))
do

	echo >> wifi.txt
	echo -n "$(echo "$var * 5" | bc)" >> wifi.txt
	
	adb shell dumpsys batterystats > batterystats.txt
	
	echo -n "$(grep -inr "WiFi Battery drain:" ./batterystats.txt | cut -d ":" -f2- |
	cut -d ":" -f2 | cut -d "m" -f1)" >> wifi.txt
	
	sleep 5
	
done

echo Wi-Fi test with a sampling rate of 6 second \(0.16 hz\)
adb shell dumpsys batterystats --reset

for ((var = 1; var <= 10; var++))
do

	echo >> wifi.txt
	echo -n "$(echo "$var * 6" | bc)" >> wifi.txt
	
	adb shell dumpsys batterystats > batterystats.txt
	
	echo -n "$(grep -inr "WiFi Battery drain:" ./batterystats.txt | cut -d ":" -f2- |
	cut -d ":" -f2 | cut -d "m" -f1)" >> wifi.txt
	
	sleep 6
	
done

echo Wi-Fi test with a sampling rate of 7 second \(0.14 hz\)
adb shell dumpsys batterystats --reset

for ((var = 1; var <= 8; var++))
do

	echo >> wifi.txt
	echo -n "$(echo "$var * 7" | bc)" >> wifi.txt
	
	adb shell dumpsys batterystats > batterystats.txt
	
	echo -n "$(grep -inr "WiFi Battery drain:" ./batterystats.txt | cut -d ":" -f2- |
	cut -d ":" -f2 | cut -d "m" -f1)" >> wifi.txt
	
	sleep 7
	
done

echo Wi-Fi test with a sampling rate of 8 second \(0.12 hz\)
adb shell dumpsys batterystats --reset

for ((var = 1; var <= 7; var++))
do

	echo >> wifi.txt
	echo -n "$(echo "$var * 8" | bc)" >> wifi.txt
	
	adb shell dumpsys batterystats > batterystats.txt 
	
	echo -n "$(grep -inr "WiFi Battery drain:" ./batterystats.txt | cut -d ":" -f2- |
	cut -d ":" -f2 | cut -d "m" -f1)" >> wifi.txt
	
	sleep 8
	
done

echo Wi-Fi test with a sampling rate of 9 second \(0.11 hz\)
adb shell dumpsys batterystats --reset

for ((var = 1; var <= 6; var++))
do

	echo >> wifi.txt
	echo -n "$(echo "$var * 9" | bc)" >> wifi.txt
	
	adb shell dumpsys batterystats > batterystats.txt
	
	echo -n "$(grep -inr "WiFi Battery drain:" ./batterystats.txt | cut -d ":" -f2- |
	cut -d ":" -f2 | cut -d "m" -f1)" >> wifi.txt
	
	sleep 9
	
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
