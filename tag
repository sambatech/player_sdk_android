#!/bin/bash

PROD_URL='git@github.com:sambatech/player_sdk_android.git'
TEMP_REPO='_tmp'

alreadyProd=$([[ $(git config --get remote.origin.url) == $PROD_URL ]] && echo 1)

# if repo not public, switch to it and get tags
# WARNING: assuming every branch is compatible with prod/master
if [[ alreadyProd -ne 1 ]]; then
	git remote rename origin $TEMP_REPO
	git remote add origin $PROD_URL 2> /dev/null
	git pull origin master --tags
fi

# TODO: grab version from sambaplayersdk.gradle
v=$(cat sambaplayersdk/src/main/res/values/strings.xml | grep 'version' | sed 's/[^\>]*\>//' | sed 's/\<.*$//')
# tags type must be annotated otherwise git cannot get the correct latest
t=$(git describe --tags --abbrev=0 2> /dev/null)

# if exists some tagged version
if [[ ${#t} -gt 0 ]]; then
	echo "Current version: $t"
else
	echo 'No tagged versions.'
fi

# if configured version differ from latest
if [[ ${#v} -gt 0 && (${#t} -eq 0 || "$v" != "$t") ]]; then
	read -p "New version detected ($v). Create new release? (Y/n) " yn < /dev/tty
	case $yn in
	Y )
		git tag -a $v -m "$(git log --oneline -n 1)"
		git push origin $v --no-verify
		;;
	esac
fi

# switch back to current repo
if [[ alreadyProd -ne 1 ]]; then
	git remote remove origin
	git remote rename $TEMP_REPO origin
fi

exit 0
