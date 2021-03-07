#!/bin/bash

#First you need to turn on airplane mode.

adb shell dumpsys battery unplug

#This command may not work on some devices. You should check it in advance.
adb shell svc bluetooth enable

adb shell input keyevent 26

echo "Wait 30 seconds to avoid Bluetooth scans or other negative impacts on testing"
sleep 30

echo Bluetooth disregarded test with a sampling rate of 0.1 second \(10 hz\)

for ((bar = 1; bar <= 300; bar++))
do

	adb shell dumpsys batterystats --reset

	echo >> bluetooth.txt
	echo -n "$(echo "$bar * .1" | bc)" >> bluetooth.txt
	echo -n " " >> bluetooth.txt
	
	adb shell dumpsys batterystats > batterystats.txt
	
	echo -n "$(grep -inr "Bluetooth:" ./batterystats.txt | cut -d ":" -f2- | cut -d ":" -f2 |
	cut -d "b" -f2- | cut -d "b" -f2 | cut -d "t" -f2- | cut -d "t" -f2 | cut -d "=" -f2- |
	cut -d "=" -f2 | cut -d " " -f1)" >> bluetooth.txt
	
	sleep .1
	
done

for ((foo = 1; foo <= 5; foo++))
do

	echo Bluetooth test $foo with a sampling rate of 0.5 second \(2 hz\)

	for ((bar = 1; bar <= 60; bar++))
	do

		adb shell dumpsys batterystats --reset

		echo >> bluetooth.txt
		echo -n "$(echo "$bar * .5" | bc)" >> bluetooth.txt
		echo -n " " >> bluetooth.txt
		
		adb shell dumpsys batterystats > batterystats.txt
		
		echo -n "$(grep -inr "Bluetooth:" ./batterystats.txt | cut -d ":" -f2- | cut -d ":" -f2 |
		cut -d "b" -f2- | cut -d "b" -f2 | cut -d "t" -f2- | cut -d "t" -f2 | cut -d "=" -f2- |
		cut -d "=" -f2 | cut -d " " -f1)" >> bluetooth.txt
		
		sleep .5
		
	done

done

echo "Testing completed"

adb shell input keyevent 26

#This command may not work on some devices. You should check it in advance.
adb shell svc bluetooth disable

adb shell dumpsys battery set ac 1