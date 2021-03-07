#!/bin/bash

#First you need to turn on airplane mode.

#adb shell dumpsys battery set ac 0
#adb shell dumpsys battery set usb 0
#adb shell dumpsys battery set wireless 0

adb shell dumpsys battery unplug

#This command may not work on some devices. You should check it in advance.
#adb shell svc bluetooth enable

adb shell input keyevent 26

echo "Wait 30 seconds to avoid Bluetooth scans or other negative impacts on testing"
sleep 30

echo Bluetooth disregarded test with a sampling rate of 0.1 second \(10 hz\)
adb shell dumpsys batterystats --reset

for ((var = 1; var <= 300; var++))
do
	
	echo >> bluetooth.txt
	echo -n "$(echo "$var * .1" | bc) " >> bluetooth.txt
	
	adb shell dumpsys batterystats > batterystats.txt
	
	echo -n "$(grep -inr "Bluetooth:" ./batterystats.txt | cut -d ":" -f2- | cut -d ":" -f2 |
	cut -d "b" -f2- | cut -d "b" -f2 | cut -d "t" -f2- | cut -d "t" -f2 |
	cut -d "=" -f2- | cut -d "=" -f2 | cut -d " " -f1)" >> bluetooth.txt
	
	sleep .1
	
done

echo Bluetooth disregarded test with a sampling rate of 0.2 second \(5 hz\)
adb shell dumpsys batterystats --reset

for ((var = 1; var <= 150; var++))
do
	
	echo >> bluetooth.txt
	echo -n "$(echo "$var * .2" | bc) " >> bluetooth.txt
	
	adb shell dumpsys batterystats > batterystats.txt
	
	echo -n "$(grep -inr "Bluetooth:" ./batterystats.txt | cut -d ":" -f2- | cut -d ":" -f2 |
	cut -d "b" -f2- | cut -d "b" -f2 | cut -d "t" -f2- | cut -d "t" -f2 |
	cut -d "=" -f2- | cut -d "=" -f2 | cut -d " " -f1)" >> bluetooth.txt
	
	sleep .2
	
done

echo Bluetooth disregarded test with a sampling rate of 0.3 second \(3.33 hz\)
adb shell dumpsys batterystats --reset

for ((var = 1; var <= 100; var++))
do
	
	echo >> bluetooth.txt
	echo -n "$(echo "$var * .3" | bc) " >> bluetooth.txt
	
	adb shell dumpsys batterystats > batterystats.txt
	
	echo -n "$(grep -inr "Bluetooth:" ./batterystats.txt | cut -d ":" -f2- | cut -d ":" -f2 |
	cut -d "b" -f2- | cut -d "b" -f2 | cut -d "t" -f2- | cut -d "t" -f2 |
	cut -d "=" -f2- | cut -d "=" -f2 | cut -d " " -f1)" >> bluetooth.txt
	
	sleep .3
	
done

echo Bluetooth disregarded test with a sampling rate of 0.4 second \(2.5 hz\)
adb shell dumpsys batterystats --reset

for ((var = 1; var <= 75; var++))
do
	
	echo >> bluetooth.txt
	echo -n "$(echo "$var * .4" | bc) " >> bluetooth.txt
	
	adb shell dumpsys batterystats > batterystats.txt
	
	echo -n "$(grep -inr "Bluetooth:" ./batterystats.txt | cut -d ":" -f2- | cut -d ":" -f2 |
	cut -d "b" -f2- | cut -d "b" -f2 | cut -d "t" -f2- | cut -d "t" -f2 |
	cut -d "=" -f2- | cut -d "=" -f2 | cut -d " " -f1)" >> bluetooth.txt
	
	sleep .4
	
done

echo Bluetooth disregarded test with a sampling rate of 0.5 second \(2 hz\)
adb shell dumpsys batterystats --reset

for ((var = 1; var <= 60; var++))
do
	
	echo >> bluetooth.txt
	echo -n "$(echo "$var * .5" | bc) " >> bluetooth.txt
	
	adb shell dumpsys batterystats > batterystats.txt
	
	echo -n "$(grep -inr "Bluetooth:" ./batterystats.txt | cut -d ":" -f2- | cut -d ":" -f2 |
	cut -d "b" -f2- | cut -d "b" -f2 | cut -d "t" -f2- | cut -d "t" -f2 |
	cut -d "=" -f2- | cut -d "=" -f2 | cut -d " " -f1)" >> bluetooth.txt
	
	sleep .5
	
