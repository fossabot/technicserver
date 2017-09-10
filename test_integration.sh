#!/bin/bash

JAR_FILE="$( grep -Po "(?<=rootProject.name = ').*(?=')" settings.gradle )"-all-"$( grep -Po "(?<=version ').*(?=')" build.gradle ).jar"
pushd .

function test_solder_pack {
	set -x
	rm -rf run
	mkdir run
	cd run
	cp ../build/libs/${JAR_FILE} .
	java -jar ${JAR_FILE} && exit 1
	cat <<EOF > modpack.properties
#Thu Aug 17 11:05:03 CEST 2017
url=http://api.technicpack.net/modpack/litwr-after-humans-unofficial-import-bgde
autoupdate=no
build=2017.08.18
javaArgs=-server -Xmx4G -XX:+DisableExplicitGC -XX:+AggressiveOpts
EOF
	java -jar ${JAR_FILE} || exit 2
	test $(ls cache/ | wc -l) -lt 50 && exit 3
	cd ..
	rm -rf run
	exit 0
}

function test_zip_pack {
	set -x
	rm -rf run
	mkdir run
	cd run
	cp ../build/libs/${JAR_FILE} .
	java -jar ${JAR_FILE} && echo Missing Properties test failed && exit 1
	cat <<EOF > modpack.properties
#Thu Aug 17 11:05:03 CEST 2017
url=http://api.technicpack.net/modpack/minecraft-after-humans
autoupdate=no
build=latest
javaArgs=-server -Xmx4G -XX:+DisableExplicitGC -XX:+AggressiveOpts
EOF
	java -jar ${JAR_FILE} || exit 2
	test $(ls cache/ | wc -l)  -ne 1 && exit 3
	test -f cache/package.zip || exit 4
	cd ..
	rm -rf run
	exit 0
}

case $1 in
    solder)
      test_solder_pack
      ;;
    *)
      test_zip_pack
      ;;
esac

popd
