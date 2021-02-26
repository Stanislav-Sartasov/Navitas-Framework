#!/bin/bash

function wifi
# $1 - time in seconds
# $2 - sampling frequency in seconds
{

	local cnt=$(echo "$1 / $2" | bc)
	local hz=$(echo "scale=2; 1 / $2" | bc)

	echo "Wi-Fi test with a sampling rate of $hz hz ($2 second)"
	echo "Wi-Fi test with a sampling rate of $hz hz ($2 second)" >> wifi.txt
	
	adb shell dumpsys batterystats --reset

	for ((i = 1; i <= $cnt; i++))
	do
	
		echo >> wifi.txt
		echo -n "$(echo "scale=1; $i * $2" | bc) " >> wifi.txt
		
		adb shell dumpsys batterystats > batterystats_temp.txt
		
		echo -n "$(grep -inr "Wifi:" ./batterystats_temp.txt | cut -d ":" -f2- |
		cut -d ":" -f2 | cut -d "w" -f2- | cut -d "w" -f2 | cut -d "i" -f2- |
		cut -d "i" -f2 | cut -d "=" -f2- | cut -d "=" -f2 | cut -d " " -f1)" >> wifi.txt
		
		sleep $2
	
	done

	echo >> wifi.txt
	echo >> wifi.txt
}

function bluetooth
# $1 - time in seconds
# $2 - sampling frequency in seconds
{

	local cnt=$(echo "$1 / $2" | bc)
	local hz=$(echo "scale=2; 1 / $2" | bc)

	echo "Bluetooth test with a sampling rate $hz hz ($2 second)"
	echo "Bluetooth test with a sampling rate of $hz hz ($2 second)" >> bluetooth.txt
	
	adb shell dumpsys batterystats --reset

	for ((i = 1; i <= $cnt; i++))
	do
	
		echo >> bluetooth.txt
		echo -n "$(echo "scale=1; $i * $2" | bc) " >> bluetooth.txt
		
		adb shell dumpsys batterystats > batterystats_temp.txt
		
		echo -n "$(grep -inr "Bluetooth:" ./batterystats_temp.txt | cut -d ":" -f2- | cut -d ":" -f2 |
		cut -d "b" -f2- | cut -d "b" -f2 | cut -d "t" -f2- | cut -d "t" -f2 |
		cut -d "=" -f2- | cut -d "=" -f2 | cut -d " " -f1)" >> bluetooth.txt
		
		sleep $2
	
	done
	
	echo >> bluetooth.txt
	echo >> bluetooth.txt
}
