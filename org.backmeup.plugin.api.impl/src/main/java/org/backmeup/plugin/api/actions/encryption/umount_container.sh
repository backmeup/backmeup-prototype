#!/bin/sh

# copy this file to "/usr/local/sbin"

#containername="/tmp/test.tc"

containername="$1"

tccmd="truecrypt"


check_result ()
{
	result="$1"
	#message="$2"

	if [ "$result" != "0" ]
	then
		# set color
		#echo -en "\e[49;1;31m"
		#echo "Error with: $message"
		# unset color
		#echo -en "\e[49;0;39m"

		${tccmd} -d "${containername}" > /dev/null 2>&1

		exit 1
	fi
}



${tccmd} -d ${containername} > /dev/null 2>&1
check_result $?