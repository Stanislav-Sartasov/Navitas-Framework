#!/bin/bash

#First you need to turn on airplane mode.

adb shell dumpsys battery unplug

#This command may not work on some devices. You should check it in advance.
adb shell svc bluetooth enable

adb shell input keyevent 26

echo "Wait 30 seconds to avoid Bluetooth scans or other negative impacts on testing"
sleep 30

echo Bluetooth disregarded test with a sampling rate of 0.1 second \(10 hz\)
adb shell dumpsys batterystats --reset

for ((var = 1; var <= 600; var++))
do

	echo >> bluetooth.txt
	echo -n "$(echo "$var * .1" | bc)" >> bluetooth.txt
	echo -n " " >> bluetooth.txt
	
	adb shell dumpsys batterystats > batterystats.txt
	
	echo -n "$(grep -inr "Bluetooth:" ./batterystats.txt | cut -d ":" -f2- | cut -d ":" -f2 |
	cut -d "b" -f2- | cut -d "b" -f2 | cut -d "t" -f2- | cut -d "t" -f2 | cut -d "=" -f2- |
	cut -d "=" -f2 | cut -d " " -f1)">> bluetooth.txt
	
	sleep .1
	
done

echo Bluetooth test with a sampling rate of 0.25 second \(4 hz\)
adb shell dumpsys batterystats --reset

for ((var = 1; var <= 240; var++))
do

	echo >> bluetooth.txt
	echo -n "$(echo "$var * .25" | bc)" >> bluetooth.txt
	echo -n " " >> bluetooth.txt
	
	adb shell dumpsys batterystats > batterystats.txt
	
	echo -n "$(grep -inr "Bluetooth:" ./batterystats.txt | cut -d ":" -f2- | cut -d ":" -f2 |
	cut -d "b" -f2- | cut -d "b" -f2 | cut -d "t" -f2- | cut -d "t" -f2 | cut -d "=" -f2- |
	cut -d "=" -f2 | cut -d " " -f1)" >> bluetooth.txt
	
	sleep .25
	
done

echo Bluetooth test with a sampling rate of 0.5 second \(2 hz\)
adb shell dumpsys batterystats --reset

for ((var = 1; var <= 120; var++))
do

	echo >> bluetooth.txt
	echo -n "$(echo "$var * .5" | bc)" >> bluetooth.txt
	echo -n " " >> bluetooth.txt
	
	adb shell dumpsys batterystats > batterystats.txt
	
	echo -n "$(grep -inr "Bluetooth:" ./batterystats.txt | cut -d ":" -f2- | cut -d ":" -f2 |
	cut -d "b" -f2- | cut -d "b" -f2 | cut -d "t" -f2- | cut -d "t" -f2 | cut -d "=" -f2- |
	cut -d "=" -f2 | cut -d " " -f1)" >> bluetooth.txt
	
	sleep .5
	
done

echo Bluetooth test with a sampling rate of 1 second \(1 hz\)
adb shell dumpsys batterystats --reset

for ((var = 1; var <= 60; var++))
do

	echo >> bluetooth.txt
	echo -n "$(echo "$var * 1" | bc)" >> bluetooth.txt
	echo -n " " >> bluetooth.txt
	
	adb shell dumpsys batterystats > batterystats.txt
	
	echo -n "$(grep -inr "Bluetooth:" ./batterystats.txt | cut -d ":" -f2- | cut -d ":" -f2 |
	cut -d "b" -f2- | cut -d "b" -f2 | cut -d "t" -f2- | cut -d "t" -f2 | cut -d "=" -f2- |
	cut -d "=" -f2 | cut -d " " -f1)" >> bluetooth.txt
	
	sleep 1
	
done

echo Bluetooth test with a sampling rate of 2 second \(0.5 hz\)
adb shell dumpsys batterystats --reset

for ((var = 1; var <= 30; var++))
do

	echo >> bluetooth.txt
	echo -n "$(echo "$var * 2" | bc)" >> bluetooth.txt
	echo -n " " >> bluetooth.txt
	
	adb shell dumpsys batterystats > batterystats.txt
	
	echo -n "$(grep -inr "Bluetooth:" ./batterystats.txt | cut -d ":" -f2- | cut -d ":" -f2 |
	cut -d "b" -f2- | cut -d "b" -f2 | cut -d "t" -f2- | cut -d "t" -f2 | cut -d "=" -f2- |
	cut -d "=" -f2 | cut -d " " -f1)" >> bluetooth.txt
	
	sleep 2
	
done

echo Bluetooth test with a sampling rate of 10 second \(0.1 hz\)
adb shell dumpsys batterystats --reset

for ((var = 1; var <= 6; var++))
do

	echo >> bluetooth.txt
	echo -n "$(echo "$var * 10" | bc)" >> bluetooth.txt
	echo -n " " >> bluetooth.txt
	
	adb shell dumpsys batterystats > batterystats.txt
	
	echo -n "$(grep -inr "Bluetooth:" ./batterystats.txt | cut -d ":" -f2- | cut -d ":" -f2 |
	cut -d "b" -f2- | cut -d "b" -f2 | cut -d "t" -f2- | cut -d "t" -f2 | cut -d "=" -f2- |
	cut -d "=" -f2 | cut -d " " -f1)" >> bluetooth.txt
	
	sleep 10
	
done

echo "Testing completed"

adb shell input keyevent 26

#This command may not work on some devices. You should check it in advance.
adb shell svc bluetooth disable

adb shell dumpsys battery set ac 1
