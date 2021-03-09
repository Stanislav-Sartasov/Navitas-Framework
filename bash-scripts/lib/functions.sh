#!/bin/bash

function wifi_common
# $1 - time in seconds
# $2 - sampling frequency in seconds
# $3 (optional) - Saksonov mode
{

	local cnt=$(echo "$1 / $2" | bc)
	local hz=$(echo "scale=2; 1 / $2" | bc)
	local mode="$3"
	
	if [ "$mode" != "-s" ]
	then
	
		echo "Wi-Fi test with a sampling rate of $hz hz ($2 second)"
		echo "Wi-Fi test with a sampling rate of $hz hz ($2 second)" >> wifi.txt
		
		adb shell dumpsys batterystats --reset

	else
	
		echo "Wi-Fi test in Saksonov mode with a sampling rate of $hz hz ($2 second)"
		echo "Wi-Fi test in Saksonov mode with a sampling rate of $hz hz ($2 second)" >> wifi.txt

	fi

	for ((i = 1; i <= $cnt; i++))
	do
	
		#Wifi: 0.00253 ( cpu=0.00164 wifi=0.000899 ) Including smearing: 0.0142 ( proportional=0.0117 )
		
		if [ "$mode" == "-s" ]
		then

			adb shell dumpsys batterystats --reset

		fi
	
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

function bluetooth_common
# $1 - time in seconds
# $2 - sampling frequency in seconds
# $3 (optional) - Saksonov mode
{

	local cnt=$(echo "$1 / $2" | bc)
	local hz=$(echo "scale=2; 1 / $2" | bc)	
	local mode="$3"
	
	if [ "$mode" != "-s" ]
	then
	
		echo "Bluetooth test with a sampling rate of $hz hz ($2 second)"
		echo "Bluetooth test with a sampling rate of $hz hz ($2 second)" >> bluetooth.txt
		
		adb shell dumpsys batterystats --reset

	else
	
		echo "Bluetooth test in Saksonov mode with a sampling rate of $hz hz ($2 second)"
		echo "Bluetooth test in Saksonov mode with a sampling rate of $hz hz ($2 second)" >> bluetooth.txt

	fi

	for ((i = 1; i <= $cnt; i++))
	do
	
		if [ "$mode" == "-s" ]
		then

			adb shell dumpsys batterystats --reset

		fi
		
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

function wifi_sony
# $1 - time in seconds
# $2 - sampling frequency in seconds
# $3 (optional) - Saksonov mode
{

	local cnt=$(echo "$1 / $2" | bc)
	local hz=$(echo "scale=2; 1 / $2" | bc)
	local mode="$3"
	
	if [ "$mode" != "-s" ]
	then
	
		echo "Wi-Fi test with a sampling rate of $hz hz ($2 second)"
		echo "Wi-Fi test with a sampling rate of $hz hz ($2 second)" >> wifi.txt
		
		adb shell dumpsys batterystats --reset

	else
	
		echo "Wi-Fi test in Saksonov mode with a sampling rate of $hz hz ($2 second)"
		echo "Wi-Fi test in Saksonov mode with a sampling rate of $hz hz ($2 second)" >> wifi.txt

	fi

	for ((i = 1; i <= $cnt; i++))
	do
	
		if [ "$mode" == "-s" ]
		then

			adb shell dumpsys batterystats --reset

		fi
	
		echo >> wifi.txt
		
		local sec=$(echo "scale=1; $i * $2" | bc)
		adb shell dumpsys batterystats > batterystats_temp.txt
		local mAh=$(grep -n "Wifi:" ./batterystats_temp.txt | awk '{print $3; exit;}')
		
		echo -n "$sec $mAh" >> wifi.txt
		
		sleep $2
	
	done

	echo >> wifi.txt
	echo >> wifi.txt
}

function bluetooth_sony
# $1 - time in seconds
# $2 - sampling frequency in seconds
# $3 (optional) - Saksonov mode
{

	local cnt=$(echo "$1 / $2" | bc)
	local hz=$(echo "scale=2; 1 / $2" | bc)
	local mode="$3"
	
	if [ "$mode" != "-s" ]
	then
	
		echo "Bluetooth test with a sampling rate of $hz hz ($2 second)"
		echo "Bluetooth test with a sampling rate of $hz hz ($2 second)" >> bluetooth.txt
		
		adb shell dumpsys batterystats --reset

	else
	
		echo "Bluetooth test in Saksonov mode with a sampling rate of $hz hz ($2 second)"
		echo "Bluetooth test in Saksonov mode with a sampling rate of $hz hz ($2 second)" >> bluetooth.txt

	fi

	for ((i = 1; i <= $cnt; i++))
	do
	
		if [ "$mode" == "-s" ]
		then

			adb shell dumpsys batterystats --reset

		fi
		
		echo >> bluetooth.txt
		
		local sec=$(echo "scale=1; $i * $2" | bc)
		adb shell dumpsys batterystats > batterystats_temp.txt
		local mAh=$(grep -n "Bluetooth:" ./batterystats_temp.txt | awk '{print $3; exit;}')
		
		echo -n "$sec $mAh" >> bluetooth.txt
		
		sleep $2
	
	done
	
	echo >> bluetooth.txt
	echo >> bluetooth.txt
}
