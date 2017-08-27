#!/bin/bash

JAVA_FILES=$(find . -type -f -iname *.java -print0 | tr '\0' ' ')
CLASSES=$(find $HOME/.gradle/caches/ -type f -iname *.jar -print0 | tr '\0' ' ')
PWD=$(pwd)

git clone https://$GH_TOKEN@github.com/bennet0496/technicserver.git -b gh-pages ../technicserver-gh-pages

javadoc -locale en_US -private -splitindex -use -author -version $JAVA_FILES \
    -sourcepath "$PWD/src/main/java;$PWD/src/main/resources;$PWD/src/test/java;$PWD/src/test/resources" \
	-classpath $CLASSES -d ../technicserver-gh-pages/docs/javadoc
	
pushd .
cd ../technicserver-gh-pages
git add -A docs/javadoc
git commit -am "Travis $TRAVIS_BUILD_NUMBER: Javadoc"
git push -q origin gh-pages

popd