done

echo Bluetooth disregarded test with a sampling rate of 0.6 second \(1.67 hz\)
adb shell dumpsys batterystats --reset

for ((var = 1; var <= 50; var++))
do
	
	echo >> bluetooth.txt
	echo -n "$(echo "$var * .6" | bc) " >> bluetooth.txt
	
	adb shell dumpsys batterystats > batterystats.txt
	
	echo -n "$(grep -inr "Bluetooth:" ./batterystats.txt | cut -d ":" -f2- | cut -d ":" -f2 |
	cut -d "b" -f2- | cut -d "b" -f2 | cut -d "t" -f2- | cut -d "t" -f2 |
	cut -d "=" -f2- | cut -d "=" -f2 | cut -d " " -f1)" >> bluetooth.txt
	
	sleep .6
	
done

echo Bluetooth disregarded test with a sampling rate of 0.7 second \(1.43 hz\)
adb shell dumpsys batterystats --reset

for ((var = 1; var <= 42; var++))
do
	
	echo >> bluetooth.txt
	echo -n "$(echo "$var * .7" | bc) " >> bluetooth.txt
	
	adb shell dumpsys batterystats > batterystats.txt
	
	echo -n "$(grep -inr "Bluetooth:" ./batterystats.txt | cut -d ":" -f2- | cut -d ":" -f2 |
	cut -d "b" -f2- | cut -d "b" -f2 | cut -d "t" -f2- | cut -d "t" -f2 |
	cut -d "=" -f2- | cut -d "=" -f2 | cut -d " " -f1)" >> bluetooth.txt
	
	sleep .7
	
done

echo Bluetooth disregarded test with a sampling rate of 0.8 second \(1.25 hz\)
adb shell dumpsys batterystats --reset

for ((var = 1; var <= 37; var++))
do
	
	echo >> bluetooth.txt
	echo -n "$(echo "$var * .8" | bc) " >> bluetooth.txt
	
	adb shell dumpsys batterystats > batterystats.txt
	
	echo -n "$(grep -inr "Bluetooth:" ./batterystats.txt | cut -d ":" -f2- | cut -d ":" -f2 |
	cut -d "b" -f2- | cut -d "b" -f2 | cut -d "t" -f2- | cut -d "t" -f2 |
	cut -d "=" -f2- | cut -d "=" -f2 | cut -d " " -f1)" >> bluetooth.txt
	
	sleep .8
	
done

echo Bluetooth disregarded test with a sampling rate of 0.9 second \(1.11 hz\)
adb shell dumpsys batterystats --reset

for ((var = 1; var <= 33; var++))
do
	
	echo >> bluetooth.txt
	echo -n "$(echo "$var * .9" | bc) " >> bluetooth.txt
	
	adb shell dumpsys batterystats > batterystats.txt
	
	echo -n "$(grep -inr "Bluetooth:" ./batterystats.txt | cut -d ":" -f2- | cut -d ":" -f2 |
	cut -d "b" -f2- | cut -d "b" -f2 | cut -d "t" -f2- | cut -d "t" -f2 |
	cut -d "=" -f2- | cut -d "=" -f2 | cut -d " " -f1)" >> bluetooth.txt
	
	sleep .9
	
done

echo Bluetooth test with a sampling rate of 1 second \(1 hz\)
adb shell dumpsys batterystats --reset

for ((var = 1; var <= 30; var++))
do

	echo >> bluetooth.txt
	echo -n "$(echo "$var * 1" | bc) " >> bluetooth.txt
	
	adb shell dumpsys batterystats > batterystats.txt
	
	echo -n "$(grep -inr "Bluetooth:" ./batterystats.txt | cut -d ":" -f2- | cut -d ":" -f2 |
	cut -d "b" -f2- | cut -d "b" -f2 | cut -d "t" -f2- | cut -d "t" -f2 |
	cut -d "=" -f2- | cut -d "=" -f2 | cut -d " " -f1)" >> bluetooth.txt
	
	sleep 1
	
