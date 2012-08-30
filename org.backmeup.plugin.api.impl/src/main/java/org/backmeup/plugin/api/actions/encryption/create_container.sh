#!/bin/sh

# copy this file to "/usr/local/sbin"

#password='asd"'
#size="$((50*1024*1024))"
#containername="/tmp/test.tc"
#mountpoint="/tmp/mnt"

containername="$1"
mountpoint="$2"
size="$3"
password="$4"


tccmd="truecrypt"
fscmd="mkfs.ntfs"
mntcmd="mount"

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


${tccmd} -c "${containername}" --password="${password}" --volume-type=normal --filesystem=none --encryption=aes --size=${size} --hash=SHA-512 --random-source=/dev/urandom -k "" --non-interactive > /dev/null 2>&1
check_result $?

${tccmd} "${containername}" --password="${password}" --filesystem=none -k "" --protect-hidden=no --non-interactive > /dev/null 2>&1
check_result $?


tcdevice="$(${tccmd} -l | grep "${containername}" | awk '{print $3}')"
check_result $?


${fscmd} "${tcdevice}" > /dev/null 2>&1
check_result $?


${mntcmd} "${tcdevice}" "${mountpoint}" > /dev/null 2>&1
check_result $?