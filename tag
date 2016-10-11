#!/bin/bash

v=$(cat sambaplayersdk/src/main/res/values/strings.xml | grep 'version' | sed 's/[^\>]*\>//' | sed 's/\<.*$//')
t=$(git describe --tags --abbrev=0 2> /dev/null)

# if exists some tagged version
if [[ ${#t} -gt 0 ]]; then
	echo "Current version: $t"
else
	echo 'No tagged versions.'
fi

# if configured version is greater than latest
if [[ ${#v} -gt 0 && (${#t} -eq 0 || "$v" -gt "$t") ]]; then
	read -p "New version detected ($v). Create new release? (Y/n) " yn < /dev/tty
	case $yn in
		Y ) echo 'yes!!'; exit 1; break;;
		* ) exit 0;;
	esac
fi

exit 0