done

echo Bluetooth test with a sampling rate of 1.1 second \(0.91 hz\)
adb shell dumpsys batterystats --reset

for ((var = 1; var <= 27; var++))
do

	echo >> bluetooth.txt
	echo -n "$(echo "$var * 1.1" | bc) " >> bluetooth.txt
	
	adb shell dumpsys batterystats > batterystats.txt
	
	echo -n "$(grep -inr "Bluetooth:" ./batterystats.txt | cut -d ":" -f2- | cut -d ":" -f2 |
	cut -d "b" -f2- | cut -d "b" -f2 | cut -d "t" -f2- | cut -d "t" -f2 |
	cut -d "=" -f2- | cut -d "=" -f2 | cut -d " " -f1)" >> bluetooth.txt
	
	sleep 1.1
	
done

echo Bluetooth test with a sampling rate of 1.2 second \(0.83 hz\)
adb shell dumpsys batterystats --reset

for ((var = 1; var <= 25; var++))
do

	echo >> bluetooth.txt
	echo -n "$(echo "$var * 1.2" | bc) " >> bluetooth.txt
	
	adb shell dumpsys batterystats > batterystats.txt
	
	echo -n "$(grep -inr "Bluetooth:" ./batterystats.txt | cut -d ":" -f2- | cut -d ":" -f2 |
	cut -d "b" -f2- | cut -d "b" -f2 | cut -d "t" -f2- | cut -d "t" -f2 |
	cut -d "=" -f2- | cut -d "=" -f2 | cut -d " " -f1)" >> bluetooth.txt
	
	sleep 1.2
	
done

echo Bluetooth test with a sampling rate of 1.3 second \(0.77 hz\)
adb shell dumpsys batterystats --reset

for ((var = 1; var <= 23; var++))
do

	echo >> bluetooth.txt
	echo -n "$(echo "$var * 1.3" | bc) " >> bluetooth.txt
	
	adb shell dumpsys batterystats > batterystats.txt
	
	echo -n "$(grep -inr "Bluetooth:" ./batterystats.txt | cut -d ":" -f2- | cut -d ":" -f2 |
	cut -d "b" -f2- | cut -d "b" -f2 | cut -d "t" -f2- | cut -d "t" -f2 |
	cut -d "=" -f2- | cut -d "=" -f2 | cut -d " " -f1)" >> bluetooth.txt
	
	sleep 1.3
	
done

echo Bluetooth test with a sampling rate of 1.4 second \(0.71 hz\)
adb shell dumpsys batterystats --reset

for ((var = 1; var <= 21; var++))
do

	echo >> bluetooth.txt
	echo -n "$(echo "$var * 1.4" | bc) " >> bluetooth.txt
	
	adb shell dumpsys batterystats > batterystats.txt
	
	echo -n "$(grep -inr "Bluetooth:" ./batterystats.txt | cut -d ":" -f2- | cut -d ":" -f2 |
	cut -d "b" -f2- | cut -d "b" -f2 | cut -d "t" -f2- | cut -d "t" -f2 |
	cut -d "=" -f2- | cut -d "=" -f2 | cut -d " " -f1)" >> bluetooth.txt
	
	sleep 1.4
	
done

echo Bluetooth test with a sampling rate of 1.5 second \(0.67 hz\)
adb shell dumpsys batterystats --reset

for ((var = 1; var <= 20; var++))
do

	echo >> bluetooth.txt
	echo -n "$(echo "$var * 1.5" | bc) " >> bluetooth.txt
	
	adb shell dumpsys batterystats > batterystats.txt
	
	echo -n "$(grep -inr "Bluetooth:" ./batterystats.txt | cut -d ":" -f2- | cut -d ":" -f2 |
	cut -d "b" -f2- | cut -d "b" -f2 | cut -d "t" -f2- | cut -d "t" -f2 |
	cut -d "=" -f2- | cut -d "=" -f2 | cut -d " " -f1)" >> bluetooth.txt
	
	sleep 1.5
	
done

echo Bluetooth test with a sampling rate of 1.6 second \(0.625 hz\)
adb shell dumpsys batterystats --reset

