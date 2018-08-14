#!/bin/bash

# $1: Bintray user
# $2: Bintray API key
# $3 (optional): Version suffix

repoUser=$1
repoApiKey=$2
versionSuffix=$([[ -z $3 ]] && echo 'beta' || echo $3)
ret=''

set -x

publish() {
	# $1: module name
	# $2: artifact name
	# $n: dep name
	# $n+1: dep version

	# vars
	output="$1/build/outputs/aar"
	v="$(cat "$1/build.gradle" | grep 'versionName' | sed $'s/^[^"\']*[\'"v]*//' | sed $'s/[^0-9]*$//')-$versionSuffix"
	pomPath="$output/$2-$v.pom"
	pomPathTmp="$output/tmp.pom"
	args=($@)

	# artifacts
	mkdir -p "$output"
	cat "assets/$2.pom" | sed "/$2/,/aar/ s/version\>[^\<]*/version\>$v/" > $pomPath

	# update deps version
	for ((i=2; i<$#; i+=2)); do
		cat $pomPath | sed "/${args[i]}/,/compile/ s/version\>[^\<]*/version\>${args[i+1]}/" > $pomPathTmp
		mv $pomPathTmp $pomPath
	done

	mv $output/$1-release.aar $output/$2-$v.aar
	cp $output/* $CIRCLE_ARTIFACTS

	# JFrog docs: https://www.jfrog.com/confluence/display/CLI/CLI+for+JFrog+Bintray
	# configuring repo client tool
	./jfrog bt c --user=$repoUser --key=$repoApiKey --licenses=MIT
	# uploading artifacts to repo
	./jfrog bt u "$output/$2*" "sambatech/maven/sdk-android/beta2" "com/sambatech/player/$2/$v/" --publish=true --override=true

	ret=$v
}

publish sambaplayersdk sdk-android $ret