for ((var = 1; var <= 18; var++))
do

	echo >> bluetooth.txt
	echo -n "$(echo "$var * 1.6" | bc) " >> bluetooth.txt
	
	adb shell dumpsys batterystats > batterystats.txt
	
	echo -n "$(grep -inr "Bluetooth:" ./batterystats.txt | cut -d ":" -f2- | cut -d ":" -f2 |
	cut -d "b" -f2- | cut -d "b" -f2 | cut -d "t" -f2- | cut -d "t" -f2 |
	cut -d "=" -f2- | cut -d "=" -f2 | cut -d " " -f1)" >> bluetooth.txt
	
	sleep 1.6
	
done

echo Bluetooth test with a sampling rate of 1.7 second \(0.59 hz\)
adb shell dumpsys batterystats --reset

for ((var = 1; var <= 17; var++))
do

	echo >> bluetooth.txt
	echo -n "$(echo "$var * 1.7" | bc) " >> bluetooth.txt
	
	adb shell dumpsys batterystats > batterystats.txt
	
	echo -n "$(grep -inr "Bluetooth:" ./batterystats.txt | cut -d ":" -f2- | cut -d ":" -f2 |
	cut -d "b" -f2- | cut -d "b" -f2 | cut -d "t" -f2- | cut -d "t" -f2 |
	cut -d "=" -f2- | cut -d "=" -f2 | cut -d " " -f1)" >> bluetooth.txt
	
	sleep 1.7
	
done

echo Bluetooth test with a sampling rate of 1.8 second \(0.56 hz\)
adb shell dumpsys batterystats --reset

for ((var = 1; var <= 16; var++))
do

	echo >> bluetooth.txt
	echo -n "$(echo "$var * 1.8" | bc) " >> bluetooth.txt
	
	adb shell dumpsys batterystats > batterystats.txt
	
	echo -n "$(grep -inr "Bluetooth:" ./batterystats.txt | cut -d ":" -f2- | cut -d ":" -f2 |
	cut -d "b" -f2- | cut -d "b" -f2 | cut -d "t" -f2- | cut -d "t" -f2 |
	cut -d "=" -f2- | cut -d "=" -f2 | cut -d " " -f1)" >> bluetooth.txt
	
	sleep 1.8
	
done

echo Bluetooth test with a sampling rate of 1.9 second \(0.52 hz\)
adb shell dumpsys batterystats --reset

for ((var = 1; var <= 15; var++))
do

	echo >> bluetooth.txt
	echo -n "$(echo "$var * 1.9" | bc) " >> bluetooth.txt
	
	adb shell dumpsys batterystats > batterystats.txt
	
	echo -n "$(grep -inr "Bluetooth:" ./batterystats.txt | cut -d ":" -f2- | cut -d ":" -f2 |
	cut -d "b" -f2- | cut -d "b" -f2 | cut -d "t" -f2- | cut -d "t" -f2 |
	cut -d "=" -f2- | cut -d "=" -f2 | cut -d " " -f1)" >> bluetooth.txt
	
	sleep 1.9
	
done

echo Bluetooth test with a sampling rate of 2 second \(0.5 hz\)
adb shell dumpsys batterystats --reset

for ((var = 1; var <= 15; var++))
do

	echo >> bluetooth.txt
	echo -n "$(echo "$var * 2" | bc) " >> bluetooth.txt
	
	adb shell dumpsys batterystats > batterystats.txt
	
	echo -n "$(grep -inr "Bluetooth:" ./batterystats.txt | cut -d ":" -f2- | cut -d ":" -f2 |
	cut -d "b" -f2- | cut -d "b" -f2 | cut -d "t" -f2- | cut -d "t" -f2 |
	cut -d "=" -f2- | cut -d "=" -f2 | cut -d " " -f1)" >> bluetooth.txt
	
	sleep 2
	
done

echo "Testing completed"

adb shell input keyevent 26

#This command may not work on some devices. You should check it in advance.
#adb shell svc bluetooth disable

#adb shell dumpsys battery set ac 1
#adb shell dumpsys battery set usb 1
#adb shell dumpsys battery set wireless 1
